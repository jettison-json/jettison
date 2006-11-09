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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Node {
    
    JSONObject object;
    Map attributes;
    Map namespaces;
    Iterator keys;
    QName name;
    JSONArray array;
    int arrayIndex;
    String currentKey;
    
    public Node(String name, JSONObject object, Convention con) 
        throws JSONException, XMLStreamException {
        this.object = object;
        this.namespaces = new HashMap();
        this.attributes = new HashMap();
        
        con.processAttributesAndNamespaces(this, object);
        
        keys = object.keys();

        this.name = con.createQName(name, this);
    }

    public Node(String name, Convention con) throws XMLStreamException {
        this.name = con.createQName(name, this);
        this.namespaces = new HashMap();
        this.attributes = new HashMap();
    }

    public Node(JSONObject object) {
        this.object = object;
        this.namespaces = new HashMap();
        this.attributes = new HashMap();
    }

    public Map getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Map namespaces) {
        this.namespaces = namespaces;
    }

    public Map getAttributes() {
        return attributes;
    }

    public Iterator getKeys() {
        return keys;
    }

    public QName getName() {
        return name;
    }

    public JSONObject getObject() {
        return object;
    }

    public void setObject(JSONObject object) {
        this.object = object;
    }

    public JSONArray getArray() {
        return array;
    }

    public void setArray(JSONArray array) {
        this.array = array;
    }

    public int getArrayIndex() {
        return arrayIndex;
    }

    public void setArrayIndex(int arrayIndex) {
        this.arrayIndex = arrayIndex;
    }

    public String getCurrentKey() {
        return currentKey;
    }

    public void setCurrentKey(String currentKey) {
        this.currentKey = currentKey;
    }
    
    
}
