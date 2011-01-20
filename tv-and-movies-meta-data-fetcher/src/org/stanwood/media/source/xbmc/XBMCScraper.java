package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.stanwood.media.model.Mode;
import org.stanwood.media.source.SourceException;
import org.stanwood.media.store.xmlstore.SimpleErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.org.apache.xpath.internal.XPathAPI;

/**
 * This class is used to handle the XML scraper files from XBMC.
 */
public class XBMCScraper {

	private final static String ROOT_NODE_NAME = "scraper";
	private final static Pattern INFO_PATTERN1 = Pattern.compile("(\\$INFO\\[.*\\])");
	private final static Pattern INFO_PATTERN2 = Pattern.compile("\\$INFO\\[(.*)\\]");
	private final static Pattern PARAM_PATTERN = Pattern.compile("\\$\\$\\d+");
	private final static Pattern PARAM_PATTERN2 = Pattern.compile("\\\\\\d+");
	
	private File scraperFile;
	private Document doc;

	private XBMCAddon addon;

	/**
	 * Used to create the class and set the scraper file
	 * @param scraperFile The XML scraper file
	 */
	public XBMCScraper(XBMCAddon addon,File scraperFile) {
		this.scraperFile = scraperFile;
		this.addon = addon;
	}
	
	/**
	 * Used to get the mode contained within the scraper file
	 * @return The mode of the scraper file
	 * @throws SourceException Thrown if their is a problem parsing the scraper file
	 */
	public Mode getMode() throws XBMCException  {
		Element scraperNode;
		try {
			scraperNode = (Element) XPathAPI.selectSingleNode(getDocument(),ROOT_NODE_NAME);
			String contentType = scraperNode.getAttribute("content");
			if (contentType.equalsIgnoreCase("tvshows")) {
				return Mode.TV_SHOW;
			}
			else if (contentType.equalsIgnoreCase("films")) {
				return Mode.FILM;
			}
			else {
				throw new XBMCException ("Unsupported scrapper content type: " + contentType);
			}
		} catch (TransformerException e) {
			throw new XBMCException ("Unable to read content type");
		}
	}
	
	/**
	 * Used to get the URL which should be used to search
	 * @param searchTerm The search term to use
	 * @return The search URL XML result
	 * @throws SourceException
	 * @throws XBMCException 
	 */
	public String getCreateSearchUrl(String searchTerm,String year) throws XBMCException {
		try {
			Map<Integer,String>params = new HashMap<Integer,String>();
			params.put(Integer.valueOf(1), searchTerm);
			params.put(Integer.valueOf(2), year);
					
			return executeXBMCScraperFunction("CreateSearchUrl",params);
		} catch (TransformerException e) {
			throw new XBMCException("Unable to parse scrapper XML",e);
		}
	}	
	
	public String getGetSearchResults(String rawHtml,String searchTerm) throws  XBMCException {
		try {
			Map<Integer,String>params = new HashMap<Integer,String>();
			params.put(Integer.valueOf(1), rawHtml);
			params.put(Integer.valueOf(2),searchTerm);
			
			return executeXBMCScraperFunction("GetSearchResults",params);
		} catch (TransformerException e) {
			throw new XBMCException("Unable to parse scrapper XML",e);
		}
	}
	
