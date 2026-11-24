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
            String uri = ((Node) nodes.get(i)).getNamespaceURI(prefix);
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
        Iterator prefixes = getPrefixes(namespaceURI);
        return prefixes.hasNext() ? (String) prefixes.next() : null;
    }

    /**
     * Returns all the prefixes bound to the given namespace URI in the current scope,
     * starting with the innermost declarations.
     */
    public Iterator getPrefixes(String namespaceURI) {
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

}
