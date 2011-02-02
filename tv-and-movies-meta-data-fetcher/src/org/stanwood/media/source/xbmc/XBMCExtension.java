package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.store.xmlstore.SimpleErrorHandler;
import org.stanwood.media.util.XMLParser;
import org.stanwood.media.util.XMLParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public abstract class XBMCExtension extends XMLParser {

	private final static Log log = LogFactory.getLog(XBMCExtension.class);


	private final static Pattern INFO_PATTERN1 = Pattern.compile("(\\$INFO\\[.*?\\])");
	private final static Pattern INFO_PATTERN2 = Pattern.compile("\\$INFO\\[(.*?)\\]");
	private final static Pattern PARAM_PATTERN = Pattern.compile("(\\$\\$\\d+)");

	private Document doc = null;
	private String point;
	private File scraperFile;
	private XBMCAddon addon;

	public XBMCExtension(XBMCAddon addon,File scraperFile,String point) {
		this.point = point;
		this.scraperFile = scraperFile;
		this.addon = addon;
	}

	protected Document getDocument() throws XBMCException  {
		if (doc==null) {
			File file = scraperFile;
			if (!file.exists()) {
				throw new XBMCException ("Unable to find XMBC scrapper: " + file);
			}

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);

			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				SimpleErrorHandler errorHandler = new SimpleErrorHandler(file);
				builder.setErrorHandler(errorHandler);
				doc = builder.parse(file);
				if (errorHandler.hasErrors()) {
					throw new XBMCException ("Unable to parse  XMBC scrapper, errors found in file: " + file);
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

	public String executeXBMCFunction(Element functionNode,Map<Integer,String> params) throws  XBMCException, XMLParserException {
		if (log.isDebugEnabled()) {
			log.debug("Executing function : " + functionNode.getNodeName());
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
//		String conditional = node.getAttribute("conditional");
		StringBuffer newOutput = new StringBuffer();

		int dest = getDestParam(node);
		boolean appendToDest = shouldAppendToBuffer(node);
		XBMCExpression expression = getExpression(node,params);
		if (expression!=null) {
			if (log.isDebugEnabled()) {
				String in = input;
				if (in.length()>50) {
					in = in.substring(0,50);
				}
				log.debug("perform expr " + expression.toString() +" on [" + in+"]");
			}
			Matcher m = expression.getPattern().matcher(input);
			boolean found = false;

			while (m.find()) {
				String output = orgOutput;
				found = true;

				for (int j=1;j<=m.groupCount();j++) {
					String value = m.group(j);
					value = processValue(expression,value,j);
					output = output.replaceAll("\\\\"+(j), Matcher.quoteReplacement(value));
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

	private XBMCExpression getExpression(Element node,Map<Integer, String> params) throws XBMCException {
		Element expNode = (Element) getChildNodeByName(node, "expression");

		if (expNode !=null) {
			XBMCExpression expr = new XBMCExpression();
			String regexp = "(.+)";
			if (expNode.getTextContent().length()>0) {
				regexp = expNode.getTextContent();
			}

			regexp = applyParams(regexp, params);
			regexp = processInfoVars(regexp);

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

	/* package for test*/ String applyParams(String output, Map<Integer, String> params) {
		StringBuffer buf = new StringBuffer();

		Matcher m = PARAM_PATTERN.matcher(output);
		while (m.find()) {
			int num = Integer.parseInt(m.group().substring(2));
			String value = params.get(num);
			if (value==null) {
				value = "";
			}

			m.appendReplacement(buf, Matcher.quoteReplacement(value));
		}
		m.appendTail(buf);

		return buf.toString();
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

	public abstract String executeXBMCScraperFunction(String functionName,Map<Integer,String> params) throws  XBMCException, XMLParserException;

	protected XBMCAddon getAddon() {
		return addon;
	}
}
