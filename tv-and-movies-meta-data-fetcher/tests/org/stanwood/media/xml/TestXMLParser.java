package org.stanwood.media.xml;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Used to test the {@link XMLParser}
 */
public class TestXMLParser {

	/**
	 * Used to test encodings
	 */
	@Test
	public void testEncoding() {
		Assert.assertEquals("Don&apos;t look back",XMLParser.encodeAttributeValue("Don't look back"));
		Assert.assertEquals("me &amp; you",XMLParser.encodeAttributeValue("me & you"));
	}

	/**
	 * Used to test writing entities
	 * @throws Exception Thrown if their are problems
	 */
	@Test
	public void testWrightingEntities() throws Exception {
		XMLParser parser = new XMLParser();

		Document dom = XMLParser.strToDom("<test></test>");

		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<test/>\n",XMLParser.domToStr(dom));

		Element testNode = (Element) parser.selectSingleNode(dom, "/test");
		testNode.setAttribute("test1", "this is a test");
		testNode.setAttribute("test2", "Don't look back");
		testNode.setAttribute("test3", "me & you");

		StringBuilder expected = new StringBuilder();
		expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		expected.append("<test test1=\"this is a test\" test2=\"Don't look back\" test3=\"me &amp; you\"/>\n");
		Assert.assertEquals(expected.toString(),XMLParser.domToStr(dom));

		Assert.assertNotNull(parser.selectSingleNode(dom, "/test[@test1="+parser.quoteXPathQuery("this is a test")+"]"));
		Assert.assertNotNull(parser.selectSingleNode(dom, "/test[@test2="+parser.quoteXPathQuery("Don't look back")+"]"));
		Assert.assertNotNull(parser.selectSingleNode(dom, "/test[@test3="+parser.quoteXPathQuery("me & you")+"]"));
	}
}
