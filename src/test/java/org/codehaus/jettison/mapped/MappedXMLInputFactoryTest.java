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

import java.io.ByteArrayInputStream;
import java.io.StringReader;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.codehaus.jettison.JSONSequenceTooLargeException;
import org.codehaus.jettison.json.JSONTokener;

public class MappedXMLInputFactoryTest extends TestCase {

    private static final String JSON = "{\"root\":{\"a\":1,\"b\":2,\"c\":3}}";

    /**
     * A factory that uses a tokener with an object key limit of 2, and counts
     * how many tokeners it creates.
     */
    private static final class LimitingFactory extends MappedXMLInputFactory {
        private int tokenersCreated;

        LimitingFactory() {
            super(new Configuration());
        }

        @Override
        protected JSONTokener createNewJSONTokener(String doc) {
            tokenersCreated++;
            return new JSONTokener(doc, 2);
        }
    }

    public void testCustomTokenerIsUsedForInputStream() throws Exception {
        LimitingFactory factory = new LimitingFactory();
        assertKeyLimitEnforced(factory, () -> factory.createXMLStreamReader(stream()));
        assertKeyLimitEnforced(factory, () -> factory.createXMLStreamReader(stream(), "UTF-8"));
        assertKeyLimitEnforced(factory, () -> factory.createXMLStreamReader("urn:test", stream()));
    }

    public void testCustomTokenerIsUsedForReader() throws Exception {
        LimitingFactory factory = new LimitingFactory();
        assertKeyLimitEnforced(factory, () -> factory.createXMLStreamReader(new StringReader(JSON)));
        assertKeyLimitEnforced(factory, () -> factory.createXMLStreamReader("urn:test", new StringReader(JSON)));
    }

    public void testCustomTokenerIsUsedForSource() throws Exception {
        LimitingFactory factory = new LimitingFactory();
        assertKeyLimitEnforced(factory, () -> factory.createXMLStreamReader(new StreamSource(stream())));
        assertKeyLimitEnforced(factory, () -> factory.createXMLStreamReader(new StreamSource(new StringReader(JSON))));
    }

    public void testDefaultTokenerAcceptsDocument() throws Exception {
        MappedXMLInputFactory factory = new MappedXMLInputFactory(new Configuration());
        assertNotNull(factory.createXMLStreamReader(new StringReader(JSON)));
        assertNotNull(factory.createXMLStreamReader(stream()));
    }

    private static ByteArrayInputStream stream() throws Exception {
        return new ByteArrayInputStream(JSON.getBytes("UTF-8"));
    }

    private interface ReaderCreation {
        void create() throws Exception;
    }

    private static void assertKeyLimitEnforced(LimitingFactory factory, ReaderCreation creation)
        throws Exception {
        int before = factory.tokenersCreated;
        try {
            creation.create();
            fail("Failure expected when the custom tokener's object key limit is exceeded");
        } catch (XMLStreamException ex) {
            assertTrue(String.valueOf(ex.getCause()), ex.getCause() instanceof JSONSequenceTooLargeException);
        }
        assertEquals(before + 1, factory.tokenersCreated);
    }
}
