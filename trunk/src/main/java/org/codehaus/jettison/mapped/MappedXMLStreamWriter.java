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

import java.io.IOException;
import java.io.Writer;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;

import org.codehaus.jettison.AbstractXMLStreamWriter;
import org.codehaus.jettison.util.FastStack;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MappedXMLStreamWriter extends AbstractXMLStreamWriter {
    MappedNamespaceConvention convention;
    JSONObject root;
    Object current;
    Writer writer;
    FastStack nodes = new FastStack();
    String currentKey;
    int depth = 0;
    NamespaceContext ctx = new NullNamespaceContext();
    
    public MappedXMLStreamWriter(MappedNamespaceConvention convention, Writer writer) {
        super();
        this.convention = convention;
        this.writer = writer;
    }

    public void close() throws XMLStreamException {
        
    }

    public void flush() throws XMLStreamException {

    }

    public NamespaceContext getNamespaceContext() {
        return ctx;
    }

    public String getPrefix(String arg0) throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getProperty(String arg0) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setDefaultNamespace(String arg0) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        this.ctx = context;
    }

    public void setPrefix(String arg0, String arg1) throws XMLStreamException {

    }

    public void writeAttribute(String p, String ns, String local, String value) throws XMLStreamException {
        if (convention.isElement(p, ns, local)) {
            writeStartElement(p, local, ns);
            writeCharacters(value);
            writeEndElement();
            return;
        }
        
        String key = convention.createAttributeKey(p, ns, local);
        try {
            makeCurrentJSONObject();
            
            Object o = ((JSONObject) current).opt(key);
            if (o == null) {
                ((JSONObject) current).put(key, value);
            }
        } catch (JSONException e) {
            throw new XMLStreamException(e);
        }
    }

    private void makeCurrentJSONObject() throws JSONException {
        if (current.equals("")) {
            JSONObject newCurrent = new JSONObject();
            setNewValue(newCurrent);
            current = newCurrent;
            nodes.push(newCurrent);
        }
    }

    private void setNewValue(Object newCurrent) throws JSONException {
        if (current instanceof String) {
            setNewValue(newCurrent, nodes.peek());
        } else {
            setNewValue(newCurrent, current);
        }
    }
    
    private void setNewValue(Object newCurrent, Object node) throws JSONException {
        if (node instanceof JSONObject) {
            ((JSONObject) node).put(currentKey, newCurrent);
        } else if (node instanceof JSONArray) {
            JSONArray arr = ((JSONArray) node);
            arr.put(arr.length() - 1, newCurrent);
        }
        current = newCurrent;
    }

    public void writeAttribute(String ns, String local, String value) throws XMLStreamException {
        writeAttribute(null, ns, local, value);
    }

    public void writeAttribute(String local, String value) throws XMLStreamException {
        writeAttribute(null, local, value);
    }

    public void writeCharacters(String text) throws XMLStreamException {
        try {
            if (current instanceof String) {
                current = current + text;
                setNewValue(current);
            } else if (current instanceof JSONArray) {
                JSONArray arr = (JSONArray) current;
                if (arr.get(arr.length()-1).equals("")) {
                	arr.put(arr.length()-1, text);
                } else {
                	arr.put(text);
                }
                current = text;
            }
        } catch (JSONException e) {
            throw new XMLStreamException(e);
        }
    }

    public void writeComment(String arg0) throws XMLStreamException {
    }

    public void writeDefaultNamespace(String ns) throws XMLStreamException {
        // TODO
    }

    public void writeDTD(String arg0) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    public void writeEndDocument() throws XMLStreamException {
        try {
            root.write(writer);
            writer.flush();
        } catch (JSONException e) {
            throw new XMLStreamException(e);
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    public void writeEndElement() throws XMLStreamException {
        if (current instanceof String) {
            current = nodes.peek();
        } else if (nodes.size() > 1 ) {
            nodes.pop();
            current = nodes.peek();
            if (current instanceof JSONArray) {
                nodes.pop();
                current = nodes.peek();
            }
        }
        depth--;
    }

    public void writeEntityRef(String arg0) throws XMLStreamException {
    }

    public void writeNamespace(String arg0, String arg1) throws XMLStreamException {
    }

    public void writeProcessingInstruction(String arg0, String arg1) throws XMLStreamException {
    }

    public void writeProcessingInstruction(String arg0) throws XMLStreamException {
    }

    public void writeStartDocument() throws XMLStreamException {
    }

    public void writeStartElement(String prefix, String local, String ns) throws XMLStreamException {
        depth++;
        
        try {
            if (current == null) {
                root = new JSONObject();
                current = root;
                nodes.push(root);
            } else {
                makeCurrentJSONObject(); 
            }
            
            currentKey = convention.createKey(prefix, ns, local);
            if (current instanceof JSONArray) {
            	JSONArray array = (JSONArray)current;
            	if (array.get(array.length()-1).equals("")) {             	
            		JSONObject newNode = new JSONObject();
            		newNode.put(currentKey, "");
            		setNewValue(newNode);
            		nodes.push(newNode);
            		current = "";
            	}
            } else {
                Object o = ((JSONObject) current).opt(currentKey);
                // hack to support nested arrays
                if (o == null && nodes.size() > 2) {
                	Object next = nodes.get(nodes.size() - 2);
                	if (next instanceof JSONObject) {
                		Object maybe = ((JSONObject)next).opt(currentKey);
                		if (maybe != null && maybe instanceof JSONObject) {
                			o = maybe;
                			nodes.pop();
                			current = nodes.pop();               			
                		}
                			
                	}
                }
                if (o instanceof JSONObject || o instanceof String) {
                    JSONArray arr = new JSONArray();
                    arr.put(o);
                    arr.put("");
                    setNewValue(arr);
                    current = arr;
                    nodes.push(arr);
                } else if (o instanceof JSONArray) {
                    current = "";
                    ((JSONArray) o).put("");
                    nodes.push(o);
                } else {
                    setNewValue("");
                    current = "";
                }
            }
        } catch (JSONException e) {
            throw new XMLStreamException("Could not write start element!", e);
        }
    }
}
