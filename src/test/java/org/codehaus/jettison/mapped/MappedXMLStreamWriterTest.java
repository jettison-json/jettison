/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.codehaus.jettison.mapped;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.codehaus.jettison.AbstractXMLStreamWriter;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;

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
        
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();
        
        w.close();
        strWriter.close();
        
        assertEquals("{\"root\":{\"child\":\"testtest\"}}", strWriter.toString());
    }
    
    public void testAttributes() throws Exception {
        StringWriter strWriter = new StringWriter();
        
        Map xtoj = new HashMap();
        xtoj.put("http://foo/", "foo");
        MappedNamespaceConvention con = new MappedNamespaceConvention(xtoj);
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
        MappedNamespaceConvention con = new MappedNamespaceConvention(xtoj);
        
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
}
