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

/**
 * Default converter that tries to convert value to appropriate primitive (if fails, returns original string)
 *
 * @author <a href="mailto:dejan@nighttale.net">Dejan Bosanac</a>
 * @since 1.1
 */
public class DefaultConverter implements TypeConverter {
    public Object convertToJSONPrimitive(String text) {

		Object primitive = null;
		// Attempt to convert to Integer
		try {
			//primitive = Long.valueOf(text);
			primitive = Long.decode(text);
		} catch (Exception e) {
		}
		// Attempt to convert to double
		if (primitive == null) {
			try {
				primitive = Double.valueOf(text);
			} catch (Exception e) {
			}
		}
		// Attempt to convert to boolean
		if (primitive == null) {
			if(text.trim().equalsIgnoreCase("true") || text.trim().equalsIgnoreCase("false")) {
				primitive = Boolean.valueOf(text);
			}
		}

		if (primitive == null) {
			// Default String
			primitive = text;
		}

		return primitive;
    }
}
