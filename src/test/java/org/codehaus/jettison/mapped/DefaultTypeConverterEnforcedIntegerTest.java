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

import junit.framework.TestCase;
import org.codehaus.jettison.AbstractXMLStreamWriter;
import java.io.StringWriter;

public class DefaultTypeConverterEnforcedIntegerTest extends TestCase {

    /**
     * @see DefaultTypeConverterUnenforcedIntegerTest
     */
    public void testPrimitiveEnforcedInteger() throws Exception {
        System.setProperty( "jettison.mapped.typeconverter.enforce_32bit_integer", "true" );
        assertTrue( DefaultConverter.ENFORCE_32BIT_INTEGER );
        StringWriter strWriter = new StringWriter();
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, strWriter);

        w.writeStartDocument();
        w.writeStartElement("root");

        w.writeStartElement("subchild1");
        w.writeCharacters(Integer.MAX_VALUE + "");
        w.writeEndElement();

        w.writeStartElement("subchild1");
        w.writeCharacters(Integer.MIN_VALUE + "");
        w.writeEndElement();

        w.writeStartElement("subchild1");
        w.writeCharacters(Long.MAX_VALUE + "");
        w.writeEndElement();

        w.writeStartElement("subchild1");
        w.writeCharacters(Long.MIN_VALUE + "");
        w.writeEndElement();

        w.writeEndElement();
        w.writeEndDocument();

        w.close();
        strWriter.close();
        String expected = "{\"root\":{\"subchild1\":[" + Integer.MAX_VALUE + "," + Integer.MIN_VALUE + ",\"" + Long.MAX_VALUE + "\",\"" + Long.MIN_VALUE + "\"]}}";
        String actual = strWriter.toString();
        assertEquals(expected, actual);
    }

    

}