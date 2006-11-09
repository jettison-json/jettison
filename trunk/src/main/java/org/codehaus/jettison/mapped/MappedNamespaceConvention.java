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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import org.codehaus.jettison.Convention;
import org.codehaus.jettison.Node;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MappedNamespaceConvention implements Convention {
    private Map xnsToJns = new HashMap();
    private Map jnsToXns = new HashMap();
    
    public MappedNamespaceConvention() {
        super();
    }
    public MappedNamespaceConvention(Map xnsToJns) {
        super();
        this.xnsToJns = xnsToJns;
        for (Iterator itr = xnsToJns.entrySet().iterator(); itr.hasNext();) {
            Map.Entry entry = (Map.Entry) itr.next();
            jnsToXns.put(entry.getValue(), entry.getKey());
        }
    }

    /* (non-Javadoc)
     * @see org.codehaus.xson.mapped.Convention#processNamespaces(org.codehaus.xson.Node, org.json.JSONObject)
     */
    public void processAttributesAndNamespaces(Node n, JSONObject object) throws JSONException {
        // Read in the attributes, and stop when there are no more
        for (Iterator itr = object.keys(); itr.hasNext();) {
            String k = (String) itr.next();
            
            if (k.startsWith("@")) {
                String value = object.optString(k);
                k = k.substring(1);
                if (value != null) {
                    readAttribute(n, k, value);
                } else {
                    JSONArray array = object.optJSONArray(k);
                    if (array != null) {
                        readAttribute(n, k, array);
                    }
                }
                itr.remove();
            }
        }
    }
    
    public QName createQName(String rootName, Node node) {
        return createQName(rootName);
    }

    private void readAttribute(Node n, String k, JSONArray array) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            readAttribute(n, k, array.getString(i));
        }
    }

    private void readAttribute(Node n, String name, String value) throws JSONException {
        QName qname = createQName(name);
        n.getAttributes().put(qname, value);
    }

    private QName createQName(String name) {
        int dot = name.lastIndexOf('.');
        QName qname = null;
        if (dot != -1) {
            String jns = name.substring(0, dot);
            String xns = (String) jnsToXns.get(jns);
            
            if (xns == null) {
                throw new IllegalStateException("Invalid JSON namespace: " + jns);
            }
            String local = name.substring(dot+1);
            
            qname  = new QName(xns, local);
        } else {
            qname = new QName(name);
        }
        return qname;
    }
    
    
    public String createAttributeKey(String p, String ns, String local) {
        StringBuilder builder = new StringBuilder().append('@');
        if (ns != null && ns.length() != 0) {
            builder.append(getJSONNamespace(ns)).append('.');
        }
        return builder.append(local).toString();
    }
    
    private String getJSONNamespace(String ns) {
        if (ns == null || ns.length() == 0) return "";
        
        String jns = (String) xnsToJns.get(ns);
        if (jns == null) {
            throw new IllegalStateException("Invalid JSON namespace: " + ns);
        }
        return jns;
    }
    
    public String createKey(String p, String ns, String local) {
        StringBuilder builder = new StringBuilder();
        if (ns != null && ns.length() != 0) {
            builder.append(getJSONNamespace(ns)).append('.');
        }
        return builder.append(local).toString();
    }
}
