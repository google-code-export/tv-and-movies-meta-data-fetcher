package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.stanwood.media.xml.SimpleErrorHandler;
import org.stanwood.media.xml.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XBMCScraperParser extends XMLParser {
	
	private static final int MAX_SCRAPER_BUFFERS = 20;
	
	private Document document = null;
	private Element rootElement = null; 
	private String searchStringEncoding = "UTF-8";
	private File scraperFile = null;
	private String params[] = new String[MAX_SCRAPER_BUFFERS];

	public void load(File scraperFile) throws XBMCScraperException {
		clear();
		
		document = getDocument(scraperFile);
		
		this.scraperFile = scraperFile;
		
		loadFromXML();
	}
	
	private void loadFromXML() throws XBMCScraperException {
		rootElement = (Element) document.getChildNodes().item(0);
		if (rootElement.getNodeName().equals("scraper")) {
			Element el = getFirstChildElement(rootElement, "CreateSearchUrl");
			if (el!=null) {
				if (!el.hasAttribute("SearchStringEncoding")) {
					searchStringEncoding = "UTF-8";
				}
			}
			return;
		}
		clear();
		
		throw new XBMCScraperException("Unable to find root node <scraper>");
	}

	public void clear() {
		rootElement = null;
		document = null;
		scraperFile = null;
	}
	
	public File getScraperFile() {
		return scraperFile;
	}
	
	
	public boolean hasFunction(String functionName) {
		return getFirstChildElement(rootElement,functionName) != null;
	}
	
	private Document getDocument(File scraperFile) throws XBMCScraperException {	
		if (!scraperFile.exists()) {
			throw new XBMCScraperException("Unable to find XMBC scrapper: " + scraperFile);
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);

		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			SimpleErrorHandler errorHandler = new SimpleErrorHandler(scraperFile);
			builder.setErrorHandler(errorHandler);
			Document doc = builder.parse(scraperFile);
			if (errorHandler.hasErrors()) {
				throw new XBMCScraperException("Unable to parse  XMBC scrapper, errors found in file: " + scraperFile);
			}
			return doc;
		} catch (SAXException e) {
			throw new XBMCScraperException("Unable to parse XMBC scrapper: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new XBMCScraperException("Unable to read XMBC scrapper: " + e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			throw new XBMCScraperException("Unable to parse  XMBC scrapper: " + e.getMessage(), e);
		}				
	}	
	
	private void parseExpression(String input,String dest,Element element,boolean append) {
		String output = element.getAttribute("output");
		
		Element expressionEl = getFirstChildElement(element, "expression");
		if (expressionEl!=null) {
			int flags = Pattern.DOTALL;
			String sensitive = expressionEl.getAttribute("cs");
			if (sensitive!=null && sensitive.equals("yes")) {			
				flags = flags | Pattern.CASE_INSENSITIVE;
			}
			
			String expression;
			if (firstChild(expressionEl)!=null) {
				expression = firstChild(expressionEl).getNodeValue();
			}
			else {
				expression = "(.*)";
			}
								
			expression = replaceBuffers(expression);
			output = replaceBuffers(output);
			
			Pattern reg = Pattern.compile(expression, flags);

		}
	}

	private String replaceBuffers(String output) {
		int pos = -1;
		while ((pos = output.indexOf("\\"))!=-1) {		
			int paramNum = getParam(output,pos);
			String paramValue = params[paramNum];
			if (paramValue!=null) {			
				StringBuilder newOutput = new StringBuilder();
				newOutput.append(output.substring(0,pos));
				String paramIndex = String.valueOf(paramNum);					
				newOutput.append(paramValue);
				newOutput.append(output.substring(pos+1+paramIndex.length()));					
				output = newOutput.toString();
			}
			else {
				System.err.println("Did not find param : " + paramNum);
			}
		}	
		return output;
		
	}

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
	
	
}
