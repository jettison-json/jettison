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
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;

import org.codehaus.jettison.AbstractXMLStreamWriter;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.util.FastStack;

public class MappedXMLStreamWriter extends AbstractXMLStreamWriter {
    MappedNamespaceConvention convention;
    JSONObject root;
    Object current;
    Writer writer;
    FastStack nodes = new FastStack();
    String currentKey;
    NamespaceContext ctx = new NullNamespaceContext();
    String valueKey = "$";
    
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
			if (current instanceof JSONArray) {
				JSONArray array = ((JSONArray) current);
				current = array.get(array.length() - 1);
			}

			makeCurrentJSONObject();
			if (current instanceof JSONObject) {
				Object o = ((JSONObject) current).opt(key);
				if (o == null) {
					((JSONObject) current).put(key, value);
				}
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
        if (isJsonPrimitive(current)) {
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
            Object convertedPrimitive = convention.convertToJSONPrimitive(text);
            if (isJsonPrimitive(current)) {
            	//Concatenate strings
            	if(current instanceof String && convertedPrimitive instanceof String) {
            		current = current + convertedPrimitive.toString();
            	} //Overwrite other primitive type values
            	else {
            		current = convertedPrimitive;
            	}
                setNewValue(current);
            } else if (current instanceof JSONArray) {
                JSONArray arr = (JSONArray) current;
                if (arr.get(arr.length()-1).equals("")) {
                	arr.put(arr.length()-1, convertedPrimitive);
                } else {
                	arr.put(convertedPrimitive);
                }
                current = convertedPrimitive;
            } // handle value in nodes with attributes
            else if (current instanceof JSONObject && valueKey != null){
            	JSONObject obj = (JSONObject)current;
            	obj.put(valueKey, text);
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
        if (isJsonPrimitive(current)) {
            current = nodes.peek();
        } else if (nodes.size() > 1 ) {
            
            if (isEmptyArray(current)) {
                current = nodes.peek();
            } else {
                Object isArray = nodes.pop();
                current = nodes.peek();
                if (current instanceof JSONArray || isArray instanceof JSONArray) {
                    nodes.pop();
                    if (nodes.size() > 0) {
                        current = nodes.peek();
                    }
                }
            }
        }
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
        
        try {
            if (current == null) {
                root = new JSONObject();
                current = root;
                nodes.push(root);
            } else {
                makeCurrentJSONObject();
            }
            String previousKey = currentKey;
            currentKey = convention.createKey(prefix, ns, local);
            if (current instanceof JSONArray) {
            	JSONArray array = (JSONArray)current;
            	if (array.get(array.length() - 1).equals("")) {
            	    // this is in case when the first element of an array is another array
                    if (getSerializedAsArrays().contains(currentKey)) {
                        JSONObject newNode = new JSONObject();
                        newNode.put(currentKey, "");
                        setNewValue(newNode);
                        nodes.push(newNode);
                        current = "";
                        JSONArray arr = new JSONArray();
                        arr.put("");
                        setNewValue(arr);

                    } else {
                        JSONObject newNode = new JSONObject();
                        newNode.put(currentKey, "");
                        setNewValue(newNode);
                        nodes.push(newNode);
                        current = "";
                    }
            	} else {
            		if (array.get(array.length() - 1) instanceof JSONObject) {
            			array.put("");
            			nodes.push(array);
            		} else if (!currentKey.equals(previousKey)) {
            			// element with new name shouldn't be in the array
            			int i = 0;
            			while (current instanceof JSONObject == false){
            				if (i>0){ nodes.pop(); }
            				current = nodes.peek();
            				i++;
            			}
                        setNewValue("");
                        current = "";
            		}
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
                if (o instanceof JSONObject || isJsonPrimitive(o)) {
                	// JETTISON-65
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
                } else if (getSerializedAsArrays().contains(currentKey)) {
                    JSONArray arr = new JSONArray();
                    arr.put("");
                    setNewValue(arr);
                } else {
                    setNewValue("");
                    current = "";
                }
            }
        } catch (JSONException e) {
            throw new XMLStreamException("Could not write start element!", e);
        }
    }

    private boolean isJsonPrimitive(Object o) {
		if (o instanceof String || o instanceof Boolean || o instanceof Number) {
			return true;
		} else {
			return false;
		}
	}

    private boolean isEmptyArray(Object o) throws XMLStreamException {
        if (o instanceof JSONArray) {
            JSONArray arr = (JSONArray) o;
            if (arr.length() == 1) {
                try {
                    return arr.get(0).equals("");
                } catch (JSONException e) {
                    throw new XMLStreamException("Could not read array value!", e);
                }
            }
        }
        return false;
    }

	public void setValueKey(String valueKey) {
		this.valueKey = valueKey;
	}


}
