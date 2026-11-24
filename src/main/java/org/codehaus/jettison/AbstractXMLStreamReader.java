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
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public abstract class AbstractXMLStreamReader implements XMLStreamReader {
    protected int event;
    protected Node node;
    
    public boolean isAttributeSpecified(int index) {
        return false;
    }

    public boolean isCharacters() {
        return event == CHARACTERS;
    }

    public boolean isEndElement() {
        return event == END_ELEMENT;
    }

    public boolean isStandalone() {
        return false;
    }

    public boolean isStartElement() {
        return event == START_ELEMENT;
    }

    public boolean isWhiteSpace() {
        return false;
    }

    public int nextTag() throws XMLStreamException {
        int event = next();
        while (event != START_ELEMENT && event != END_ELEMENT) {
            event = next();
        }
        return event;
    }

    public int getEventType() {
        return event;
    }

    /**
     * Test if the current event is of the given type and if the namespace and name
     * match the current namespace and name of the current event, as specified by
     * {@link javax.xml.stream.XMLStreamReader#require(int, String, String)}. A null
     * namespaceURI or localName is not checked.
     */
    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        if (type != event) {
            throw new XMLStreamException("Expected event type " + type + " but the current event type is " + event);
        }
        if (namespaceURI == null && localName == null) {
            return;
        }
        if (!hasName()) {
            throw new XMLStreamException("The current event type " + event + " does not have a name");
        }
        if (namespaceURI != null && !namespaceURI.equals(getNamespaceURI())) {
            throw new XMLStreamException("Expected namespace '" + namespaceURI
                + "' but the current namespace is '" + getNamespaceURI() + "'");
        }
        if (localName != null && !localName.equals(getLocalName())) {
            throw new XMLStreamException("Expected local name '" + localName
                + "' but the current local name is '" + getLocalName() + "'");
        }
    }

    public int getAttributeCount() {
        return node.getAttributes().size();
    }

    public String getAttributeLocalName(int n) {
        return getAttributeName(n).getLocalPart();
    }

    public QName getAttributeName(int n) {
        Iterator itr = node.getAttributes().keySet().iterator();
        QName name = null;
        for (int i = 0; i <= n; i++) {
            name = (QName) itr.next();
        }
        return name;
    }

    public String getAttributeNamespace(int n) {
        return getAttributeName(n).getNamespaceURI();
    }

    public String getAttributePrefix(int n) {
        return getAttributeName(n).getPrefix();
    }

    public String getAttributeValue(int n) {
        Iterator itr = node.getAttributes().values().iterator();
        String name = null;
        for (int i = 0; i <= n; i++) {
            name = (String) itr.next();
        }
        return name;
    }

    public String getAttributeValue(String ns, String local) {
        return (String) node.getAttributes().get(new QName(ns, local));
    }

    public String getAttributeType(int index) {
        return "CDATA";
    }

    public String getLocalName() {
        return getName().getLocalPart();
    }

    public QName getName() {
        return node.getName();
    }

    public String getNamespaceURI() {
        return getName().getNamespaceURI();
    }

    public int getNamespaceCount() {
        return node.getNamespaceCount();
    }

    public String getNamespacePrefix(int n) {
        return node.getNamespacePrefix(n);
    }

    public String getNamespaceURI(int n) {
        return node.getNamespaceURI(n);
    }

    public String getNamespaceURI(String prefix) {
        return node.getNamespaceURI(prefix);
    }

    /**
     * Returns a NamespaceContext for the namespace declarations in scope at the
     * current position of the reader, i.e. those of the current element and its
     * ancestors, with inner declarations taking precedence. The context reflects
     * the position of the reader at the time each of its methods is called.
     */
    public NamespaceContext getNamespaceContext() {
        return new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                return node == null ? null : node.getNamespaceURI(prefix);
            }

            public String getPrefix(String namespaceURI) {
                Iterator<String> prefixes = getPrefixes(namespaceURI);
                return prefixes.hasNext() ? prefixes.next() : null;
            }

            public Iterator<String> getPrefixes(String namespaceURI) {
                Set<String> prefixes = new LinkedHashSet<String>();
                for (Node n = node; n != null; n = n.parent) {
                    for (int i = 0; i < n.getNamespaceCount(); i++) {
                        String prefix = n.getNamespacePrefix(i);
                        // skip prefixes redeclared by an inner element to a different namespace URI
                        if (n.getNamespaceURI(i).equals(namespaceURI)
                            && namespaceURI.equals(node.getNamespaceURI(prefix))) {
                            prefixes.add(prefix);
                        }
                    }
                }
                return Collections.unmodifiableSet(prefixes).iterator();
            }
        };
    }

    public boolean hasName() {
        return event == START_ELEMENT || event == END_ELEMENT;
    }

    public boolean hasNext() throws XMLStreamException {
        return event != END_DOCUMENT;
    }

    public boolean hasText() {
        return event == CHARACTERS;
    }

    public boolean standaloneSet() {
        return false;
    }

    public String getCharacterEncodingScheme() {
        return null;
    }

    public String getEncoding() {
        return null;
    }

    public Location getLocation() {
        return new Location() {

            public int getCharacterOffset() {
                return 0;
            }

            public int getColumnNumber() {
                return 0;
            }

            public int getLineNumber() {
                return -1;
            }

            public String getPublicId() {
                return null;
            }

            public String getSystemId() {
                return null;
            }
            
        };
    }

    public String getPIData() {
        return null;
    }

    public String getPITarget() {
        return null;
    }

    public String getPrefix() {
        return getName().getPrefix();
    }

    public Object getProperty(String arg0) throws IllegalArgumentException {
        return null;
    }

    public String getVersion() {
        return null;
    }

    public char[] getTextCharacters() {
    	String text = getText();
        return text != null ? text.toCharArray() : new char[]{};
    }

    /**
     * Copy the text of the current event into the target array, as specified by
     * {@link javax.xml.stream.XMLStreamReader#getTextCharacters(int, char[], int, int)}.
     * Returns the number of characters actually copied, which is less than length
     * (possibly 0) if there is no more text from sourceStart onwards.
     */
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        if (sourceStart < 0 || targetStart < 0 || targetStart > target.length
            || length < 0 || length > target.length - targetStart) {
            throw new IndexOutOfBoundsException("Invalid arguments: sourceStart=" + sourceStart
                + ", targetStart=" + targetStart + ", length=" + length + ", target length=" + target.length);
        }
        String text = getText();
        if (text == null || sourceStart >= text.length()) {
            return 0;
        }
        int count = Math.min(length, text.length() - sourceStart);
        text.getChars(sourceStart, sourceStart + count, target, targetStart);
        return count;
    }

    public int getTextLength() {
    	String text = getText();
        return text != null ? text.length() : 0;
    }

    public int getTextStart() {
        return 0;
    }

}
