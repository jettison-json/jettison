package org.codehaus.jettison.json;

import java.math.BigDecimal;

import junit.framework.TestCase;

public class JSONTokenerTest extends TestCase {
    
    public void testDoublePrecision() throws Exception {
        JSONTokener doubleTokener = new JSONTokener("9999999999999.9999");
        Object nextValue = doubleTokener.nextValue();
        assertEquals(Double.class, nextValue.getClass());
        assertEquals(Double.valueOf("1.0E13"), nextValue);
    }

    public void testBigDecimalPrecision() throws Exception {
        JSONTokener bigDecimalTokener = new JSONTokener("9999999999999.9999") {
            {
                this.useBigDecimal = true;
            }
        };
        Object nextValue = bigDecimalTokener.nextValue();
        assertEquals(BigDecimal.class, nextValue.getClass());
        assertEquals(new BigDecimal("9999999999999.9999"), nextValue);
    }

    public void testSyntaxErrorDoesNotIncludeWholeInput() {
        StringBuilder padding = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            padding.append('x');
        }
        String json = "{\"secret\":\"S3CR3T\",\"pad\":\"" + padding + "\" \"oops\":1}";
        try {
            new JSONObject(json);
            fail("Failure expected on malformed JSON");
        } catch (JSONException ex) {
            String message = ex.getMessage();
            assertTrue(message, message.startsWith("Expected a ',' or '}' at character "));
            assertFalse(message, message.contains("S3CR3T"));
            assertTrue(message, message.contains("...xxx"));
            assertTrue(message, message.length() < 150);
        }
    }

    public void testSyntaxErrorShowsShortInputInFull() {
        try {
            new JSONObject("{\"a\":1 \"b\":2}");
            fail("Failure expected on malformed JSON");
        } catch (JSONException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().endsWith(" near '{\"a\":1 \"b\":2}'"));
        }
    }

    public void testSyntaxErrorEscapesControlCharacters() {
        try {
            new JSONObject("{\"a\":1,\r\n\"b\"\u2028\u0007 2}");
            fail("Failure expected on malformed JSON");
        } catch (JSONException ex) {
            String message = ex.getMessage();
            assertEquals(-1, message.indexOf('\n'));
            assertEquals(-1, message.indexOf('\r'));
            assertEquals(-1, message.indexOf('\u2028'));
            assertEquals(-1, message.indexOf('\u0007'));
            assertTrue(message, message.contains("\\r\\n\"b\"\\u2028\\u0007"));
        }
    }

    public void testSyntaxErrorEscapesUnexpectedCharacter() {
        JSONTokener tokener = new JSONTokener("a\nb");
        tokener.next();
        try {
            tokener.next('b');
            fail("Failure expected on unexpected character");
        } catch (JSONException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().startsWith("Expected 'b' and instead saw '\\n'."));
            assertEquals(-1, ex.getMessage().indexOf('\n'));
        }
    }

    public void testSyntaxErrorColumnIsUnchanged() {
        try {
            new JSONObject("{\"a\":1 \"b\":2}");
            fail("Failure expected on malformed JSON");
        } catch (JSONException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("at character " + ex.getColumn() + " "));
        }
    }

}
