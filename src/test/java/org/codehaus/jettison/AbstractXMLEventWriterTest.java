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
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamWriter;

public class AbstractXMLEventWriterTest extends TestCase {

    public void testAddEventReaderDoesNotCloseWriter() throws Exception {
        StringWriter output = new StringWriter();
        final XMLStreamWriter streamWriter = new BadgerFishXMLStreamWriter(output);
        final int[] closeCount = {0};
        XMLStreamWriter countingWriter = (XMLStreamWriter) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[] {XMLStreamWriter.class},
            (proxy, method, args) -> {
                if ("close".equals(method.getName())) {
                    closeCount[0]++;
                }
                try {
                    return method.invoke(streamWriter, args);
                } catch (InvocationTargetException ex) {
                    throw ex.getCause();
                }
            });

        XMLEventWriter eventWriter = new AbstractXMLEventWriter(countingWriter);
        XMLEventReader eventReader = XMLInputFactory.newInstance()
            .createXMLEventReader(new StringReader("<a>x</a>"));
        eventWriter.add(eventReader);

        assertEquals(0, closeCount[0]);
        assertEquals("{\"a\":{\"$\":\"x\"}}", output.toString());

        // closing the event writer closes the underlying stream writer
        eventWriter.close();
        assertEquals(1, closeCount[0]);
    }
}
