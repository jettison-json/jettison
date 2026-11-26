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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;


public class Node {
    
    JSONObject object;
    Map attributes;
    Map namespaces;
    Iterator keys;
    // the keys of the JSON object read as attributes or namespace declarations
    Collection skippedKeys;
    QName name;
    JSONArray array;
    int arrayIndex;
    String currentKey;
    Node parent;
    // The attribute names and namespace prefixes in map order, so that the
    // indexed accessors used by StAX consumers don't walk the maps on every
    // call, which made reading all the attributes of an element quadratic
    private Object[] attributeNames;
    private Map indexedAttributes;
    private Object[] namespacePrefixes;
    private Map indexedNamespaces;
    
    public Node(Node parent, String name, JSONObject object, Convention con) 
        throws JSONException, XMLStreamException {
        this.parent = parent;
        this.object = object;
        
        /* Should really use a _Linked_ HashMap to preserve
         * ordering (insert order) -- regular one has arbitrary ordering.
         * But there are some funky dependencies within unit tests
         * that will fail right now, need to investigate that bit more
         */
        this.namespaces = new LinkedHashMap();
        this.attributes = new LinkedHashMap();
        
        con.processAttributesAndNamespaces(this, object);
        
        keys = skippedKeys == null ? object.keys() : new SkippingIterator(object.keys(), skippedKeys);

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

    public int getNamespaceCount() {
        return namespaces.size();
    }

    public String getNamespaceURI(String prefix) {
        String result = (String) namespaces.get(prefix);
        if (result == null && parent != null) {
            result = parent.getNamespaceURI(prefix);
        }
        return result;
    }

    public String getNamespaceURI(int index) {
        if (index < 0 || index >= getNamespaceCount()) {
            throw new IllegalArgumentException("Illegal index: element has "+getNamespaceCount()+" namespace declarations");
        }
        Object ns = namespaces.get(namespacePrefixes()[index]);
        return ns == null ? "" : ns.toString();
    }

    public String getNamespacePrefix(String URI) {
        String result = null;
        for (Iterator nsItr = namespaces.entrySet().iterator(); nsItr.hasNext();) {
            Map.Entry e = (Map.Entry) nsItr.next();
            if (e.getValue().equals(URI)) {
                result = (String) e.getKey();
            }
        }
        if (result == null && parent != null) {
            result = parent.getNamespacePrefix(URI);
        }
        return result;
    }

    public String getNamespacePrefix(int index) {
        if (index < 0 || index >= getNamespaceCount()) {
            throw new IllegalArgumentException("Illegal index: element has "+getNamespaceCount()+" namespace declarations");
        }
        return namespacePrefixes()[index].toString();
    }

    public void setNamespaces(Map namespaces) {
        this.namespaces = namespaces;
        namespacePrefixes = null;
    }

    public void setNamespace(String prefix, String uri) {
        namespaces.put(prefix, uri);
        namespacePrefixes = null;
    }

    public Map getAttributes() {
        return attributes;
    }

    public void setAttribute(QName name, String value) {
        attributes.put(name, value);
        attributeNames = null;
    }

    /**
     * Get the name of the attribute at the given index, in the iteration order
     * of the attribute map. A negative index gives null, and an index past the
     * last attribute throws a NoSuchElementException.
     */
    QName getAttributeName(int index) {
        if (index < 0) {
            return null;
        }
        Object[] names = attributeNames();
        if (index >= names.length) {
            throw new NoSuchElementException();
        }
        return (QName) names[index];
    }

    /**
     * Get the value of the attribute at the given index, in the iteration order
     * of the attribute map. A negative index gives null, and an index past the
     * last attribute throws a NoSuchElementException.
     */
    String getAttributeValue(int index) {
        QName name = getAttributeName(index);
        return name == null ? null : (String) attributes.get(name);
    }

    // The maps are normally filled in before the node is read, but they're
    // exposed, so rebuild the arrays if a map has been replaced or resized
    private Object[] attributeNames() {
        if (attributeNames == null || indexedAttributes != attributes
            || attributeNames.length != attributes.size()) {
            attributeNames = attributes.keySet().toArray();
            indexedAttributes = attributes;
        }
        return attributeNames;
    }

    private Object[] namespacePrefixes() {
        if (namespacePrefixes == null || indexedNamespaces != namespaces
            || namespacePrefixes.length != namespaces.size()) {
            namespacePrefixes = namespaces.keySet().toArray();
            indexedNamespaces = namespaces;
        }
        return namespacePrefixes;
    }

    public Iterator getKeys() {
        return keys;
    }

    /**
     * Mark a key of the JSON object of this node as processed by the convention,
     * e.g. as an attribute or a namespace declaration, so that it isn't returned
     * by getKeys() as a child element. The JSON object itself isn't modified.
     */
    public void skipKey(String key) {
        if (skippedKeys == null) {
            // usually only a few keys are skipped per object, so a list is cheaper than a set
            skippedKeys = new ArrayList(4);
        } else if (skippedKeys.size() == SKIPPED_KEYS_LIST_LIMIT && skippedKeys instanceof ArrayList) {
            // but contains() on a list is linear, so an object with many attributes
            // would make reading it quadratic
            skippedKeys = new HashSet(skippedKeys);
        }
        skippedKeys.add(key);
    }

    private static final int SKIPPED_KEYS_LIST_LIMIT = 8;

    private static class SkippingIterator implements Iterator {
        private final Iterator delegate;
        private final Collection skipped;
        private Object next;
        private boolean hasNext;

        SkippingIterator(Iterator delegate, Collection skipped) {
            this.delegate = delegate;
            this.skipped = skipped;
            advance();
        }

        private void advance() {
            hasNext = false;
            while (delegate.hasNext()) {
                Object candidate = delegate.next();
                if (!skipped.contains(candidate)) {
                    next = candidate;
                    hasNext = true;
                    return;
                }
            }
        }

        public boolean hasNext() {
            return hasNext;
        }

        public Object next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            Object result = next;
            advance();
            return result;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
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
    
    public String toString() {
		if (this.name != null) {
			return this.name.toString();
		} else {
			return super.toString();
		}
	}
    
}
