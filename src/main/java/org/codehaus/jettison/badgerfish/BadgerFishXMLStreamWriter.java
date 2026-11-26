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
package org.codehaus.jettison.badgerfish;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;

import org.codehaus.jettison.AbstractXMLStreamWriter;
import org.codehaus.jettison.Node;
import org.codehaus.jettison.XsonNamespaceContext;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.util.FastStack;

public class BadgerFishXMLStreamWriter extends AbstractXMLStreamWriter {
    private JSONObject root;
    private JSONObject currentNode;
    private Writer writer;
    private FastStack nodes;
    private String currentKey;
    private NamespaceContext ctx;

    public BadgerFishXMLStreamWriter(Writer writer) {
        this(writer, new JSONObject());
    }

    public BadgerFishXMLStreamWriter(Writer writer, JSONObject currentNode) {
        this(writer, currentNode, new FastStack());
    }

    public BadgerFishXMLStreamWriter(Writer writer,
        JSONObject currentNode,
        FastStack nodes) {
        super();
        this.currentNode = currentNode;
        this.root = currentNode;
        this.writer = writer;
        this.nodes = nodes;
        this.ctx = new XsonNamespaceContext(nodes);
    }

    @Override
    public void close() throws XMLStreamException {
    }

    @Override
    public void flush() throws XMLStreamException {

    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return ctx;
    }

    @Override
    public String getPrefix(String ns) throws XMLStreamException {
        return getNamespaceContext().getPrefix(ns);
    }

    @Override
    public Object getProperty(String arg0) throws IllegalArgumentException {
        return null;
    }

    @Override
    public void setDefaultNamespace(String arg0) throws XMLStreamException {
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        this.ctx = context;
    }

    @Override
    public void setPrefix(String arg0, String arg1) throws XMLStreamException {

    }

    @Override
    public void writeAttribute(String p, String ns, String local, String value) throws XMLStreamException {
        String key = createAttributeKey(p, ns, local);
        try {
            getCurrentNode().put(key, value);
        } catch (JSONException e) {
            throw new XMLStreamException(e);
        }
    }

    private String createAttributeKey(String p, String ns, String local) {
        return "@" + createKey(p, ns, local);
    }

    private String createKey(String p, String ns, String local) {
        if (p == null || p.isEmpty()) {
            return local;
        }

        return p + ":" + local;
    }

    /**
     * Write an attribute in the given namespace, using a prefix bound to the namespace
     * URI in the current scope. As attributes are not in the default namespace, only a
     * non-empty prefix is used. If no such prefix is bound, the attribute is written
     * without a prefix, as in previous releases.
     */
    @Override
    public void writeAttribute(String ns, String local, String value) throws XMLStreamException {
        String prefix = null;
        if (ns != null && !ns.isEmpty()) {
            for (Iterator prefixes = getNamespaceContext().getPrefixes(ns); prefixes.hasNext();) {
                String candidate = (String) prefixes.next();
                if (!candidate.isEmpty()) {
                    prefix = candidate;
                    break;
                }
            }
        }
        writeAttribute(prefix, ns, local, value);
    }

    @Override
    public void writeAttribute(String local, String value) throws XMLStreamException {
        writeAttribute(null, local, value);
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        if (text.isEmpty()) {
            return;
        }
        try {
            Object o = getCurrentNode().opt("$");
            if (o instanceof JSONArray) {
                ((JSONArray) o).put(text);
            } else if (o instanceof String) {
                JSONArray arr = new JSONArray();
                arr.put(o);
                arr.put(text);
                getCurrentNode().put("$", arr);
            } else {
                getCurrentNode().put("$", text);
            }
        } catch (JSONException e) {
            throw new XMLStreamException(e);
        }
    }

    @Override
    public void writeDefaultNamespace(String ns) throws XMLStreamException {
        writeNamespace("", ns);
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        if (getNodes().size() > 1) {
            getNodes().pop();
            currentNode = ((Node) getNodes().peek()).getObject();
        }
    }

    @Override
    public void writeEntityRef(String arg0) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeNamespace(String prefix, String ns) throws XMLStreamException {
        // as specified by XMLStreamWriter, a null, empty or "xmlns" prefix
        // declares the default namespace
        if (prefix == null || "xmlns".equals(prefix)) {
            prefix = "";
        }
        ((Node) getNodes().peek()).setNamespace(prefix, ns);
        try {
            JSONObject nsObj = getCurrentNode().optJSONObject("@xmlns");
            if (nsObj == null) {
                nsObj = new JSONObject();
                getCurrentNode().put("@xmlns", nsObj);
            }
            if (prefix.isEmpty()) {
                prefix = "$";
            }
            nsObj.put(prefix, ns);
        } catch (JSONException e) {
            throw new XMLStreamException(e);
        }
    }

    @Override
    public void writeProcessingInstruction(String arg0, String arg1) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeProcessingInstruction(String arg0) throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
    }


    @Override
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

    @Override
    public void writeStartElement(String prefix, String local, String ns) throws XMLStreamException {
        try {

            // TODO ns
            currentKey = createKey(prefix, ns, local);

            Object existing = getCurrentNode().opt(currentKey);
            if (existing instanceof JSONObject) {
                JSONArray array = new JSONArray();
                array.put(existing);

                JSONObject newCurrent = new JSONObject();
                array.put(newCurrent);

                getCurrentNode().put(currentKey, array);

                currentNode = newCurrent;
                Node node = new Node(currentNode);
                getNodes().push(node);
            } else {
                JSONObject newCurrent = new JSONObject();

                if (existing instanceof JSONArray) {
                    ((JSONArray) existing).put(newCurrent);
                } else {
                    getCurrentNode().put(currentKey, newCurrent);
                }

                currentNode = newCurrent;
                Node node = new Node(currentNode);
                getNodes().push(node);
            }
        } catch (JSONException e) {
            throw new XMLStreamException("Could not write start element!", e);
        }
    }

    protected JSONObject getCurrentNode() {
        return currentNode;
    }

    protected FastStack getNodes() {
        return nodes;
    }
}
