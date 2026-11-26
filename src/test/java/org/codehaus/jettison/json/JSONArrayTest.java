package org.codehaus.jettison.json;

import junit.framework.TestCase;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JSONArrayTest extends TestCase {
    public void testInvalidArraySequence() throws Exception {
    	try {
    	    new JSONArray("[32,");
    	    fail("Exception expected");
    	} catch (JSONException ex) {
    		assertTrue(ex.getMessage().startsWith("JSONArray text has a trailing ','"));
    	}
    }

    public void testInvalidArraySequence2() throws Exception {
    	try {
    	    new JSONArray("[32,34");
    	    fail("Exception expected");
    	} catch (JSONException ex) {
    		assertTrue(ex.getMessage().startsWith("Expected a ',' or ']'"));
    	}
    }

    public void testEscapingInArrayIsOnByDefault() {
      JSONArray array = new JSONArray();
      array.put("a string with / character");
      String expectedValue = "[\"a string with \\/ character\"]";
      assertEquals(expectedValue, array.toString());
    }

    public void testEscapingInArrayIsTrunedOff() throws JSONException {

      JSONObject obj = new JSONObject();
      obj.put("key", "http://example.com/foo");
      obj.setEscapeForwardSlashAlways(false);

      JSONArray array = new JSONArray();
      array.put("a string with / character");
      array.put(obj);
      array.setEscapeForwardSlashAlways(false);

      System.out.println(array.toString());
      String expectedValue = "[\"a string with / character\",{\"key\":\"http://example.com/foo\"}]";
      assertEquals(expectedValue, array.toString());
    }

    public void testInfiniteLoop() {
        String str = "[*/*A25] **";
        try {
            new JSONArray(str);
            fail("Failure expected on malformed JSON");
        } catch (JSONException ex) {
            // expected
        }
    }

    public void testInfiniteLoop2() {
        String str = "[/";
        try {
            new JSONArray(str);
            fail("Failure expected on malformed JSON");
        } catch (JSONException ex) {
            // expected
        }
    }

    public void testIssue52() throws JSONException {
        JSONObject.setGlobalRecursionDepthLimit(10);
        new JSONArray("[{}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {a:10}]");
        JSONObject.setGlobalRecursionDepthLimit(500);
    }

    // https://github.com/jettison-json/jettison/issues/60
    public void testIssue60() throws JSONException {
        List<Object> list = new ArrayList<>();
        list.add(list);
        try {
            new JSONArray(list);
        } catch (JSONException ex) {
            assertEquals(ex.getMessage(), "JSONArray has reached recursion depth limit of 500");
        }
    }

    public void testStream() throws JSONException
    {
        JSONArray jsonArray = new JSONArray(Arrays.asList("a", "b", "c"));
        String result = jsonArray.stream().map(element -> element + "-mapped").collect(Collectors.joining("|"));
        assertEquals("a-mapped|b-mapped|c-mapped", result);
    }

    public void testIssue88() throws JSONException
    {
        try {
            new JSONArray().put(1829517625, 1.0719845412539998E291);
            fail("Exception expected");
        } catch (JSONException ex) {
            assertTrue(ex.getMessage().startsWith("JSONArray index 1829517625 exceeds array length limit of "));
        }
    }

    public void testIssue96() throws JSONException
    {
        JSONArray array = new JSONArray(new JSONTokener("[\"\\xgg\"]"));
        assertEquals("xgg", array.getString(0));
    }

    public void testIssue1NonStandardHexEscapesAreNotExpanded() throws JSONException
    {
        JSONArray array = new JSONArray(new JSONTokener("[\"\\x6e\\x6f\\x6e\\x65\"]"));
        assertEquals("x6ex6fx6ex65", array.getString(0));
    }

    public void testRemoveObjectRemovesMatchingValueAndReturnsSameInstance() {
        JSONArray array = new JSONArray();
        array.put("a").put("b").put("c");

        JSONArray result = array.remove("b");

        assertSame(array, result);
        assertEquals(2, array.length());
        assertEquals("a", array.optString(0));
        assertEquals("c", array.optString(1));
    }

    public void testRemoveObjectNoMatchKeepsArrayUnchangedAndReturnsSameInstance() {
        JSONArray array = new JSONArray();
        array.put("a").put("b");

        JSONArray result = array.remove("x");

        assertSame(array, result);
        assertEquals(2, array.length());
        assertEquals("a", array.optString(0));
        assertEquals("b", array.optString(1));
    }

    public void testRemoveAtRemovesElementAtIndexAndReturnsSameInstance() {
        JSONArray array = new JSONArray();
        array.put("a").put("b").put("c");

        JSONArray result = array.removeAt(1);

        assertSame(array, result);
        assertEquals(2, array.length());
        assertEquals("a", array.optString(0));
        assertEquals("c", array.optString(1));
    }

    public void testRemoveAtOutOfBoundsThrowsIndexOutOfBoundsException() {
        JSONArray array = new JSONArray();
        array.put("a");

        try {
            array.removeAt(1);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            // expected
        }
    }

    public void testSerializationOfCyclicArrayIsRejected() throws JSONException {
        JSONArray array = new JSONArray();
        array.put(array);

        // toString() keeps its contract of returning null if the JSON text can't be produced
        assertNull(array.toString());
        try {
            array.toString(2);
            fail("Failure expected on serialization depth limit");
        } catch (JSONException ex) {
            assertTrue(ex.getMessage().contains("recursion depth limit"));
        }
        try {
            array.join(",");
            fail("Failure expected on serialization depth limit");
        } catch (JSONException ex) {
            assertTrue(ex.getMessage().contains("recursion depth limit"));
        }
        try {
            array.write(new StringWriter());
            fail("Failure expected on serialization depth limit");
        } catch (JSONException ex) {
            assertTrue(ex.getMessage().contains("recursion depth limit"));
        }
    }

    public void testSerializationOfNestedArrays() throws JSONException {
        JSONArray array = new JSONArray("[[1,[2,{\"a\":[3]}]],[]]");
        assertEquals("[[1,[2,{\"a\":[3]}]],[]]", array.toString());
        assertEquals("[[1,[2,{\"a\":[3]}]],[]]", array.write(new StringWriter()).toString());
    }

    public void testIsNull() throws Exception {
        assertTrue(new JSONArray("[null]").isNull(0));

        // Out of bounds indeces result in true
        assertTrue(new JSONArray("[0, 1, 2]").isNull(3));

        // None of the elements in this array are null, so the assertion should fail for all of them
        JSONArray ja = new JSONArray("[0, \"\", false, {}, []]");
        for (int i = 0; i < ja.length(); i++) assertFalse(ja.isNull(i));
    }

    public void testListMapCycleIsRejected() {
        List<Object> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        list.add(map);
        map.put("list", list);
        try {
            new JSONArray(list);
            fail("Failure expected on a List/Map cycle");
        } catch (JSONException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("recursion depth limit"));
        }
    }

    // https://github.com/jettison-json/jettison/issues/105
    public void testOptStringExplicitNull() throws JSONException {
        JSONArray array = new JSONArray("[null, \"value\"]");
        assertEquals("Default", array.optString(0, "Default"));
        assertEquals("", array.optString(0));
        assertEquals("value", array.optString(1, "Default"));
        assertEquals("Default", array.optString(5, "Default"));
    }

    public void testGetLongFromString() throws JSONException {
        JSONArray array = new JSONArray("[\"9007199254740993\", \"1.5\", \"1e3\", \"-42\", \"abc\"]");
        assertEquals(9007199254740993L, array.getLong(0));
        assertEquals(1L, array.getLong(1));
        assertEquals(1000L, array.getLong(2));
        assertEquals(-42L, array.getLong(3));
        try {
            array.getLong(4);
            fail("Failure expected on a non-numeric value");
        } catch (JSONException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("is not a number"));
        }
        assertEquals(7L, array.optLong(4, 7L));
        assertEquals(9007199254740993L, array.optLong(0, 7L));
    }

    // https://github.com/jettison-json/jettison/issues/34
    public void testJavaArrayValues() throws Exception {
        JSONArray array = new JSONArray();
        array.put(new String[] {"x", "y"});
        array.put(new double[] {1.5});
        String expected = "[[\"x\",\"y\"],[1.5]]";
        assertEquals(expected, array.toString());

        StringWriter writer = new StringWriter();
        array.write(writer);
        assertEquals(expected, writer.toString());

        assertEquals(expected, new JSONArray(array.toString(2)).toString());
    }

    // https://github.com/jettison-json/jettison/issues/46
    public void testLeadingByteOrderMark() throws JSONException {
        assertEquals("[1,2]", new JSONArray("\uFEFF[1,2]").toString());
    }

    // https://github.com/jettison-json/jettison/issues/6
    public void testNoOctalOrHexNumbers() throws JSONException {
        JSONArray array = new JSONArray("[010, 0x1F, 08]");
        assertEquals(Integer.valueOf(10), array.get(0));
        assertEquals("0x1F", array.get(1));
        assertEquals(Integer.valueOf(8), array.get(2));
    }

    public void testElisionAfterNestedArray() throws Exception {
        // an elided element is read as null, whatever the type of the first element
        assertEquals("[[1],null,2]", new JSONArray("[[1],,2]").toString());
        assertEquals("[1,null,2]", new JSONArray("[1,,2]").toString());
        assertEquals("[{},null,2]", new JSONArray("[{},,2]").toString());
        assertEquals("[[1],null,null,[2]]", new JSONArray("[[1],,,[2]]").toString());
        // a trailing comma before the closing bracket is still allowed
        assertEquals("[[1]]", new JSONArray("[[1],]").toString());
        // truncated input is still rejected
        try {
            new JSONArray("[[1],");
            fail("Failure expected on truncated input");
        } catch (JSONException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().startsWith("JSONArray text has a trailing ','"));
        }
        try {
            new JSONArray("[[1],,");
            fail("Failure expected on truncated input");
        } catch (JSONException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().startsWith("JSONArray text has a trailing ','"));
        }
    }

    public void testNonFiniteNumbersAreRejected() throws Exception {
        String[] inputs = {"[1e999]", "[-1e999]", "[-Infinity]", "[-NaN]", "[1, 1e999]", "{\"a\":[1e999]}"};
        for (String input : inputs) {
            try {
                if (input.startsWith("{")) {
                    new JSONObject(input);
                } else {
                    new JSONArray(input);
                }
                fail("Failure expected on a non-finite number: " + input);
            } catch (JSONException ex) {
                assertTrue(ex.getMessage(), ex.getMessage().startsWith("JSON does not allow non-finite numbers"));
            }
        }
        // finite numbers are still accepted
        assertEquals("[1.5,-2,1.0E300]", new JSONArray("[1.5, -2, 1e300]").toString());
    }

    public void testGetIntAndGetLongClipOutOfRangeValues() throws Exception {
        JSONArray array = new JSONArray("[4294967301,-4294967301,-42]");
        array.put(new BigDecimal("1e100"));
        array.put(new BigInteger("-123456789012345678901234567890"));
        assertEquals(Integer.MAX_VALUE, array.getInt(0));
        assertEquals(Integer.MIN_VALUE, array.getInt(1));
        assertEquals(-42, array.getInt(2));
        assertEquals(Integer.MAX_VALUE, array.optInt(0, 7));
        assertEquals(4294967301L, array.getLong(0));
        assertEquals(Long.MAX_VALUE, array.getLong(3));
        assertEquals(Long.MIN_VALUE, array.getLong(4));
        assertEquals(Integer.MIN_VALUE, array.getInt(4));
    }
}
