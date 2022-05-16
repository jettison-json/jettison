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

	public Object convertToJSONPrimitive(String text) {
        if(text == null) return text;

        // If there's at least one character
        if (text.length() >= 1) {
            // find the first character
            char first = text.charAt(0);

            // Is it incompatible with a number?
            if ((first < '0' || first > '9') && first != '-') {
                // Yes it is, so special case check for Boolean values
                if (first == 't') {
                    if (text.equals("true")) {
                       return Boolean.TRUE;
                    }
                } else if (first == 'f') {
                    if (text.equals("false")) {
                        return Boolean.FALSE;
                    }
                }

                // Definitely not a Boolean or a number, so return the original value
                return text;
            }
        }

        Object primitive = null;

        if (enforce32BitInt) {
            primitive = getInteger(text);
        } else {
            primitive = getLong(text);
        }

        if (primitive == null) {
            Double dbl = getDouble(text);

            if (dbl != null) {
                if (dbl.isInfinite() || dbl.isNaN()) {
                    primitive = text;
                }
                else {
                    primitive = dbl;
                }
            }
        }

        if (primitive == null || !primitive.toString().equals(text)) {
            // Default String
            primitive = text;
        }

        return primitive;
    }

	private static final int MAX_LENGTH_LONG = String.valueOf(Long.MAX_VALUE).length();
	private static final int MAX_LENGTH_LONG_NEGATIVE = String.valueOf(Long.MAX_VALUE).length() + 1;

	/**
     *  Try to get a Long value efficiently, avoiding Exceptions
     */
    private static Long getLong(String text)
    {
        // handle an empty string
        if (text.isEmpty()) return null;

        // if the text is too long it can't be a Long
        if (text.charAt(0) == '-') {
            if (text.length() > MAX_LENGTH_LONG_NEGATIVE) {
                return null;
	        }
        } else if (text.length() > MAX_LENGTH_LONG) {
            return null;
        }

        // Handle a leading minus sign
        int i = 0;
        if (text.charAt(0) == '-') {
            if (text.length() > 1) {
                i++;
            } else {
                return null;
            }
        }

        // Check each character is a digit
        for (; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return null;
            }
        }

        // It looks like it might be a Long, so give it a go
        try {
            return Long.parseLong(text);
        } catch (Exception e) {
            // It isn't a Long
            return null;
        }
    }

    private static final int MAX_LENGTH_INTEGER = String.valueOf(Integer.MAX_VALUE).length();
    private static final int MAX_LENGTH_INTEGER_NEGATIVE = String.valueOf(Integer.MAX_VALUE).length() + 1;

    /**
     *  Try to get an Integer value efficiently, avoiding Exceptions
     */
    private static Integer getInteger(String text) {
        // handle an empty string
        if (text.isEmpty()) return null;

        // if the text is too long it can't be an Integer
        if (text.charAt(0) == '-') {
            if (text.length() > MAX_LENGTH_INTEGER_NEGATIVE) {
                return null;
	        }
        }
        else if (text.length() > MAX_LENGTH_INTEGER) {
            return null;
        }

        // Handle a leading minus sign
        int i = 0;
        if (text.charAt(0) == '-') {
            if (text.length() > 1) {
                i++;
            } else {
                return null;
            }
        }

        // Check each character is a digit
        for (; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return null;
            }
        }

        // It looks like it might be an Integer, so give it a go
        try {
            return Integer.parseInt(text);
        } catch (Exception e) {
            // It isn't an Integer
            return null;
        }
    }

    /**
     * Try to get a Double value efficiently, avoiding Exceptions
     */
    private static Double getDouble(String text) {
        boolean foundDP = false;
        boolean foundExp = false;

        // handle an empty string
        if (text.isEmpty())
            return null;

        // Handle a leading minus sign
        int i = 0;
        if (text.charAt(0) == '-') {
            if (text.length() > 1)
                i++;
            else
                return null;
        }

        // Check each character is a digit
        for (; i < text.length(); i++) {
            char next = text.charAt(i);
            if (!Character.isDigit(next)) {
                if (next == '.') {
                    if (foundDP)
                        return null;
                    foundDP = true;
                } else if (next == 'E' || next == 'e') {
                    if (foundExp)
                        return null;
                    foundExp = true;
                } else
                    return null;
            }
        }

        // It looks like it might be a Double, so give it a go
        try {
            return Double.parseDouble(text);
        } catch (Exception e) {
            // It isn't a Double
            return null;
        }
    }
}
