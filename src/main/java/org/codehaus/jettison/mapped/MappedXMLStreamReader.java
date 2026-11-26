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

import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;

import org.codehaus.jettison.AbstractXMLStreamReader;
import org.codehaus.jettison.Convention;
import org.codehaus.jettison.Node;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.util.FastStack;

public class MappedXMLStreamReader extends AbstractXMLStreamReader {
    private FastStack nodes;
    private String currentValue;
    private MappedNamespaceConvention convention;
    private String valueKey = "$";
    private NamespaceContext ctx;
    private int popArrayNodes;
    // the root node, if the root element is read as an array with primitiveArrayKeys
    private Node uniformArrayRoot;
    private Node rootNode;
    // true if the root element is read from an array, wrapped by the root node
    private boolean rootArrayWrapper;
    public MappedXMLStreamReader(JSONObject obj)
            throws JSONException, XMLStreamException {
        this(obj, new MappedNamespaceConvention());
    }

    public MappedXMLStreamReader(JSONObject obj, MappedNamespaceConvention con)
            throws JSONException, XMLStreamException {
        if (obj.length() == 0) {
            throw new XMLStreamException("The JSON object has no root element");
        }
        String rootName = (String) obj.keys().next();

        this.convention = con;
        this.nodes = new FastStack();
        this.ctx = con;
        Object top = obj.get(rootName);
        // An empty element written as an array is serialized as [""]
        boolean emptyArray = top instanceof JSONArray
            && ((JSONArray)top).length() == 1 && ((JSONArray)top).get(0).equals("");
        if (top instanceof JSONObject) {
            this.node = new Node(null, rootName, (JSONObject)top, convention);
        } else if (top instanceof JSONArray && !emptyArray) {
            if (con.isRootElementArrayWrapper()) {
                this.node = new Node(null, rootName, obj, convention);
                rootArrayWrapper = true;
            } else {
                this.node = new Node(null, rootName, ((JSONArray)top).getJSONObject(0), convention);
            }
        } else {
            node = new Node(rootName, convention);
            convention.processAttributesAndNamespaces(node, obj);
            if (emptyArray) {
                currentValue = "";
            } else {
                currentValue = JSONObject.NULL.equals(top) ? null : top.toString();
            }
        }
        rootNode = node;
        nodes.push(node);
        event = START_DOCUMENT;
    }


    public int next() throws XMLStreamException {
        if (event == START_DOCUMENT) {
            event = START_ELEMENT;
        } else if (event == CHARACTERS) {
            event = END_ELEMENT;
            node = popNode();
            currentValue = null;
        } else if (event == END_ELEMENT && nodes.size() == 0 && node == rootNode
                   && node.getArray() != null && node.getArrayIndex() >= node.getArray().length()) {
            // the root element was read as an array (see primitiveArrayKeys), and has been closed
            event = END_DOCUMENT;
        } else if (event == START_ELEMENT || event == END_ELEMENT) {
            if (event == END_ELEMENT && nodes.size() > 0) {
                node = peekNode();
                // only when the element that has ended is an item of such an array,
                // not an element nested inside an item
                if (popArrayNodes > 0 && isUniformArrayNode(node)) {
                	popNode();
                	if (node.getArray() != null) {
                		popArrayNodes--;
                		event = END_ELEMENT;
                		return event;
                	}
                }
            }
            if (currentValue != null) {
                event = CHARACTERS;
            } else if ((node.getKeys() != null && node.getKeys().hasNext()) || node.getArray() != null) {
                processElement();
            } else {
                if (nodes.size() > 0) {
                    event = END_ELEMENT;
                    node = popNode();
                } else {
                    event = END_DOCUMENT;
                }
            }
        }
        // handle value in nodes with attributes
        if (nodes.size() > 0) {
        	Node next = peekNode();
        	if (event == START_ELEMENT && next.getName().getLocalPart().equals(valueKey)) {
        		event = CHARACTERS;
        		node = popNode();
        	}
        }
        return event;
    }

