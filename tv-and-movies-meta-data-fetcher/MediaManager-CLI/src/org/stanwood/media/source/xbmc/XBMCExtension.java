package org.stanwood.media.source.xbmc;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stanwood.media.xml.SimpleErrorHandler;
import org.stanwood.media.xml.XMLParser;
import org.stanwood.media.xml.XMLParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * All types of XBMC extension classes should inherit from this class as it implements
 * methods generic to all XBMC extensions.
 */
public abstract class XBMCExtension extends XMLParser {

	private final static Log log = LogFactory.getLog(XBMCExtension.class);


	private final static Pattern INFO_PATTERN1 = Pattern.compile("(\\$INFO\\[.*?\\])"); //$NON-NLS-1$
	private final static Pattern INFO_PATTERN2 = Pattern.compile("\\$INFO\\[(.*?)\\]"); //$NON-NLS-1$
	private final static Pattern PARAM_PATTERN = Pattern.compile("(\\$\\$\\d+)"); //$NON-NLS-1$

	private Document doc = null;
	private String point;
	private File scraperFile;
	private XBMCAddon addon;

	/**
	 * The constructor
	 * @param addon the addon
	 * @param scraperFile The scraper file the extension is been read from
	 * @param point The extension point been used for the extension
	 */
	public XBMCExtension(XBMCAddon addon,File scraperFile,String point) {
		this.point = point;
		this.scraperFile = scraperFile;
		this.addon = addon;
	}

