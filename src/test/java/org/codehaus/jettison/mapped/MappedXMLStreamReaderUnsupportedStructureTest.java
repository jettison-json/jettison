/**
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
import java.util.Arrays;
import java.util.HashSet;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

/**
 * JSON structures the reader can't map to XML events must fail with an
 * XMLStreamException, not an unchecked exception.
 */
public class MappedXMLStreamReaderUnsupportedStructureTest extends TestCase {

    public void testValueKeyWithChildObject() throws Exception {
        assertUnsupported(new Configuration(), "{\"r\":{\"$\":\"t\",\"r\":{}}}");
    }

    public void testValueKeyWithChildObjectWithoutRootArrayWrapper() throws Exception {
        Configuration config = new Configuration();
        config.setRootElementArrayWrapper(false);
        assertUnsupported(config, "{\"r\":{\"$\":\"t\",\"r\":{}}}");
    }

    public void testRootArrayWithPrimitiveArrayKeys() throws Exception {
        assertUnsupported(primitiveArrayKeys(), "{\"r\":[{\"r\":1}]}");
    }

    public void testValueKeyWithEmptyArrayWithPrimitiveArrayKeys() throws Exception {
        assertUnsupported(primitiveArrayKeys(), "{\"r\":{\"$\":\"t\",\"r\":[]}}");
    }

    public void testRootArrayOfAttributesWithPrimitiveArrayKeys() throws Exception {
        assertUnsupported(primitiveArrayKeys(), "{\"r\":[{\"@a\":\"v\"}]}");
    }

    public void testSupportedStructureStillReads() throws Exception {
        assertEquals("<r a=\"v\">t</r>", readEvents(new Configuration(), "{\"r\":{\"@a\":\"v\",\"$\":\"t\"}}"));
        assertEquals("<r><b>1</b><c><d>x</d></c></r>",
            readEvents(primitiveArrayKeys(), "{\"r\":{\"b\":1,\"c\":{\"d\":\"x\"}}}"));
    }

    private static Configuration primitiveArrayKeys() {
        Configuration config = new Configuration();
        config.setPrimitiveArrayKeys(new HashSet<String>(Arrays.asList("a", "r")));
        return config;
    }

    private static void assertUnsupported(Configuration config, String json) throws Exception {
        try {
            readEvents(config, json);
            fail("Failure expected on an unsupported JSON structure: " + json);
        } catch (XMLStreamException ex) {
            assertEquals("The structure of the JSON input is not supported", ex.getMessage());
        }
    }

    private static String readEvents(Configuration config, String json) throws Exception {
        XMLStreamReader reader = new MappedXMLInputFactory(config).createXMLStreamReader(new StringReader(json));
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
}