    private void processElement() throws XMLStreamException {
        try {
            // Loop rather than recurse when moving on to the next key or array
            // element, so that the stack depth doesn't depend on the input
            while (true) {
                Object newObj = null;
                String nextKey = null;
                if (node.getArray() != null) {
                    int index = node.getArrayIndex();
                    if (index >= node.getArray().length()) {

                        popNode();

                        node = peekNode();


                        if ((node.getKeys() != null && node.getKeys().hasNext()) || node.getArray() != null) {
                            if (popArrayNodes > 0 && isUniformArrayNode(node)) {
                                node = popNode();
                            }
                            continue;
                        }
                        else {
                                event = END_ELEMENT;
                                node = popNode();
                        }
                        return;
                    }
                    newObj = node.getArray().get(index++);
                    nextKey = node.getName().getLocalPart();
                    if (!"".equals(node.getName().getNamespaceURI())) {
                        nextKey = this.convention.getPrefix(node.getName().getNamespaceURI())
                                + this.getConvention().getNamespaceSeparator() + nextKey;
                    }
                    node.setArrayIndex(index);
                } else {
                    if (node.getKeys() == null || !node.getKeys().hasNext()) {
                        throw unsupportedStructure();
                    }
                    nextKey = (String) node.getKeys().next();
                    newObj = node.getObject().get(nextKey);
                }
                if (newObj instanceof String) {
                    node = new Node(nextKey, convention);
                    nodes.push(node);
                    currentValue = (String) newObj;
                    event = START_ELEMENT;
                    return;
                } else if (newObj instanceof JSONArray) {
                    JSONArray array = (JSONArray) newObj;
                    if (nextKey.equals(valueKey)) {
                        // the text of the element, in parts: read it as if the
                        // parts were joined into a single string
                        String text = joinText(array);
                        if (text != null) {
                            node = new Node(nextKey, convention);
                            nodes.push(node);
                            currentValue = text;
                            event = START_ELEMENT;
                            return;
                        }
                        // no text, so read it as an empty array
                        array = new JSONArray();
                    }
                    if (!processUniformArrayIfPossible(nextKey, array)) {
                        node = new Node(nextKey, convention);
                        node.setArray(array);
                        node.setArrayIndex(0);
                        nodes.push(node);
                        continue;
                    }
                    return;
                } else if (newObj instanceof JSONObject) {
                    node = new Node(peekNode(), nextKey, (JSONObject) newObj, convention);
                    nodes.push(node);
                    event = START_ELEMENT;
                    return;
                } else {
                    node = new Node(nextKey, convention);
                    nodes.push(node);
                    currentValue = JSONObject.NULL.equals(newObj) ? null : newObj.toString();
                    event = START_ELEMENT;
                    return;
                }
            }
        } catch (JSONException e) {
            throw new XMLStreamException(e);
        }
    }

    private boolean processUniformArrayIfPossible(String arrayKey, JSONArray array) throws JSONException, XMLStreamException {
    	
    	if (!isAvoidArraySpecificEvents(arrayKey)) {
    		return false;
    	}
    	
    	int arrayLength = array.length();
    	int depth = 0;
    	String lastKey = null;
    	int parentIndex = nodes.size();
        if (nodes.size() == 0) {
            throw unsupportedStructure();
        }
    	// the array is the root element only if it's the array read by the root node,
    	// not an array nested in the document whose key is the same as the root's name
    	boolean isRoot = rootArrayWrapper && node == rootNode
    		&& rootNode.getName().getLocalPart().equals(arrayKey);
    	Node parent = !isRoot ? new UniformArrayNode(arrayKey, convention) : node;
        
    	for (int i = arrayLength - 1; i >= 0; i--) {
    		Object object = array.get(i);
    		// lets limit to single key JSONObjects for now, and fall back to the
    		// standard array processing for any other values so that none are lost
    		if (!(object instanceof JSONObject) || ((JSONObject)object).length() != 1) {
    			lastKey = null;
    			break;
    		}
    		JSONObject jsonObject = (JSONObject)object;
    		String theKey = jsonObject.keys().next().toString();
    		if (lastKey == null || lastKey.equals(theKey)) {
    			lastKey = theKey;
    			depth++;
    			Node theNode = new UniformArrayNode(parent, theKey, jsonObject, convention);
    			nodes.push(theNode);
    		} else {
    			lastKey = null;
    			break;
    		}
    	}
    	if (lastKey == null) {
    		for (int i = 0; i < depth; i++) {
    			popNode();
    		}
    		return false;
    	}
    	
    	parent.setArray(array);
        parent.setArrayIndex(arrayLength);
        if (isRoot) {
            uniformArrayRoot = parent;
        }
        if (!isRoot) {
            nodes.add(parentIndex, parent);
            nodes.push(parent);
            node = parent;
            event = START_ELEMENT;
        } else {
        	node = popNode();
        	processElement();
        }
        popArrayNodes++;
        return true;
    }
    
