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
package org.codehaus.jettison;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.CharBuffer;

import javax.xml.stream.EventFilter;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;

import org.json.JSONTokener;

public abstract class AbstractXMLInputFactory extends XMLInputFactory {

    public XMLEventReader createFilteredReader(XMLEventReader arg0, EventFilter arg1) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLStreamReader createFilteredReader(XMLStreamReader arg0, StreamFilter arg1) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLEventReader createXMLEventReader(InputStream arg0, String arg1) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLEventReader createXMLEventReader(InputStream arg0) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLEventReader createXMLEventReader(Reader arg0) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLEventReader createXMLEventReader(Source arg0) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLEventReader createXMLEventReader(String arg0, InputStream arg1) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLEventReader createXMLEventReader(String arg0, Reader arg1) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLEventReader createXMLEventReader(XMLStreamReader arg0) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLStreamReader createXMLStreamReader(InputStream is) throws XMLStreamException {
        return createXMLStreamReader(is, "UTF-8");
    }

    public XMLStreamReader createXMLStreamReader(InputStream is, String charset) throws XMLStreamException {
        try {
            if (charset == null) {
                charset = "UTF-8";
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            copy(is, bos, 1024);
            return createXMLStreamReader(new JSONTokener(new String(bos.toByteArray(), charset)));
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }
    
    public static void copy(final InputStream input,
                            final OutputStream output,
                            final int bufferSize)
        throws IOException {
        final byte[] buffer = new byte[bufferSize];
        int n = 0;
        n = input.read(buffer);
        while (-1 != n) {
            output.write(buffer, 0, n);
            n = input.read(buffer);
        }
    }
    
    public abstract XMLStreamReader createXMLStreamReader(JSONTokener tokener) throws XMLStreamException;

    public XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
        CharBuffer cb = CharBuffer.allocate(4096);
        try {
            while (reader.read(cb.array()) != -1) {
                // keep reading
            }
            return createXMLStreamReader(new JSONTokener(cb.toString()));
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    
    public XMLStreamReader createXMLStreamReader(Source arg0) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

    
    public XMLStreamReader createXMLStreamReader(String arg0, InputStream arg1) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLStreamReader createXMLStreamReader(String arg0, Reader arg1) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLEventAllocator getEventAllocator() {
        // TODO Auto-generated method stub
        return null;
    }

    
    public Object getProperty(String arg0) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLReporter getXMLReporter() {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLResolver getXMLResolver() {
        // TODO Auto-generated method stub
        return null;
    }

    
    public boolean isPropertySupported(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    
    public void setEventAllocator(XMLEventAllocator arg0) {
        // TODO Auto-generated method stub
        
    }

    
    public void setProperty(String arg0, Object arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        
    }

    
    public void setXMLReporter(XMLReporter arg0) {
        // TODO Auto-generated method stub
        
    }

    
    public void setXMLResolver(XMLResolver arg0) {
        // TODO Auto-generated method stub
        
    }

}
