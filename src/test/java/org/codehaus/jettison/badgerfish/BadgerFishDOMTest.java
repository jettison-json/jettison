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

import org.codehaus.jettison.DOMTest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test BadgerFish DOM API
 * 
 * @author Thomas.Diesler@jboss.com
 * @author <a href="mailto:dejan@nighttale.net">Dejan Bosanac</a>
 * @author <a href="http://treleis.org">twayne</a>
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
	
	public void testParentScopedElementNamespace() throws Exception {
        String xmlStr = "<ns:A xmlns:ns=\"http://json/test\"><ns:B><ns:C><ns:D><ns:E>10</ns:E></ns:D></ns:C></ns:B></ns:A>";
        String expStr = "{\"ns:A\":{\"@xmlns\":{\"ns\":\"http:\\/\\/json\\/test\"},\"ns:B\":{\"ns:C\":{\"ns:D\":{\"ns:E\":{\"$\":\"10\"}}}}}}";
        String resStr = toJSON(parse(xmlStr));
        assertEquals("Unexpected result: " + resStr, expStr, resStr);

        String resXML = toXML(resStr).replace(System.getProperty("line.separator"), "").replaceAll(">\\s+<", "><");
        assertEquals("Unexpected result: " + resXML, xmlStr, resXML);
    }
	
	public void testParentScopedAttributeNamespace() throws Exception {
        String xmlStr = "<P xmlns=\"http://json/test\"><Q><R xmlns:ns=\"http://foo\"><S ns:a=\"bar\"><T>30</T></S></R></Q></P>";
        String expStr = "{\"P\":{\"@xmlns\":{\"$\":\"http:\\/\\/json\\/test\"},\"Q\":{\"R\":{\"@xmlns\":{\"ns\":\"http:\\/\\/foo\"},\"S\":{\"@ns:a\":\"bar\",\"T\":{\"$\":\"30\"}}}}}}";
        String resStr = toJSON(parse(xmlStr));
        assertEquals("Unexpected result: " + resStr, expStr, resStr);

        String resXML = toXML(resStr).replace(System.getProperty("line.separator"), "").replaceAll(">\\s+<", "><");
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
