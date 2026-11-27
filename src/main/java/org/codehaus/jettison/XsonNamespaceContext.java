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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;

import org.codehaus.jettison.util.FastStack;

public class XsonNamespaceContext implements NamespaceContext {
    private FastStack nodes;
    
    public XsonNamespaceContext(FastStack nodes) {
        super();
        this.nodes = nodes;
    }

    /**
     * Returns the namespace URI bound to the given prefix, searching from the innermost
     * element outwards, so that an inner declaration of a prefix takes precedence.
     * Returns null if the prefix is not bound.
     */
    public String getNamespaceURI(String prefix) {
        for (int i = nodes.size() - 1; i >= 0; i--) {
            Node node = (Node) nodes.get(i);
            String uri;
            if (i > 0 && node.parent == nodes.get(i - 1)) {
                // the node would look in the nodes below it, as the loop does,
                // so only look at its own declarations
                uri = (String) node.namespaces.get(prefix);
            } else {
                uri = node.getNamespaceURI(prefix);
            }
            if (uri != null) {
                return uri;
            }
        }
        return null;
    }

    /**
     * Returns a prefix bound to the given namespace URI in the current scope, or null
     * if there is none. A prefix that has been redeclared by an inner element to a
     * different namespace URI is not returned.
     */
    public String getPrefix(String namespaceURI) {
        Node top = namespaceURI == null ? null : linkedTop();
        if (top != null) {
            return top.getPrefixesInScope(namespaceURI).get(0);
        }
        Iterator prefixes = getPrefixes(namespaceURI);
        return prefixes.hasNext() ? (String) prefixes.next() : null;
    }

    /**
     * Returns all the prefixes bound to the given namespace URI in the current scope,
     * starting with the innermost declarations.
     */
    public Iterator getPrefixes(String namespaceURI) {
        Node top = namespaceURI == null ? null : linkedTop();
        if (top != null) {
            return new PrefixIterator(top.getPrefixesInScope(namespaceURI));
        }
        Set prefixes = new LinkedHashSet();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            Node node = (Node) nodes.get(i);
            for (int j = 0; j < node.getNamespaceCount(); j++) {
                String prefix = node.getNamespacePrefix(j);
                if (node.getNamespaceURI(j).equals(namespaceURI)
                    && namespaceURI.equals(getNamespaceURI(prefix))) {
                    prefixes.add(prefix);
                }
            }
        }
        return Collections.unmodifiableSet(prefixes).iterator();
    }

    /**
     * Get the node at the top of the stack if each node's parent is the node
     * below it, as the BadgerFish writer creates them. The lookups can then
     * use the prefixes in scope that the nodes keep, rather than scanning
     * every namespace declaration on the stack for each lookup, which made
     * writing an element with many namespaced attributes quadratic in the
     * number of declarations. Otherwise, e.g. for a stack built by a
     * subclass or a very deep one, return null.
     */
    private Node linkedTop() {
        if (nodes.size() > MAX_LINKED_DEPTH) {
            // finding the prefixes in scope recurses through the ancestors
            return null;
        }
        Node below = null;
        for (int i = 0; i < nodes.size(); i++) {
            Object node = nodes.get(i);
            if (!(node instanceof Node) || ((Node) node).parent != below) {
                return null;
            }
            below = (Node) node;
        }
        return below;
    }

    private static final int MAX_LINKED_DEPTH = 500;

    private static final class PrefixIterator implements Iterator {
        private final Node.PrefixSequence prefixes;
        private int index;

        PrefixIterator(Node.PrefixSequence prefixes) {
            this.prefixes = prefixes;
        }

        public boolean hasNext() {
            return prefixes.get(index) != null;
        }

        public Object next() {
            String prefix = prefixes.get(index);
            if (prefix == null) {
                throw new NoSuchElementException();
            }
            index++;
            return prefix;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
