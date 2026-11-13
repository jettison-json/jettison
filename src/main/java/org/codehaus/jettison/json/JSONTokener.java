/*
Copyright (c) 2002 JSON.org

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.codehaus.jettison.json;

import java.math.BigDecimal;

/**
 * A JSONTokener takes a source string and extracts characters and tokens from
 * it. It is used by the JSONObject and JSONArray constructors to parse
 * JSON source strings.
 * @author JSON.org
 * @version 2
 */
public class JSONTokener {

    private static final String USE_BIGDECIMAL_JSONTOKENER_KEY = "jettison.json.jsontokener.use_bigdecimal";
    public static final boolean USE_BIGDECIMAL_JSONTOKENER = Boolean.getBoolean( USE_BIGDECIMAL_JSONTOKENER_KEY );
    protected boolean useBigDecimal = USE_BIGDECIMAL_JSONTOKENER; 

    final static int DEFAULT_OBJECT_KEY_LIMIT = 1000000;

    static volatile int OBJECT_KEY_LIMIT = DEFAULT_OBJECT_KEY_LIMIT;

    final static int DEFAULT_NUMBER_LENGTH_LIMIT = 1000;

    static volatile int NUMBER_LENGTH_LIMIT = DEFAULT_NUMBER_LENGTH_LIMIT;

    /**
     * Set the maximum length of a number that is parsed as a BigDecimal, when
     * BigDecimal parsing is enabled (the
     * "jettison.json.jsontokener.use_bigdecimal" system property). The time
     * taken to parse a BigDecimal grows faster than linearly with its length.
     * The default value is 1000.
     * @param newNumberLengthLimit the new number length limit to set
     */
    public static void setGlobalNumberLengthLimit(int newNumberLengthLimit) {
        NUMBER_LENGTH_LIMIT = newNumberLengthLimit;
    }

    /**
     * Get the maximum length of a number that is parsed as a BigDecimal.
     * The default value is 1000.
     * @return the number length limit
     */
    public static int getGlobalNumberLengthLimit() {
        return NUMBER_LENGTH_LIMIT;
    }


    /**
     * The index of the next character.
     */
    private int myIndex;


    /**
     * The source string being tokenized.
     */
    private String mySource;


    private int threshold = OBJECT_KEY_LIMIT;

    private int recursionDepth;

    /**
     * Construct a JSONTokener from a string.
     *
     * @param s     A source string.
     */
    public JSONTokener(String s) {
        this.myIndex = 0;
        this.mySource = s.trim();
        this.threshold = OBJECT_KEY_LIMIT;
    }

    /**
     * Construct a JSONTokener from a string.
     *
     * @param s     A source string.
     * @param threshold     A source threshold.
     */
    public JSONTokener(String s, int threshold) {
        this.myIndex = 0;
        this.mySource = s.trim();
        this.threshold = threshold;
    }

    public int getThreshold() {
    	return threshold;
    }

    /**
     * Back up one character. This provides a sort of lookahead capability,
     * so that you can test for a digit or letter before attempting to parse
     * the next number or identifier.
     */
    public void back() {
        if (this.myIndex > 0) {
            this.myIndex -= 1;
        }
    }



