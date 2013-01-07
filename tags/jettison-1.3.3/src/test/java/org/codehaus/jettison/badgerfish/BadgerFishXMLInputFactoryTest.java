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

import java.io.StringReader;

import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

public class BadgerFishXMLInputFactoryTest extends TestCase {
   
    private static final String XML_JSON =
        "{\"d:root\":{\"child\":{\"@xsi:type\":\"d:ChildType\",\"name\":{\"$\":\"Dummy\"}},\"@xmlns\":{\"d\":\"http:\\/\\/www.example.com\\/dummy\",\"xsi\":\"http:\\/\\/www.w3.org\\/2001\\/XMLSchema-instance\"}}}";

    public void testRoundTrip() throws Exception {

        BadgerFishXMLInputFactory xif = new BadgerFishXMLInputFactory();
        XMLStreamReader reader = xif.createXMLStreamReader(new StringReader(XML_JSON));
        while (reader.hasNext()) {
            reader.next();
        }
    }
}
