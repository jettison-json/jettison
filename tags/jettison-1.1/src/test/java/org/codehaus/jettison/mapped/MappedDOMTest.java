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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.DOMTest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test Mapped DOM API
 * 
 * @author Thomas.Diesler@jboss.com
 * @author <a href="mailto:dejan@nighttale.net">Dejan Bosanac</a>
 * @since 21-Mar-2008
 */
public class MappedDOMTest extends DOMTest {
	

	public MappedDOMTest() throws Exception {
		super();
	}
	
	public void testSimple() throws Exception {
		String xmlStr = "<kermit>the frog</kermit>";
		String expStr = "{\"kermit\":\"the frog\"}";
		String resStr = toJSON(parse(xmlStr));
		assertEquals("Unexpected result: " + resStr, expStr, resStr);

		String resXML = toXML(resStr);
		assertEquals("Unexpected result: " + resXML, xmlStr, resXML);
	}

	public void testSimpleAttribute() throws Exception {
		String xmlStr = "<kermit mygirl=\"piggy\">the frog</kermit>";
		String expStr = "{\"kermit\":{\"@mygirl\":\"piggy\",\"$\":\"the frog\"}}";
		String resStr = toJSON(parse(xmlStr));
		assertEquals("Unexpected result: " + resStr, expStr, resStr);

		String resXML = toXML(resStr);
		assertEquals("Unexpected result: " + resXML, xmlStr, resXML);
	}

	public void testDefaultNamespace() throws Exception {
		String xmlStr = "<kermit xmlns=\"http://somens\">the frog</kermit>";
		String expStr = "{\"somens.kermit\":\"the frog\"}";
		String resStr = toJSON(parse(xmlStr));
		assertEquals("Unexpected result: " + resStr, expStr, resStr);

		String resXML = toXML(resStr);
		assertEquals("Unexpected result: " + resXML, xmlStr, resXML);
	}

	private String toJSON(Element srcDOM) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Configuration conf = new Configuration();
		Map xnsToJns = new HashMap();
		xnsToJns.put("http://somens", "somens");
		conf.setXmlToJsonNamespaces(xnsToJns);
		new MappedDOMDocumentSerializer(baos, conf).serialize(srcDOM);
		return new String(baos.toByteArray());
	}

	private String toXML(String jsonStr) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(jsonStr.getBytes());
		Configuration conf = new Configuration();
		Map xnsToJns = new HashMap();
		xnsToJns.put("http://somens", "somens");
		conf.setXmlToJsonNamespaces(xnsToJns);
		Document resDOM = new MappedDOMDocumentParser(conf).parse(bais);
		return printNode(resDOM);
	}
	
}
