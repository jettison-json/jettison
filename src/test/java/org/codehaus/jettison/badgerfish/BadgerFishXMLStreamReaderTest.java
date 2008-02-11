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

import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.codehaus.jettison.AbstractXMLStreamReader;
import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamReader;
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
        assertEquals("http://some-namespace", reader.getNamespaceURI(0));
        assertEquals("", reader.getNamespacePrefix(0));
        assertEquals("http://some-namespace", reader.getNamespaceURI(""));
       
        assertEquals("http://some-other-namespace", reader.getNamespaceURI(1));
        assertEquals("charlie", reader.getNamespacePrefix(1));
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
        
        assertEquals("http://some-namespace", reader.getNamespaceURI(0));
        assertEquals("", reader.getNamespacePrefix(0));
        assertEquals("http://some-namespace", reader.getNamespaceURI(""));
        
        assertEquals("http://some-other-namespace", reader.getNamespaceURI(1));
        assertEquals("charlie", reader.getNamespacePrefix(1));
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
        
        assertEquals("http://some-namespace", reader.getNamespaceURI(0));
        assertEquals("", reader.getNamespacePrefix(0));
        assertEquals("http://some-namespace", reader.getNamespaceURI(""));
        
        assertEquals("http://some-other-namespace", reader.getNamespaceURI(1));
        assertEquals("charlie", reader.getNamespacePrefix(1));
        assertEquals("http://some-other-namespace", reader.getNamespaceURI("charlie"));
       
        
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("frank", reader.getText());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("edgar", reader.getName().getLocalPart());
        
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("alice", reader.getName().getLocalPart());
    }
    
}
