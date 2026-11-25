/**
 * Copyright 2006 Envoi Solutions LLC
 * 
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
package org.codehaus.jettison.badgerfish;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

import org.codehaus.jettison.AbstractXMLStreamReader;
import org.codehaus.jettison.json.JSONObject;

public class BadgerFishXMLStreamReaderTest extends TestCase {
    public void testRootWithText() throws Exception {
        JSONObject obj = new JSONObject("{ \"alice\": { \"$\" : \"bob\" } }");
        AbstractXMLStreamReader reader = new BadgerFishXMLStreamReader(obj);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("alice", reader.getName().getLocalPart());
        
        assertEquals(-1, reader.getLocation().getLineNumber());
        
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("bob", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("alice", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.END_DOCUMENT, reader.next());
    }
   
    public void testTwoChildren() throws Exception {
        JSONObject obj = new JSONObject(
                        "{ \"alice\": { \"bob\" : { \"$\": \"charlie\" }," +
                        " \"david\": { \"$\": \"edgar\"} } }");
        AbstractXMLStreamReader reader = new BadgerFishXMLStreamReader(obj);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("alice", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("bob", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("charlie", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("bob", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("david", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("edgar", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("david", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("alice", reader.getName().getLocalPart());
    }
    
    public void testTwoChildrenWithSameName() throws Exception {
        JSONObject obj = new JSONObject(
        "{ \"alice\": { \"bob\" : [ {\"$\": \"charlie\" }, {\"$\": \"david\" } ] } }");
        AbstractXMLStreamReader reader = new BadgerFishXMLStreamReader(obj);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("alice", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("bob", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("charlie", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("bob", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("bob", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("david", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("bob", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("alice", reader.getName().getLocalPart());
    }

    public void testAttributeAndText() throws Exception {
        JSONObject obj = new JSONObject(
        "{ \"alice\": { \"$\" : \"bob\", \"@charlie\" : \"david\" } }");
        AbstractXMLStreamReader reader = new BadgerFishXMLStreamReader(obj);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("alice", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("bob", reader.getText());
        
        assertEquals(1, reader.getAttributeCount());
        assertEquals("charlie", reader.getAttributeLocalName(0));
        assertEquals("david", reader.getAttributeValue(0));
        assertEquals("", reader.getAttributeNamespace(0));
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("alice", reader.getName().getLocalPart());
    }

    public void testDefaultNamespace() throws Exception {
        JSONObject obj = new JSONObject(
            "{ \"alice\": { \"$\" : \"bob\", \"@xmlns\": { \"$\" : \"http:\\/\\/some-namespace\"} } }");
        AbstractXMLStreamReader reader = new BadgerFishXMLStreamReader(obj);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("alice", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("bob", reader.getText());
        
        assertEquals(0, reader.getAttributeCount());
        assertEquals(1, reader.getNamespaceCount());
        assertEquals("http://some-namespace", reader.getNamespaceURI(0));
        assertEquals("", reader.getNamespacePrefix(0));
        assertEquals("http://some-namespace", reader.getNamespaceURI(""));
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("alice", reader.getName().getLocalPart());
    }
    
    public void testPrefixedNamespace() throws Exception {
        JSONObject obj = new JSONObject(
            "{ \"alice\": { \"$\" : \"bob\", \"@xmlns\": { \"$\" : \"http:\\/\\/some-namespace\", \"charlie\" : \"http:\\/\\/some-other-namespace\" } } }");
        AbstractXMLStreamReader reader = new BadgerFishXMLStreamReader(obj);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("alice", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("bob", reader.getText());
        
        assertEquals(0, reader.getAttributeCount());
        assertEquals(2, reader.getNamespaceCount());
        
        // namespaces are ordered differently on different platforms/jvms, 
        // so we can't really test the order
//        assertEquals("http://some-namespace", reader.getNamespaceURI(0));
//        assertEquals("", reader.getNamespacePrefix(0));
        assertEquals("http://some-namespace", reader.getNamespaceURI(""));
       
//        assertEquals("http://some-other-namespace", reader.getNamespaceURI(1));
//        assertEquals("charlie", reader.getNamespacePrefix(1));
        assertEquals("http://some-other-namespace", reader.getNamespaceURI("charlie"));
         
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("alice", reader.getName().getLocalPart());
    }
    

    
    public void testPrefixedElements() throws Exception {
        JSONObject obj = new JSONObject(
            "{ \"alice\" : { " +
              "\"bob\" : { " +
                "\"$\" : \"david\" , " +
                "\"@xmlns\" : {" +
                  "\"charlie\" : \"http:\\/\\/some-other-namespace\" , " +
                  "\"$\" : \"http:\\/\\/some-namespace\"} " +
                "} , " +
                "\"charlie:edgar\" : { " +
                  "\"$\" : \"frank\" , " +
                  "\"@xmlns\" : {" +
                    "\"charlie\":\"http:\\/\\/some-other-namespace\", " +
                    "\"$\" : \"http:\\/\\/some-namespace\"" +
                  "} " +
                "}, " +
                "\"@xmlns\" : { " +
                  "\"charlie\" : \"http:\\/\\/some-other-namespace\", " +
                  "\"$\" : \"http:\\/\\/some-namespace\"" +
                "} " +
              "} }");
        AbstractXMLStreamReader reader = new BadgerFishXMLStreamReader(obj);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("alice", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("bob", reader.getLocalName());
        
        assertEquals(0, reader.getAttributeCount());
        assertEquals(2, reader.getNamespaceCount());
        
//        assertEquals("http://some-namespace", reader.getNamespaceURI(0));
//        assertEquals("", reader.getNamespacePrefix(0));
        assertEquals("http://some-namespace", reader.getNamespaceURI(""));
        
//        assertEquals("http://some-other-namespace", reader.getNamespaceURI(1));
//        assertEquals("charlie", reader.getNamespacePrefix(1));
        assertEquals("http://some-other-namespace", reader.getNamespaceURI("charlie"));
       
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("david", reader.getText());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("bob", reader.getName().getLocalPart());
        
        // ----
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("edgar", reader.getLocalName());
        assertEquals("charlie", reader.getPrefix());
        assertEquals("http://some-other-namespace", reader.getNamespaceURI());
        
        assertEquals(0, reader.getAttributeCount());
        assertEquals(2, reader.getNamespaceCount());
        
//        assertEquals("http://some-namespace", reader.getNamespaceURI(0));
//        assertEquals("", reader.getNamespacePrefix(0));
        assertEquals("http://some-namespace", reader.getNamespaceURI(""));
        
//        assertEquals("http://some-other-namespace", reader.getNamespaceURI(1));
//        assertEquals("charlie", reader.getNamespacePrefix(1));
        assertEquals("http://some-other-namespace", reader.getNamespaceURI("charlie"));
       
        
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("frank", reader.getText());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("edgar", reader.getName().getLocalPart());
        
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("alice", reader.getName().getLocalPart());
    }

    public void testElementWithInvalidValue() throws Exception {
        JSONObject obj = new JSONObject("{ \"alice\": { \"foo\" : \"bob\" } }");
        AbstractXMLStreamReader reader = new BadgerFishXMLStreamReader(obj);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("alice", reader.getName().getLocalPart());
        
        try {
        	reader.next();
        	fail("Must cause exception");
        } catch (XMLStreamException e) {
        }
    }

    public void testEmptyObjectIsRejected() throws Exception {
        try {
            new BadgerFishXMLStreamReader(new JSONObject("{}"));
            fail("Failure expected on a JSON object with no root element");
        } catch (XMLStreamException ex) {
            assertEquals("The JSON object has no root element", ex.getMessage());
        }
    }

    public void testEmptyObjectIsRejectedByFactory() throws Exception {
        try {
            new BadgerFishXMLInputFactory().createXMLStreamReader(new StringReader(" { } "));
            fail("Failure expected on a JSON object with no root element");
        } catch (XMLStreamException ex) {
            assertEquals("The JSON object has no root element", ex.getMessage());
        }
    }

    // https://github.com/jettison-json/jettison/issues/9
    public void testGetElementText() throws Exception {
        XMLStreamReader reader = new BadgerFishXMLStreamReader(
            new JSONObject("{\"root\":{\"a\":{\"$\":\"text\"},\"b\":{\"@attr\":\"x\",\"$\":\"more\"},\"c\":{}}}"));
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getLocalName());

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("a", reader.getLocalName());
        assertEquals("text", reader.getElementText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.getEventType());
        assertEquals("a", reader.getLocalName());

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("b", reader.getLocalName());
        assertEquals("x", reader.getAttributeValue(null, "attr"));
        assertEquals("more", reader.getElementText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.getEventType());
        assertEquals("b", reader.getLocalName());

        // an empty element has empty text content
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("c", reader.getLocalName());
        assertEquals("", reader.getElementText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.getEventType());
        assertEquals("c", reader.getLocalName());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("root", reader.getLocalName());
        assertEquals(XMLStreamReader.END_DOCUMENT, reader.next());
    }

    public void testGetElementTextOnCharacters() throws Exception {
        // the previous behaviour of returning the text from a CHARACTERS event is kept
        XMLStreamReader reader = new BadgerFishXMLStreamReader(new JSONObject("{\"a\":{\"$\":\"text\"}}"));
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("text", reader.getElementText());
        assertEquals(XMLStreamReader.CHARACTERS, reader.getEventType());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
    }

    public void testGetElementTextWithChildElement() throws Exception {
        XMLStreamReader reader = new BadgerFishXMLStreamReader(new JSONObject("{\"a\":{\"b\":{\"$\":\"text\"}}}"));
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        try {
            reader.getElementText();
            fail("Failure expected on an element with a child element");
        } catch (XMLStreamException ex) {
            // expected
        }
    }

    public void testHasNameAndRequire() throws Exception {
        XMLStreamReader reader = new BadgerFishXMLStreamReader(new JSONObject("{\"root\":{\"$\":\"text\"}}"));
        assertFalse(reader.hasName());

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertTrue(reader.hasName());
        reader.require(XMLStreamReader.START_ELEMENT, "", "root");
        try {
            reader.require(XMLStreamReader.START_ELEMENT, null, "other");
            fail("Failure expected on a different local name");
        } catch (XMLStreamException ex) {
            // expected
        }

        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertFalse(reader.hasName());
        try {
            reader.require(XMLStreamReader.START_ELEMENT, null, null);
            fail("Failure expected on a different event type");
        } catch (XMLStreamException ex) {
            // expected
        }

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertTrue(reader.hasName());
        reader.require(XMLStreamReader.END_ELEMENT, null, "root");
    }

    public void testGetTextCharacters() throws Exception {
        XMLStreamReader reader = new BadgerFishXMLStreamReader(new JSONObject("{\"root\":{\"$\":\"hello\"}}"));
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        char[] target = new char[16];
        assertEquals(5, reader.getTextCharacters(0, target, 0, 16));
        assertEquals("hello", new String(target, 0, 5));
        assertEquals(0, reader.getTextCharacters(5, target, 0, 16));
    }

    public void testNamespaceContext() throws Exception {
        XMLStreamReader reader = new BadgerFishXMLStreamReader(new JSONObject(
            "{\"a\":{\"@xmlns\":{\"p\":\"urn:1\",\"$\":\"urn:d\"},"
            + "\"b\":{\"@xmlns\":{\"p\":\"urn:2\"},\"$\":\"t\"}}}"));
        // the context reflects the current position of the reader
        NamespaceContext ctx = reader.getNamespaceContext();
        assertNotNull(ctx);

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("a", reader.getLocalName());
        assertEquals("urn:1", ctx.getNamespaceURI("p"));
        assertEquals("urn:d", ctx.getNamespaceURI(""));
        assertEquals("p", ctx.getPrefix("urn:1"));
        assertEquals("", ctx.getPrefix("urn:d"));
        assertNull(ctx.getNamespaceURI("unbound"));
        assertNull(ctx.getPrefix("urn:unbound"));
        assertFalse(ctx.getPrefixes("urn:unbound").hasNext());

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("b", reader.getLocalName());
        // the inner declaration of p takes precedence, and the default namespace is inherited
        assertEquals("urn:2", ctx.getNamespaceURI("p"));
        assertEquals("urn:d", ctx.getNamespaceURI(""));
        assertNull(ctx.getPrefix("urn:1"));
        Iterator<?> prefixes = ctx.getPrefixes("urn:2");
        assertEquals("p", prefixes.next());
        assertFalse(prefixes.hasNext());
        assertEquals("urn:2", reader.getNamespaceContext().getNamespaceURI("p"));
    }

    public void testNonStringAttributeValues() throws Exception {
        XMLStreamReader reader = new BadgerFishXMLStreamReader(new JSONObject(
            "{\"a\":{\"@n\":5,\"@d\":1.5,\"@b\":true,\"@s\":\"x\",\"@z\":null,\"$\":\"t\"}}"));
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals(5, reader.getAttributeCount());
        assertEquals("5", reader.getAttributeValue(null, "n"));
        assertEquals("1.5", reader.getAttributeValue(null, "d"));
        assertEquals("true", reader.getAttributeValue(null, "b"));
        assertEquals("x", reader.getAttributeValue(null, "s"));
        assertNull(reader.getAttributeValue(null, "z"));
        assertEquals("t", reader.getElementText());
    }

    public void testPrefixedAttributeBeforeNamespaceDeclaration() throws Exception {
        // the attribute appears before the namespace declaration it uses
        XMLStreamReader reader = new BadgerFishXMLStreamReader(new JSONObject(
            "{\"a\":{\"@p:x\":\"1\",\"@xmlns\":{\"p\":\"urn:p\"},\"@y\":\"2\"}}"));
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals(2, reader.getAttributeCount());
        assertEquals("1", reader.getAttributeValue("urn:p", "x"));
        assertEquals("p", reader.getAttributePrefix(0));
        assertEquals("2", reader.getAttributeValue(null, "y"));
        assertEquals(1, reader.getNamespaceCount());
        assertEquals("urn:p", reader.getNamespaceURI("p"));

        // the declaration before the attribute still works
        reader = new BadgerFishXMLStreamReader(new JSONObject(
            "{\"a\":{\"@xmlns\":{\"p\":\"urn:p\"},\"@p:x\":\"1\"}}"));
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("1", reader.getAttributeValue("urn:p", "x"));
    }

    public void testRoundTripAttributeWrittenBeforeNamespace() throws Exception {
        // writing the attribute before its namespace declaration is legal in StAX
        StringWriter strWriter = new StringWriter();
        XMLStreamWriter writer = new BadgerFishXMLStreamWriter(strWriter);
        writer.writeStartDocument();
        writer.writeStartElement("a");
        writer.writeAttribute("p", "urn:p", "x", "1");
        writer.writeNamespace("p", "urn:p");
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();

        XMLStreamReader reader = new BadgerFishXMLStreamReader(new JSONObject(strWriter.toString()));
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("1", reader.getAttributeValue("urn:p", "x"));
    }
}
