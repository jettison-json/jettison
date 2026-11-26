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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.codehaus.jettison.badgerfish.BadgerFishXMLInputFactory;
import org.codehaus.jettison.json.JSONObject;

/**
 * The NamespaceContext of a reader finds the prefixes in scope for a namespace
 * URI. A lookup mustn't scan every declaration in scope, as the declarations
 * are controlled by the JSON input and consumers may look up the namespace of
 * every attribute.
 */
public class NamespaceContextTest extends TestCase {

    public void testPrefixesInScope() throws Exception {
        XMLStreamReader reader = reader("{\"root\":{\"@xmlns\":{\"p\":\"urn:a\",\"q\":\"urn:b\",\"r\":\"urn:a\"},"
            + "\"child\":{\"@xmlns\":{\"s\":\"urn:a\",\"q\":\"urn:a\",\"r\":\"urn:c\"},\"$\":\"x\"}}}");
        nextStartElement(reader);
        NamespaceContext ctx = reader.getNamespaceContext();
        assertEquals("p", ctx.getPrefix("urn:a"));
        assertEquals(list("p", "r"), list(ctx.getPrefixes("urn:a")));
        assertEquals(list("q"), list(ctx.getPrefixes("urn:b")));

        nextStartElement(reader);
        // the child's own declarations come first, and r is redeclared to another URI
        assertEquals("s", ctx.getPrefix("urn:a"));
        assertEquals(list("s", "q", "p"), list(ctx.getPrefixes("urn:a")));
        assertEquals(list(), list(ctx.getPrefixes("urn:b")));
        assertEquals(list("r"), list(ctx.getPrefixes("urn:c")));
        assertEquals("urn:a", ctx.getNamespaceURI("q"));
    }

    public void testDefaultNamespaceAndUnknownURIs() throws Exception {
        XMLStreamReader reader = reader("{\"root\":{\"@xmlns\":{\"$\":\"urn:d\",\"p\":\"urn:d\"},\"$\":\"x\"}}");
        nextStartElement(reader);
        NamespaceContext ctx = reader.getNamespaceContext();
        assertEquals(list("", "p"), list(ctx.getPrefixes("urn:d")));
        assertNull(ctx.getPrefix("urn:unknown"));
        assertFalse(ctx.getPrefixes("urn:unknown").hasNext());
        assertNull(ctx.getPrefix(null));
        assertFalse(ctx.getPrefixes(null).hasNext());
    }

    public void testPrefixesIterator() throws Exception {
        XMLStreamReader reader = reader("{\"root\":{\"@xmlns\":{\"p\":\"urn:a\"},\"$\":\"x\"}}");
        nextStartElement(reader);
        Iterator<String> prefixes = reader.getNamespaceContext().getPrefixes("urn:a");
        assertEquals("p", prefixes.next());
        try {
            prefixes.remove();
            fail("The prefixes can't be modified");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
        assertFalse(prefixes.hasNext());
        try {
            prefixes.next();
            fail("Failure expected past the last prefix");
        } catch (NoSuchElementException ex) {
            // expected
        }
    }

    public void testLookupsDontScanEveryDeclarationInScope() throws Exception {
        int count = 1000;
        // the root binds many prefixes, and then q, to urn:u; the child rebinds
        // the many prefixes, so each lookup of urn:u at the child has to skip them
        CountingMap rootNamespaces = new CountingMap();
        CountingMap childNamespaces = new CountingMap();
        for (int i = 0; i < count; i++) {
            rootNamespaces.put("p" + i, "urn:u");
            childNamespaces.put("p" + i, "urn:v");
        }
        rootNamespaces.put("q", "urn:u");
        Node root = new Node(new JSONObject());
        root.setNamespaces(rootNamespaces);
        Node child = new Node(new JSONObject());
        child.parent = root;
        child.setNamespaces(childNamespaces);

        for (int i = 0; i < count; i++) {
            assertEquals("q", child.getPrefixesInScope("urn:u").get(0));
        }
        int operations = rootNamespaces.operations + childNamespaces.operations;
        // scanning every declaration in scope for each lookup takes over a million
        assertTrue("map operations: " + operations, operations < 10 * count);
    }

    public void testManyRedeclaredPrefixes() throws Exception {
        int count = 20000;
        StringBuilder json = new StringBuilder("{\"root\":{\"@xmlns\":{");
        for (int i = 0; i < count; i++) {
            json.append("\"p").append(i).append("\":\"urn:u\",");
        }
        json.append("\"q\":\"urn:u\"},\"child\":{\"@xmlns\":{");
        for (int i = 0; i < count; i++) {
            json.append(i > 0 ? "," : "").append("\"p").append(i).append("\":\"urn:v\"");
        }
        json.append('}');
        for (int i = 0; i < count; i++) {
            json.append(",\"@q:a").append(i).append("\":\"v\"");
        }
        json.append("}}}");

        XMLStreamReader reader = reader(json.toString());
        nextStartElement(reader);
        nextStartElement(reader);
        NamespaceContext ctx = reader.getNamespaceContext();
        assertEquals(count, reader.getAttributeCount());
        for (int i = 0; i < count; i++) {
            assertEquals("q", ctx.getPrefix(reader.getAttributeNamespace(i)));
        }
    }

    private static XMLStreamReader reader(String json) throws Exception {
        return new BadgerFishXMLInputFactory().createXMLStreamReader(new StringReader(json));
    }

    private static void nextStartElement(XMLStreamReader reader) throws Exception {
        while (reader.next() != XMLStreamConstants.START_ELEMENT) {
            // skip to the next element
        }
    }

    private static List<String> list(String... values) {
        List<String> result = new ArrayList<String>();
        for (String value : values) {
            result.add(value);
        }
        return result;
    }

    private static List<String> list(Iterator<?> values) {
        List<String> result = new ArrayList<String>();
        while (values.hasNext()) {
            result.add((String) values.next());
        }
        return result;
    }

    /**
     * Counts the lookups and iterations of the map.
     */
    private static final class CountingMap extends LinkedHashMap<Object, Object> {
        private static final long serialVersionUID = 1L;
        int operations;

        @Override
        public Object get(Object key) {
            operations++;
            return super.get(key);
        }

        @Override
        public java.util.Set<Object> keySet() {
            operations += size();
            return super.keySet();
        }

        @Override
        public java.util.Collection<Object> values() {
            operations += size();
            return super.values();
        }

        @Override
        public java.util.Set<java.util.Map.Entry<Object, Object>> entrySet() {
            operations += size();
            return super.entrySet();
        }
    }
}