    private boolean isUniformArrayNode(Node node) {
        return node instanceof UniformArrayNode || (node != null && node == uniformArrayRoot);
    }

    /**
     * A node created for an array read with primitiveArrayKeys: the parent of the
     * array, or the holder of one of its items.
     */
    private static final class UniformArrayNode extends Node {
        UniformArrayNode(String name, Convention con) throws XMLStreamException {
            super(name, con);
        }

        UniformArrayNode(Node parent, String name, JSONObject object, Convention con)
            throws JSONException, XMLStreamException {
            super(parent, name, object, con);
        }
    }

    private Node peekNode() throws XMLStreamException {
        if (nodes.size() == 0) {
            throw unsupportedStructure();
        }
        return (Node) nodes.peek();
    }

    private Node popNode() throws XMLStreamException {
        if (nodes.size() == 0) {
            throw unsupportedStructure();
        }
        return (Node) nodes.pop();
    }

    /**
     * Join the items of an array of text parts, given for the value key, skipping
     * null items. Returns null if there's no text, i.e. the array is empty or
     * all its items are null.
     */
    private static String joinText(JSONArray parts) throws JSONException, XMLStreamException {
        StringBuilder text = null;
        for (int i = 0; i < parts.length(); i++) {
            Object part = parts.opt(i);
            if (JSONObject.isNullValue(part)) {
                continue;
            }
            if (part instanceof JSONObject || part instanceof JSONArray) {
                // there's no text representation of an object or array
                throw unsupportedStructure();
            }
            if (text == null) {
                text = new StringBuilder();
            }
            text.append(part.toString());
        }
        return text == null ? null : text.toString();
    }

    /**
     * Some combinations of JSON structures, such as a "$" value key alongside
     * child elements, aren't supported by the reader's state tracking.
     */
    private static XMLStreamException unsupportedStructure() {
        return new XMLStreamException("The structure of the JSON input is not supported");
    }

    public void close() throws XMLStreamException {
    }

    /**
     * Read the text content of a text-only element, as specified by
     * {@link javax.xml.stream.XMLStreamReader#getElementText()} if enabled: the reader
     * must be positioned on a START_ELEMENT, and is left on the corresponding END_ELEMENT.
     * A JSON null value is returned as null, unless readNullAsString is configured.
     * Calling this method on a CHARACTERS event returns the current text without
     * moving the reader.
     * <p>
     * This behaviour must be enabled with Configuration.setStandardGetElementText(true).
     * By default, the text is returned and the reader is left on a CHARACTERS event,
     * so that the following call to next() returns the END_ELEMENT.
     */
    public String getElementText() throws XMLStreamException {
        if (!convention.isStandardGetElementText()) {
            event = CHARACTERS;
            return currentValue;
        }
        if (event == CHARACTERS) {
            return getText();
        }
        if (event != START_ELEMENT) {
            throw new XMLStreamException("The reader must be on a START_ELEMENT to read the element text");
        }
        StringBuilder content = null;
        int eventType = next();
        while (eventType != END_ELEMENT) {
            if (eventType == CHARACTERS) {
                String text = getText();
                if (text != null) {
                    if (content == null) {
                        content = new StringBuilder();
                    }
                    content.append(text);
                }
            } else if (eventType == START_ELEMENT) {
                throw new XMLStreamException("The element text content may not contain a START_ELEMENT");
            } else {
                throw new XMLStreamException("Unexpected event type " + eventType + " while reading the element text");
            }
            eventType = next();
        }
        return content == null ? null : content.toString();
    }

    public NamespaceContext getNamespaceContext() {
        return ctx;
    }

    public String getText() {
    	if (currentValue != null 
    		&& "null".equals(currentValue)
    		&& !convention.isReadNullAsString()) {
    		return null;
    	}
        return currentValue;
    }
    
	public void setValueKey(String valueKey) {
		this.valueKey = valueKey;
	}

	public boolean isAvoidArraySpecificEvents(String key) {
		Set<?> keys = convention.getPrimitiveArrayKeys();
		return keys != null && keys.contains(key);
	}
	public MappedNamespaceConvention getConvention() {
		return convention;
	}
}
