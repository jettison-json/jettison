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

import java.io.*;

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
import javax.xml.transform.stream.StreamSource;

import org.codehaus.jettison.json.JSONTokener;

public abstract class AbstractXMLInputFactory extends XMLInputFactory {

    final static int INPUT_BUF_SIZE = 512;

    public XMLEventReader createFilteredReader(XMLEventReader arg0, EventFilter arg1) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLStreamReader createFilteredReader(XMLStreamReader arg0, StreamFilter arg1) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLEventReader createXMLEventReader(InputStream arg0, String encoding) throws XMLStreamException {
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

    
    public XMLEventReader createXMLEventReader(String systemId, InputStream arg1) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLEventReader createXMLEventReader(String systemId, Reader arg1) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLEventReader createXMLEventReader(XMLStreamReader arg0) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    
    public XMLStreamReader createXMLStreamReader(InputStream is) throws XMLStreamException {
        return createXMLStreamReader(is, null);
    }

    public XMLStreamReader createXMLStreamReader(InputStream is, String charset) throws XMLStreamException {
        /* !!! This is not really correct: should (try to) auto-detect
         * encoding, since JSON only allows 3 Unicode-based variants.
         * For now it's ok to default to UTF-8 though.
         */
        if (charset == null) {
            charset = "UTF-8";
        }
        try {
            String doc = readAll(is, charset);
            return createXMLStreamReader(new JSONTokener(doc));
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * This helper method tries to read and decode input efficiently
     * into a result String.
     */
    private String readAll(InputStream in, String encoding)
        throws IOException
    {
        final byte[] buffer = new byte[INPUT_BUF_SIZE];
        ByteArrayOutputStream bos = null;
        while (true) {
            int count = in.read(buffer);
            if (count < 0) { // EOF
                break;
            }
            /* Let's create buffer lazily, to be able to create something
             * that's not too small (many resizes) or too big (slower
             * to allocate): mostly to speed up handling of tiny docs.
             */
            if (bos == null) {
                int cap;
                if (count < 64) {
                    cap = 64;
                } else if (count == INPUT_BUF_SIZE) {
                    // Let's assume there's more coming, not just this chunk
                    cap = INPUT_BUF_SIZE * 4;
                } else {
                    cap = count;
                }
                bos = new ByteArrayOutputStream(cap);
            }
            bos.write(buffer, 0, count);
        }
        return (bos == null) ? "" : bos.toString(encoding);
    }
    
    public abstract XMLStreamReader createXMLStreamReader(JSONTokener tokener) throws XMLStreamException;

    public XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException {
    	StringWriter wrt = new StringWriter();
        try {
        	int len;
        	char[] buf = new char[1024];
            while ((len = reader.read(buf)) != -1) {
            	wrt.write(buf, 0, len);
            }
            return createXMLStreamReader(new JSONTokener(wrt.toString()));
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    
    public XMLStreamReader createXMLStreamReader(Source src) throws XMLStreamException
    {
        // Can only support simplest of sources:
        if (src instanceof StreamSource) {
            StreamSource ss = (StreamSource) src;
            InputStream in = ss.getInputStream();
            String systemId = ss.getSystemId();
            if (in != null) {
                if (systemId != null) {
                    return createXMLStreamReader(systemId, in);
                }
                return createXMLStreamReader(in);
            }
            Reader r = ss.getReader();
            if (r != null) {
                if (systemId != null) {
                    return createXMLStreamReader(systemId, r);
                }
                return createXMLStreamReader(r);
            }
            throw new UnsupportedOperationException("Only those javax.xml.transform.stream.StreamSource instances supported that have an InputStream or Reader");
        }
        throw new UnsupportedOperationException("Only javax.xml.transform.stream.StreamSource type supported");
    }

    
    public XMLStreamReader createXMLStreamReader(String systemId, InputStream arg1) throws XMLStreamException {
        // How (if) should the system id be used?
        return createXMLStreamReader(arg1, null);
    }

    
    public XMLStreamReader createXMLStreamReader(String systemId, Reader r) throws XMLStreamException {
        return createXMLStreamReader(r);
    }

    
    public XMLEventAllocator getEventAllocator() {
        // TODO Auto-generated method stub
        return null;
    }

    
    public Object getProperty(String arg0) throws IllegalArgumentException {
        // TODO: should gracefully handle standard properties
        throw new IllegalArgumentException();
    }

    
    public XMLReporter getXMLReporter() {
        return null;
    }

    
    public XMLResolver getXMLResolver() {
        return null;
    }

    
    public boolean isPropertySupported(String arg0) {
        return false;
    }

    
    public void setEventAllocator(XMLEventAllocator arg0) {
        // TODO Auto-generated method stub
        
    }

    
    public void setProperty(String arg0, Object arg1) throws IllegalArgumentException {
        // TODO: should gracefully handle standard properties
        throw new IllegalArgumentException();
    }

    
    public void setXMLReporter(XMLReporter arg0) {
        // TODO Auto-generated method stub
        
    }

    
    public void setXMLResolver(XMLResolver arg0) {
        // TODO Auto-generated method stub
        
    }

}
