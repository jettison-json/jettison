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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.codehaus.jettison.Convention;
import org.codehaus.jettison.Node;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class MappedNamespaceConvention implements Convention {
    private Map xnsToJns = new HashMap();
    private Map jnsToXns = new HashMap();
    private List attributesAsElements;
    private List jsonAttributesAsElements;

    private TypeConverter typeConverter;
    
    public MappedNamespaceConvention() {
        super();
        typeConverter = new DefaultConverter();
    }
    public MappedNamespaceConvention(Configuration config) {
        super();
        this.xnsToJns = config.getXmlToJsonNamespaces();
        this.attributesAsElements = config.getAttributesAsElements();
        
        for (Iterator itr = xnsToJns.entrySet().iterator(); itr.hasNext();) {
            Map.Entry entry = (Map.Entry) itr.next();
            jnsToXns.put(entry.getValue(), entry.getKey());
        }
        
        jsonAttributesAsElements = new ArrayList();
        if (attributesAsElements != null) {
            for (Iterator itr = attributesAsElements.iterator(); itr.hasNext();) {
                QName q = (QName) itr.next();
                jsonAttributesAsElements.add(createAttributeKey(q.getPrefix(), 
                                                                q.getNamespaceURI(), 
                                                                q.getLocalPart()));
            }
        }
        typeConverter = config.getTypeConverter();
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
            } else if (jsonAttributesAsElements != null 
                && jsonAttributesAsElements.contains(k)) {
                String value = object.optString(k);
                if (value != null) {
                    readAttribute(n, k, value);
                } else {
                    JSONArray array = object.optJSONArray(k);
                    if (array != null) {
                        readAttribute(n, k, array);
                    }
                }
                itr.remove();
            } else {
                int dot = k.lastIndexOf('.');

                if (dot != -1) {
                	String jns = k.substring(0, dot);
                	n.setNamespace("", (String)jnsToXns.get(jns));
                }
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
        String local = name;

        if (dot == -1) {
            dot = 0;
        } else {
            local = local.substring(dot+1);
        }
        
        String jns = name.substring(0, dot);
        String xns = (String) jnsToXns.get(jns);

        if (xns == null) {
            qname = new QName(name);
        } else {            
            qname  = new QName(xns, local);
        }

        return qname;
    }
    
    
    public String createAttributeKey(String p, String ns, String local) {
        StringBuffer builder = new StringBuffer().append('@');
        String jns = getJSONNamespace(ns);
        if (jns != null && jns.length() != 0) {
            builder.append(jns).append('.');
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
        StringBuffer builder = new StringBuffer();
        String jns = getJSONNamespace(ns);
        if (jns != null && jns.length() != 0) {
            builder.append(jns).append('.');
        }
        return builder.append(local).toString();
    }
    
    public boolean isElement(String p, String ns, String local) {
        if (attributesAsElements == null) return false;
        
        for (Iterator itr = attributesAsElements.iterator(); itr.hasNext();) {
            QName q = (QName) itr.next();
            
            if (q.getNamespaceURI().equals(ns)
                && q.getLocalPart().equals(local)) {
                return true;
            }
        }
        return false;
    }

    public Object convertToJSONPrimitive(String text) {
        return typeConverter.convertToJSONPrimitive(text);
    }

}
