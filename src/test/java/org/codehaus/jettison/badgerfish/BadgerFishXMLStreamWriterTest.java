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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.codehaus.jettison.AbstractXMLStreamWriter;
import org.codehaus.jettison.Node;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.util.FastStack;

public class BadgerFishXMLStreamWriterTest extends TestCase {
    public void testRootWithText() throws Exception {
        StringWriter strWriter = new StringWriter();
        AbstractXMLStreamWriter w = new BadgerFishXMLStreamWriter(strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("alice");
        w.writeCharacters("bob");
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertJSONEquals("{\"alice\":{\"$\":\"bob\"}}", strWriter.toString());
    }
   
    private void assertJSONEquals(String string, String string2) {
        string = string.replace(" ", "");
        string2 = string2.replace(" ", "");
        
        assertEquals(string, string2);
    }

    public void testTwoChildren() throws Exception {
        StringWriter strWriter = new StringWriter();
        AbstractXMLStreamWriter w = new BadgerFishXMLStreamWriter(strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("alice");

        w.writeStartElement("bob");
        w.writeCharacters("charlie");
        w.writeEndElement();
        
        w.writeStartElement("david");
        w.writeCharacters("edgar");
        w.writeEndElement();
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertJSONEquals("{\"alice\":{\"bob\":{\"$\":\"charlie\"},"+
                         "\"david\":{\"$\":\"edgar\"}}}", strWriter.toString());
    }
    
    public void testTwoChildrenWithSameName() throws Exception {
        StringWriter strWriter = new StringWriter();
        AbstractXMLStreamWriter w = new BadgerFishXMLStreamWriter(strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("alice");

        w.writeStartElement("bob");
        w.writeCharacters("charlie");
        w.writeEndElement();
        
        w.writeStartElement("bob");
        w.writeCharacters("david");
        w.writeEndElement();
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertJSONEquals("{\"alice\":{\"bob\":[{\"$\":\"charlie\"},{\"$\":\"david\"}]}}", 
                         strWriter.toString());
    }

    public void testMultipleChildrenAgain() throws Exception {
        StringWriter strWriter = new StringWriter();
        AbstractXMLStreamWriter xsw = new BadgerFishXMLStreamWriter(strWriter);
        
        xsw.writeStartDocument();
        xsw.writeStartElement("Ratings");

        xsw.writeStartElement("Rating");
        xsw.writeAttribute("id", "100");
        xsw.writeAttribute("value", "3");
        xsw.writeEndElement();

        xsw.writeStartElement("Rating");
        xsw.writeAttribute("id", "200");
        xsw.writeAttribute("value", "4");
        xsw.writeEndElement();

        xsw.writeStartElement("Rating");
        xsw.writeAttribute("id", "300");
        xsw.writeAttribute("value", "5");
        xsw.writeEndElement();

        xsw.writeEndElement();
        xsw.writeEndDocument ();
        xsw.flush();

        xsw.close();
        strWriter.close();
        System.out.println(strWriter.toString());
        
        assertJSONEquals("{\"Ratings\":{\"Rating\":[" +
                        "{\"@id\":\"100\",\"@value\":\"3\"}," +
                        "{\"@id\":\"200\",\"@value\":\"4\"}," +
                        "{\"@id\":\"300\",\"@value\":\"5\"}" +
                        "]}}", 
                         strWriter.toString());
    }
    
    public void testAttributeAndText() throws Exception {
        StringWriter strWriter = new StringWriter();
        AbstractXMLStreamWriter w = new BadgerFishXMLStreamWriter(strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("alice");
        
        w.writeAttribute("charlie", "david");
        w.writeCharacters("bob");
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertJSONEquals("{\"alice\":{\"@charlie\":\"david\",\"$\":\"bob\"}}", 
                         strWriter.toString());
    }

    public void testWhitespaceOnlyTextIsPreserved() throws Exception {
        StringWriter strWriter = new StringWriter();
        AbstractXMLStreamWriter w = new BadgerFishXMLStreamWriter(strWriter);

        w.writeStartDocument();
        w.writeStartElement("alice");
        w.writeCharacters("   ");
        w.writeEndElement();
        w.writeEndDocument();

        w.close();
        strWriter.close();

        JSONObject json = new JSONObject(strWriter.toString());
        assertEquals("   ", json.getJSONObject("alice").getString("$"));
    }

    public void testEmptyTextIsIgnored() throws Exception {
        StringWriter strWriter = new StringWriter();
        AbstractXMLStreamWriter w = new BadgerFishXMLStreamWriter(strWriter);

        w.writeStartDocument();
        w.writeStartElement("alice");
        w.writeCharacters("");
        w.writeEndElement();
        w.writeEndDocument();

        w.close();
        strWriter.close();

        JSONObject json = new JSONObject(strWriter.toString());
        assertFalse(json.getJSONObject("alice").has("$"));
    }

    public void testDefaultNamespace() throws Exception {
        StringWriter strWriter = new StringWriter();
        BadgerFishXMLStreamWriter w = new BadgerFishXMLStreamWriter(strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("alice");
        
        w.writeDefaultNamespace("http://some-namespace");
        w.writeCharacters("bob");
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertJSONEquals("{\"alice\":{\"@xmlns\":{\"$\":\"http:\\/\\/some-namespace\"},\"$\":\"bob\"}}", 
                         strWriter.toString());
    }
    
    public void testPrefixedNamespace() throws Exception {
        StringWriter strWriter = new StringWriter();
        BadgerFishXMLStreamWriter w = new BadgerFishXMLStreamWriter(strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("alice");
        
        w.writeDefaultNamespace("http://some-namespace");
        w.writeNamespace("charlie", "http://some-other-namespace");
        
        assertEquals("http://some-other-namespace", w.getNamespaceContext().getNamespaceURI("charlie"));
        assertEquals("charlie", w.getNamespaceContext().getPrefix("http://some-other-namespace"));
        
        w.writeCharacters("bob");
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertJSONEquals("{ \"alice\": { \"@xmlns\": { \"$\" : \"http:\\/\\/some-namespace\", \"charlie\" : \"http:\\/\\/some-other-namespace\" }, \"$\" : \"bob\" } }", 
                         strWriter.toString());
        
    }
    

    
    public void testPrefixedElements() throws Exception {
        StringWriter strWriter = new StringWriter();
        BadgerFishXMLStreamWriter w = new BadgerFishXMLStreamWriter(strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("alice");
        
        w.writeDefaultNamespace("http://some-namespace");
        w.writeNamespace("charlie", "http://some-other-namespace");
        
        w.writeStartElement("bob");
        w.writeCharacters("david");
        w.writeEndElement();
        

        w.writeStartElement("charlie", "edgar", "http://some-other-namespace");
        w.writeCharacters("frank");
        w.writeEndElement();
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        assertJSONEquals(
            "{ \"alice\" : { " +
              "\"@xmlns\" : { " +
                "\"$\" : \"http:\\/\\/some-namespace\"," +
                "\"charlie\" : \"http:\\/\\/some-other-namespace\"" +
              "}, " +
              "\"bob\" : { " +
                  "\"$\" : \"david\" " +
              "}, " +
              "\"charlie:edgar\" : { " +
                "\"$\" : \"frank\" " +
              "} " +
            "} }", strWriter.toString());
    }
    
    /**
     * 
     * <alice><bob>charlie</bob><david>edgar</david></alice>
     * 
     * <alice><bob>charlie</bob><bob>david</bob></alice>
     * { \"alice\": { \"bob\" : [{\"$\": charlie\" }, {\"$\": \"david\" }] } }
     * 
     * <alice charlie=\"david\">bob</alice>
     * { \"alice\": { \"$\" : \"bob\", \"@charlie\" : \"david\" } }
     * 
     * <alice xmlns=\"http://some-namespace\">bob</alice>
     * { \"alice\": { \"$\" : \"bob\", \"@xmlns\": { \"$\" : \"http:\/\/some-namespace\"} } }
     * 
     * <alice xmlns=\"http:\/\/some-namespace\" xmlns:charlie=\"http:\/\/some-other-namespace\">bob</alice>
     * { \"alice\": { \"$\" : \"bob\", \"@xmlns\": { \"$\" : \"http:\/\/some-namespace\", \"charlie\" : \"http:\/\/some-other-namespace\" } } }
     * 
     * <alice xmlns=\"http://some-namespace\" xmlns:charlie=\"http://some-other-namespace\"> <bob>david</bob> <charlie:edgar>frank</charlie:edgar> </alice>
     * { \"alice\" : { \"bob\" : { \"$\" : \"david\" , \"@xmlns\" : {\"charlie\" : \"http:\/\/some-other-namespace\" , \"$\" : \"http:\/\/some-namespace\"} } , \"charlie:edgar\" : { \"$\" : \"frank\" , \"@xmlns\" : {\"charlie\":\"http:\/\/some-other-namespace\", \"$\" : \"http:\/\/some-namespace\"} }, \"@xmlns\" : { \"charlie\" : \"http:\/\/some-other-namespace\", \"$\" : \"http:\/\/some-namespace\"} } }
     * 
     * @throws Exception
     */

    public void testNamespaceContextInnerDeclarationTakesPrecedence() throws Exception {
        AbstractXMLStreamWriter writer = new BadgerFishXMLStreamWriter(new StringWriter());
        NamespaceContext ctx = writer.getNamespaceContext();
        writer.writeStartDocument();
        writer.writeStartElement("a");
        writer.writeNamespace("p", "urn:1");
        writer.writeNamespace("q", "urn:2");
        assertEquals("urn:1", ctx.getNamespaceURI("p"));
        assertEquals("p", ctx.getPrefix("urn:1"));

        writer.writeStartElement("b");
        writer.writeNamespace("p", "urn:2");
        // the inner declaration of p takes precedence
        assertEquals("urn:2", ctx.getNamespaceURI("p"));
        assertEquals("urn:2", ctx.getNamespaceURI("q"));
        // p is no longer bound to urn:1 in this scope
        assertNull(ctx.getPrefix("urn:1"));
        assertFalse(ctx.getPrefixes("urn:1").hasNext());
        assertEquals("p", ctx.getPrefix("urn:2"));
        assertEquals(Arrays.asList("p", "q"), toList(ctx.getPrefixes("urn:2")));
        assertNull(ctx.getNamespaceURI("unbound"));
        assertNull(ctx.getPrefix("urn:unbound"));
        assertEquals(Collections.emptyList(), toList(ctx.getPrefixes("urn:unbound")));

        writer.writeEndElement();
        // back in the scope of a
        assertEquals("urn:1", ctx.getNamespaceURI("p"));
        assertEquals("p", ctx.getPrefix("urn:1"));
        assertEquals(Arrays.asList("q"), toList(ctx.getPrefixes("urn:2")));
        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private static List<Object> toList(Iterator<?> itr) {
        List<Object> list = new ArrayList<Object>();
        while (itr.hasNext()) {
            list.add(itr.next());
        }
        return list;
    }

    public void testWriteAttributeWithNamespaceURI() throws Exception {
        StringWriter strWriter = new StringWriter();
        AbstractXMLStreamWriter writer = new BadgerFishXMLStreamWriter(strWriter);
        writer.writeStartDocument();
        writer.writeStartElement("a");
        writer.writeNamespace("p", "urn:p");
        writer.writeAttribute("urn:p", "x", "1");
        // an attribute with no namespace
        writer.writeAttribute("", "y", "2");
        writer.writeStartElement("b");
        writer.writeNamespace("q", "urn:p");
        // the innermost prefix bound to the namespace is used
        writer.writeAttribute("urn:p", "z", "3");
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();

        String json = strWriter.toString();
        assertEquals("{\"a\":{\"@xmlns\":{\"p\":\"urn:p\"},\"@p:x\":\"1\",\"@y\":\"2\","
            + "\"b\":{\"@xmlns\":{\"q\":\"urn:p\"},\"@q:z\":\"3\"}}}", json);

        // the attribute is read back in its namespace
        XMLStreamReader reader = new BadgerFishXMLStreamReader(new JSONObject(json));
        reader.next();
        assertEquals("1", reader.getAttributeValue("urn:p", "x"));
    }

    public void testManyNamespacedAttributes() throws Exception {
        // Each lookup of a prefix used to scan every namespace declaration in
        // scope, so writing an element with many declarations and many
        // namespaced attributes was quadratic: 400 million steps here
        int count = 20000;
        StringWriter strWriter = new StringWriter();
        AbstractXMLStreamWriter writer = new BadgerFishXMLStreamWriter(strWriter);
        writer.writeStartDocument();
        writer.writeStartElement("a");
        for (int i = 0; i < count; i++) {
            writer.writeNamespace("p" + i, "urn:p");
        }
        writer.writeStartElement("b");
        writer.writeNamespace("q", "urn:q");
        for (int i = 0; i < count; i++) {
            writer.writeAttribute("urn:p", "x" + i, "v");
            assertEquals("q", writer.getPrefix("urn:q"));
        }
        assertEquals(count, toList(writer.getNamespaceContext().getPrefixes("urn:p")).size());
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();

        JSONObject b = new JSONObject(strWriter.toString()).getJSONObject("a").getJSONObject("b");
        // each attribute has one of the prefixes bound to its namespace
        assertEquals(count + 1, b.length());
    }

    public void testNamespaceContextOfSuppliedStack() throws Exception {
        // a node the writer didn't create, e.g. for an enclosing element
        FastStack nodes = new FastStack();
        Node enclosing = new Node(new JSONObject());
        enclosing.setNamespace("p", "urn:1");
        nodes.push(enclosing);
        AbstractXMLStreamWriter writer = new BadgerFishXMLStreamWriter(new StringWriter(), new JSONObject(), nodes);
        NamespaceContext ctx = writer.getNamespaceContext();
        writer.writeStartElement("a");
        writer.writeNamespace("q", "urn:1");
        assertEquals("urn:1", ctx.getNamespaceURI("p"));
        assertEquals("q", ctx.getPrefix("urn:1"));
        assertEquals(Arrays.asList("q", "p"), toList(ctx.getPrefixes("urn:1")));
        writer.writeStartElement("b");
        writer.writeNamespace("p", "urn:2");
        assertEquals(Arrays.asList("q"), toList(ctx.getPrefixes("urn:1")));
        assertEquals("urn:2", ctx.getNamespaceURI("p"));
    }

    public void testNamespaceContextOfDeeplyNestedElements() throws Exception {
        AbstractXMLStreamWriter writer = new BadgerFishXMLStreamWriter(new StringWriter());
        NamespaceContext ctx = writer.getNamespaceContext();
        writer.writeStartElement("a");
        writer.writeNamespace("p", "urn:1");
        int depth = 20000;
        for (int i = 0; i < depth; i++) {
            writer.writeStartElement("b");
        }
        writer.writeNamespace("q", "urn:1");
        assertEquals("urn:1", ctx.getNamespaceURI("p"));
        assertEquals(Arrays.asList("q", "p"), toList(ctx.getPrefixes("urn:1")));
        for (int i = 0; i < depth; i++) {
            writer.writeEndElement();
        }
        assertEquals(Arrays.asList("p"), toList(ctx.getPrefixes("urn:1")));
    }

    public void testWriteAttributeWithUnboundNamespaceURI() throws Exception {
        StringWriter strWriter = new StringWriter();
        AbstractXMLStreamWriter writer = new BadgerFishXMLStreamWriter(strWriter);
        writer.writeStartDocument();
        writer.writeStartElement("a");
        // with no prefix bound to the namespace URI, the attribute is written without a prefix
        writer.writeAttribute("urn:unbound", "x", "1");
        // the default namespace does not apply to attributes, so isn't used as a prefix either
        writer.writeDefaultNamespace("urn:default");
        writer.writeAttribute("urn:default", "y", "2");
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
        assertEquals("{\"a\":{\"@x\":\"1\",\"@xmlns\":{\"$\":\"urn:default\"},\"@y\":\"2\"}}",
            strWriter.toString());
    }

    public void testWriteToSuppliedJSONObject() throws Exception {
        JSONObject target = new JSONObject();
        StringWriter strWriter = new StringWriter();
        AbstractXMLStreamWriter writer = new BadgerFishXMLStreamWriter(strWriter, target);
        writer.writeStartDocument();
        writer.writeStartElement("a");
        writer.writeCharacters("x");
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();

        // the content is written to the supplied JSONObject, as well as to the Writer
        assertEquals("{\"a\":{\"$\":\"x\"}}", target.toString());
        assertEquals("{\"a\":{\"$\":\"x\"}}", strWriter.toString());
    }

    public void testWriteEmptyElementWithNamespace() throws Exception {
        StringWriter strWriter = new StringWriter();
        AbstractXMLStreamWriter writer = new BadgerFishXMLStreamWriter(strWriter);
        writer.writeStartDocument();
        writer.writeStartElement("root");
        writer.writeEmptyElement("", "a");
        writer.writeEmptyElement("urn:x", "b");
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
        // the same as writeStartElement(namespaceURI, localName) followed by writeEndElement()
        assertEquals("{\"root\":{\"a\":{},\"b\":{}}}", strWriter.toString());
    }

    public void testWriteNamespaceWithDefaultNamespacePrefix() throws Exception {
        // a null, empty or "xmlns" prefix declares the default namespace
        for (String prefix : new String[] {null, "", "xmlns"}) {
            StringWriter strWriter = new StringWriter();
            AbstractXMLStreamWriter writer = new BadgerFishXMLStreamWriter(strWriter);
            writer.writeStartDocument();
            writer.writeStartElement("r");
            writer.writeNamespace(prefix, "urn:d");
            assertEquals("urn:d", writer.getNamespaceContext().getNamespaceURI(""));
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.close();
            assertEquals("prefix " + prefix, "{\"r\":{\"@xmlns\":{\"$\":\"urn:d\"}}}", strWriter.toString());

            // the element is read back in the default namespace
            XMLStreamReader reader = new BadgerFishXMLStreamReader(new JSONObject(strWriter.toString()));
            reader.next();
            assertEquals("urn:d", reader.getNamespaceURI());
        }
    }
}
