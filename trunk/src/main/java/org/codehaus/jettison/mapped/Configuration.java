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
import java.util.List;
import java.util.Map;

public class Configuration {
    private Map xmlToJsonNamespaces;
    private List attributesAsElements;
    private List ignoredElements;
    private boolean supressAtAttributes; 
    private String attributeKey = "@"; 

    
    private TypeConverter typeConverter = new DefaultConverter();
    
    public Configuration() {
        super();
        this.xmlToJsonNamespaces = new HashMap();
    }
    
    public Configuration(Map xmlToJsonNamespaces) {
        super();
        this.xmlToJsonNamespaces = xmlToJsonNamespaces;
    }

    public Configuration(Map xmlToJsonNamespaces, List attributesAsElements, List ignoredElements) {
        super();
        this.xmlToJsonNamespaces = xmlToJsonNamespaces;
        this.attributesAsElements = attributesAsElements;
        this.ignoredElements = ignoredElements;
    }
    
    public List getAttributesAsElements() {
        return attributesAsElements;
    }
    public void setAttributesAsElements(List attributesAsElements) {
        this.attributesAsElements = attributesAsElements;
    }
    public List getIgnoredElements() {
        return ignoredElements;
    }
    public void setIgnoredElements(List ignoredElements) {
        this.ignoredElements = ignoredElements;
    }
    public Map getXmlToJsonNamespaces() {
        return xmlToJsonNamespaces;
    }
    public void setXmlToJsonNamespaces(Map xmlToJsonNamespaces) {
        this.xmlToJsonNamespaces = xmlToJsonNamespaces;
    }

    public TypeConverter getTypeConverter() {
        return typeConverter;
    }

    public void setTypeConverter(TypeConverter typeConverter) {
        this.typeConverter = typeConverter;
    }

	public boolean isSupressAtAttributes() {
		return this.supressAtAttributes;
	}

	public void setSupressAtAttributes(boolean supressAtAttributes) {
		this.supressAtAttributes = supressAtAttributes;
	}
	
   public String getAttributeKey() {
        return this.attributeKey;
    }

    public void setAttributeKey(String attributeKey) {
        this.attributeKey = attributeKey;
    }
}
