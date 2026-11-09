package org.codehaus.jettison.json;

import junit.framework.TestCase;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    }
}
