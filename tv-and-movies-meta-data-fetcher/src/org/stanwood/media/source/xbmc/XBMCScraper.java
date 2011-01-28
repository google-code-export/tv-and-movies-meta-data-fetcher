package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
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
public class XBMCScraper extends XBMCExtension {

	private final static Log log = LogFactory.getLog(XBMCScraper.class);

	private final static String ROOT_NODE_NAME = "scraper";
	private final static Pattern INFO_PATTERN1 = Pattern.compile("(\\$INFO\\[.*?\\])");
	private final static Pattern INFO_PATTERN2 = Pattern.compile("\\$INFO\\[(.*?)\\]");
	private final static Pattern PARAM_PATTERN = Pattern.compile("\\$\\$\\d+");

	private File scraperFile;
	private Map<File,Document> docs = new HashMap<File,Document>();

	private XBMCAddon addon;

	private Mode mode;

	/**
	 * Used to create the class and set the scraper file
	 * @param addon The addon been used
	 * @param scraperFile The XML scraper file
	 */
	public XBMCScraper(XBMCAddon addon,File scraperFile,String point,Mode mode) {
		super(point);
		this.scraperFile = scraperFile;
		this.addon = addon;
		this.mode = mode;
	}

	public Mode getMode() {
		return mode;
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
			params.put(Integer.valueOf(1),URLEncoder.encode(searchTerm,"UTF-8"));
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
	 * @throws XBMCException Thrown if their are any problems creating the search urlXML
	 */
	public Document getGetSearchResults(String rawHtml,String searchTerm) throws  XBMCException {
		try {
			Map<Integer,String>params = new HashMap<Integer,String>();
			params.put(Integer.valueOf(1), rawHtml);
			params.put(Integer.valueOf(2),URLEncoder.encode(searchTerm,"UTF-8"));

			String result = executeXBMCScraperFunction("GetSearchResults",params);
			Document doc = XMLParser.strToDom(result);
			return doc;
		} catch (Exception e) {
			throw new XBMCException("Unable to parse scrapper XML",e);
		}
	}

	/**
	 * Used to get the show/film details as a XML document. It takes as input the
	 * 1 or more webpages downloaded from the URL obtained with the @{link getCreateSearchUrl(String,String)} call.
	 * @param contents A list of webpage contents
	 * @return The results as a XML document
	 * @throws XBMCException Thrown if their are any problems
	 */
	public Document getGetDetails(String... contents) throws  XBMCException {
		try {
			Map<Integer,String>params = new HashMap<Integer,String>();
			if (contents.length>=9) {
				throw new XBMCException("Not allowed more than 9 shows");
			}
			for (int i=0;i<contents.length;i++) {
				params.put(i+1, contents[i]);
			}

			String result = executeXBMCScraperFunction("GetDetails",params);
			Document doc = XMLParser.strToDom(result);
			return doc;
		} catch (Exception e) {
			throw new XBMCException("Unable to parse scrapper XML",e);
		}
	}

	private Document getDocument(File file) throws XBMCException  {
		if (docs.get(file)==null) {
			if (!scraperFile.exists()) {
				throw new XBMCException ("Unable to find XMBC scrapper: " + scraperFile);
			}

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			Document doc;
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
			docs.put(file,doc);
		}
		return docs.get(file);
	}

	private String executeXBMCScraperFunction(String functionName,Map<Integer,String> params) throws TransformerException, XBMCException {
		Element functionNode = (Element) XPathAPI.selectSingleNode(getDocument(scraperFile), ROOT_NODE_NAME+"/"+functionName);


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
		boolean appendToDest = shouldAppendToBuffer(node);
		XBMCExpression expression = getExpression(node,params);
		if (expression!=null) {
			if (log.isDebugEnabled()) {
				String in = input;
				if (in.length()>20) {
					in = in.substring(0,20);
				}
				log.debug("perform expr " + expression.getPattern().toString() +" on [" + in+"]");
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

				if (!expression.getRepeat()) {
					break;
				}
			}
		}
		else {
			newOutput.append(input);
		}

		String output = processInfoVars(newOutput.toString());
		if (dest!=-1) {
			if (log.isDebugEnabled()) {
				log.debug("Put param " + dest + " - " + output);
			}
			if (appendToDest) {
				output = params.get(Integer.valueOf(dest))+output;
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

	private XBMCExpression getExpression(Element node,Map<Integer, String> params) {
		Element expNode = (Element) getChildNodeByName(node, "expression");

		if (expNode !=null) {
			XBMCExpression expr = new XBMCExpression();
			String regexp = "(.+)";
			if (expNode.getTextContent().length()>0) {
				regexp = expNode.getTextContent();
			}

			regexp = applyParams(regexp, params);

			Pattern p = Pattern.compile(regexp,Pattern.MULTILINE | Pattern.DOTALL);
			expr.setPattern(p);

			expr.setNoClean(expNode.getAttribute("noclean"));


			if (expNode.getAttribute("clear").equals("yes")){
				expr.setClear(true);
			}

			if (expNode.getAttribute("repeat").equals("yes")){
				expr.setRepeat(true);
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
		if (sDest!=null && sDest.length()>0) {
			if (sDest.endsWith("+")) {
				sDest = sDest.substring(0,sDest.length()-1);
			}
			dest = Integer.parseInt(sDest);
		}
		return dest;
	}

	private boolean shouldAppendToBuffer(Element node) {
		String sDest = node.getAttribute("dest");
		return (sDest.endsWith("+"));
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
