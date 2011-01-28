package org.stanwood.media.source.xbmc;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.model.Mode;
import org.stanwood.media.util.XMLParser;
import org.stanwood.media.util.XMLParserException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class is used to handle the XML scraper files from XBMC.
 */
public class XBMCScraper extends XBMCExtension {

	private final static Log log = LogFactory.getLog(XBMCScraper.class);
	private final static String ROOT_NODE_NAME = "scraper";


	private Mode mode;

	/**
	 * Used to create the class and set the scraper file
	 * @param addon The addon been used
	 * @param scraperFile The XML scraper file
	 */
	public XBMCScraper(XBMCAddon addon,File scraperFile,String point,Mode mode) {
		super(addon,scraperFile,point);

		this.mode = mode;
	}

	/**
	 * Get the mode of the scrper
	 * @return The scraper mode
	 */
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

			for (Node node : selectNodeList(doc, "details/chain")) {
				resolveChainNodes(doc,(Element) node);
			}

			for (Node node : selectNodeList(doc, "details/url")) {
				resultUrlNodes(doc,(Element) node);
			}

			return doc;
		} catch (Exception e) {
			throw new XBMCException("Unable to parse scrapper XML",e);
		}
	}

	private void resolveChainNodes(Document doc,Element node) throws XMLParserException,
			XBMCException {
		String functionName = node.getAttribute("function");
		String param = node.getTextContent();

		Map<Integer,String> subParams = new HashMap<Integer,String>();
		subParams.put(1,param);
		Document results = strToDom(getAddon().executeFunction(functionName, subParams));
		Node parent = node.getParentNode();
		parent.removeChild(node);

		for (Node n : selectNodeList(results, "details/*")) {
			Node newNode = doc.importNode(n,true);
			parent.appendChild(newNode);
		}
	}

	private void resultUrlNodes(Document doc,Element node) throws DOMException, XMLParserException {
		String functionName = node.getAttribute("function");
		try {
			URL url = new URL(node.getTextContent());
		} catch (MalformedURLException e) {
			throw new XMLParserException("Invalid URL '"+node.getTextContent()+"'");
		}
		if (!functionName.equals("")) {

		}
	}

	@Override
	public String executeXBMCScraperFunction(String functionName,Map<Integer,String> params) throws  XBMCException, XMLParserException {
		Element functionNode = (Element) selectSingleNode(getDocument(), ROOT_NODE_NAME+"/"+functionName);
		if (functionNode==null) {
			throw new XBMCException("Unable to find scraper function '" + functionName+"'");
		}
		return executeXBMCFunction(functionNode,params);
	}


}
