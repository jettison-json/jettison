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
    // The prefixes declared on this node for each namespace URI, and the
    // sequences of prefixes in scope at this node for the URIs looked up, so
    // that NamespaceContext lookups don't scan every declaration in scope
    private Map prefixesByUri;
    private Map indexedPrefixesByUri;
    private int indexedPrefixesByUriSize;
    private Map prefixesInScope;
    private Map indexedPrefixesInScope;
    private int indexedPrefixesInScopeSize;
    
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

    /**
     * Create a node for an element nested in the element of the given parent
     * node, so that the namespace declarations of the parent are in scope.
     */
    public Node(Node parent, JSONObject object) {
        this(object);
        this.parent = parent;
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
        namespacesChanged();
    }

    public void setNamespace(String prefix, String uri) {
        namespaces.put(prefix, uri);
        namespacesChanged();
    }

    private void namespacesChanged() {
        namespacePrefixes = null;
        prefixesByUri = null;
        prefixesInScope = null;
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

    /**
     * Get the prefixes in scope at this node that are bound to the given
     * namespace URI: those declared on this node, in declaration order,
     * followed by those in scope at the parent that aren't redeclared here.
     * The sequence is built lazily, so finding the first prefix doesn't
     * require finding them all.
     */
    PrefixSequence getPrefixesInScope(String uri) {
        // the prefixes in scope are those of the nearest node that declares any,
        // as nothing is declared or redeclared in between; this and the sequences
        // don't recurse through the ancestors, as the nesting isn't limited
        Node node = this;
        while (node.namespaces.isEmpty()) {
            node = node.parent;
            if (node == null) {
                return PrefixSequence.EMPTY;
            }
        }
        return node.declaringPrefixesInScope(uri);
    }

    private PrefixSequence declaringPrefixesInScope(String uri) {
        if (prefixesInScope == null || indexedPrefixesInScope != namespaces
            || indexedPrefixesInScopeSize != namespaces.size()) {
            prefixesInScope = new HashMap(4);
            indexedPrefixesInScope = namespaces;
            indexedPrefixesInScopeSize = namespaces.size();
        }
        PrefixSequence sequence = (PrefixSequence) prefixesInScope.get(uri);
        if (sequence == null) {
            sequence = new PrefixSequence(this, uri);
            prefixesInScope.put(uri, sequence);
        }
        return sequence;
    }

    // The prefixes declared on this node for the URI, as getNamespaceURI(int)
    // reports their URIs
    private Iterator declaredPrefixes(String uri) {
        if (namespaces.size() <= SMALL_NAMESPACE_COUNT) {
            // cheaper to scan a few declarations than to index them
            ArrayList prefixes = null;
            for (Iterator itr = namespaces.entrySet().iterator(); itr.hasNext();) {
                Map.Entry e = (Map.Entry) itr.next();
                String value = e.getValue() == null ? "" : e.getValue().toString();
                if (value.equals(uri)) {
                    if (prefixes == null) {
                        prefixes = new ArrayList(1);
                    }
                    prefixes.add(e.getKey().toString());
                }
            }
            return prefixes == null ? EMPTY_ITERATOR : prefixes.iterator();
        }
        if (prefixesByUri == null || indexedPrefixesByUri != namespaces
            || indexedPrefixesByUriSize != namespaces.size()) {
            prefixesByUri = new HashMap();
            indexedPrefixesByUri = namespaces;
            indexedPrefixesByUriSize = namespaces.size();
            for (Iterator itr = namespaces.entrySet().iterator(); itr.hasNext();) {
                Map.Entry e = (Map.Entry) itr.next();
                String value = e.getValue() == null ? "" : e.getValue().toString();
                ArrayList prefixes = (ArrayList) prefixesByUri.get(value);
                if (prefixes == null) {
                    prefixes = new ArrayList(1);
                    prefixesByUri.put(value, prefixes);
                }
                prefixes.add(e.getKey().toString());
            }
        }
        ArrayList prefixes = (ArrayList) prefixesByUri.get(uri);
        return prefixes == null ? EMPTY_ITERATOR : prefixes.iterator();
    }

    private static final Iterator EMPTY_ITERATOR = new ArrayList(0).iterator();

    private static final int SMALL_NAMESPACE_COUNT = 8;

    /**
     * The prefixes in scope at a node for a namespace URI, found as they're
     * needed and kept for later lookups at the node and its descendants.
     */
    static final class PrefixSequence {
        static final PrefixSequence EMPTY = new PrefixSequence(null, null);

        private final Node node;
        private final String uri;
        private final ArrayList prefixes = new ArrayList(2);
        private Iterator declared;
        private PrefixSequence inherited;
        private int inheritedIndex;
        private boolean complete;

        PrefixSequence(Node node, String uri) {
            this.node = node;
            this.uri = uri;
            this.complete = node == null;
        }

        /**
         * Get the prefix at the given position, or null if there are no more.
         */
        String get(int index) {
            if (prefixes.size() <= index && !complete) {
                find(index);
            }
            return index < prefixes.size() ? (String) prefixes.get(index) : null;
        }

        // Find prefixes until there's one at the index, or there are no more. A
        // sequence that needs the next prefix of the parent's sequence waits on
        // a stack while that sequence finds it, rather than calling it.
        private void find(int index) {
            ArrayList waiting = null;
            PrefixSequence sequence = this;
            while (prefixes.size() <= index && !complete) {
                if (sequence.needsInherited()) {
                    if (waiting == null) {
                        waiting = new ArrayList();
                    }
                    waiting.add(sequence);
                    sequence = sequence.inherited;
                } else {
                    sequence.step();
                    if (waiting != null && !waiting.isEmpty()
                        && !((PrefixSequence) waiting.get(waiting.size() - 1)).needsInherited()) {
                        sequence = (PrefixSequence) waiting.remove(waiting.size() - 1);
                    }
                }
            }
        }

        // Whether the next prefix depends on a prefix of the parent's sequence
        // that hasn't been found yet
        private boolean needsInherited() {
            if (complete) {
                return false;
            }
            if (declared == null) {
                declared = node.declaredPrefixes(uri);
            }
            if (declared.hasNext()) {
                return false;
            }
            if (inherited == null) {
                inherited = node.parent == null ? EMPTY : node.parent.getPrefixesInScope(uri);
            }
            return inheritedIndex >= inherited.prefixes.size() && !inherited.complete;
        }

        // Consider one declared or inherited prefix, or complete the sequence if
        // there are no more; needsInherited() must be false
        private void step() {
            // a prefix declared on the node is in scope for the URI if looking
            // it up from the node gives the URI
            if (declared.hasNext()) {
                String prefix = (String) declared.next();
                // the declared prefixes are distinct
                if (uri.equals(lookUp(node, prefix))) {
                    prefixes.add(prefix);
                }
                return;
            }
            // a prefix in scope at the parent is in scope here unless it's bound
            // here, as looking it up from here then gives the same URI
            if (inheritedIndex >= inherited.prefixes.size()) {
                complete = true;
                return;
            }
            String prefix = (String) inherited.prefixes.get(inheritedIndex++);
            if (node.namespaces.get(prefix) == null
                // a prefix declared here with no URI is looked up from the parent,
                // so it may have been found among the declared prefixes already
                && !(node.namespaces.containsKey(prefix) && prefixes.contains(prefix))) {
                prefixes.add(prefix);
            }
        }

        // getNamespaceURI(prefix) of the node, without recursing
        private static String lookUp(Node node, String prefix) {
            for (Node n = node; n != null; n = n.parent) {
                String uri = (String) n.namespaces.get(prefix);
                if (uri != null) {
                    return uri;
                }
            }
            return null;
        }
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
