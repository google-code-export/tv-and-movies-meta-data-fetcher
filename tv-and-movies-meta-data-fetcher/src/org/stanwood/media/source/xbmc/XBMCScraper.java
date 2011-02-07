package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.model.Mode;
import org.stanwood.media.source.SourceException;
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
	private XBMCAddonManager addonMgr;

	/**
	 * Used to create the class and set the scraper file
	 * @param addon The addon been used
	 * @param scraperFile The XML scraper file
	 */
	public XBMCScraper(XBMCAddon addon,File scraperFile,String point,Mode mode) {
		super(addon,scraperFile,point);
		addonMgr = addon.getManager();

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
		return executeFunctionByName("CreateSearchUrl",searchTerm,year);
	}

	public Document getEpisodeGuideUrl(String rawHtml) throws XBMCException {
		return executeFunctionByName("EpisodeGuideUrl",rawHtml);
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
		return executeFunctionByName("GetSearchResults",rawHtml,searchTerm);
	}

	/**
	 * Used to get the show/film details as a XML document. It takes as input the
	 * 1 or more webpages downloaded from the URL obtained with the @{link getCreateSearchUrl(String,String)} call.
	 * @param contents A list of webpage contents
	 * @return The results as a XML document
	 * @throws XBMCException Thrown if their are any problems
	 */
	public Document getGetDetails(String... contents) throws  XBMCException {
		return executeFunctionByName("GetDetails",contents);
	}

	public Document getGetEpisodeDetails(String... contents) throws  XBMCException {
		return executeFunctionByName("GetEpisodeDetails",contents);
	}

	private Document executeFunctionByName(String funcName,String... contents) throws  XBMCException {
		try {
			Map<Integer, String> params = convertParams(contents);

			String result = executeXBMCScraperFunction(funcName,params);
			Document doc = XMLParser.strToDom(result);
			resolveElements(doc);
			return doc;
		} catch (Exception e) {
			throw new XBMCException("Unable to execute scraper function '"+funcName+"' in addon '"+getAddon().getId()+"'",e);
		}
	}

	public Document getGetEpisodeList(String html, URL showURL) throws XBMCException {
		return executeFunctionByName("GetEpisodeList",html,showURL.toExternalForm());
	}

	private void resolveElements(Document doc) throws XMLParserException,
			XBMCException, IOException, SourceException {
		for (Node node : selectNodeList(doc, "details/chain")) {
			resolveChainNodes(doc,(Element) node);
		}

		for (Node node : selectNodeList(doc, "details/url")) {
			resolveUrlNodes(doc,(Element) node);
		}
	}

	private Map<Integer, String> convertParams(String... contents)
			throws XBMCException {
		Map<Integer,String>params = new HashMap<Integer,String>();
		if (contents.length>=9) {
			throw new XBMCException("Not allowed more than 9 params");
		}
		for (int i=0;i<contents.length;i++) {
			params.put(i+1, contents[i]);
		}
		return params;
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

	private void resolveUrlNodes(final Document doc,final Element node) throws DOMException, XMLParserException, IOException, SourceException {
		final String functionName = node.getAttribute("function");
		try {
			URL url = new URL(node.getTextContent());

			if (!functionName.equals("")) {
				StreamProcessor processor = new StreamProcessor(addonMgr.getStreamToURL(url)) {


					@Override
					public void processContents(String contents) throws SourceException {
						Map<Integer, String> params = new HashMap<Integer,String>();
						params.put(1, contents);
						try {
							Document results = strToDom(getAddon().executeFunction(functionName, params));
							Node parent = node.getParentNode();
							parent.removeChild(node);

							for (Node n : selectNodeList(results, "details/*")) {
								Node newNode = doc.importNode(n,true);
								parent.appendChild(newNode);
							}
						} catch (XMLParserException e) {
							throw new SourceException("Unable to execute function '"+functionName+"'");
						}
					}
				};
				processor.handleStream();
			}
		} catch (MalformedURLException e) {
			throw new XMLParserException("Invalid URL '"+node.getTextContent()+"'");
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
