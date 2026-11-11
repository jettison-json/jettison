/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.jettison;

import java.io.ByteArrayInputStream;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.codehaus.jettison.badgerfish.BadgerFishDOMDocumentParser;
import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedDOMDocumentParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class AbstractDOMDocumentParserTest extends TestCase {

    public void testDocumentBuilderFactoryIsHardened() throws Exception {
        DocumentBuilderFactory factory = AbstractDOMDocumentParser.createDocumentBuilderFactory();
        assertTrue(factory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
        assertTrue(factory.getFeature("http://apache.org/xml/features/disallow-doctype-decl"));
        assertFalse(factory.isXIncludeAware());
        assertFalse(factory.isExpandEntityReferences());
        assertTrue(factory.isNamespaceAware());
        assertFalse(factory.isValidating());
    }

    public void testDoctypeIsRejected() throws Exception {
        DocumentBuilder builder = AbstractDOMDocumentParser.createDocumentBuilderFactory().newDocumentBuilder();
        builder.setErrorHandler(new ThrowingErrorHandler());
        try {
            builder.parse(new InputSource(new StringReader("<!DOCTYPE root><root/>")));
            fail("Failure expected on a DOCTYPE declaration");
        } catch (SAXException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("DOCTYPE"));
        }
    }

    public void testDocumentWithoutDoctypeIsParsed() throws Exception {
        DocumentBuilder builder = AbstractDOMDocumentParser.createDocumentBuilderFactory().newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(
                "<root xmlns=\"urn:test\" a=\"1\"><child>text</child></root>")));
        Element root = doc.getDocumentElement();
        assertEquals("urn:test", root.getNamespaceURI());
        assertEquals("1", root.getAttribute("a"));
        assertEquals("text", root.getFirstChild().getTextContent());
    }

    public void testMappedJsonToDom() throws Exception {
        String json = "{\"root\":{\"@id\":\"1\",\"child\":[\"a\",\"b\"]}}";
        Document doc = new MappedDOMDocumentParser(new Configuration())
                .parse(new ByteArrayInputStream(json.getBytes("UTF-8")));
        Element root = doc.getDocumentElement();
        assertEquals("root", root.getTagName());
        assertEquals("1", root.getAttribute("id"));
        assertEquals(2, root.getElementsByTagName("child").getLength());
        assertEquals("b", root.getElementsByTagName("child").item(1).getTextContent());
    }

    public void testBadgerFishJsonToDom() throws Exception {
        String json = "{\"root\":{\"@id\":\"1\",\"child\":{\"$\":\"text\"}}}";
        Document doc = new BadgerFishDOMDocumentParser()
                .parse(new ByteArrayInputStream(json.getBytes("UTF-8")));
        Element root = doc.getDocumentElement();
        assertEquals("root", root.getTagName());
        assertEquals("1", root.getAttribute("id"));
        assertEquals("text", root.getElementsByTagName("child").item(0).getTextContent());
    }

    private static final class ThrowingErrorHandler implements ErrorHandler {
        public void warning(SAXParseException ex) {
        }

        public void error(SAXParseException ex) throws SAXException {
            throw ex;
        }

        public void fatalError(SAXParseException ex) throws SAXException {
            throw ex;
        }
    }
}
