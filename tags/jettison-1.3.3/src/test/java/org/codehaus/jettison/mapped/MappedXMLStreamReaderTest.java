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
package org.codehaus.jettison.mapped;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;

public class MappedXMLStreamReaderTest extends TestCase {
    public void testStreamReader() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"root\" : { " +
                           "\"child1\" : \"child1\"" +
                           "} }");
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("child1", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart()); 
    }

    public void testMultipleChildren() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"root\" : { " +
                           "\"child1\" : \"child1\"," +
                           "\"child2\" : \"child2\"" +
                           "} }");
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("child1", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("child2", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("child2", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("child2", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart()); 
    }
    
    public void testMultipleChildrenWithSameName() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"root\" : { " +
                           "\"child1\" : \"child1\"," +
                           "\"child1\" : \"child11\"" +
                           "} }");
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("child1", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("child11", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart()); 
    }
    
    public void testNestedArrayOfChildren() throws Exception {
        JSONObject obj = 
            new JSONObject("{" +
            		"\"root\":" +
            				"{\"child1\":" +
            					"[{\"subchild2\":" +
            						"[\"first sub2\",\"second sub2\",\"third sub2\"]}" +
            					",\"sub1\",\"sub2\"]" +
            				",\"child2\":\"child2\"}" +
            		"}");
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("subchild2", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("first sub2", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("subchild2", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("subchild2", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("second sub2", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("subchild2", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("subchild2", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("third sub2", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("subchild2", reader.getName().getLocalPart());
              
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
                  
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("sub1", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("sub2", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("child2", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("child2", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("child2", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
    }

    
    public void testNamespaces() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"foo.root\" : { " +
                           "\"foo.child1\" : \"childtext\"," +
                           "} }");
        
        Map xtoj = new HashMap();
        xtoj.put("http://foo/", "foo");
        MappedNamespaceConvention con = new MappedNamespaceConvention(new Configuration(xtoj));
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
        assertEquals("http://foo/", reader.getName().getNamespaceURI());
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals("http://foo/", reader.getName().getNamespaceURI());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("childtext", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals("http://foo/", reader.getName().getNamespaceURI());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart()); 
        assertEquals("http://foo/", reader.getName().getNamespaceURI());
    }
    
    public void testDefaultNamespace() throws Exception {
		JSONObject obj = new JSONObject("{ " + "\"root\" : { "
				+ "\"child1\" : \"childtext\"," + "} }");

		Map xtoj = new HashMap();
		xtoj.put("http://foo/", "");
		MappedNamespaceConvention con = new MappedNamespaceConvention(
				new Configuration(xtoj));
		XMLStreamReader reader = new MappedXMLStreamReader(obj, con);

		assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
		assertEquals("root", reader.getName().getLocalPart());
		assertEquals("http://foo/", reader.getName().getNamespaceURI());
		assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
		assertEquals("child1", reader.getName().getLocalPart());
		assertEquals("http://foo/", reader.getName().getNamespaceURI());
		assertEquals(XMLStreamReader.CHARACTERS, reader.next());
		assertEquals("childtext", reader.getText());
		assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
		assertEquals("child1", reader.getName().getLocalPart());
		assertEquals("http://foo/", reader.getName().getNamespaceURI());
		assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
		assertEquals("root", reader.getName().getLocalPart());
		assertEquals("http://foo/", reader.getName().getNamespaceURI());
	}    
    
    public void testArrayWithSameJSONObjects() throws Exception {
    	String str = "{\"theBook\":"
           + "{" 
           + "\"Names\":[{\"Name\":\"1\"}, {\"Name\":\"2\"}]"
           + " }                   "
           + "} ";
    	Configuration config = new Configuration();
    	config.setPrimitiveArrayKeys(Collections.singleton("Names"));
    	MappedXMLInputFactory factory = new MappedXMLInputFactory(config);
    	XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(str));
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("theBook", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("Names", reader.getName().getLocalPart());
                
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("Name", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("1", reader.getText());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("Name", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("Name", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("2", reader.getText());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("Name", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("Names", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("theBook", reader.getName().getLocalPart());
    	
    }
    
    public void testArrayWithSameJSONObjects2() throws Exception {
    	String str = "{\"theBook\":[{\"Name\":\"1\"}, {\"Name\":\"2\"}]}";
    	Configuration config = new Configuration();
    	config.setPrimitiveArrayKeys(Collections.singleton("theBook"));
    	MappedXMLInputFactory factory = new MappedXMLInputFactory(config);
    	XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(str));
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("theBook", reader.getName().getLocalPart());
                
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("Name", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("1", reader.getText());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("Name", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("Name", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("2", reader.getText());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("Name", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("theBook", reader.getName().getLocalPart());
    	
    }
    
    public void testArrayWithSameJSONObjects3() throws Exception {
    	String str = "{\"theBook\":"
           + "{" 
           + "\"Names\":["
           + "{\"Name\":[{\"value\":\"11\"}, {\"value\":\"12\"}]},"
           + "{\"Name\":[{\"value\":\"21\"}, {\"value\":\"22\"}]}"
           + "]}"
           + "} ";
    	Configuration config = new Configuration();
    	Set<String> set = new HashSet<String>();
    	set.add("Names");
    	set.add("Name");
    	config.setPrimitiveArrayKeys(set);
    	MappedXMLInputFactory factory = new MappedXMLInputFactory(config);
    	XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(str));
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("theBook", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("Names", reader.getName().getLocalPart());
                
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("Name", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("value", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("11", reader.getText());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("value", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("value", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("12", reader.getText());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("value", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("Name", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("Name", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("value", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("21", reader.getText());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("value", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("value", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("22", reader.getText());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("value", reader.getName().getLocalPart());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("Name", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("Names", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("theBook", reader.getName().getLocalPart());
    	
    }
    
    public void testExceptionLocation() throws Exception {
    	String str = "{\"junk";
    	Configuration config = new Configuration();
    	MappedXMLInputFactory factory = new MappedXMLInputFactory(config);
    	try {
    	    factory.createXMLStreamReader(new StringReader(str));
    	    fail("Exception expected");
    	} catch (XMLStreamException ex) {
    		Location loc = ex.getLocation();
    		assertNotNull(loc);
    		assertEquals(0, loc.getLineNumber());
    		assertEquals(6, loc.getColumnNumber());
    	}
    }
    
    public void testArrayWithNotSameJSONObjects() throws Exception {
    	String str = "{\"theBook\":[{\"Name\":\"1\"}, {\"Bar\":\"2\"}]}";
    	Configuration config = new Configuration();
    	config.setPrimitiveArrayKeys(Collections.singleton("theBook"));
    	MappedXMLInputFactory factory = new MappedXMLInputFactory(config);
    	XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(str));
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("theBook", reader.getName().getLocalPart());
                
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("theBook", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("Name", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("1", reader.getText());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("Name", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("theBook", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("theBook", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("Bar", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("2", reader.getText());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("Bar", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("theBook", reader.getName().getLocalPart());
    	
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("theBook", reader.getName().getLocalPart());
    }
    
    
    public void testMultipleArrays() throws Exception {
		JSONObject obj = new JSONObject("{ \"root\": " 
										+ " [ "
											+ " { "
											+ "     \"relationships\":[\"friend\"] , "
											+ "     \"emails\":[{\"value\":\"f@foo.com\"},{\"value\":\"b@bar.com\"}] "
											+ " } "
										+ " ] "
									+ " } ");

        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
                
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("relationships", reader.getName().getLocalPart());

        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("friend", reader.getText());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("relationships", reader.getName().getLocalPart());

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("emails", reader.getName().getLocalPart());

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("value", reader.getName().getLocalPart());

        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("f@foo.com", reader.getText());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("value", reader.getName().getLocalPart());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("emails", reader.getName().getLocalPart());

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("emails", reader.getName().getLocalPart());

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("value", reader.getName().getLocalPart());

        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("b@bar.com", reader.getText());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("value", reader.getName().getLocalPart());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("emails", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());

    }
    
    public void testSingleArrayWithOneElement() throws Exception {
		JSONObject obj = new JSONObject("{ \"root\": " 
										+ " [ "
											+ " { "
											+ "     \"relationship\":\"friend\" , "
											+ "     \"email\":\"f@foo.com\" "
											+ " } "
											+ " , "
											+ " { "
											+ "     \"relationship\":\"relative\" , "
											+ "     \"email\":\"b@foo.com\" "
											+ " } "
										+ " ] "
									+ " } ");

        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("relationship", reader.getName().getLocalPart());

        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("friend", reader.getText());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("relationship", reader.getName().getLocalPart());

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("email", reader.getName().getLocalPart());

        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("f@foo.com", reader.getText());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("email", reader.getName().getLocalPart());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("relationship", reader.getName().getLocalPart());

        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("relative", reader.getText());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("relationship", reader.getName().getLocalPart());

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("email", reader.getName().getLocalPart());

        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("b@foo.com", reader.getText());

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("email", reader.getName().getLocalPart());
                
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());

    }
    
    public void testStreamReaderWithNullValue() throws Exception {
        JSONObject obj = new JSONObject("{ " +
                               "\"root\" : { " +
                               "\"child1\" : null" +
                               "} }");
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
    
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals(null, reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart()); 
    }
    
    public void testGetElementTextNull() throws Exception {
        JSONObject obj = new JSONObject("{ " +
    	                          "\"root\" : null }");
    	Configuration conf = new Configuration();
    	MappedNamespaceConvention con = new MappedNamespaceConvention(conf);
    	XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
    	assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
    	assertEquals("root", reader.getName().getLocalPart());
    	assertEquals(null, reader.getElementText());
    	assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
    }
        
    public void testAttributes() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"root\" : { " +
                           "\"@att\" : \"attvalue\"," +
                           "\"child1\" : \"child1\"" +
                           "} }");
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
        
        assertEquals(1, reader.getAttributeCount());
        assertEquals("att", reader.getAttributeLocalName(0));
        assertEquals("", reader.getAttributeNamespace(0));
        assertEquals("attvalue", reader.getAttributeValue(0));
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("child1", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart()); 
    }
    
    public void testAttributesAsElements() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"root\" : { " +
                           "\"@att\" : \"attvalue\"," +
                           "\"att2\" : \"attvalue\"," +
                           "\"child1\" : \"child1\"" +
                           "} }");
        List atts = new ArrayList();
        atts.add(new QName("att2"));
        Configuration c = new Configuration();
        c.setAttributesAsElements(atts);
        
        MappedNamespaceConvention con = new MappedNamespaceConvention(c);
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
        
        assertEquals(1, reader.getAttributeCount());
        assertEquals("att", reader.getAttributeLocalName(0));
        assertEquals("", reader.getAttributeNamespace(0));
        assertEquals("attvalue", reader.getAttributeValue(0));
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("att2", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("attvalue", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("child1", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart()); 
    }
    
    public void testElementNameWithDot() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"org.codehaus.jettison.mapped.root\" : { " +
                           "\"org.codehaus.jettison.mapped.child1\" : \"org.codehaus.jettison.mapped.child1\"" +
                           "} }");
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());       
        assertEquals("org.codehaus.jettison.mapped.root", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("org.codehaus.jettison.mapped.child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("org.codehaus.jettison.mapped.child1", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("org.codehaus.jettison.mapped.child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("org.codehaus.jettison.mapped.root", reader.getName().getLocalPart()); 
    }
    
    
    public void testNonStringObjects() throws Exception {
    	JSONObject obj = new JSONObject("{\"root\":{\"foo\":true, \"foo2\":3.14, \"foo3\":17}}");
        
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());       
        assertEquals("root", reader.getName().getLocalPart());        
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());       
        assertEquals("foo", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("true", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());       
        assertEquals("foo2", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("3.14", reader.getText()); 
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());        
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());       
        assertEquals("foo3", reader.getName().getLocalPart());
        
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("17", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
    }
    
    public void testNonStringAttributes() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"root\" : { " +
                           "\"@att\" : 1," +
                           "\"child1\" : \"child1\"" +
                           "} }");
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
        
        assertEquals(1, reader.getAttributeCount());
        assertEquals("att", reader.getAttributeLocalName(0));
        assertEquals("", reader.getAttributeNamespace(0));
        assertEquals("1", reader.getAttributeValue(0));
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("child1", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart()); 
    }
    
    // issue 29
    public void testComplexElements() throws Exception {
    	JSONObject obj = new JSONObject("{\"a\":{\"o\":{\"@class\":\"string\",\"$\":\"1\"}}}");
        
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());       
        assertEquals("a", reader.getName().getLocalPart());        
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());  
        assertEquals("o", reader.getName().getLocalPart());
        
        assertEquals(1, reader.getAttributeCount());
        assertEquals("class", reader.getAttributeLocalName(0));
        assertEquals("", reader.getAttributeNamespace(0));
        assertEquals("string", reader.getAttributeValue(0));
        
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("$", reader.getName().getLocalPart());
        assertEquals("1", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals(XMLStreamReader.END_DOCUMENT, reader.next());
        
    }
    
    // issue 29
    public void testIgnoreComplexElements() throws Exception {
    	JSONObject obj = new JSONObject("{\"a\":{\"o\":{\"@class\":\"string\",\"$\":\"1\"}}}");
        
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        MappedXMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        reader.setValueKey(null);

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());       
        assertEquals("a", reader.getName().getLocalPart());        
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());  
        assertEquals("o", reader.getName().getLocalPart());
        
        assertEquals(1, reader.getAttributeCount());
        assertEquals("class", reader.getAttributeLocalName(0));
        assertEquals("", reader.getAttributeNamespace(0));
        assertEquals("string", reader.getAttributeValue(0));
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("$", reader.getName().getLocalPart());
        assertEquals("1", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals(XMLStreamReader.END_DOCUMENT, reader.next());
        
    }    
    
    // issue 16
    public void testSimple() throws Exception {
    	JSONObject obj = new JSONObject("{\"root\":\"json string\"}");
        
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        MappedXMLStreamReader reader = new MappedXMLStreamReader(obj, con);

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());       
        assertEquals("root", reader.getName().getLocalPart());     
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("json string", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        
        
    }   
    
    //issue 52
    public void testAttributeKey() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"root\" : { " +
                           "\"!att\" : \"attvalue\"," +
                           "\"child1\" : \"child1\"" +
                           "} }");
        Configuration conf = new Configuration();
        conf.setAttributeKey("!");
        MappedNamespaceConvention con = new MappedNamespaceConvention(conf);
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
        
        assertEquals(1, reader.getAttributeCount());
        assertEquals("att", reader.getAttributeLocalName(0));
        assertEquals("", reader.getAttributeNamespace(0));
        assertEquals("attvalue", reader.getAttributeValue(0));
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("child1", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart()); 
    }    
    
    //issue 50
    public void testGetElementText() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"root\" : \"test\" }");
        
        Configuration conf = new Configuration();
        MappedNamespaceConvention con = new MappedNamespaceConvention(conf);
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
        
        assertEquals("test", reader.getElementText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
    }
    // Issue 59
    public void testListOfObject() throws Exception {
        String json = "{\"folders\":{" +
                        "\"folder\" : [" +
                            "{" +
                                "\"name\":\"folder1\"," +
                                "\"subfolder\":" +
                                "[" +
                                    "{\"name\":\"subfolder1\"}," +
                                    "{\"name\":\"subfolder2\"}" +
                                "]," +
                                "\"file\":" +
                                "[" +
                                    "{\"name\":\"file1\"}," +
                                    "{\"name\":\"file2\"}" +
                                "]" +                               
                            "}," +
                        "]" +
                       "}}";
        // Expected XML
        String expXml = "<folders>" +
                            "<folder>" +
                                "<name>folder1</name>" +
                                "<subfolder>" +
                                    "<name>subfolder1</name>" +
                                "</subfolder>" +
                                "<subfolder>" +
                                    "<name>subfolder2</name>" +
                                "</subfolder>" +
                                "<file>" +
                                    "<name>file1</name>" +
                                "</file>" +
                                "<file>" +
                                    "<name>file2</name>" +
                                "</file>" +
                            "</folder>" +
                        "</folders>";

        doTestEvents(json, expXml);
    }
    
    private void doTestEvents(String json, String expXml) throws JSONException, XMLStreamException,
            FactoryConfigurationError {
        JSONObject obj = new JSONObject(new JSONTokener(json));
        Configuration config = new Configuration();

        
        ArrayList refEvents = new ArrayList();

        // reference reader
        XMLStreamReader refReader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(expXml));
        while (refReader.hasNext()) {
            refEvents.add(Integer.valueOf(refReader.next()));            
        }
        refReader.close();

        // tested reader
        MappedXMLStreamReader mpdReader = new MappedXMLStreamReader(obj, new MappedNamespaceConvention(config));

        ArrayList mpdEvents = new ArrayList();        
        StringBuilder sb = new StringBuilder();
        while (mpdReader.hasNext()) {
            int eventType = mpdReader.next();
        
            mpdEvents.add(Integer.valueOf(eventType));
            
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                sb.append("<").append(mpdReader.getName()).append(">");
            } else if (eventType == XMLStreamConstants.CHARACTERS) {
                sb.append(mpdReader.getTextCharacters());
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                sb.append("</").append(mpdReader.getName()).append(">");                
            }
        }
        mpdReader.close();

        String mpdXml = sb.toString();
        
        assertEquals(expXml, mpdXml);
        assertEquals(refEvents, mpdEvents);
    }
    
}
