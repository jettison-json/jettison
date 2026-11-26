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
package org.codehaus.jettison.badgerfish;

import java.io.StringReader;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

/**
 * Empty arrays produce no events. Moving past them must not use stack space
 * in proportion to the number of them, or the input could exhaust the stack.
 */
public class BadgerFishXMLStreamReaderStackDepthTest extends TestCase {

    private static final int COUNT = 100000;

    public void testEmptyArrayValues() throws Exception {
        assertEquals("<r><b>x</b><d>y</d></r>",
            readEvents("{\"r\":{\"a\":[],\"b\":{\"$\":\"x\"},\"c\":[],\"d\":[{\"$\":\"y\"}],\"e\":[]}}"));
        assertEquals("<r><c></c></r>", readEvents("{\"r\":{\"a\":[],\"b\":[],\"c\":{\"@id\":\"1\"}}}"));
    }

    public void testManySiblingEmptyArrays() throws Exception {
        StringBuilder json = new StringBuilder("{\"r\":{");
        for (int i = 0; i < COUNT; i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append("\"a").append(i).append("\":[]");
        }
        json.append(",\"b\":{\"$\":\"x\"}}}");
        assertEquals("<r><b>x</b></r>", readEvents(json.toString()));
    }

    public void testManySiblingEmptyArraysBetweenElements() throws Exception {
        StringBuilder json = new StringBuilder("{\"r\":{");
        for (int i = 0; i < COUNT; i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append("\"a").append(i).append("\":[],");
            if (i % 1000 == 0) {
                json.append("\"b").append(i).append("\":{\"$\":\"x\"},");
            }
            json.append("\"c").append(i).append("\":[]");
        }
        json.append("}}");
        String events = readEvents(json.toString());
        assertTrue(events.startsWith("<r><b0>x</b0><b1000>x</b1000>"));
        assertTrue(events.endsWith("<b99000>x</b99000></r>"));
    }

    public void testNestedArrayIsStillRejected() throws Exception {
        try {
            readEvents("{\"r\":{\"a\":[{\"$\":\"x\"},[]]}}");
            fail("Failure expected on a nested array");
        } catch (XMLStreamException ex) {
            assertTrue(String.valueOf(ex.getMessage()), ex.getMessage().contains("nested array"));
        }
    }

    private static String readEvents(String json) throws Exception {
        XMLStreamReader reader = new BadgerFishXMLInputFactory().createXMLStreamReader(new StringReader(json));
        StringBuilder sb = new StringBuilder();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                sb.append('<').append(reader.getLocalName()).append('>');
            } else if (event == XMLStreamConstants.CHARACTERS) {
                sb.append(reader.getText());
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                sb.append("</").append(reader.getLocalName()).append('>');
            }
        }
        return sb.toString();
    }
}