	protected Document getDocument() throws XBMCException  {
		if (doc==null) {
			File file = scraperFile;
			if (!file.exists()) {
				throw new XBMCException (MessageFormat.format(Messages.getString("XBMCExtension.UNABLE_FIND_SCRAPER"),file)); //$NON-NLS-1$
			}

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);

			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				SimpleErrorHandler errorHandler = new SimpleErrorHandler(file);
				builder.setErrorHandler(errorHandler);
				doc = builder.parse(file);
				if (errorHandler.hasErrors()) {
					throw new XBMCException (MessageFormat.format(Messages.getString("XBMCExtension.UNABLE_PARSE_SCRAPER_HAS_ERRORS"), file)); //$NON-NLS-1$
				}
			} catch (SAXException e) {
				throw new XBMCException (Messages.getString("XBMCExtension.UNABLE_PARSE_SCRAPER"), e); //$NON-NLS-1$
			} catch (IOException e) {
				throw new XBMCException (Messages.getString("XBMCExtension.UNABLE_READ_SCRAPPER"), e); //$NON-NLS-1$
			} catch (ParserConfigurationException e) {
				throw new XBMCException (Messages.getString("XBMCExtension.UNABLE_PARSE_SCRAPER"), e); //$NON-NLS-1$
			}
		}
		return doc;
	}

	/**
	 * Used to execute a XBMC function
	 * @param functionNode The node of the function
	 * @param params The parameters been passed to the function
	 * @return The result of executing the function
	 * @throws XBMCException Thrown if their is a XBMC problem
	 * @throws XMLParserException Thrown if their is a XML problem
	 */
	public String executeXBMCFunction(Element functionNode,Map<Integer,String> params) throws  XBMCException, XMLParserException {
		if (log.isDebugEnabled()) {
			log.debug("Executing function : " + functionNode.getNodeName()); //$NON-NLS-1$
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

				if (node.getNodeName().equals("RegExp")) { //$NON-NLS-1$
					performRegexp(params, node);
				}
			}
		}
	}

	private void performRegexp(Map<Integer, String> params, Element node) throws XBMCException {
		String input = getInputAttr(params,node);
		String orgOutput = getOutputAttr(params,node);

		String conditional = node.getAttribute("conditional"); //$NON-NLS-1$
		if ( (!conditional.equals("")) && !addon.checkCondition(conditional)) { //$NON-NLS-1$
			return;
		}

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
				log.debug("perform expr " + expression.toString() +" on [" + in+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			Matcher m = expression.getPattern().matcher(input);
			boolean found = false;

			while (m.find()) {
				String output = orgOutput;
				found = true;

				for (int j=1;j<=m.groupCount();j++) {
					String value = m.group(j);
					if (value!=null) {
						value = processValue(expression,value,j);
						output = output.replaceAll("\\\\"+(j), Matcher.quoteReplacement(value)); //$NON-NLS-1$
					}
				}
				if (found==false && expression.getClear()) {
					output = ""; //$NON-NLS-1$
				}
				newOutput.append(output);

				if (!expression.getRepeat()) {
					break;
				}
			}
			if (found==false && !expression.getClear()) {
				return;
			}
		}
		else {
			newOutput.append(input);
		}

//		String s = XMLParser.encodeAttributeValue(newOutput.toString());
		String s = newOutput.toString();
		String output = processInfoVars(s);

		if (dest!=-1) {
			if (log.isDebugEnabled()) {
				log.debug("Put param " + dest + " - " + output);  //$NON-NLS-1$//$NON-NLS-2$
			}
			if (appendToDest) {
				String orgValue = params.get(Integer.valueOf(dest));
				if (orgValue!=null) {
					output = orgValue+output;
				}
			}

			params.put(Integer.valueOf(dest), output);
		}

	}

	private String processValue(XBMCExpression expression,String value,int group) {
		if (!expression.getNoClean(group)) {
			value = value.replaceAll("\\<.*?\\>", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (expression.getTrim(group)) {
			value = value.trim();
		}
		return value;
	}

	private XBMCExpression getExpression(Element node,Map<Integer, String> params) throws XBMCException {
		Element expNode = (Element) getChildNodeByName(node, "expression"); //$NON-NLS-1$

		if (expNode !=null) {
			XBMCExpression expr = new XBMCExpression();
			String regexp = "(.+)"; //$NON-NLS-1$
			if (expNode.getTextContent().length()>0) {
				regexp = expNode.getTextContent();
			}

			regexp = applyParams(regexp, params);
			regexp = processInfoVars(regexp);

			Pattern p = Pattern.compile(regexp,Pattern.MULTILINE | Pattern.DOTALL);
			expr.setPattern(p);

			expr.setNoClean(expNode.getAttribute("noclean")); //$NON-NLS-1$


			expr.setClear((expNode.getAttribute("clear").equals("yes"))); //$NON-NLS-1$ //$NON-NLS-2$

			expr.setRepeat((expNode.getAttribute("repeat").equals("yes"))); //$NON-NLS-1$ //$NON-NLS-2$

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
				value = ""; //$NON-NLS-1$
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
		String sDest = node.getAttribute("dest"); //$NON-NLS-1$
		if (sDest!=null && sDest.length()>0) {
			if (sDest.endsWith("+")) { //$NON-NLS-1$
				sDest = sDest.substring(0,sDest.length()-1);
			}
			dest = Integer.parseInt(sDest);
		}
		return dest;
	}

	private boolean shouldAppendToBuffer(Element node) {
		String sDest = node.getAttribute("dest"); //$NON-NLS-1$
		return (sDest.endsWith("+")); //$NON-NLS-1$
	}

	private String getInputAttr(Map<Integer, String> params, Element node) {
		String value = node.getAttribute("input"); //$NON-NLS-1$
		if (value==null || value.equals("")) { //$NON-NLS-1$
			value="$$1"; //$NON-NLS-1$
		}
		value = applyParams(value,params);
		return value;
	}

	private String getOutputAttr(Map<Integer, String> params, Element node) {
		String value = node.getAttribute("output"); //$NON-NLS-1$
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

	/**
	 * Used to execute a XBMC function
	 * @param functionName The name of the function
	 * @param params The parameters been passed to the function
	 * @return The result of executing the function
	 * @throws XBMCException Thrown if their is a XBMC problem
	 * @throws XMLParserException Thrown if their is a XML problem
	 */
	public abstract String executeXBMCScraperFunction(String functionName,Map<Integer,String> params) throws  XBMCException, XMLParserException;

	protected XBMCAddon getAddon() {
		return addon;
	}


	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("XBMCExtension:"); //$NON-NLS-1$
		result.append(scraperFile);
		result.append(" : "); //$NON-NLS-1$
		result.append(point);
		return result.toString();
	}


}
