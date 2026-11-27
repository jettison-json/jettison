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
package org.codehaus.jettison;

import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedXMLInputFactory;

/**
 * StAX consumers such as JAXB read the attributes and namespace declarations of
 * an element by index. Reading all of them must take time proportional to their
 * number, not to its square, as the number is controlled by the JSON input.
 */
public class NodeAttributeIndexTest extends TestCase {

    public void testIndexedAccessorsFollowAttributeOrder() throws Exception {
        XMLStreamReader reader = readerAtRoot("{\"root\":{\"@b\":\"2\",\"@a\":\"1\",\"@c\":\"3\",\"child\":\"x\"}}");
        assertEquals(3, reader.getAttributeCount());
        assertEquals("b", reader.getAttributeLocalName(0));
        assertEquals("2", reader.getAttributeValue(0));
        assertEquals("a", reader.getAttributeLocalName(1));
        assertEquals("1", reader.getAttributeValue(1));
        assertEquals("c", reader.getAttributeLocalName(2));
        assertEquals("3", reader.getAttributeValue(2));
        assertEquals("1", reader.getAttributeValue("", "a"));
    }

    public void testOutOfRangeIndexes() throws Exception {
        XMLStreamReader reader = readerAtRoot("{\"root\":{\"@a\":\"1\"}}");
        // unchanged from the earlier iterator-based implementation
        assertNull(reader.getAttributeName(-1));
        assertNull(reader.getAttributeValue(-1));
        try {
            reader.getAttributeName(1);
            fail("Failure expected for an index past the last attribute");
        } catch (NoSuchElementException ex) {
            // expected
        }
        try {
            reader.getAttributeValue(1);
            fail("Failure expected for an index past the last attribute");
        } catch (NoSuchElementException ex) {
            // expected
        }
        try {
            reader.getNamespacePrefix(0);
            fail("Failure expected for an index past the last namespace declaration");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    public void testIndexFollowsChangesToTheMaps() throws Exception {
        Node node = new Node(new JSONObject());
        node.setAttribute(new QName("a"), "1");
        assertEquals(new QName("a"), node.getAttributeName(0));
        assertEquals("1", node.getAttributeValue(0));

        node.setAttribute(new QName("b"), "2");
        assertEquals(new QName("b"), node.getAttributeName(1));
        assertEquals("2", node.getAttributeValue(1));

        // changes made through the exposed map are seen too
        node.getAttributes().put(new QName("c"), "3");
        assertEquals(new QName("c"), node.getAttributeName(2));
        node.getAttributes().put(new QName("a"), "changed");
        assertEquals("changed", node.getAttributeValue(0));

        node.setNamespace("p", "urn:p");
        assertEquals("p", node.getNamespacePrefix(0));
        assertEquals("urn:p", node.getNamespaceURI(0));
        node.setNamespace("q", "urn:q");
        assertEquals("q", node.getNamespacePrefix(1));

        Map<String, String> namespaces = new LinkedHashMap<String, String>();
        namespaces.put("r", "urn:r");
        node.setNamespaces(namespaces);
        assertEquals(1, node.getNamespaceCount());
        assertEquals("r", node.getNamespacePrefix(0));
        assertEquals("urn:r", node.getNamespaceURI(0));
    }

    public void testIndexedAccessDoesNotWalkTheMapsOnEveryCall() throws Exception {
        Node node = new Node(new JSONObject());
        CountingMap attributes = new CountingMap();
        CountingMap namespaces = new CountingMap();
        for (int i = 0; i < 100; i++) {
            attributes.put(new QName("a" + i), "v" + i);
            namespaces.put("p" + i, "urn:" + i);
        }
        node.attributes = attributes;
        node.setNamespaces(namespaces);

        for (int i = 0; i < 100; i++) {
            assertEquals(new QName("a" + i), node.getAttributeName(i));
            assertEquals("v" + i, node.getAttributeValue(i));
            assertEquals("p" + i, node.getNamespacePrefix(i));
            assertEquals("urn:" + i, node.getNamespaceURI(i));
        }
        assertTrue("attributes walked " + attributes.walks + " times", attributes.walks <= 1);
        assertTrue("namespaces walked " + namespaces.walks + " times", namespaces.walks <= 1);
    }

    public void testManyAttributeKeysAreSkippedWithASet() throws Exception {
        StringBuilder json = new StringBuilder("{");
        for (int i = 0; i < 20; i++) {
            json.append("\"@a").append(i).append("\":\"v\",");
        }
        json.append("\"b\":\"x\",\"c\":\"y\"}");
        Node node = new Node(null, "root", new JSONObject(json.toString()),
            new org.codehaus.jettison.mapped.MappedNamespaceConvention());

        // a list would make each lookup linear, and reading the object quadratic
        assertTrue(node.skippedKeys instanceof Set);
        assertEquals(20, node.getAttributes().size());
        assertEquals("b", node.getKeys().next());
        assertEquals("c", node.getKeys().next());
        assertFalse(node.getKeys().hasNext());
    }

    public void testManyAttributes() throws Exception {
        int count = 50000;
        StringBuilder json = new StringBuilder("{\"root\":{");
        for (int i = 0; i < count; i++) {
            json.append("\"@k").append(i).append("\":\"v").append(i).append("\",");
        }
        json.append("\"child\":\"x\"}}");
        XMLStreamReader reader = readerAtRoot(json.toString());
        assertEquals(count, reader.getAttributeCount());
        for (int i = 0; i < count; i++) {
            assertEquals("k" + i, reader.getAttributeLocalName(i));
            assertEquals("v" + i, reader.getAttributeValue(i));
        }
    }

    public void testManyAttributesWithCollidingHashCodes() throws Exception {
        // Attribute names made of "Aa" and "BB" blocks all have the same hash
        // code. QName isn't Comparable, so a HashMap can't keep colliding names
        // in a tree, and reading them took time quadratic in their number
        int count = 1 << 15;
        StringBuilder json = new StringBuilder("{\"root\":{");
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            StringBuilder name = new StringBuilder("a");
            for (int b = 0; b < 15; b++) {
                name.append(((i >> b) & 1) == 0 ? "Aa" : "BB");
            }
            names[i] = name.toString();
            json.append("\"@").append(names[i]).append("\":\"v").append(i).append("\",");
        }
        json.append("\"child\":\"x\"}}");
        XMLStreamReader reader = readerAtRoot(json.toString());
        assertEquals(count, reader.getAttributeCount());
        for (int i = 0; i < count; i++) {
            assertEquals(names[i], reader.getAttributeLocalName(i));
            assertEquals("v" + i, reader.getAttributeValue("", names[i]));
        }
    }

    public void testPlainQNameLookupsWithCollidingHashCodes() throws Exception {
        // Code using the attribute map of a node with plain QNames also gets
        // the protection against colliding hash codes
        int count = 1 << 15;
        StringBuilder json = new StringBuilder("{");
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            StringBuilder name = new StringBuilder("a");
            for (int b = 0; b < 15; b++) {
                name.append(((i >> b) & 1) == 0 ? "Aa" : "BB");
            }
            names[i] = name.toString();
            json.append("\"@").append(names[i]).append("\":\"v").append(i).append("\",");
        }
        json.append("\"child\":\"x\"}");
        Node node = new Node(null, "root", new JSONObject(json.toString()),
            new org.codehaus.jettison.mapped.MappedNamespaceConvention());
        Map attributes = node.getAttributes();
        for (int i = 0; i < count; i++) {
            QName name = new QName(names[i]);
            assertTrue(attributes.containsKey(name));
            assertEquals("v" + i, attributes.get(name));
            assertEquals("v" + i, attributes.getOrDefault(name, null));
        }
        // and names put with a plain QName are kept in order and found
        for (int i = 0; i < count; i++) {
            attributes.put(new QName("urn:x", names[i]), "w" + i);
        }
        assertEquals(2 * count, attributes.size());
        assertEquals(new QName("urn:x", names[0]), node.getAttributeName(count));
        assertEquals("w1", attributes.get(new QName("urn:x", names[1])));
        assertEquals("w1", attributes.remove(new QName("urn:x", names[1])));
        assertNull(attributes.get(new QName("urn:x", names[1])));
    }

