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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.model.Mode;
import org.stanwood.media.store.xmlstore.SimpleErrorHandler;
import org.stanwood.media.util.XMLParser;
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

	private final static Log log = LogFactory.getLog(XBMCScraper.class);
	
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
	 * @param addon The addon been used
	 * @param scraperFile The XML scraper file
	 */
	public XBMCScraper(XBMCAddon addon,File scraperFile) {
		this.scraperFile = scraperFile;
		this.addon = addon;
	}
	
	/**
	 * Used to get the mode contained within the scraper file
	 * @return The mode of the scraper file
	 * @throws XBMCException Thrown if their is a problem parsing the scraper file
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
	 * @param year The year to search for the result or empty string for any
	 * @return The search URL XML result
	 * @throws XBMCException Thrown if their are any problems creating the search URL
	 */
	public Document getCreateSearchUrl(String searchTerm,String year) throws XBMCException {
		try {
			Map<Integer,String>params = new HashMap<Integer,String>();
			params.put(Integer.valueOf(1), searchTerm);
			params.put(Integer.valueOf(2), year);
					
			String result = executeXBMCScraperFunction("CreateSearchUrl",params);
			Document doc = XMLParser.strToDom(result);
			return doc;
		} catch (Exception e) {
			throw new XBMCException("Unable to parse scrapper XML",e);
		} 
	}	
	
	/**
	 * This function is used to get a XML document of the search results. It takes as input the
	 * webpage downloaded from the URL obtained with the @{link getCreateSearchUrl(String,String)} call. 
	 * @param rawHtml The raw search results
	 * @param searchTerm The term been searched for
	 * @return The search results as a XML document
	 * @throws XBMCException Thrown if their are any problems creating the search url
	 */
	public Document getGetSearchResults(String rawHtml,String searchTerm) throws  XBMCException {
		try {
			Map<Integer,String>params = new HashMap<Integer,String>();
			params.put(Integer.valueOf(1), rawHtml);
			params.put(Integer.valueOf(2),searchTerm);
			
			String result = executeXBMCScraperFunction("GetSearchResults",params);
			Document doc = XMLParser.strToDom(result);
			return doc;
		} catch (Exception e) {
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
		if (log.isDebugEnabled()) {
			log.debug("Executing function : " + functionName);
		}
			
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
		StringBuffer newOutput = new StringBuffer();
		
		int dest = getDestParam(node);		
		XBMCExpression expression = getExpression(node);
		if (expression!=null) {
			if (log.isDebugEnabled()) {
				log.debug("perform expr " + expression.getPattern().toString() +" on [" + input+"]");
			}
			Matcher m = expression.getPattern().matcher(input);
			boolean found = false;
			
			while (m.find()) {
				String output = orgOutput;
				found = true;
				
				for (int j=1;j<=m.groupCount();j++) {
					String value = m.group(j);
					value = processValue(expression,value,j);					
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
//			newOutput.append(input);
		}
					
		String output = processInfoVars(newOutput.toString());					
		if (dest!=-1) {
			if (log.isDebugEnabled()) {
				log.debug("Put param " + dest + " - " + output);
			}
			params.put(Integer.valueOf(dest), output);			
		}
	}
	
	private String processValue(XBMCExpression expression,String value,int group) {
		if (!expression.getNoClean(group)) {
			value = value.replaceAll("\\<.*?\\>", "");
		}
		
		if (expression.getTrim(group)) {
			value = value.trim();
		}
		return value;
	}

	private XBMCExpression getExpression(Element node) {				
		Element expNode = (Element) getChildNodeByName(node, "expression");
		
		if (expNode !=null) {
			XBMCExpression expr = new XBMCExpression();
			String regexp = "(.+)";
			if (expNode.getTextContent().length()>0) {
				regexp = expNode.getTextContent();
			}									
						
			Pattern p = Pattern.compile(regexp,Pattern.MULTILINE | Pattern.DOTALL);
			expr.setPattern(p);
						
			expr.setNoClean(expNode.getAttribute("noclean"));
			
			
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

	/**
	 * Used to get the XML scraper file been used
	 * @return The XML scraper file
	 */
	public File getFile() {
		return scraperFile;
	}
}
