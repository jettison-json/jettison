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
    /* Were there a constants class, this key would live there. */
    private static final String ENFORCE_32BIT_INTEGER_KEY = "jettison.mapped.typeconverter.enforce_32bit_integer";
    public static final boolean ENFORCE_32BIT_INTEGER = Boolean.getBoolean( ENFORCE_32BIT_INTEGER_KEY );
    private boolean enforce32BitInt = ENFORCE_32BIT_INTEGER; 

   	public void setEnforce32BitInt(boolean enforce32BitInt) {
		this.enforce32BitInt = enforce32BitInt;
	}

	@Override
	public Object convertToJSONPrimitive(String text) {
        if(text == null) {
        	return null;
		}

		// Attempt to convert to Integer
		try {
			return enforce32BitInt ? Integer.valueOf(text) : Long.valueOf(text);
		} catch (NumberFormatException e) {
			/* No action needed: text is not an integer or long */
		}

		// Attempt to convert to double
		try {
			Double v = Double.valueOf(text);
			return !v.isInfinite() && !v.isNaN() ? v : text;
		} catch (NumberFormatException e) {
			/* No action needed: text is not a double */
		}

		// Attempt to convert to boolean
		if(text.trim().equalsIgnoreCase("true") || text.trim().equalsIgnoreCase("false")) {
			return Boolean.valueOf(text);
		}

		// Default String
		return text;
    }
}
