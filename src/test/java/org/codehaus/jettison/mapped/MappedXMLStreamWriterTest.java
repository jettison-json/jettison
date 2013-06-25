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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.codehaus.jettison.AbstractXMLStreamWriter;

public class MappedXMLStreamWriterTest extends TestCase {
    
    public void testRoot() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
       
        assertEquals("{\"root\":\"\"}", strWriter.toString());
    }
    
    public void testChild() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        w.writeStartElement("child");
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"root\":{\"child\":\"\"}}", strWriter.toString());
    }
    
    public void testChildDropElement() throws Exception {
        StringWriter strWriter = new StringWriter();
        Configuration c = new Configuration();
        c.setDropRootElement(true);
        MappedNamespaceConvention con = new MappedNamespaceConvention(c);
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        w.writeStartElement("child");
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"child\":\"\"}", strWriter.toString());
    }
    
    public void testChildDropElement2() throws Exception {
        StringWriter strWriter = new StringWriter();
        Configuration c = new Configuration();
        c.setDropRootElement(true);
        MappedNamespaceConvention con = new MappedNamespaceConvention(c);
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        w.writeStartElement("root2");
        w.writeStartElement("child");
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"root2\":{\"child\":\"\"}}", strWriter.toString());
    }
    
    public void testChildDropElement3() throws Exception {
        StringWriter strWriter = new StringWriter();
        Configuration c = new Configuration();
        List<String> ignoredElements = new LinkedList<String>();
        ignoredElements.add("root");
        ignoredElements.add("root2");
        c.setIgnoredElements(ignoredElements);
        MappedNamespaceConvention con = new MappedNamespaceConvention(c);
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        w.writeStartElement("root2");
        w.writeStartElement("child");
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"child\":\"\"}", strWriter.toString());
    }
    
    public void testChildDropElement4() throws Exception {
        StringWriter strWriter = new StringWriter();
        Configuration c = new Configuration();
        List<String> ignoredElements = new LinkedList<String>();
        ignoredElements.add("child");
        c.setIgnoredElements(ignoredElements);
        MappedNamespaceConvention con = new MappedNamespaceConvention(c);
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        w.writeStartElement("child");
        w.writeEndElement();
        w.writeStartElement("child2");
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"root\":{\"child2\":\"\"}}", strWriter.toString());
    }
    
    public void testText() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        w.writeStartElement("child");
        
        w.writeCharacters("test");
        w.writeCharacters("test");
        w.writeCharacters("test".toCharArray(), 0, 4);
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"root\":{\"child\":\"testtesttest\"}}", strWriter.toString());
    }
    
    public void testTextNullAsString() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        w.writeStartElement("child");
        w.writeCharacters(null);
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"root\":{\"child\":\"null\"}}", strWriter.toString());
    }
    
    public void testTextNullAsNull() throws Exception {
        StringWriter strWriter = new StringWriter();
        Configuration c = new Configuration();
        c.setWriteNullAsString(false);
        MappedNamespaceConvention con = new MappedNamespaceConvention(c);
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        w.writeStartElement("child");
        w.writeCharacters(null);
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"root\":{\"child\":null}}", strWriter.toString());
    }
    
    public void testAttributes() throws Exception {
        StringWriter strWriter = new StringWriter();
        
        Map xtoj = new HashMap();
        xtoj.put("http://foo/", "foo");
        MappedNamespaceConvention con = new MappedNamespaceConvention(new Configuration(xtoj));
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        w.writeAttribute("att", "attvalue");
        w.writeAttribute("http://foo/", "att2", "attvalue");
        
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"root\":{\"@att\":\"attvalue\",\"@foo.att2\":\"attvalue\"}}", strWriter.toString());
    }

    public void testAttributesAsElements() throws Exception {
        StringWriter strWriter = new StringWriter();
        
        Map xtoj = new HashMap();
        xtoj.put("http://foo/", "foo");
        List atts = new ArrayList();
        atts.add(new QName("http://foo/", "att2"));
        
        Configuration c = new Configuration(xtoj, atts, null);
        
        MappedNamespaceConvention con = new MappedNamespaceConvention(c);
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        w.writeAttribute("att", "attvalue");
        w.writeAttribute("http://foo/", "att2", "attvalue");
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"root\":{\"@att\":\"attvalue\",\"foo.att2\":\"attvalue\"}}", strWriter.toString());
    }

    public void testAttributesWithAtSupressed() throws Exception {
        StringWriter strWriter = new StringWriter();
        
        Map xtoj = new HashMap();
        xtoj.put("http://foo/", "foo");
        List atts = new ArrayList();
        atts.add(new QName("http://foo/", "att2"));
        
        Configuration c = new Configuration(xtoj, atts, null);
        c.setSupressAtAttributes(true);
        
        MappedNamespaceConvention con = new MappedNamespaceConvention(c);
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        w.writeAttribute("att", "attvalue");
        w.writeAttribute("http://foo/", "att2", "attvalue");
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"root\":{\"att\":\"attvalue\",\"foo.att2\":\"attvalue\"}}", strWriter.toString());
    }
    
    public void testTwoChildren() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        
        w.writeStartElement("child1");
        w.writeEndElement();
        
        w.writeStartElement("child2");
        w.writeEndElement();
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"root\":{\"child1\":\"\",\"child2\":\"\"}}", strWriter.toString());
    }
    
    public void testTwoChildrenWithSubChild() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        
        w.writeStartElement("child1");
        w.writeStartElement("subchild");
        w.writeEndElement();
        w.writeEndElement();
        
        w.writeStartElement("child2");
        w.writeStartElement("subchild");
        w.writeEndElement();
        w.writeEndElement();
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"root\":{" +
                        "\"child1\":{\"subchild\":\"\"}," +
                        "\"child2\":{\"subchild\":\"\"}}}", strWriter.toString());
    }
 
    public void testTwoChildrenWithSubChildWithText() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        
        w.writeStartElement("child1");
        w.writeStartElement("subchild1");
        w.writeCharacters("test");
        w.writeEndElement();
        
        w.writeStartElement("subchild2");
        w.writeCharacters("test");
        w.writeEndElement();
        
        w.writeEndElement();
        
        w.writeStartElement("child2");
        w.writeStartElement("subchild");
        w.writeCharacters("test");
        w.writeEndElement();
        w.writeEndElement();
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"root\":{" +
                        "\"child1\":{\"subchild1\":\"test\",\"subchild2\":\"test\"}," +
                        "\"child2\":{\"subchild\":\"test\"}}}", strWriter.toString());
    }
    
    public void testNestedArrayOfChildren() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        
        	w.writeStartElement("subchild1");
        
        		w.writeStartElement("subchild2");
        			w.writeCharacters("first sub2");
        		w.writeEndElement();
        
        		w.writeStartElement("subchild2");
        			w.writeStartElement("subchild3");
        				w.writeCharacters("first sub3");
        			w.writeEndElement();
        			w.writeStartElement("subchild3");
        				w.writeCharacters("second sub3");
        			w.writeEndElement();
        		w.writeEndElement();
        
        		w.writeStartElement("subchild2");
        			w.writeCharacters("third sub2");
        		w.writeEndElement();
        
        	w.writeEndElement();
        
        	w.writeStartElement("subchild1");
        		w.writeCharacters("sub1");
        	w.writeEndElement();
        	
        	//w.writeEndElement();
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"root\":{\"subchild1\":[{\"subchild2\":[\"first sub2\",{\"subchild3\":[\"first sub3\",\"second sub3\"]},\"third sub2\"]},\"sub1\"]}}"
, strWriter.toString());
    }
    
    //ISSUE-48
    public void testNestedArrayOfChildrenWithComplexElements() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        
        	w.writeStartElement("subchild1");
        
        		w.writeStartElement("subchild2");
        			w.writeCharacters("first sub2");
        		w.writeEndElement();
        		
        	w.writeEndElement();
        	
        	w.writeStartElement("subchild1");
    			w.writeCharacters("text");
    		w.writeEndElement();
    		
        	w.writeStartElement("subchild1");
		 	   w.writeCharacters("text1");
		    w.writeEndElement();
			
			w.writeStartElement("outer");
				w.writeCharacters("test");
			w.writeEndElement();
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals(strWriter.toString(), "{\"root\":{\"subchild1\":[{\"subchild2\":\"first sub2\"},\"text\",\"text1\"],\"outer\":\"test\"}}");
    }
    
    public void testArrayOfChildren() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        
        w.writeStartElement("child");
        w.writeCharacters("first");
        w.writeEndElement();
        
        w.writeStartElement("child");
        w.writeCharacters("second");
        w.writeEndElement();
        
        w.writeStartElement("child");
        w.writeCharacters("third");
        w.writeEndElement();
        
        w.writeEndElement();
        
        w.writeStartElement("other");
        w.writeCharacters("test");
        w.writeEndElement();
        
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"root\":{\"child\":[\"first\",\"second\",\"third\"]},\"other\":\"test\"}", strWriter.toString());
    }
    
    public void testMixedArrayAndJSONObject() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        w.serializeAsArray("results");
        w.writeStartDocument();
        w.writeStartElement("SearchResult");
        w.writeStartElement("results");
        w.writeStartElement("field");
        w.writeCharacters("1");
        w.writeEndElement();
        w.writeStartElement("field");
        w.writeCharacters("2");
        w.writeEndElement();
        w.writeEndElement();
        w.writeStartElement("results");
        w.writeStartElement("field");
        w.writeCharacters("1");
        w.writeEndElement();
        w.writeStartElement("field");
        w.writeCharacters("2");
        w.writeEndElement();
        w.writeEndElement();
        
        w.writeStartElement("total");
        w.writeCharacters("2");
        w.writeEndElement();
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        assertEquals("{\"SearchResult\":{\"results\":[{\"field\":[1,2]},{\"field\":[1,2]}],\"total\":2}}", 
        	strWriter.toString());
    }
    
    public void testMixedArrayAndJSONObject2() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        w.writeStartDocument();
        w.writeStartElement("TestResult");
        w.writeStartElement("company");
        w.writeStartElement("name");
        w.writeCharacters("Acme");
        w.writeEndElement();
        w.writeStartElement("phone");
        w.writeStartElement("type");
        w.writeCharacters("main");
        w.writeEndElement();
        w.writeStartElement("number");
        w.writeCharacters("123");
        w.writeEndElement();
        w.writeEndElement();
        
        w.writeStartElement("ceo");
        w.writeStartElement("firstname");
        w.writeCharacters("John");
        w.writeEndElement();
        w.writeStartElement("phone");
        w.writeCharacters("567");
        w.writeEndElement();
        w.writeEndElement();
        
        w.writeStartElement("address");
        w.writeCharacters("Main st");
        w.writeEndElement();
        
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        assertEquals("{\"TestResult\":{\"company\":{\"name\":\"Acme\",\"phone\":"
        		+ "{\"type\":\"main\",\"number\":123},\"ceo\":{\"firstname\":\"John\","
        		+ "\"phone\":567},\"address\":\"Main st\"}}}", 
        	strWriter.toString());
    }
    
    public void testComplexArrayOfChildren() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        
        addChild(w);
        addChild(w);
        addChild(w);
        addChild(w);
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"root\":{\"child\":[" +
                     "{\"subchild1\":\"test\",\"subchild2\":\"test\"}," +
                     "{\"subchild1\":\"test\",\"subchild2\":\"test\"}," +
                     "{\"subchild1\":\"test\",\"subchild2\":\"test\"}," +
                     "{\"subchild1\":\"test\",\"subchild2\":\"test\"}]}}", strWriter.toString());
    }

    private void addChild(AbstractXMLStreamWriter w) throws XMLStreamException {
        w.writeStartElement("child");
        
        w.writeStartElement("subchild1");
        w.writeCharacters("test");
        w.writeEndElement();
        
        w.writeStartElement("subchild2");
        w.writeCharacters("test");
        w.writeEndElement();
        
        w.writeEndElement();
    }
    
    public void testNamespacedElements() throws Exception {
        StringWriter strWriter = new StringWriter();
        
        Map xtoj = new HashMap();
        xtoj.put("http://foo/", "foo");
        MappedNamespaceConvention con = new MappedNamespaceConvention(new Configuration(xtoj));
        
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("http://foo/", "root");
        
        w.writeStartElement("http://foo/", "child");
        w.writeEndElement();
        
        w.writeStartElement("http://foo/", "child");
        w.writeEndElement();
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"foo.root\":{\"foo.child\":[\"\",\"\"]}}", strWriter.toString());
    }
    
    public void testIssue18Enh() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("", "a", "");

        w.writeStartElement("", "vals", "");
        
        w.writeStartElement("", "string", "");
        w.writeCharacters("1");
        w.writeEndElement();
        
        w.writeStartElement("", "string", "");
        w.writeCharacters("2");
        w.writeEndElement();
        
        w.writeStartElement("", "string", "");
        w.writeCharacters("3");
        w.writeEndElement();
        
        w.writeEndElement();
        
        w.writeStartElement("", "n", "");
        w.writeCharacters("5");
        w.writeEndElement();
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();

        assertEquals("{\"a\":{\"vals\":{\"string\":[1,2,3]},\"n\":5}}", strWriter.toString());
    }
    
    public void testMap() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        w.writeStartDocument();
        
        w.writeStartElement("map");
        	w.writeStartElement("entry");
        		w.writeStartElement("string");
        			w.writeCharacters("id");
        		w.writeEndElement();
        		w.writeStartElement("string");
        			w.writeCharacters("6");
        		w.writeEndElement();
        	w.writeEndElement();
        	w.writeStartElement("entry");
        		w.writeStartElement("string");
    				w.writeCharacters("name");
    			w.writeEndElement();
    			w.writeStartElement("string");
    				w.writeCharacters("Dejan");
    			w.writeEndElement();
    		w.writeEndElement();
    		w.writeStartElement("entry");
        		w.writeStartElement("string");
        			w.writeCharacters("city");
        		w.writeEndElement();
        		w.writeStartElement("string");
    				w.writeCharacters("Belgrade");
    			w.writeEndElement();
    		w.writeEndElement();
        w.writeEndElement();
        
        w.writeEndDocument();
        w.close();
        strWriter.close();
        String result = strWriter.toString();
        assertEquals(result, "{\"map\":{\"entry\":[{\"string\":[\"id\",6]},{\"string\":[\"name\",\"Dejan\"]},{\"string\":[\"city\",\"Belgrade\"]}]}}");
    }
    
    public void testPrimitiveTypes() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        
        w.writeStartElement("subchild1");
        
        w.writeStartElement("subchild2");
        w.writeCharacters(5 + "");
        w.writeEndElement();
        
        w.writeStartElement("subchild2");
        w.writeCharacters(3.14 + "");
        w.writeEndElement();
        
        w.writeStartElement("subchild2");
        w.writeCharacters(true + "");
        w.writeEndElement();
        
        w.writeStartElement("subchild2");
        w.writeCharacters("000123");
        w.writeEndElement();

        w.writeStartElement("subchild2");
        w.writeCharacters("Infinity");
        w.writeEndElement();
        
        w.writeEndElement();
        
        w.writeStartElement("subchild1");
        w.writeCharacters("sub1");
        w.writeEndElement();
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        String expected = "{\"root\":{\"subchild1\":[{\"subchild2\":[5,3.14,true,\"000123\",\"Infinity\"]},\"sub1\"]}}";
        String actual = strWriter.toString();
        assertEquals(expected, actual);
    }

    // issue 64
    public void testPrimitiveInfinityNaN() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);

        w.writeStartDocument();
        w.writeStartElement("root");


        w.writeStartElement("subchild1");
        w.writeCharacters("Infinity");
        w.writeEndElement();

        w.writeStartElement("subchild1");
        w.writeCharacters("NaN");
        w.writeEndElement();


        w.writeEndElement();
        w.writeEndDocument();

        w.close();
        strWriter.close();
        String expected = "{\"root\":{\"subchild1\":[\"Infinity\",\"NaN\"]}}";
        String actual = strWriter.toString();
        assertEquals(expected, actual);
    }

    //issue 29
    public void testComplexElements() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("", "a", "");

        w.writeStartElement("", "o", "");
        w.writeAttribute("class", "string");
        w.writeCharacters("1");
        w.writeEndElement();
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"a\":{\"o\":{\"@class\":\"string\",\"$\":\"1\"}}}", strWriter.toString());
    }
    
    //issue 29
    public void testIgnoreComplexElements() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        MappedXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        w.setValueKey(null);
        
        w.writeStartDocument();
        w.writeStartElement("", "a", "");

        w.writeStartElement("", "o", "");
        w.writeAttribute("class", "string");
        w.writeCharacters("1");
        w.writeEndElement();
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"a\":{\"o\":{\"@class\":\"string\"}}}", strWriter.toString());
    }    
    
    public void testSingleArrayElement() throws Exception {
		StringWriter strWriter = new StringWriter();
		MappedNamespaceConvention con = new MappedNamespaceConvention();
		AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
		w.serializeAsArray(con.createKey("", "", "array-a"));

		w.writeStartDocument();
		w.writeStartElement("", "array-a", "");

		w.writeStartElement("", "a", "");
		w.writeStartElement("", "n", "");
		w.writeCharacters("1");
		w.writeEndElement();
		w.writeEndElement();

		w.writeEndElement();
		w.writeEndDocument();

		w.close();
		strWriter.close();

		assertEquals("{\"array-a\":[{\"a\":{\"n\":1}}]}", strWriter.toString());
	}
    
    public void testEmptySerializedArrayElement() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        w.serializeAsArray(con.createKey("", "", "array-a"));

        w.writeStartDocument();
        w.writeStartElement("", "array-a", "");

        w.writeEndElement();
        w.writeEndDocument();

        w.close();
        strWriter.close();

        assertEquals("{\"array-a\":[\"\"]}", strWriter.toString());
    }
    
    public void testNestedSerializedArrayElement() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        w.serializeAsArray(con.createKey("", "", "docs"));
        w.serializeAsArray(con.createKey("", "", "filters"));
        w.serializeAsArray(con.createKey("", "", "hosts"));

        w.writeStartDocument();
        w.writeStartElement("", "docs", "");
        w.writeStartElement("", "doc", "");
        
        w.writeStartElement("", "id", "");
        w.writeCharacters("24");
        w.writeEndElement();
        
        w.writeStartElement("", "filters", "");
        w.writeEndElement();
        
        w.writeStartElement("", "hosts", "");
        w.writeStartElement("", "host", "");
        w.writeStartElement("", "name", "");
        w.writeCharacters("foobar.com");
        w.writeEndElement(); //name
        w.writeStartElement("", "ip", "");
        w.writeCharacters("255.255.255.255");
        w.writeEndElement(); //ip
        w.writeEndElement(); // host
        w.writeEndElement(); //hosts

        w.writeEndElement(); // doc
        w.writeEndElement(); // docs
        w.writeEndDocument();

        w.close();
        strWriter.close();

        assertEquals("{\"docs\":[{\"doc\":{\"id\":24,\"filters\":[\"\"],\"hosts\":[{\"host\":{\"name\":\"foobar.com\",\"ip\":\"255.255.255.255\"}}]}}]}", strWriter.toString());
    }
    
    public void testSingleArrayElementIgnore() throws Exception {
		StringWriter strWriter = new StringWriter();
		MappedNamespaceConvention con = new MappedNamespaceConvention();
		AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);

		w.writeStartDocument();
		w.writeStartElement("", "array-a", "");

		w.writeStartElement("", "a", "");
		w.writeStartElement("", "n", "");
		w.writeCharacters("1");
		w.writeEndElement();
		w.writeEndElement();

		w.writeEndElement();
		w.writeEndDocument();

		w.close();
		strWriter.close();

		assertEquals("{\"array-a\":{\"a\":{\"n\":1}}}", strWriter.toString());
	} 
    
    //issue 26
    public void testArraysAndAttributes() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        
        w.writeStartElement("child");
        w.writeAttribute("x", "y");
        w.writeCharacters("value");
        w.writeEndElement();
        
        w.writeStartElement("child");
        w.writeAttribute("a", "b");
        w.writeCharacters("value");
        w.writeEndElement();
        
        w.writeStartElement("child");
        w.writeAttribute("x", "z");
        w.writeCharacters("value");
        w.writeEndElement();        
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"root\":{\"child\":[{\"@x\":\"y\",\"$\":\"value\"},{\"@a\":\"b\",\"$\":\"value\"},{\"@x\":\"z\",\"$\":\"value\"}]}}", strWriter.toString());
    }

    //issue 36
    public void testConverter() throws Exception {
        StringWriter strWriter = new StringWriter();
        Configuration config = new Configuration();
        config.setTypeConverter(new SimpleConverter());
        MappedNamespaceConvention con = new MappedNamespaceConvention(config);
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);

        w.writeStartDocument();
        w.writeStartElement("root");
        w.writeCharacters("true");
        w.writeEndElement();

        w.writeEndDocument();

        w.close();
        strWriter.close();

        assertEquals("{\"root\":\"true\"}", strWriter.toString());        
    }
    
    //issue 52
    public void testAttributeKey() throws Exception {
        StringWriter strWriter = new StringWriter();
        
        Map xtoj = new HashMap();
        xtoj.put("http://foo/", "foo");
        Configuration conf = new Configuration(xtoj);
        conf.setAttributeKey("!");
        MappedNamespaceConvention con = new MappedNamespaceConvention(conf);
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        w.writeAttribute("att", "attvalue");
        w.writeAttribute("http://foo/", "att2", "attvalue");
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"root\":{\"!att\":\"attvalue\",\"!foo.att2\":\"attvalue\"}}", strWriter.toString());
    }   
    
    // issue 63
    public void testArrayAsFirstInAnArray() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        w.serializeAsArray(con.createKey("", "", "bazs"));
        w.serializeAsArray(con.createKey("", "", "quacks"));

        w.writeStartDocument();
        w.writeStartElement("Foo");
        	w.writeStartElement("bazs"); // array
        		w.writeStartElement("quacks"); // array
        			w.writeStartElement("goof");
        			w.writeEndElement();
        		w.writeEndElement();
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();

        w.close();
        strWriter.close();

        assertEquals("{\"Foo\":{\"bazs\":[{\"quacks\":[{\"goof\":\"\"}]}]}}", strWriter.toString());
    }

    // JETTISON-65
    public void x_testImplicitCollections() throws Exception {
        StringWriter strWriter = new StringWriter();
        Configuration conf = new Configuration();
        MappedNamespaceConvention con = new MappedNamespaceConvention(conf);
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        
        addChild(w);
        addChild(w);
        addChild(w);
        addChild(w);
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"root\":[" +
                     "{\"subchild1\":\"test\",\"subchild2\":\"test\"}," +
                     "{\"subchild1\":\"test\",\"subchild2\":\"test\"}," +
                     "{\"subchild1\":\"test\",\"subchild2\":\"test\"}," +
                     "{\"subchild1\":\"test\",\"subchild2\":\"test\"}]}", strWriter.toString());
    }

    // JETTISON-57
    public void testChildClassPropertyNameSameAsParentObject() throws Exception {

        StringWriter strWriter = new StringWriter();
        Configuration conf = new Configuration();
        MappedNamespaceConvention con = new MappedNamespaceConvention(conf);
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);

        w.writeStartDocument();
            w.writeStartElement("definition");
                w.writeStartElement("structure");
                    w.writeAttribute("name", "conversation");
                    w.writeStartElement("symbolic");
                        w.writeAttribute("name", "reason");
                    w.writeEndElement();
                    w.writeStartElement("symbolic");
                        w.writeAttribute("name", "terms");
                    w.writeEndElement();
                    w.writeStartElement("numeric");
                        w.writeAttribute("name", "amountasked");
                    w.writeEndElement();
                    w.writeStartElement("numeric");
                        w.writeAttribute("name", "amountoffered");
                    w.writeEndElement();
                    w.writeStartElement("structure");
                        w.writeAttribute("name", "check");
                        w.writeStartElement("symbolic");
                            w.writeAttribute("name", "date");
                        w.writeEndElement();
                        w.writeStartElement("structure");
                            w.writeAttribute("name", "lines");
                            w.writeAttribute("repeating", "true");
                            w.writeStartElement("symbolic");
                                w.writeAttribute("name", "type");
                            w.writeEndElement();
                            w.writeStartElement("numeric");
                                w.writeAttribute("name", "amount");
                            w.writeEndElement();
                            w.writeStartElement("numeric");
                                w.writeAttribute("name", "cost");
                            w.writeEndElement();
                        w.writeEndElement();
                    w.writeEndElement();
                w.writeEndElement();
            w.writeEndElement();
        w.writeEndDocument();

        w.close();
        strWriter.close();

        assertEquals("{\"definition\":" +
                "{\"structure\":{\"@name\":\"conversation\",\"symbolic\":" +
                "[{\"@name\":\"reason\"},{\"@name\":\"terms\"}],\"numeric\":[" +
                "{\"@name\":\"amountasked\"},{\"@name\":\"amountoffered\"}]," +
                "\"structure\":{\"@name\":\"check\",\"symbolic\":" +
                "{\"@name\":\"date\"},\"structure\":{\"@name\":\"lines\",\"@repeating\":\"true\",\"symbolic\"" +
                ":{\"@name\":\"type\"},\"numeric\":[{\"@name\":\"amount\"},{\"@name\":\"cost\"}]}}}}}"
                , strWriter.toString());
    }
    
}
