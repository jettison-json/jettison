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

    public void testBigDecimalAtNumberLengthLimit() throws Exception {
        String number = digits(JSONTokener.getGlobalNumberLengthLimit());
        Object value = bigDecimalTokener(number).nextValue();
        assertEquals(new BigDecimal(number), value);
    }

    public void testBigDecimalNumberLengthLimitExceeded() throws Exception {
        String number = digits(JSONTokener.getGlobalNumberLengthLimit() + 1);
        try {
            bigDecimalTokener(number).nextValue();
            fail("Failure expected when the number length limit is exceeded");
        } catch (JSONException ex) {
            String message = ex.getMessage();
            assertTrue(message, message.startsWith("Number length limit of 1000 exceeded at character "));
            assertTrue(message, message.length() < 150);
        }
    }

    public void testBigDecimalNumberLengthLimitInDocument() throws Exception {
        String json = "{\"a\":" + digits(5000) + "}";
        try {
            new JSONObject(bigDecimalTokener(json));
            fail("Failure expected when the number length limit is exceeded");
        } catch (JSONException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().startsWith("Number length limit of 1000 exceeded"));
        }
    }

    public void testNumberLengthLimitDoesNotApplyWithoutBigDecimal() throws Exception {
        String number = digits(5000);
        Object value = new JSONTokener(number) {
            {
                this.useBigDecimal = false;
            }
        }.nextValue();
        assertEquals(Double.class, value.getClass());
    }

    public void testNumberLengthLimitIsConfigurable() throws Exception {
        int originalLimit = JSONTokener.getGlobalNumberLengthLimit();
        JSONTokener.setGlobalNumberLengthLimit(25);
        try {
            assertEquals(new BigDecimal("12345678901234567890.1234"),
                bigDecimalTokener("12345678901234567890.1234").nextValue());
            try {
                bigDecimalTokener("12345678901234567890.12345").nextValue();
                fail("Failure expected when the number length limit is exceeded");
            } catch (JSONException ex) {
                assertTrue(ex.getMessage(), ex.getMessage().startsWith("Number length limit of 25 exceeded"));
            }
        } finally {
            JSONTokener.setGlobalNumberLengthLimit(originalLimit);
        }
    }

    private static JSONTokener bigDecimalTokener(String json) {
        return new JSONTokener(json) {
            {
                this.useBigDecimal = true;
            }
        };
    }

    /**
     * A decimal number of the given length (including the decimal point),
     * too large to be parsed as a long.
     */
    private static String digits(int length) {
        StringBuilder sb = new StringBuilder("1");
        for (int i = 1; i < length - 2; i++) {
            sb.append((char) ('0' + (i % 10)));
        }
        return sb.append(".5").toString();
    }
}
