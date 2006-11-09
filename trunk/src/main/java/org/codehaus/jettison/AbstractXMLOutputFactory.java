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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;


public abstract class AbstractXMLOutputFactory extends XMLOutputFactory {

    public XMLEventWriter createXMLEventWriter(OutputStream arg0, String arg1) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

    public XMLEventWriter createXMLEventWriter(OutputStream out) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

    public XMLEventWriter createXMLEventWriter(Result arg0) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

    public XMLEventWriter createXMLEventWriter(Writer arg0) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

    public XMLStreamWriter createXMLStreamWriter(OutputStream out, String charset) throws XMLStreamException {
        if (charset == null) {
            charset = "UTF-8";
        }
        try {
            return createXMLStreamWriter(new OutputStreamWriter(out, charset));
        } catch (UnsupportedEncodingException e) {
            throw new XMLStreamException(e);
        }
    }

    public XMLStreamWriter createXMLStreamWriter(OutputStream out) throws XMLStreamException {
        return createXMLStreamWriter(out, null);
    }

    public XMLStreamWriter createXMLStreamWriter(Result arg0) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }

    public abstract XMLStreamWriter createXMLStreamWriter(Writer writer) throws XMLStreamException;

    public Object getProperty(String arg0) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isPropertySupported(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public void setProperty(String arg0, Object arg1) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        
    }

}
