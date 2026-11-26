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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
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
    
    public void testStreamReaderNullTextAsString() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"root\" : { " +
                           "\"child1\" : \"null\"" +
                           "} }");
        Configuration c = new Configuration();
        c.setReadNullAsString(true);
        MappedNamespaceConvention con = new MappedNamespaceConvention(c);
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("null", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart()); 
    }
    
    public void testStreamReaderNullTextAsNull() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"root\" : { " +
                           "\"child1\" : \"null\"" +
                           "} }");
        Configuration c = new Configuration();
        MappedNamespaceConvention con = new MappedNamespaceConvention(c);
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("child1", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals(null, reader.getText());
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

        assertEquals(XMLStreamReader.END_DOCUMENT, reader.next());
        assertFalse(reader.hasNext());
    }

    public void testArrayWithSameJSONObjectsSingleElement() throws Exception {
        String str = "{\"theBook\":[{\"Name\":\"1\"}]}";
        Configuration config = new Configuration();
        config.setPrimitiveArrayKeys(Collections.singleton("theBook"));
        XMLStreamReader reader = new MappedXMLInputFactory(config).createXMLStreamReader(new StringReader(str));

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("theBook", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("Name", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("1", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("theBook", reader.getName().getLocalPart());
        assertEquals(XMLStreamReader.END_DOCUMENT, reader.next());
        assertFalse(reader.hasNext());
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

    public void testEmptyObjectIsRejected() throws Exception {
        try {
            new MappedXMLStreamReader(new JSONObject("{}"));
            fail("Failure expected on a JSON object with no root element");
        } catch (XMLStreamException ex) {
            assertEquals("The JSON object has no root element", ex.getMessage());
        }
    }

    public void testEmptyObjectIsRejectedByFactory() throws Exception {
        try {
            new MappedXMLInputFactory(new Configuration()).createXMLStreamReader(new StringReader(" { } "));
            fail("Failure expected on a JSON object with no root element");
        } catch (XMLStreamException ex) {
            assertEquals("The JSON object has no root element", ex.getMessage());
        }
    }

    public void testSupressAtAttributesReading() throws Exception {
        Configuration config = new Configuration();
        config.setSupressAtAttributes(true);
        MappedXMLInputFactory factory = new MappedXMLInputFactory(config);

        assertEquals("<root><@id>1</@id><name>x</name><child><@lang>en</@lang>text</child></root>",
            readEvents(factory, "{\"root\":{\"@id\":\"1\",\"name\":\"x\",\"child\":{\"@lang\":\"en\",\"$\":\"text\"}}}"));
        assertEquals("<root><a>1</a><a>2</a><b><@c>3</@c><d>4</d></b></root>",
            readEvents(factory, "{\"root\":{\"a\":[1,2],\"b\":{\"@c\":\"3\",\"d\":\"4\"}}}"));
    }

    public void testSupressAtAttributesDoesNotRetainKeys() throws Exception {
        Configuration config = new Configuration();
        config.setSupressAtAttributes(true);
        MappedXMLInputFactory factory = new MappedXMLInputFactory(config);

        // Keys read from the input used to be added to a list on the shared
        // convention, which grew without bound and made reading quadratic in
        // the number of distinct keys. The list isn't exposed, so check it
        // via reflection.
        MappedNamespaceConvention convention = ((MappedXMLStreamReader) factory.createXMLStreamReader(
            new StringReader("{\"root\":{\"a\":1}}"))).getConvention();
        java.lang.reflect.Field field =
            MappedNamespaceConvention.class.getDeclaredField("jsonAttributesAsElements");
        field.setAccessible(true);
        int sizeBefore = ((List<?>) field.get(convention)).size();

        StringBuilder json = new StringBuilder("{\"root\":{");
        for (int i = 0; i < 1000; i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append("\"k").append(i).append("\":1");
        }
        json.append("}}");
        readEvents(factory, json.toString());

        assertEquals(sizeBefore, ((List<?>) field.get(convention)).size());
    }

    private static String readEvents(MappedXMLInputFactory factory, String json) throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(json));
        StringBuilder sb = new StringBuilder();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                sb.append('<').append(reader.getLocalName());
                for (int i = 0; i < reader.getAttributeCount(); i++) {
                    sb.append(' ').append(reader.getAttributeLocalName(i))
                        .append("=\"").append(reader.getAttributeValue(i)).append('"');
                }
                sb.append('>');
            } else if (event == XMLStreamConstants.CHARACTERS) {
                sb.append(reader.getText());
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                sb.append("</").append(reader.getLocalName()).append('>');
            }
        }
        return sb.toString();
    }

    // https://github.com/jettison-json/jettison/issues/9
    public void testGetElementTextLeavesReaderOnEndElement() throws Exception {
        JSONObject obj = new JSONObject("{\"root\":{\"a\":\"text\",\"b\":{\"@attr\":\"x\",\"$\":\"more\"},"
            + "\"c\":[\"one\",\"two\"],\"d\":{},\"e\":\"\",\"f\":null}}");
        XMLStreamReader reader = new MappedXMLStreamReader(obj, new MappedNamespaceConvention(standard()));
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getLocalName());

        assertElementText(reader, "a", "text");
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("x", reader.getAttributeValue(null, "attr"));
        assertEquals("b", reader.getLocalName());
        assertEquals("more", reader.getElementText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.getEventType());
        assertEquals("b", reader.getLocalName());
        // repeated elements from a JSON array
        assertElementText(reader, "c", "one");
        assertElementText(reader, "c", "two");
        // an empty element and an empty string
        assertElementText(reader, "d", null);
        assertElementText(reader, "e", "");
        // a JSON null
        assertElementText(reader, "f", null);

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("root", reader.getLocalName());
        assertEquals(XMLStreamReader.END_DOCUMENT, reader.next());
    }

    private static void assertElementText(XMLStreamReader reader, String name, String text) throws Exception {
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals(name, reader.getLocalName());
        assertEquals(text, reader.getElementText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.getEventType());
        assertEquals(name, reader.getLocalName());
    }

    public void testGetElementTextNullString() throws Exception {
        // the string "null" is read as null, unless readNullAsString is configured
        XMLStreamReader reader = new MappedXMLStreamReader(new JSONObject("{\"root\":\"null\"}"),
            new MappedNamespaceConvention(standard()));
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertNull(reader.getElementText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.getEventType());

        Configuration conf = standard();
        conf.setReadNullAsString(true);
        reader = new MappedXMLStreamReader(new JSONObject("{\"root\":\"null\"}"),
            new MappedNamespaceConvention(conf));
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("null", reader.getElementText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.getEventType());
    }

    public void testGetElementTextOnCharacters() throws Exception {
        XMLStreamReader reader = new MappedXMLStreamReader(new JSONObject("{\"root\":\"test\"}"),
            new MappedNamespaceConvention(standard()));
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("test", reader.getElementText());
        assertEquals(XMLStreamReader.CHARACTERS, reader.getEventType());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
    }

    public void testGetElementTextWithChildElement() throws Exception {
        XMLStreamReader reader = new MappedXMLStreamReader(new JSONObject("{\"root\":{\"child\":\"test\"}}"),
            new MappedNamespaceConvention(standard()));
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        try {
            reader.getElementText();
            fail("Failure expected on an element with a child element");
        } catch (XMLStreamException ex) {
            // expected
        }
    }

    public void testDefaultGetElementText() throws Exception {
        // by default the reader is left on CHARACTERS, so next() returns the END_ELEMENT
        XMLStreamReader[] readers = {
            new MappedXMLStreamReader(new JSONObject("{\"root\":\"test\"}"),
                new MappedNamespaceConvention(new Configuration())),
            new MappedXMLStreamReader(new JSONObject("{\"root\":\"test\"}"))
        };
        for (XMLStreamReader reader : readers) {
            assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
            assertEquals("test", reader.getElementText());
            assertEquals(XMLStreamReader.CHARACTERS, reader.getEventType());
            assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
            assertEquals(XMLStreamReader.END_DOCUMENT, reader.next());
        }
    }

    private static Configuration standard() {
        Configuration conf = new Configuration();
        conf.setStandardGetElementText(true);
        return conf;
    }

    public void testHasNameAndRequire() throws Exception {
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("urn:1", "p1");
        XMLStreamReader reader = new MappedXMLStreamReader(new JSONObject("{\"p1.root\":{\"child\":\"text\"}}"),
            new MappedNamespaceConvention(new Configuration(namespaces)));

        assertFalse(reader.hasName());
        reader.require(XMLStreamConstants.START_DOCUMENT, null, null);

        assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
        assertTrue(reader.hasName());
        reader.require(XMLStreamConstants.START_ELEMENT, "urn:1", "root");
        reader.require(XMLStreamConstants.START_ELEMENT, null, "root");
        reader.require(XMLStreamConstants.START_ELEMENT, "urn:1", null);
        assertRequireFails(reader, XMLStreamConstants.END_ELEMENT, null, null);
        assertRequireFails(reader, XMLStreamConstants.START_ELEMENT, "urn:2", "root");
        assertRequireFails(reader, XMLStreamConstants.START_ELEMENT, "", "root");
        assertRequireFails(reader, XMLStreamConstants.START_ELEMENT, "urn:1", "other");

        assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
        reader.require(XMLStreamConstants.START_ELEMENT, "", "child");

        assertEquals(XMLStreamConstants.CHARACTERS, reader.next());
        assertFalse(reader.hasName());
        reader.require(XMLStreamConstants.CHARACTERS, null, null);
        // a name can't be checked on an event that has no name
        assertRequireFails(reader, XMLStreamConstants.CHARACTERS, null, "child");

        assertEquals(XMLStreamConstants.END_ELEMENT, reader.next());
        assertTrue(reader.hasName());
        reader.require(XMLStreamConstants.END_ELEMENT, "", "child");

        assertEquals(XMLStreamConstants.END_ELEMENT, reader.next());
        reader.require(XMLStreamConstants.END_ELEMENT, "urn:1", "root");

        assertEquals(XMLStreamConstants.END_DOCUMENT, reader.next());
        assertFalse(reader.hasName());
        reader.require(XMLStreamConstants.END_DOCUMENT, null, null);
    }

    private static void assertRequireFails(XMLStreamReader reader, int type, String namespaceURI,
                                           String localName) {
        try {
            reader.require(type, namespaceURI, localName);
            fail("Failure expected on require(" + type + ", " + namespaceURI + ", " + localName + ")");
        } catch (XMLStreamException ex) {
            // expected
        }
    }

    public void testGetTextCharacters() throws Exception {
        XMLStreamReader reader = new MappedXMLStreamReader(new JSONObject("{\"root\":\"hello\"}"));
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
        assertEquals(XMLStreamConstants.CHARACTERS, reader.next());

        // a target buffer larger than the text
        char[] target = new char[16];
        assertEquals(5, reader.getTextCharacters(0, target, 0, 16));
        assertEquals("hello", new String(target, 0, 5));

        // copying into the middle of the target, starting part way through the text
        target = new char[] {'x', 'x', 'x', 'x', 'x', 'x'};
        assertEquals(3, reader.getTextCharacters(2, target, 1, 5));
        assertEquals("xllox", new String(target, 0, 5));

        // no more text
        assertEquals(0, reader.getTextCharacters(5, target, 0, 3));
        assertEquals(0, reader.getTextCharacters(10, target, 0, 3));

        // reading the text in fixed size chunks, as in the XMLStreamReader Javadoc
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[2];
        for (int sourceStart = 0;; sourceStart += buffer.length) {
            int copied = reader.getTextCharacters(sourceStart, buffer, 0, buffer.length);
            sb.append(buffer, 0, copied);
            if (copied < buffer.length) {
                break;
            }
        }
        assertEquals("hello", sb.toString());

        // invalid arguments
        assertGetTextCharactersFails(reader, -1, new char[5], 0, 5);
        assertGetTextCharactersFails(reader, 0, new char[5], -1, 5);
        assertGetTextCharactersFails(reader, 0, new char[5], 6, 0);
        assertGetTextCharactersFails(reader, 0, new char[5], 0, -1);
        assertGetTextCharactersFails(reader, 0, new char[5], 2, 4);
    }

    private static void assertGetTextCharactersFails(XMLStreamReader reader, int sourceStart, char[] target,
                                                     int targetStart, int length) throws XMLStreamException {
        try {
            reader.getTextCharacters(sourceStart, target, targetStart, length);
            fail("Failure expected on invalid arguments");
        } catch (IndexOutOfBoundsException ex) {
            // expected
        }
    }

    public void testNamespaceContextGetPrefixes() throws Exception {
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("urn:1", "p1");
        namespaces.put("urn:2", "p2");
        XMLStreamReader reader = new MappedXMLStreamReader(new JSONObject("{\"p1.root\":\"x\"}"),
            new MappedNamespaceConvention(new Configuration(namespaces)));
        NamespaceContext ctx = reader.getNamespaceContext();

        Iterator<?> prefixes = ctx.getPrefixes("urn:1");
        assertEquals("p1", prefixes.next());
        assertFalse(prefixes.hasNext());
        prefixes = ctx.getPrefixes("urn:2");
        assertEquals("p2", prefixes.next());
        assertFalse(prefixes.hasNext());
        assertFalse(ctx.getPrefixes("urn:unknown").hasNext());
        assertEquals("p1", ctx.getPrefix("urn:1"));
    }

    public void testRootEmptyArray() throws Exception {
        // MappedXMLStreamWriter writes an empty element that is serialized as an array as [""]
        XMLStreamReader reader = new MappedXMLStreamReader(new JSONObject("{\"root\":[\"\"]}"));
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
        assertEquals("root", reader.getLocalName());
        assertEquals(XMLStreamConstants.CHARACTERS, reader.next());
        assertEquals("", reader.getText());
        assertEquals(XMLStreamConstants.END_ELEMENT, reader.next());
        assertEquals(XMLStreamConstants.END_DOCUMENT, reader.next());

        // the same as a nested [""] value
        reader = new MappedXMLStreamReader(new JSONObject("{\"root\":{\"child\":[\"\"]}}"));
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
        assertEquals("child", reader.getLocalName());
        assertEquals(XMLStreamConstants.CHARACTERS, reader.next());
        assertEquals("", reader.getText());
    }

    public void testMultiCharacterAttributeKey() throws Exception {
        Configuration conf = new Configuration();
        conf.setAttributeKey("_attr_");
        XMLStreamReader reader = new MappedXMLStreamReader(
            new JSONObject("{\"root\":{\"_attr_id\":7,\"child\":\"x\"}}"), new MappedNamespaceConvention(conf));
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
        assertEquals(1, reader.getAttributeCount());
        assertEquals("id", reader.getAttributeLocalName(0));
        assertEquals("7", reader.getAttributeValue(0));
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
        assertEquals("child", reader.getLocalName());
    }

    public void testPrimitiveArrayKeysWithMixedItems() throws Exception {
        Configuration config = new Configuration();
        config.setPrimitiveArrayKeys(Collections.singleton("Names"));
        String[] inputs = {
            // the item that isn't a single key object last, first and in the middle
            "{\"root\":{\"Names\":[{\"Name\":\"1\"},{\"Name\":\"2\"},\"s\"]}}",
            "{\"root\":{\"Names\":[\"s\",{\"Name\":\"1\"},{\"Name\":\"2\"}]}}",
            "{\"root\":{\"Names\":[{\"Name\":\"1\"},5,{\"Name\":\"2\"}]}}",
            "{\"root\":{\"Names\":[{\"Name\":\"1\"},{\"a\":1,\"b\":2}]}}"
        };
        for (String input : inputs) {
            // no item is lost: the array is read in the same way as without primitiveArrayKeys
            assertEquals(input, readEvents(input, new Configuration()), readEvents(input, config));
        }
        assertEquals("<root><Names><Name>1</Name></Names><Names><Name>2</Name></Names><Names>s</Names></root>",
            readEvents(inputs[0], config));
    }

    private static String readEvents(String json, Configuration config) throws Exception {
        XMLStreamReader reader = new MappedXMLStreamReader(new JSONObject(json), new MappedNamespaceConvention(config));
        StringBuilder sb = new StringBuilder();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                sb.append('<').append(reader.getLocalName()).append('>');
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                sb.append("</").append(reader.getLocalName()).append('>');
            } else if (event == XMLStreamConstants.CHARACTERS) {
                sb.append(reader.getText());
            }
        }
        return sb.toString();
    }

    public void testInputIsNotModified() throws Exception {
        String json = "{\"a\":{\"@x\":\"1\",\"c\":{\"@y\":\"2\",\"$\":\"v\"},\"d\":\"w\"}}";
        JSONObject obj = new JSONObject(json);
        String first = readEvents(json, new Configuration());
        assertEquals("<a><c>v</c><d>w</d></a>", first);

        XMLStreamReader reader = new MappedXMLStreamReader(obj);
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
        assertEquals("1", reader.getAttributeValue(null, "x"));
        while (reader.hasNext()) {
            reader.next();
        }
        // the attributes are still in the caller's JSONObject
        assertEquals(new JSONObject(json).toString(), obj.toString());
        // so reading it again gives the same attributes
        reader = new MappedXMLStreamReader(obj);
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
        assertEquals(1, reader.getAttributeCount());
        assertEquals("1", reader.getAttributeValue(null, "x"));
    }

    public void testNextTag() throws Exception {
        XMLStreamReader reader = new MappedXMLStreamReader(new JSONObject("{\"root\":{\"a\":\"x\",\"b\":\"y\"}}"));
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.nextTag());
        assertEquals("root", reader.getLocalName());
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.nextTag());
        assertEquals("a", reader.getLocalName());
        // the text is skipped
        assertEquals(XMLStreamConstants.END_ELEMENT, reader.nextTag());
        assertEquals("a", reader.getLocalName());
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.nextTag());
        assertEquals("b", reader.getLocalName());
        assertEquals(XMLStreamConstants.END_ELEMENT, reader.nextTag());
        assertEquals(XMLStreamConstants.END_ELEMENT, reader.nextTag());
        assertEquals("root", reader.getLocalName());

        // there are no more tags: fail rather than loop forever
        assertNextTagFails(reader);
        assertEquals(XMLStreamConstants.END_DOCUMENT, reader.getEventType());
        assertNextTagFails(reader);
    }

    private static void assertNextTagFails(XMLStreamReader reader) {
        try {
            reader.nextTag();
            fail("Failure expected at the end of the document");
        } catch (XMLStreamException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("end of the document"));
        }
    }

    public void testPrimitiveArrayKeysWithNestedItems() throws Exception {
        Configuration config = new Configuration();
        config.setPrimitiveArrayKeys(new HashSet<String>(Arrays.asList("a", "n")));
        // items with elements nested more than one level deep
        assertEquals("<r><a><c><d>1</d></c><c><d>2</d></c></a><b>z</b></r>",
            readEvents("{\"r\":{\"a\":[{\"c\":{\"d\":1}},{\"c\":{\"d\":2}}],\"b\":\"z\"}}", config));
        assertEquals("<r><a><c><d>1</d></c><c><d>2</d></c><c><d>3</d></c></a><b>z</b></r>",
            readEvents("{\"r\":{\"a\":[{\"c\":{\"d\":1}},{\"c\":{\"d\":2}},{\"c\":{\"d\":3}}],\"b\":\"z\"}}",
                config));
        assertEquals("<r><a><c><d><e>1</e></d></c><c><d><e>2</e></d></c></a></r>",
            readEvents("{\"r\":{\"a\":[{\"c\":{\"d\":{\"e\":1}}},{\"c\":{\"d\":{\"e\":2}}}]}}", config));
        // an item containing a standard array
        assertEquals("<r><a><c><e>1</e><e>2</e><f>x</f></c><c><e>3</e><f>y</f></c></a><b>z</b></r>",
            readEvents("{\"r\":{\"a\":[{\"c\":{\"e\":[1,2],\"f\":\"x\"}},{\"c\":{\"e\":[3],\"f\":\"y\"}}],"
                + "\"b\":\"z\"}}", config));
        // an item containing another array listed in the primitive array keys
        assertEquals("<r><a><c><n><m>1</m><m>2</m></n></c><c><n><m>3</m></n></c></a><b>z</b></r>",
            readEvents("{\"r\":{\"a\":[{\"c\":{\"n\":[{\"m\":1},{\"m\":2}]}},{\"c\":{\"n\":[{\"m\":3}]}}],"
                + "\"b\":\"z\"}}", config));
    }

    public void testPrimitiveArrayKeyWithTheRootName() throws Exception {
        Configuration config = new Configuration();
        config.setPrimitiveArrayKeys(Collections.singleton("r"));
        // a nested array whose key is the same as the root element's name isn't the root array
        assertEquals("<r><x><r><c>1</c><c>2</c></r></x><y>1</y></r>",
            readEvents("{\"r\":{\"x\":{\"r\":[{\"c\":1},{\"c\":2}]},\"y\":1}}", config));
        assertEquals("<r><r><c>1</c><c>2</c></r><y>1</y></r>",
            readEvents("{\"r\":{\"r\":[{\"c\":1},{\"c\":2}],\"y\":1}}", config));
        // the root array itself
        assertEquals("<r><c>1</c><c>2</c></r>", readEvents("{\"r\":[{\"c\":1},{\"c\":2}]}", config));
    }

    public void testValueKeyArrayIsReadAsJoinedText() throws Exception {
        Configuration config = new Configuration();
        assertEquals("<root>t1t2</root>", readEvents("{\"root\":{\"$\":[\"t1\",\"t2\"]}}", config));
        assertEquals("<root>t1</root>", readEvents("{\"root\":{\"$\":[\"t1\"]}}", config));
        assertEquals("<root>t1t2</root>", readEvents("{\"root\":{\"@a\":\"v\",\"$\":[\"t1\",\"t2\"]}}", config));
        assertEquals("<root><c>t1t2</c><d>y</d></root>",
            readEvents("{\"root\":{\"c\":{\"$\":[\"t1\",\"t2\"]},\"d\":\"y\"}}", config));
        assertEquals("<root><b>xy</b><b>z</b></root>",
            readEvents("{\"root\":{\"b\":[{\"$\":[\"x\",\"y\"]},{\"$\":[\"z\"]}]}}", config));
        // numbers and booleans are text too, and null parts are skipped
        assertEquals("<root>1truex</root>", readEvents("{\"root\":{\"$\":[1,true,\"x\"]}}", config));
        assertEquals("<root>t1t2</root>", readEvents("{\"root\":{\"$\":[\"t1\",null,\"t2\"]}}", config));
        // no text
        assertEquals("<root></root>", readEvents("{\"root\":{\"$\":[]}}", config));
        assertEquals("<root></root>", readEvents("{\"root\":{\"$\":[null]}}", config));
    }

    public void testValueKeyArrayReadsLikeAString() throws Exception {
        String[][] pairs = {
            {"{\"root\":{\"$\":[\"t1\",\"t2\"]}}", "{\"root\":{\"$\":\"t1t2\"}}"},
            {"{\"root\":{\"b\":\"x\",\"$\":[\"t1\",\"t2\"]}}", "{\"root\":{\"b\":\"x\",\"$\":\"t1t2\"}}"},
            {"{\"root\":{\"@a\":\"v\",\"$\":[\"t\",1]}}", "{\"root\":{\"@a\":\"v\",\"$\":\"t1\"}}"},
        };
        for (String[] pair : pairs) {
            assertEquals(pair[1], events(pair[1]), events(pair[0]));
        }

        Configuration primitive = new Configuration();
        primitive.setPrimitiveArrayKeys(new HashSet<String>(Arrays.asList("$")));
        assertEquals("<root>t1t2</root>", readEvents("{\"root\":{\"$\":[\"t1\",\"t2\"]}}", primitive));

        XMLStreamReader reader = new MappedXMLStreamReader(new JSONObject("{\"root\":{\"$\":[\"t1\",\"t2\"]}}"),
            new MappedNamespaceConvention(standard()));
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
        assertEquals("t1t2", reader.getElementText());
        assertEquals(XMLStreamConstants.END_ELEMENT, reader.getEventType());
    }

    public void testValueKeyArrayWithObjectOrArrayIsRejected() throws Exception {
        for (String json : new String[] {"{\"root\":{\"$\":[{\"a\":\"1\"}]}}", "{\"root\":{\"$\":[[\"x\"]]}}",
                                         "{\"root\":{\"$\":[\"t\",{\"a\":\"1\"}]}}"}) {
            try {
                readEvents(json, new Configuration());
                fail("Failure expected for " + json);
            } catch (XMLStreamException ex) {
                assertEquals("The structure of the JSON input is not supported", ex.getMessage());
            }
        }
    }

    /**
     * The full event sequence, including the names of end elements.
     */
    private static String events(String json) throws Exception {
        XMLStreamReader reader = new MappedXMLStreamReader(new JSONObject(json));
        StringBuilder sb = new StringBuilder();
        while (reader.hasNext()) {
            int event = reader.next();
            sb.append(event);
            if (event == XMLStreamConstants.START_ELEMENT || event == XMLStreamConstants.END_ELEMENT) {
                sb.append(reader.getLocalName());
            } else if (event == XMLStreamConstants.CHARACTERS) {
                sb.append(reader.getText());
            }
            sb.append(' ');
        }
        return sb.toString();
    }

    public void testValueKeyTextBeforeOtherKeys() throws Exception {
        Configuration config = new Configuration();
        assertEquals("<root>t<b>x</b></root>", readEvents("{\"root\":{\"$\":\"t\",\"b\":\"x\"}}", config));
        assertEquals("<root><b>x</b>t<c>y</c></root>",
            readEvents("{\"root\":{\"b\":\"x\",\"$\":\"t\",\"c\":\"y\"}}", config));
        assertEquals("<root>t<b><c>y</c></b></root>",
            readEvents("{\"root\":{\"@a\":\"v\",\"$\":\"t\",\"b\":{\"c\":\"y\"}}}", config));
        assertEquals("<root>t<b>x</b><b>y</b></root>", readEvents("{\"root\":{\"$\":\"t\",\"b\":[\"x\",\"y\"]}}", config));
        assertEquals("<root>t1t2<b>x</b></root>", readEvents("{\"root\":{\"$\":[\"t1\",\"t2\"],\"b\":\"x\"}}", config));
        // the items of an array of elements
        assertEquals("<root><b>x<c>y</c></b><b>z</b></root>",
            readEvents("{\"root\":{\"b\":[{\"$\":\"x\",\"c\":\"y\"},{\"$\":\"z\"}]}}", config));
        // the full event sequence, with the end of each element
        assertEquals("1root 4t 1b 4x 2b 2root 8 ", events("{\"root\":{\"$\":\"t\",\"b\":\"x\"}}"));

        Configuration primitive = new Configuration();
        primitive.setPrimitiveArrayKeys(new HashSet<String>(Arrays.asList("list")));
        assertEquals("<root><list><item>t<c>y</c></item><item>u</item></list><d>z</d></root>",
            readEvents("{\"root\":{\"list\":[{\"item\":{\"$\":\"t\",\"c\":\"y\"}},{\"item\":{\"$\":\"u\"}}],\"d\":\"z\"}}",
                       primitive));
    }

    public void testValueKeyTextLastIsUnchanged() throws Exception {
        Configuration config = new Configuration();
        assertEquals("<root>t</root>", readEvents("{\"root\":{\"$\":\"t\"}}", config));
        assertEquals("<root><b>x</b>t</root>", readEvents("{\"root\":{\"b\":\"x\",\"$\":\"t\"}}", config));
        assertEquals("<root><c>t</c><d>y</d></root>", readEvents("{\"root\":{\"c\":{\"$\":\"t\"},\"d\":\"y\"}}", config));
    }

    public void testRoundTripOfTextBeforeAChildElement() throws Exception {
        java.io.StringWriter out = new java.io.StringWriter();
        javax.xml.stream.XMLStreamWriter writer = new MappedXMLOutputFactory(new Configuration()).createXMLStreamWriter(out);
        writer.writeStartDocument();
        writer.writeStartElement("r");
        writer.writeAttribute("a", "v");
        writer.writeCharacters("text");
        writer.writeStartElement("b");
        writer.writeCharacters("x");
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
        assertEquals("{\"r\":{\"@a\":\"v\",\"$\":\"text\",\"b\":\"x\"}}", out.toString());

        XMLStreamReader reader = new MappedXMLInputFactory(new Configuration())
            .createXMLStreamReader(new StringReader(out.toString()));
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
        assertEquals("v", reader.getAttributeValue(null, "a"));
        assertEquals(XMLStreamConstants.CHARACTERS, reader.next());
        assertEquals("text", reader.getText());
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
        assertEquals("b", reader.getLocalName());
        assertEquals(XMLStreamConstants.CHARACTERS, reader.next());
        assertEquals(XMLStreamConstants.END_ELEMENT, reader.next());
        assertEquals("b", reader.getLocalName());
        assertEquals(XMLStreamConstants.END_ELEMENT, reader.next());
        assertEquals("r", reader.getLocalName());
        assertEquals(XMLStreamConstants.END_DOCUMENT, reader.next());
    }

    public void testPrimitiveArrayKeysWithAttributeOnlyItems() throws Exception {
        Configuration primitive = new Configuration();
        primitive.setPrimitiveArrayKeys(new HashSet<String>(Arrays.asList("c", "r")));
        MappedXMLInputFactory factory = new MappedXMLInputFactory(primitive);
        MappedXMLInputFactory standard = new MappedXMLInputFactory(new Configuration());

        // an item whose only key is an attribute or a namespace declaration isn't a
        // child element, so the array is read as a standard array
        String[] docs = {
            "{\"r\":{\"c\":[{\"@x\":\"v\"}]}}",
            "{\"r\":{\"c\":[{\"@x\":\"v\"},{\"@x\":\"w\"}],\"d\":\"y\"}}",
            "{\"r\":[{\"@a\":\"v\"},{\"@a\":\"w\"}]}",
            "{\"r\":{\"c\":[{\"@xmlns\":{\"p\":\"urn:p\"}}]}}",
            "{\"r\":{\"c\":[{\"item\":\"x\"},{\"@x\":\"v\"}]}}",
        };
        for (String json : docs) {
            assertEquals(json, readEvents(standard, json), readEvents(factory, json));
        }
        assertEquals("<r><c x=\"v\"></c><c x=\"w\"></c><d>y</d></r>", readEvents(factory, docs[1]));

        // arrays of child elements are still read without the array elements
        assertEquals("<r><c><item>x</item><item>y</item></c><d>z</d></r>",
            readEvents(factory, "{\"r\":{\"c\":[{\"item\":\"x\"},{\"item\":\"y\"}],\"d\":\"z\"}}"));
        assertEquals("<r><c><item x=\"v\"><d>y</d></item></c></r>",
            readEvents(factory, "{\"r\":{\"c\":[{\"item\":{\"@x\":\"v\",\"d\":\"y\"}}]}}"));
    }

    public void testPrimitiveArrayKeysWithArrayValues() throws Exception {
        Configuration primitive = new Configuration();
        primitive.setPrimitiveArrayKeys(new HashSet<String>(Arrays.asList("c", "r")));

        // an item's value that is an array gives an element for each of its items
        assertEquals("<x><c><d>a0</d><d>a1</d><d>b0</d></c><y>z</y></x>",
            readEvents("{\"x\":{\"c\":[{\"d\":[\"a0\",\"a1\"]},{\"d\":[\"b0\"]}],\"y\":\"z\"}}", primitive));
        assertEquals("<x><c><d>a0</d></c></x>", readEvents("{\"x\":{\"c\":[{\"d\":[]},{\"d\":[\"a0\"]}]}}", primitive));
        assertEquals("<x><c></c><y>z</y></x>", readEvents("{\"x\":{\"c\":[{\"d\":[]}],\"y\":\"z\"}}", primitive));
        assertEquals("<x><c><d>a0</d><d>b0</d></c><y>z</y></x>",
            readEvents("{\"x\":{\"c\":[{\"d\":[\"a0\"]},{\"d\":\"b0\"},{\"d\":[]}],\"y\":\"z\"}}", primitive));
        assertEquals("<x><c><d><e>x</e></d></c></x>", readEvents("{\"x\":{\"c\":[{\"d\":[{\"e\":\"x\"}]}]}}", primitive));

        // the root array
        assertEquals("<r><d>a0</d></r>", readEvents("{\"r\":[{\"d\":[\"a0\"]}]}", primitive));
        assertEquals("<r><d>a0</d><d>a1</d><d>b0</d></r>",
            readEvents("{\"r\":[{\"d\":[\"a0\",\"a1\"]},{\"d\":[\"b0\"]}]}", primitive));
        assertEquals("<r><d>a0</d></r>", readEvents("{\"r\":[{\"d\":[]},{\"d\":[\"a0\"]}]}", primitive));
        assertEquals("<r></r>", readEvents("{\"r\":[{\"d\":[]}]}", primitive));

        // nested arrays read with primitiveArrayKeys
        assertEquals("<x><c><d>a0</d></c><c2><c><d>b0</d></c></c2></x>",
            readEvents("{\"x\":{\"c\":[{\"d\":[\"a0\"]}],\"c2\":{\"c\":[{\"d\":[\"b0\"]}]}}}", primitive));

        // items with other values are unchanged
        assertEquals("<r><d>a0</d><d>b0</d></r>", readEvents("{\"r\":[{\"d\":\"a0\"},{\"d\":\"b0\"}]}", primitive));
        assertEquals("<r><d><e>a0</e><e>a1</e></d></r>", readEvents("{\"r\":[{\"d\":{\"e\":[\"a0\",\"a1\"]}}]}", primitive));
    }
}