    public void testAttributeNamesEqualQNames() throws Exception {
        XMLStreamReader reader = readerAtRoot("{\"root\":{\"@a\":\"1\",\"child\":\"x\"}}");
        QName name = reader.getAttributeName(0);
        assertEquals(new QName("a"), name);
        assertEquals(new QName("a").hashCode(), name.hashCode());
        assertEquals("1", reader.getAttributeValue(null, "a"));
        // the attribute map is found by a plain QName too
        Node node = new Node(new JSONObject());
        node.setAttribute(new QName("urn:x", "b", "p"), "2");
        assertEquals("2", node.getAttributes().get(new QName("urn:x", "b")));
        assertEquals("p", node.getAttributeName(0).getPrefix());
    }

    private static XMLStreamReader readerAtRoot(String json) throws Exception {
        XMLStreamReader reader = new MappedXMLInputFactory(new Configuration())
            .createXMLStreamReader(new StringReader(json));
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
        return reader;
    }

    /**
     * Counts how often the key set or values of the map are requested, as the
     * earlier implementation did on every indexed access.
     */
    private static final class CountingMap extends LinkedHashMap<Object, Object> {
        private static final long serialVersionUID = 1L;
        int walks;

        @Override
        public Set<Object> keySet() {
            walks++;
            return super.keySet();
        }

        @Override
        public Collection<Object> values() {
            walks++;
            return super.values();
        }

        @Override
        public Set<Map.Entry<Object, Object>> entrySet() {
            walks++;
            return super.entrySet();
        }
    }
}
