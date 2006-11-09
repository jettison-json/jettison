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

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;

import org.codehaus.jettison.AbstractXMLStreamReader;
import org.codehaus.jettison.Node;
import org.codehaus.jettison.util.FastStack;
import org.json.JSONException;
import org.json.JSONObject;

public class MappedXMLStreamReader extends AbstractXMLStreamReader {
    private FastStack nodes;
    private String currentValue;
    private MappedNamespaceConvention convention;

    public MappedXMLStreamReader(JSONObject obj) 
        throws JSONException, XMLStreamException {
       this(obj, new MappedNamespaceConvention());
    }
    
    public MappedXMLStreamReader(JSONObject obj, MappedNamespaceConvention con)
        throws JSONException, XMLStreamException {
        String rootName = (String) obj.keys().next();
        
        this.convention = con;
        this.node = new Node(rootName, obj.getJSONObject(rootName), convention);
        this.nodes = new FastStack();
        nodes.push(node);
        event = START_DOCUMENT;
    }
    

    public int next() throws XMLStreamException {
        if (event == START_DOCUMENT) {
            event = START_ELEMENT;
        } else if (event == CHARACTERS) {
            event = END_ELEMENT;
            if (nodes.size() > 1) {
                node = (Node) nodes.pop();
            }
            currentValue = null;
        }
        else if (event == START_ELEMENT || event == END_ELEMENT) {
            if (event == END_ELEMENT && nodes.size() > 0) {
                node = (Node) nodes.peek();
            }
            if (currentValue != null) {
                event = CHARACTERS;
            } else if (node.getKeys() != null && node.getKeys().hasNext()) {
                processElement();
            } else {
                if (nodes.size() >  0) {
                    node = (Node) nodes.pop();
                }
                event = END_ELEMENT;
            }
        }
         
        return event;
    }
    
    private void processElement() throws XMLStreamException {
        try {
            String nextKey = (String) node.getKeys().next();
            
            Object newObj = node.getObject().get(nextKey);
            if (newObj instanceof String) {
                node = new Node(nextKey, convention);
                nodes.push(node);
                currentValue = (String) newObj;
                event = START_ELEMENT;
                return;
            } else if (newObj instanceof JSONObject) {
                node = new Node(nextKey, (JSONObject) newObj, convention);
                nodes.push(node);
                event = START_ELEMENT;
                return;
            }
            event = END_ELEMENT;
        } catch (JSONException e) {
            throw new XMLStreamException(e);
        }
    }

    public void close() throws XMLStreamException {
    }

    public String getElementText() throws XMLStreamException {
        return currentValue;
    }

    public NamespaceContext getNamespaceContext() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getText() {
        return currentValue;
    }

    public char[] getTextCharacters() {
        return currentValue.toCharArray();
    }

    public int getTextCharacters(int arg0, char[] arg1, int arg2, int arg3) throws XMLStreamException {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getTextLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getTextStart() {
        // TODO Auto-generated method stub
        return 0;
    }
}
