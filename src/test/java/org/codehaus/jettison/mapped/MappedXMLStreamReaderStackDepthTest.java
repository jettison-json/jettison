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
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

/**
 * Empty arrays produce no events. Moving past them must not use stack space
 * in proportion to the number of them, or the input could exhaust the stack.
 */
public class MappedXMLStreamReaderStackDepthTest extends TestCase {

    private static final int COUNT = 100000;

    public void testManySiblingEmptyArrays() throws Exception {
        StringBuilder json = new StringBuilder("{\"r\":{");
        for (int i = 0; i < COUNT; i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append("\"a").append(i).append("\":[]");
        }
        json.append(",\"b\":\"x\"}}");
        assertEquals("<r><b>x</b></r>", readEvents(new Configuration(), json.toString()));
    }

    public void testManyEmptyArrayElements() throws Exception {
        assertEquals("<r><a>x</a></r>", readEvents(new Configuration(), arrayOfEmptyArrays()));
    }

    public void testManyEmptyArrayElementsWithPrimitiveArrayKeys() throws Exception {
        Configuration config = new Configuration();
        config.setPrimitiveArrayKeys(new HashSet<String>(Arrays.asList("a")));
        assertEquals("<r><a>x</a></r>", readEvents(config, arrayOfEmptyArrays()));
    }

    public void testDeeplyNestedArraysStartingWithEmptyArrays() throws Exception {
        // Nested within the parser's recursion depth limit
        StringBuilder json = new StringBuilder("{\"r\":{\"a\":");
        int depth = 497;
        for (int i = 0; i < depth; i++) {
            json.append("[[],");
        }
        json.append("\"x\"");
        for (int i = 0; i < depth; i++) {
            json.append(']');
        }
        json.append("}}");
        assertEquals("<r><a>x</a></r>", readEvents(new Configuration(), json.toString()));
    }

    public void testEmptyArraysProduceNoEvents() throws Exception {
        assertEquals("<r><b>x</b><d>1</d><d>2</d></r>",
            readEvents(new Configuration(), "{\"r\":{\"a\":[],\"b\":\"x\",\"c\":[],\"d\":[1,2],\"e\":[]}}"));
        assertEquals("<r><a>x</a><a>y</a></r>",
            readEvents(new Configuration(), "{\"r\":{\"a\":[[],[],\"x\",[],[\"y\"],[]]}}"));
    }

    private static String arrayOfEmptyArrays() {
        StringBuilder json = new StringBuilder("{\"r\":{\"a\":[");
        for (int i = 0; i < COUNT; i++) {
            json.append("[],");
        }
        json.append("\"x\"]}}");
        return json.toString();
    }

    private static String readEvents(Configuration config, String json) throws Exception {
        XMLStreamReader reader = new MappedXMLInputFactory(config).createXMLStreamReader(new StringReader(json));
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
