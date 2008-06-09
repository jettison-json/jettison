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
        
        System.out.println(strWriter.toString());
        
        assertEquals("{\"root\":{\"child\":\"\"}}", strWriter.toString());
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
        
        //w.writeCharacters("test");
        
        w.writeEndElement();
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
        
        //w.writeCharacters("test");
        
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"root\":{\"@att\":\"attvalue\",\"foo.att2\":\"attvalue\"}}", strWriter.toString());
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
        
        System.out.println(strWriter.toString());
        
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
        
        System.out.println(strWriter.toString());
        
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
        
        System.out.println(strWriter.toString());
        
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
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        System.out.println(strWriter.toString());
        
        assertEquals("{\"root\":{\"subchild1\":[{\"subchild2\":[\"first sub2\",{\"subchild3\":[\"first sub3\",\"second sub3\"]},\"third sub2\"]},\"sub1\"]}}"
, strWriter.toString());
    }
    
    public void testArrayOfChildren() throws Exception {
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
        
        w.writeStartDocument();
        w.writeStartElement("root");
        
        w.writeStartElement("child");
        w.writeEndElement();
        
        w.writeStartElement("child");
        w.writeEndElement();
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        System.out.println(strWriter.toString());
        
        assertEquals("{\"root\":{\"child\":[\"\",\"\"]}}", strWriter.toString());
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
        
        System.out.println(strWriter.toString());
        
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
        
        System.out.println(strWriter.toString());
        
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
        
        w.writeEndElement();
        
        w.writeStartElement("subchild1");
        w.writeCharacters("sub1");
        w.writeEndElement();
        
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        System.out.println(strWriter.toString());
        
        assertEquals("{\"root\":{\"subchild1\":[{\"subchild2\":[5,3.14,true,\"000123\"]},\"sub1\"]}}", strWriter.toString());      
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

        System.out.println(strWriter.toString());
        
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

        System.out.println(strWriter.toString());
        
        assertEquals("{\"a\":{\"o\":{\"@class\":\"string\"}}}", strWriter.toString());
    }    
    
    public void testSingleArrayElement() throws Exception {
		StringWriter strWriter = new StringWriter();
		MappedNamespaceConvention con = new MappedNamespaceConvention();
		AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);
		w.seriliazeAsArray(con.createKey("", "", "array-a"));

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

		System.out.println(strWriter.toString());

		assertEquals("{\"array-a\":{\"a\":[{\"n\":1}]}}", strWriter.toString());
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

		System.out.println(strWriter.toString());

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
        
        System.out.println(strWriter.toString());
        
        assertEquals("{\"root\":{\"child\":[{\"@x\":\"y\",\"$\":\"value\"},{\"@a\":\"b\",\"$\":\"value\"},{\"@x\":\"z\",\"$\":\"value\"}]}}", strWriter.toString());
    }
    
    
}