    /**
     * Get the hex value of a character (base16).
     * @param c A character between '0' and '9' or between 'A' and 'F' or
     * between 'a' and 'f'.
     * @return  An int between 0 and 15, or -1 if c was not a hex digit.
     */
    public static int dehexchar(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F') {
            return c - ('A' - 10);
        }
        if (c >= 'a' && c <= 'f') {
            return c - ('a' - 10);
        }
        return -1;
    }


    /**
     * Determine if the source string still contains characters that next()
     * can consume.
     * @return true if not yet at the end of the source.
     */
    public boolean more() {
        return this.myIndex < this.mySource.length();
    }


    /**
     * Get the next character in the source string.
     *
     * @return The next character, or 0 if past the end of the source string.
     */
    public char next() {
        if (more()) {
            char c = this.mySource.charAt(this.myIndex);
            this.myIndex += 1;
            return c;
        }
        return 0;
    }


    /**
     * Consume the next character, and check that it matches a specified
     * character.
     * @param c The character to match.
     * @return The character.
     * @throws JSONException if the character does not match.
     */
    public char next(char c) throws JSONException {
        char n = next();
        if (n != c) {
            throw syntaxError("Expected '" + printable(c) + "' and instead saw '" +
                    printable(n) + "'.");
        }
        return n;
    }


    /**
     * Get the next n characters.
     *
     * @param n     The number of characters to take.
     * @return      A string of n characters.
     * @throws JSONException
     *   Substring bounds error if there are not
     *   n characters remaining in the source string.
     */
     public String next(int n) throws JSONException {
         int i = this.myIndex;
         int j = i + n;
         if (j >= this.mySource.length()) {
            throw syntaxError("Substring bounds error");
         }
         this.myIndex += n;
         return this.mySource.substring(i, j);
     }


    /**
     * Get the next char in the string, skipping whitespace
     * and comments (slashslash, slashstar, and hash).
     * @throws JSONException JSONException
     * @return  A character, or 0 if there are no more characters.
     */
    public char nextClean() throws JSONException {
        for (;;) {
            char c = next();
            if (c == '/') {
                switch (next()) {
                case '/':
                    do {
                        c = next();
                    } while (c != '\n' && c != '\r' && c != 0);
                    break;
                case '*':
                    for (;;) {
                        c = next();
                        if (c == 0) {
                            throw syntaxError("Unclosed comment.");
                        }
                        if (c == '*') {
                            if (next() == '/') {
                                break;
                            }
                        }
                    }
                    break;
                default:
                    if (!more()) {
                        throw syntaxError("The JSON text is malformed");
                    }
                    back();
                    return '/';
                }
            } else if (c == '#') {
                do {
                    c = next();
                } while (c != '\n' && c != '\r' && c != 0);
            } else if (c == 0 || c > ' ') {
                return c;
            }
        }
    }


    /**
     * Return the characters up to the next close quote character.
     * Backslash processing is done. The formal JSON format does not
     * allow strings in single quotes, but an implementation is allowed to
     * accept them.
     * @param quote The quoting character, either
     *      <code>"</code>&nbsp;<small>(double quote)</small> or
     *      <code>'</code>&nbsp;<small>(single quote)</small>.
     * @return      A String.
     * @throws JSONException Unterminated string.
     */
    public String nextString(char quote) throws JSONException {
        char c;
        StringBuilder sb = new StringBuilder();
        for (;;) {
            c = next();
            switch (c) {
            case 0:
            case '\n':
            case '\r':
                throw syntaxError("Unterminated string");
            case '\\':
                c = next();
                switch (c) {
                case 'b':
                    sb.append('\b');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case 'n':
                    sb.append('\n');
                    break;
                case 'f':
                    sb.append('\f');
                    break;
                case 'r':
                    sb.append('\r');
                    break;
                case 'u':
                    try {
                        sb.append((char) Integer.parseInt(next(4), 16));
                    } catch (NumberFormatException e) {
                        throw syntaxError("Illegal escape.");
                    }
                    break;
                default:
                    sb.append(c);
                }
                break;
            default:
                if (c == quote) {
                    return sb.toString();
                }
                sb.append(c);
            }
        }
    }


    /**
     * Get the text up but not including the specified character or the
     * end of line, whichever comes first.
     * @param  d A delimiter character.
     * @return   A string.
     */
    public String nextTo(char d) {
        StringBuilder sb = new StringBuilder();
        for (;;) {
            char c = next();
            if (c == d || c == 0 || c == '\n' || c == '\r') {
                if (c != 0) {
                    back();
                }
                return sb.toString().trim();
            }
            sb.append(c);
        }
    }


    /**
     * Get the text up but not including one of the specified delimeter
     * characters or the end of line, whichever comes first.
     * @param delimiters A set of delimiter characters.
     * @return A string, trimmed.
     */
    public String nextTo(String delimiters) {
        char c;
        StringBuilder sb = new StringBuilder();
        for (;;) {
            c = next();
            if (delimiters.indexOf(c) >= 0 || c == 0 ||
                    c == '\n' || c == '\r') {
                if (c != 0) {
                    back();
                }
                return sb.toString().trim();
            }
            sb.append(c);
        }
    }


    /**
     * Get the next value. The value can be a Boolean, Double/BigDecimal
     * (depending on -Djettison.json.jsontokener.use_bigdecimal), Integer,
     * JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
     * @throws JSONException If syntax error.
     *
     * @return An object.
     */
    public Object nextValue() throws JSONException {
        char c = nextClean();
        String s;

        switch (c) {
            case '"':
            case '\'':
                return nextString(c);
            case '{':
                back();
                return newJSONObject();
            case '[':
                back();
                return newJSONArray();
        }

        /*
         * Handle unquoted text. This could be the values true, false, or
         * null, or it can be a number. An implementation (such as this one)
         * is allowed to also accept non-standard forms.
         *
         * Accumulate characters until we reach the end of the text or a
         * formatting character.
         */

        StringBuilder sb = new StringBuilder();
        char b = c;
        while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
            sb.append(c);
            c = next();
        }
        back();

        /*
         * If it is true, false, or null, return the proper value.
         */

        s = sb.toString().trim();
        if (s.equals("")) {
            throw syntaxError("Missing value.");
        }
        if (s.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (s.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        if (s.equalsIgnoreCase("null")) {
            return JSONObject.EXPLICIT_NULL;
        }

        /*
         * If it might be a number, try converting it. We support the 0- and 0x-
         * conventions. If a number cannot be produced, then the value will just
         * be a string. Note that the 0-, 0x-, plus, and implied string
         * conventions are non-standard. A JSON parser is free to accept
         * non-JSON forms as long as it accepts all correct JSON forms.
         */

        if ((b >= '0' && b <= '9') || b == '.' || b == '-' || b == '+') {
            if (b == '0') {
                if (s.length() > 2 &&
                        (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
                    try {
                        return Integer.valueOf(Integer.parseInt(s.substring(2),
                                16));
                    } catch (Exception e) {
                        /* Ignore the error */
                    }
                } else {
                    try {
                        return Integer.valueOf(Integer.parseInt(s, 8));
                    } catch (Exception e) {
                        /* Ignore the error */
                    }
                }
            }
            try {
                return Integer.valueOf(s);
            } catch (Exception e) {
                try {
                    return Long.valueOf(s);
                } catch (Exception f) {
                    if (useBigDecimal && s.length() > NUMBER_LENGTH_LIMIT) {
                        throw syntaxError("Number length limit of " + NUMBER_LENGTH_LIMIT + " exceeded");
                    }
                    try {
                        if (useBigDecimal) {
                            return new BigDecimal(s);
                        } else {
                            return new Double(s);
                        }
                    }  catch (Exception g) {
                        return s;
                    }
                }
            }
        }
        return s;
    }

    protected JSONObject newJSONObject() throws JSONException {
        checkRecursionDepth();
        JSONObject object = new JSONObject(this);
        recursionDepth--;
        return object;
    }

    protected JSONArray newJSONArray() throws JSONException {
        checkRecursionDepth();
        JSONArray array = new JSONArray(this);
        recursionDepth--;
        return array;
    }

    private void checkRecursionDepth() throws JSONException {
        recursionDepth++;
        if (recursionDepth > JSONObject.RECURSION_DEPTH_LIMIT) {
            throw new JSONException("JSONTokener has reached recursion depth limit of " + JSONObject.RECURSION_DEPTH_LIMIT);
        }
    }
    
    /**
     * Skip characters until the next character is the requested character.
     * If the requested character is not found, no characters are skipped.
     * @param to A character to skip to.
     * @return The requested character, or zero if the requested character
     * is not found.
     */
    public char skipTo(char to) {
        char c;
        int index = this.myIndex;
        do {
            c = next();
            if (c == 0) {
                this.myIndex = index;
                return c;
            }
        } while (c != to);
        back();
        return c;
    }


    /**
     * Skip characters until past the requested string.
     * If it is not found, we are left at the end of the source.
     * @param to A string to skip past.
     */
    public void skipPast(String to) {
        this.myIndex = this.mySource.indexOf(to, this.myIndex);
        if (this.myIndex < 0) {
            this.myIndex = this.mySource.length();
        } else {
            this.myIndex += to.length();
        }
    }


    /**
     * Make a JSONException to signal a syntax error.
     *
     * @param message The error message.
     * @return  A JSONException object, suitable for throwing
     */
    public JSONException syntaxError(String message) {
        return new JSONException(message + toString(), 0, myIndex);
    }


    /**
     * Make a printable string of this JSONTokener, giving the current
     * position and a short excerpt of the source around it. The whole source
     * is deliberately not included, as it may be large or contain sensitive
     * data, and the result is often used in exception messages that end up
     * in logs. Control characters in the excerpt are escaped.
     *
     * @return " at character [this.myIndex] near '[excerpt]'"
     */
    public String toString() {
        return " at character " + this.myIndex + " near '" + excerpt() + "'";
    }


    /**
     * The number of characters either side of the current position to
     * include in error messages.
     */
    private static final int EXCERPT_RADIUS = 20;


    /**
     * Get an escaped excerpt of the source around the current position,
     * with "..." marking any text that was cut off.
     */
    private String excerpt() {
        int length = this.mySource.length();
        int start = Math.max(0, Math.min(this.myIndex, length) - EXCERPT_RADIUS);
        int end = Math.min(length, Math.max(this.myIndex, 0) + EXCERPT_RADIUS);
        // Don't split a surrogate pair at either end
        if (start > 0 && Character.isLowSurrogate(this.mySource.charAt(start))) {
            start--;
        }
        if (end < length && Character.isHighSurrogate(this.mySource.charAt(end - 1))) {
            end++;
        }

        StringBuilder sb = new StringBuilder();
        if (start > 0) {
            sb.append("...");
        }
        for (int i = start; i < end; i++) {
            appendEscaped(sb, this.mySource.charAt(i));
        }
        if (end < length) {
            sb.append("...");
        }
        return sb.toString();
    }


    /**
     * Append a character, escaping control and line separator characters so
     * that the result is a single printable line.
     */
    private static void appendEscaped(StringBuilder sb, char c) {
        switch (c) {
        case '\n':
            sb.append("\\n");
            break;
        case '\r':
            sb.append("\\r");
            break;
        case '\t':
            sb.append("\\t");
            break;
        default:
            if (Character.isISOControl(c) || c == ' ' || c == ' ') {
                String hex = Integer.toHexString(c);
                sb.append("\\u");
                for (int i = hex.length(); i < 4; i++) {
                    sb.append('0');
                }
                sb.append(hex);
            } else {
                sb.append(c);
            }
        }
    }


    /**
     * Get a printable form of a single character for an error message.
     */
    private static String printable(char c) {
        StringBuilder sb = new StringBuilder();
        appendEscaped(sb, c);
        return sb.toString();
    }
}