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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.codehaus.jettison.DOMTest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Test BadgerFish DOM API
 * 
 * @author Thomas.Diesler@jboss.com
 * @author <a href="mailto:dejan@nighttale.net">Dejan Bosanac</a>
 * @since 21-Mar-2008
 */
public class BadgerFishDOMTest extends DOMTest {
	

	
	public BadgerFishDOMTest() throws Exception {
		super();
	}
	
	public void testSimple() throws Exception {
		String xmlStr = "<kermit>the frog</kermit>";
		String expStr = "{\"kermit\":{\"$\":\"the frog\"}}";
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
		String expStr = "{\"kermit\":{\"@xmlns\":{\"$\":\"http:\\/\\/somens\"},\"$\":\"the frog\"}}";
		String resStr = toJSON(parse(xmlStr));
		assertEquals("Unexpected result: " + resStr, expStr, resStr);

		String resXML = toXML(resStr);
		assertEquals("Unexpected result: " + resXML, xmlStr, resXML);
	}

	public void testElementNamespace() throws Exception {
		String xmlStr = "<ns1:kermit xmlns:ns1=\"http://somens\">the frog</ns1:kermit>";
		String expStr = "{\"ns1:kermit\":{\"@xmlns\":{\"ns1\":\"http:\\/\\/somens\"},\"$\":\"the frog\"}}";
		String resStr = toJSON(parse(xmlStr));
		assertEquals("Unexpected result: " + resStr, expStr, resStr);

		String resXML = toXML(resStr);
		assertEquals("Unexpected result: " + resXML, xmlStr, resXML);
	}

	public void testElementAttributeNamespace() throws Exception {
		String xmlStr = "<ns1:kermit xmlns:ns1=\"http://somens\" ns1:mygirl=\"piggy\">the frog</ns1:kermit>";
		String expStr = "{\"ns1:kermit\":{\"@xmlns\":{\"ns1\":\"http:\\/\\/somens\"},\"@ns1:mygirl\":\"piggy\",\"$\":\"the frog\"}}";
		String resStr = toJSON(parse(xmlStr));
		assertEquals("Unexpected result: " + resStr, expStr, resStr);

		String resXML = toXML(resStr);
		assertEquals("Unexpected result: " + resXML, xmlStr, resXML);
	}

	private String toJSON(Element srcDOM) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new BadgerFishDOMDocumentSerializer(baos).serialize(srcDOM);
		return new String(baos.toByteArray());
	}

	private String toXML(String jsonStr) throws Exception {
	      ByteArrayInputStream bais = new ByteArrayInputStream(jsonStr.getBytes());
	      Document resDOM = new BadgerFishDOMDocumentParser().parse(bais);	      
	      return printNode(resDOM);	      
	}
}
