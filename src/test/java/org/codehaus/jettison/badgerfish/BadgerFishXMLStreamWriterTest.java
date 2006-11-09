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

import junit.framework.TestCase;

import org.codehaus.jettison.AbstractXMLStreamWriter;
import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamWriter;

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
}