	private Document getDocument() throws XBMCException  {
		if (doc==null) {
			if (!scraperFile.exists()) {
				throw new XBMCException ("Unable to find XMBC scrapper: " + scraperFile);
			}

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);

			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				SimpleErrorHandler errorHandler = new SimpleErrorHandler(scraperFile);
				builder.setErrorHandler(errorHandler);
				doc = builder.parse(scraperFile);
				if (errorHandler.hasErrors()) {
					throw new XBMCException ("Unable to parse  XMBC scrapper, errors found in file: " + scraperFile);
				}
			} catch (SAXException e) {
				throw new XBMCException ("Unable to parse XMBC scrapper: " + e.getMessage(), e);
			} catch (IOException e) {
				throw new XBMCException ("Unable to read XMBC scrapper: " + e.getMessage(), e);
			} catch (ParserConfigurationException e) {
				throw new XBMCException ("Unable to parse  XMBC scrapper: " + e.getMessage(), e);
			}
		}
		return doc;
	}	
	
	private String executeXBMCScraperFunction(String functionName,Map<Integer,String> params) throws TransformerException, XBMCException {
		Element functionNode = (Element) XPathAPI.selectSingleNode(getDocument(), ROOT_NODE_NAME+"/"+functionName);
		if (functionNode==null) {
			throw new XBMCException("Unable to find scraper function '" + functionName+"'"); 
		}
		System.out.println("Executing function : " + functionName);		
		executeChildNodes(functionNode,params);
		int dest = getDestParam(functionNode);
		if (dest!=-1) {
			return params.get(dest);
		}
		else {
			// TODO throw exception
			return null;
		}		
	}
	
	private void executeChildNodes(Element functionNode,Map<Integer,String> params) throws XBMCException {
		NodeList nodes = functionNode.getChildNodes();
		for (int i=0;i<nodes.getLength();i++) {
			if (nodes.item(i) instanceof Element ) {
				Element node = (Element) nodes.item(i);
				
				if (node.hasChildNodes()) {
					executeChildNodes(node,params);
				}
				
				if (node.getNodeName().equals("RegExp")) {						
					performRegexp(params, node);
				}								
			}
		}
	}

	private void performRegexp(Map<Integer, String> params, Element node) throws XBMCException {
		String input = getInputAttr(params,node);
		String orgOutput = getOutputAttr(params,node);
		StringBuilder newOutput = new StringBuilder();
		
		int dest = getDestParam(node);		
		XBMCExpression expression = getExpression(node);
		if (expression!=null) {		
			System.out.println("perform expr " + expression.getPattern().toString());
			Matcher m = expression.getPattern().matcher(input);
			boolean found = false;
			while (m.find()) {
				String output = orgOutput;
				found = true;
				
				for (int j=1;j<=m.groupCount();j++) {
					String value = m.group(j);
					if (expression.getClean()) {
						value = value.replaceAll("\\<.*?\\>", "");
					}
					output = output.replaceAll("\\\\"+(j), value);										
				}
				
				if (found==false && expression.getClear()) {
					output = "";
				}
				newOutput.append(output);
			}
		}
		else {			
			newOutput.append(applyParams2(orgOutput,params));
		}
					
		String output = processInfoVars(newOutput.toString());					
		if (dest!=-1) {
			System.out.println("Put param " + dest + " - " + output);
			params.put(Integer.valueOf(dest), output);			
		}
	}

	private XBMCExpression getExpression(Element node) {				
		Element expNode = (Element) getChildNodeByName(node, "expression");
		if (expNode !=null && expNode.getTextContent().length()>0) {
			XBMCExpression expr = new XBMCExpression();
			String regexp = expNode.getTextContent();			
			Pattern p = Pattern.compile(regexp,Pattern.MULTILINE | Pattern.DOTALL);
			expr.setPattern(p);
			
			if (expNode.getAttribute("noclean").equals("1")){
				expr.setClean(false);
			}
			
			if (expNode.getAttribute("clear").equals("yes")){
				expr.setClear(true);
			}
			
			return expr;
		}
		
		return null;
	}

	private String applyParams(String output, Map<Integer, String> params) {
		String out = output;
		
		Matcher m = PARAM_PATTERN.matcher(out);
		while (m.find()) {
			int num = Integer.parseInt(m.group().substring(2));
			String value = params.get(num);
			if (value==null) {
				value = "";
			}
			out = m.replaceAll(value);
		}
		
//		m = PARAM_PATTERN2.matcher(out);
//		while (m.find()) {
//			int num = Integer.parseInt(m.group().substring(1));
//			String value = params.get(num);
//			if (value==null) {
//				value = "";
//			}
//			out = m.replaceAll(value);
//		}
		
		return out;
	}

	private String applyParams2(String output, Map<Integer, String> params) {
		String out = output;
		
		Matcher m = PARAM_PATTERN2.matcher(out);
		while (m.find()) {
			int num = Integer.parseInt(m.group().substring(1));
			String value = params.get(num);
			if (value==null) {
				value = "";
			}
			out = m.replaceAll(value);
		}
		
		return out;
	}
	
	private String processInfoVars(String output) throws XBMCException {
		Matcher m = INFO_PATTERN1.matcher(output);
		while (m.find()) {		
			Matcher m2 = INFO_PATTERN2.matcher(m.group());
			if (m2.matches()) {
				output = m.replaceAll(addon.getInfoSetting(m2.group(1)));				
			}
		}
		return output;
	}
	
	private Node getChildNodeByName(Node parent,String name) {
		NodeList nodeList = parent.getChildNodes();
		for (int j=0;j<nodeList.getLength();j++) {
			Node node = nodeList.item(j);
			if (node.getNodeName().equals(name)) {
				return node;
			}
		}
		return null;
	}

	private int getDestParam(Element node) {
		int dest = -1;
		String sDest = node.getAttribute("dest");
		if (sDest!=null) {						
			dest = Integer.parseInt(sDest);
			
		}
		return dest;
	}
	
	private String getInputAttr(Map<Integer, String> params, Element node) {
		String value = node.getAttribute("input");
		if (value==null || value.equals("")) {
			value="$$1";
		}
		value = applyParams(value,params);
		return value;
	}
	
	private String getOutputAttr(Map<Integer, String> params, Element node) {
		String value = node.getAttribute("output");
		value = applyParams(value,params);
		return value;
	}
	
//	private String fillParams(String output,Map<Integer,String> params) {			
//		
//		int pos = -1;
//		while ((pos = output.indexOf("\\"))!=-1) {		
//			int paramNum = getParam(output,pos);
//			String paramValue = params.get(paramNum);
//			if (paramValue!=null) {			
//				StringBuilder newOutput = new StringBuilder();
//				newOutput.append(output.substring(0,pos));
//				String paramIndex = String.valueOf(paramNum);					
//				newOutput.append(paramValue);
//				newOutput.append(output.substring(pos+1+paramIndex.length()));					
//				output = newOutput.toString();
//			}
//			else {
//				System.err.println("Did not find param : " + paramNum);
//			}
//		}				
//		
//		return output;
//	}

	private Integer getParam(String output, int pos) {
		StringBuilder param = new StringBuilder();
		pos++;
		while (pos < output.length() && Character.isDigit(output.charAt(pos))) {
			param.append(output.charAt(pos++));
		}
		
		if (param.length()==0) {
			return null;
		}

		return Integer.parseInt(param.toString());
	}

	/**
	 * Used to get the XML scraper file been used
	 * @return The XML scraper file
	 */
	public File getFile() {
		return scraperFile;
	}
}
