package org.stanwood.media;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.junit.Assert;
import org.stanwood.media.util.FileHelper;
import org.stanwood.media.util.XMLParser;
import org.stanwood.media.util.XMLParserException;
import org.w3c.dom.Document;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * A helper class for tests
 */
public class TestHelper {

	/**
	 * Used to compare XML files
	 * @param expected The stream to the expected XML file
	 * @param actual The stream to the actual XML file
	 * @param params Parameters which are placed into the expected contents where it finds $<key>$.
	 * @throws IOException Thrown if their are IO problems
	 * @throws XMLParserException Thrown if their are XML problems
	 */
	public static void assertXMLEquals(InputStream expected,InputStream actual,Map<String,String> params) throws IOException, XMLParserException {
		String actualContents = FileHelper.readFileContents(actual);
		String expectedContents = FileHelper.readFileContents(expected);

		for (Entry<String,String>e : params.entrySet()) {
			expectedContents = expectedContents.replaceAll("\\$+"+e.getKey()+"\\$",Matcher.quoteReplacement(e.getValue()));
		}
		Assert.assertEquals("Check the XML files are equal",formatXML(expectedContents),formatXML(actualContents));

	}

	private static String formatXML(String xml) throws XMLParserException, IOException {
		Document document = XMLParser.strToDom(xml);
		OutputFormat format = new OutputFormat(document);
        format.setLineWidth(65);
        format.setIndenting(true);
        format.setIndent(2);
        Writer out = new StringWriter();
        XMLSerializer serializer = new XMLSerializer(out, format);
        serializer.serialize(document);

		return out.toString();
	}

	/**
	 * Used to compare XML files
	 * @param expected The stream to the expected XML file
	 * @param actual The stream to the actual XML file
	 * @throws IOException Thrown if their are IO problems
	 * @throws XMLParserException Thrown if their are XML problems
	 */
	public static void assertXMLEquals(InputStream expected,InputStream actual) throws IOException, XMLParserException {
		assertXMLEquals(actual,expected,new HashMap<String,String>());
	}

}
