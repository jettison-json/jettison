package org.codehaus.jettison.json;

import junit.framework.TestCase;
import org.codehaus.jettison.JSONSequenceTooLargeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JSONObjectTest extends TestCase {

    public void testEquals() throws Exception {
    	JSONObject aJsonObj = new JSONObject("{\"x\":\"y\"}");
    	JSONObject bJsonObj = new JSONObject("{\"x\":\"y\"}");
    	assertEquals(aJsonObj,bJsonObj);
    }
    
    public void testToLong() throws Exception {
    	String json = "{\"key\":\"10001325703114005\"}";
    	JSONObject jsonObject = new JSONObject(json);
    	long actual = jsonObject.getLong("key");
    	long expected = 10001325703114005L;
    	assertTrue(expected < Long.MAX_VALUE);
    	assertEquals(expected, actual);
    }
    
    public void testNotEquals() throws Exception {
    	JSONObject aJsonObj = new JSONObject("{\"x\":\"y\"}");
    	JSONObject bJsonObj = new JSONObject("{\"x\":\"b\"}");
    	assertTrue(!aJsonObj.equals(bJsonObj));
    }
    
    public void testAppend() throws Exception {
    	JSONObject obj = new JSONObject();
    	obj.append("arr", "val1");
    	obj.append("arr", "val2");
    	obj.append("arr", "val3");
    	assertEquals("{\"arr\":[\"val1\",\"val2\",\"val3\"]}", obj.toString());
    }
    
    public void testInvalidArraySequence() throws Exception {
    	try {
    	    new JSONObject("{\"a\":[");
    	    fail("Exception expected");
    	} catch (JSONException ex) {
    		assertTrue(ex.getMessage().startsWith("JSONArray text must end with ']'"));
    	}
    }
    
    public void testInvalidArraySequence2() throws Exception {
    	try {
    	    new JSONObject("{\"a\":[,");
    	    fail("Exception expected");
    	} catch (JSONException ex) {
    		assertTrue(ex.getMessage().startsWith("JSONArray text has a trailing ','"));
    	}
    }
    
    public void testInvalidArraySequence3() throws Exception {
    	String corruptedJSON = "{\"a\":[[\"b\",{\"revision\": 760839}],";
    	try {
    	    new JSONObject(corruptedJSON);
    	    fail("Exception expected");
    	} catch (JSONException ex) {
    		assertTrue(ex.getMessage().startsWith("JSONArray text has a trailing ','"));
    	}
    }
    
    public void testNullInQuotesGetString() throws Exception {
    	JSONObject obj = new JSONObject("{\"a\":\"null\"}");
    	assertEquals("null", obj.getString("a"));
    }
    
    public void testExplicitNullGetString() throws Exception {
    	JSONObject obj = new JSONObject("{\"a\":null}");
    	assertNull(obj.getString("a"));
    }
    public void testExplicitNullIsNull() throws Exception {
        JSONObject obj = new JSONObject("{\"a\":null}");
        assertTrue(obj.isNull("a"));
    }
    public void testMissingIsNull() throws Exception {
        JSONObject obj = new JSONObject("{\"a\":null}");
        assertTrue(obj.isNull("b"));
    }

    public void testIsNullValueOnSelf() throws Exception {
        JSONObject jo = new JSONObject("{\"a\":null}");
        assertTrue(JSONObject.isNullValue(jo.optJSONObject("a")));
        assertTrue(JSONObject.isNullValue(jo.opt("a")));
    }
    public void testIsNullValueOnSelfMissing() throws Exception {
        JSONObject jo = new JSONObject("{\"a\":null}");
        assertTrue(JSONObject.isNullValue(jo.optJSONObject("b")));
        assertTrue(JSONObject.isNullValue(jo.opt("b")));
    }
    public void testIsNullValueOnJONulls() throws Exception {
        assertTrue(JSONObject.isNullValue(JSONObject.NULL));
        assertTrue(JSONObject.isNullValue(JSONObject.EXPLICIT_NULL));
    }
    public void testIsNullValueVarious() throws Exception {
        assertFalse(JSONObject.isNullValue(""));
        assertFalse(JSONObject.isNullValue(0));
        assertFalse(JSONObject.isNullValue(false));
        assertFalse(JSONObject.isNullValue(new JSONObject()));
    }
    
    public void testSlashEscapingTurnedOnByDefault() throws Exception {
       JSONObject obj = new JSONObject();
       obj.put("key", "http://example.com/foo");
       assertEquals("{\"key\":\"http:\\/\\/example.com\\/foo\"}", obj.toString());
        
        obj = new JSONObject();
        obj.put("key", "\\\\");
        assertEquals("{\"key\":\"\\\\\\\\\"}", obj.toString());
    }
    
    public void testForwardSlashEscapingModifiedfBySetter() throws Exception {
      JSONObject obj = new JSONObject();
      obj.put("key", "http://example.com/foo");
      assertEquals(obj.toString(), "{\"key\":\"http:\\/\\/example.com\\/foo\"}");
      obj.setEscapeForwardSlashAlways(false);
      assertEquals(obj.toString(), "{\"key\":\"http://example.com/foo\"}");
      obj.setEscapeForwardSlashAlways(true);
      assertEquals(obj.toString(), "{\"key\":\"http:\\/\\/example.com\\/foo\"}");
    }

    public void testMalformedObject() throws Exception {
       try {
           new JSONObject("{/");
           fail("Failure expected on malformed JSON");
       } catch (JSONException ex) {
           // expected
       }
    }

    public void testMalformedObject2() throws Exception {
        try {
            new JSONObject("{x");
            fail("Failure expected on malformed JSON");
        } catch (JSONException ex) {
            // expected
        }
    }

    public void testMalformedObject3() throws Exception {
        try {
            new JSONObject("{/x");
            fail("Failure expected on malformed JSON");
        } catch (JSONException ex) {
            // expected
        }
    }

    public void testMalformedObject4() throws Exception {
        try {
            new JSONObject("{/*");
            fail("Failure expected on malformed JSON");
        } catch (JSONException ex) {
            // expected
        }
    }

    public void testMalformedObject5() throws Exception {
        try {
            new JSONObject("{//");
            fail("Failure expected on malformed JSON");
        } catch (JSONException ex) {
            // expected
        }
    }

    public void testMalformedArray() throws Exception {
        try {
            new JSONObject("{[/");
            fail("Failure expected on malformed JSON");
        } catch (JSONException ex) {
            // expected
        }
    }

    // https://github.com/jettison-json/jettison/issues/52
    public void testIssue52() throws Exception {
        Map<String,Object> map = new HashMap<>();
        map.put("t",map);
        new JSONObject(map);
    }

    // https://github.com/jettison-json/jettison/issues/52
    public void testIssue52Recursive() throws Exception {
        try {
            Map<String, Object> map = new HashMap<>();
            Map<String, Object> map2 = new HashMap<>();
            map.put("t", map2);
            map2.put("t", map);
            new JSONObject(map);
            fail("Failure expected");
        } catch (JSONException e) {
            assertTrue(e.getMessage().contains("JSONObject has reached recursion depth limit"));
            // expected
        }
    }

    // https://github.com/jettison-json/jettison/issues/45
    public void testFuzzerTestCase() throws Exception, JSONException {
        try {
            new JSONObject("{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{\"G\":[30018084,1,6,32768,1,1,6,1,1]}:[3,1,6,32768,1,1,6,1,1]}:[3,1,6,32768,1,1,6,1,1]}:[3,1,6,32768,1,1,6,1,1]}:[3,1,6,32768,1,1,6,1,1]}:[3,1,6,32768,1,1,6,1,1]}:[3,1,6,32768,1,1,6,1,1]}:[3,1,6,38,1,1,6,1,1]}:[3,1,6,32768,1,1,6,1,1]}:[3,1,6,32768,1,1,6,1,1]}:[3,1,6,32768,1,1,6,1,1]}:[3,1,6,32768,1,1,6,1,1]}:[3,1,6,32768,1,1,6,1,1]}:[3,1,6,32768,1,1,6,1,0]}:[3,1,6,32768,1,1,6,1,1]}:[3,1,6,32768,1,340282366920938463463374607431768211458,6,1,1]}:[32768,1,1,6,1,0]}:[3,1,6,32768,1,1,6,1,1]}:[3,1,6,32768,1,340282366920938463463374607431768211458,6,1,1]}:[3,1,6,32768,1,1,6,1,1]}:[3,1,6,32768,9 68,1,127,1,1]}:[3,1,6,32768,1,1,6,1,1]}:[3,1,6,32768,1,1,6,1,1]}:[3,1,6,32768,1,1,6,1,1]}:[3,1,6,32768,1,1,6,1,1]}:[3,1,6,32768,1,1,6,1,1]}:[3,1,6,32768,1,1,6,1,9223372036854775807]}:[3,1,6,32768,1,1,6,1,1]}:[3,1,10,32768,1,1,6,1,1]}");
            fail("Failure expected");
        } catch (JSONException ex) {
            // expected
            assertTrue(ex.getMessage().contains("Expected a key"));
        }
    }

    public void testFuzzerTestCase2() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            sb.append("{\"key\":");
        }
        try {
            new JSONObject(sb.toString());
            fail("Failure expected");
        } catch (JSONException e) {
            assertTrue(e.getMessage().contains("JSONTokener has reached recursion depth limit"));
            // expected
        }
    }

    public void testIssue58() throws JSONException {
        Map<String, Object> map = new HashMap<>();
        map.put("request", "{\"exclude\":[\".\",\"?\",\"+\",\"*\",\"|\",\"{\",\"}\",\"[\",\"]\",\"(\",\")\",\"\\\"\",\"\\\\\",\"#\",\"@\",\"&\",\"<\",\">\",\"~\"]}");
        JSONObject jsonObject = new JSONObject(map);
        JSONObject jsonObject1 = new JSONObject(jsonObject.toString());
    }

    public void testIssue3DefaultObjectKeyLimitIsEnabled() {
        assertTrue(new JSONTokener("{}").getThreshold() > 0);
    }

    public void testIssue3DefaultObjectKeyLimitEnforcedForJSONObjectStringConstructor() throws JSONException {
        int originalLimit = JSONTokener.OBJECT_KEY_LIMIT;
        JSONTokener.OBJECT_KEY_LIMIT = 2;
        try {
            new JSONObject("{\"k1\":1,\"k2\":2,\"k3\":3}");
            fail("Failure expected when default object key limit is exceeded");
        } catch (JSONSequenceTooLargeException ex) {
            assertTrue(ex.getMessage().contains("Threshold has been exceeded"));
        } finally {
            JSONTokener.OBJECT_KEY_LIMIT = originalLimit;
        }
    }

    public void testIssue4RejectsArrayValuedObjectKeys() {
        // CVE-2023-5072 analog: an array-valued key would otherwise be stringified at every
        // serialization level, doubling the escaped characters per nesting level and leading
        // to an exponential memory blow-up (OutOfMemoryError) from a tiny input.
        try {
            new JSONObject("{[1,2,3]:1}");
            fail("Failure expected when an object key is an array");
        } catch (JSONException ex) {
            assertTrue(ex.getMessage().contains("Expected a key"));
        }
    }

    public void testIssue4RejectsNestedArrayKeyBlowUp() {
        try {
            new JSONObject("{[{[{[1]:1}]:1}]:1}");
            fail("Failure expected for nested array-valued keys");
        } catch (JSONException ex) {
            assertTrue(ex.getMessage().contains("Expected a key"));
        }
    }

    public void testSerializationOfCyclicObjectIsRejected() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("self", obj);
        assertSerializationDepthExceeded(obj);
    }

    public void testSerializationOfIndirectCycleIsRejected() throws JSONException {
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        array.put(obj);
        obj.put("array", array);
        assertSerializationDepthExceeded(obj);
    }

    public void testSerializationOfMaximumParseableDepth() throws JSONException {
        // The parser accepts up to the recursion depth limit of nested objects,
        // so serialization must accept the same structures.
        int limit = JSONObject.getGlobalRecursionDepthLimit();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < limit; i++) {
            sb.append("{\"a\":");
        }
        sb.append("{}");
        for (int i = 1; i < limit; i++) {
            sb.append('}');
        }
        String json = sb.toString();
        JSONObject obj = new JSONObject(json);

        assertEquals(json, obj.toString());
        assertNotNull(obj.toString(1));
        assertEquals(json, obj.write(new StringWriter()).toString());
    }

    public void testSerializationDepthLimitIsConfigurable() throws JSONException {
        int originalLimit = JSONObject.getGlobalRecursionDepthLimit();
        JSONObject.setGlobalRecursionDepthLimit(5);
        try {
            // Nesting depths 0 to 5 are allowed
            JSONObject allowed = nestedObjects(6);
            assertEquals("{\"a\":{\"a\":{\"a\":{\"a\":{\"a\":{}}}}}}", allowed.toString());
            assertNotNull(allowed.toString(2));
            allowed.write(new StringWriter());

            assertSerializationDepthExceeded(nestedObjects(7));
        } finally {
            JSONObject.setGlobalRecursionDepthLimit(originalLimit);
        }
    }

    private static JSONObject nestedObjects(int count) throws JSONException {
        JSONObject root = new JSONObject();
        JSONObject current = root;
        for (int i = 1; i < count; i++) {
            JSONObject child = new JSONObject();
            current.put("a", child);
            current = child;
        }
        return root;
    }

    private static void assertSerializationDepthExceeded(JSONObject obj) {
        // toString() keeps its contract of returning null if the JSON text can't be produced
        assertNull(obj.toString());
        try {
            obj.toString(2);
            fail("Failure expected on serialization depth limit");
        } catch (JSONException ex) {
            assertTrue(ex.getMessage().contains("recursion depth limit"));
        }
        try {
            obj.write(new StringWriter());
            fail("Failure expected on serialization depth limit");
        } catch (JSONException ex) {
            assertTrue(ex.getMessage().contains("recursion depth limit"));
        }
    }

    public void testToStringOfDeeplyNestedStructure() throws JSONException {
        // Nested objects and arrays are appended to one builder, rather than
        // each making a string that's copied into the enclosing one, which
        // copied the innermost text once per level
        int depth = JSONObject.getGlobalRecursionDepthLimit();
        StringBuilder json = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            json.append(i % 2 == 0 ? "{\"a\":" : "[1,");
        }
        json.append("\"leaf\"");
        for (int i = depth - 1; i >= 0; i--) {
            json.append(i % 2 == 0 ? '}' : ']');
        }
        JSONObject obj = new JSONObject(json.toString());
        assertEquals(json.toString(), obj.toString());
        assertEquals(json.toString(), new JSONObject(obj.toString(2)).toString());
    }

    public void testIndentedToStringOfNestedValues() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("a", new JSONObject().put("b", new JSONArray().put(1).put(new JSONObject())));
        obj.put("c", new JSONArray().put(new JSONArray().put("d")));
        obj.put("e", new int[] {1, 2});
        obj.put("f", new JSONString() {
            public String toJSONString() {
                return "{\"g\":true}";
            }
        });
        assertEquals("{\n  \"a\": {\n    \"b\": [\n      1,\n      {}\n    ]\n  },\n  \"c\": [[\"d\"]],\n"
            + "  \"e\": [\n    1,\n    2\n  ],\n  \"f\": {\"g\":true}\n}", obj.toString(2));
        assertEquals("{\"a\":{\"b\":[1,{}]},\"c\":[[\"d\"]],\"e\":[1,2],\"f\":{\"g\":true}}", obj.toString());
    }

    public void testDuplicateKeysAreAccumulatedIntoArray() throws JSONException {
        JSONObject obj = new JSONObject("{\"a\":1,\"a\":2,\"a\":3}");
        assertEquals(1, obj.length());
        assertEquals("[1,2,3]", obj.getJSONArray("a").toString());
    }

    public void testDuplicateKeysCannotExceedArrayLengthLimit() throws JSONException {
        int originalLimit = JSONArray.getGlobalArrayLengthLimit();
        JSONArray.setGlobalArrayLengthLimit(3);
        try {
            // Accumulating up to the limit is allowed
            JSONObject obj = new JSONObject("{\"a\":1,\"a\":2,\"a\":3}");
            assertEquals(3, obj.getJSONArray("a").length());

            try {
                new JSONObject("{\"a\":1,\"a\":2,\"a\":3,\"a\":4}");
                fail("Failure expected when duplicate keys exceed the array length limit");
            } catch (JSONSequenceTooLargeException ex) {
                assertTrue(ex.getMessage().contains("JSONArray length limit of 3 exceeded"));
            }
        } finally {
            JSONArray.setGlobalArrayLengthLimit(originalLimit);
        }
    }

    public void testDuplicateKeyCannotGrowArrayValueBeyondLimit() throws JSONException {
        int originalLimit = JSONArray.getGlobalArrayLengthLimit();
        JSONArray.setGlobalArrayLengthLimit(3);
        try {
            // A duplicate key appends to an existing array value
            try {
                new JSONObject("{\"a\":[1,2,3],\"a\":4}");
                fail("Failure expected when a duplicate key exceeds the array length limit");
            } catch (JSONSequenceTooLargeException ex) {
                assertTrue(ex.getMessage().contains("JSONArray length limit of 3 exceeded"));
            }
        } finally {
            JSONArray.setGlobalArrayLengthLimit(originalLimit);
        }
    }

    public void testAccumulate() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.accumulate("a", 1);
        assertEquals(1, obj.get("a"));
        obj.accumulate("a", 2);
        obj.accumulate("a", 3);
        assertEquals("[1,2,3]", obj.getJSONArray("a").toString());
    }

    public void testAccumulateCannotExceedArrayLengthLimit() throws JSONException {
        int originalLimit = JSONArray.getGlobalArrayLengthLimit();
        JSONArray.setGlobalArrayLengthLimit(3);
        try {
            JSONObject obj = new JSONObject();
            obj.accumulate("a", 1);
            obj.accumulate("a", 2);
            obj.accumulate("a", 3);
            try {
                obj.accumulate("a", 4);
                fail("Failure expected when accumulated values exceed the array length limit");
            } catch (JSONSequenceTooLargeException ex) {
                assertTrue(ex.getMessage().contains("JSONArray length limit of 3 exceeded"));
            }
            assertEquals("[1,2,3]", obj.getJSONArray("a").toString());
        } finally {
            JSONArray.setGlobalArrayLengthLimit(originalLimit);
        }
    }

    public void testAccumulateOntoSingleValueRespectsArrayLengthLimit() throws JSONException {
        int originalLimit = JSONArray.getGlobalArrayLengthLimit();
        JSONArray.setGlobalArrayLengthLimit(1);
        try {
            // Accumulating a second value turns the single value into a two-element array
            JSONObject obj = new JSONObject();
            obj.accumulate("a", 1);
            try {
                obj.accumulate("a", 2);
                fail("Failure expected when accumulated values exceed the array length limit");
            } catch (JSONSequenceTooLargeException ex) {
                assertTrue(ex.getMessage().contains("JSONArray length limit of 1 exceeded"));
            }
            assertEquals(1, obj.get("a"));
        } finally {
            JSONArray.setGlobalArrayLengthLimit(originalLimit);
        }
    }

    public void testAppendCannotExceedArrayLengthLimit() throws JSONException {
        int originalLimit = JSONArray.getGlobalArrayLengthLimit();
        JSONArray.setGlobalArrayLengthLimit(3);
        try {
            JSONObject obj = new JSONObject();
            obj.append("arr", "val1");
            obj.append("arr", "val2");
            obj.append("arr", "val3");
            try {
                obj.append("arr", "val4");
                fail("Failure expected when appended values exceed the array length limit");
            } catch (JSONSequenceTooLargeException ex) {
                assertTrue(ex.getMessage().contains("JSONArray length limit of 3 exceeded"));
            }
            assertEquals(3, obj.getJSONArray("arr").length());
        } finally {
            JSONArray.setGlobalArrayLengthLimit(originalLimit);
        }
    }

    public void testMapListCycleIsRejected() {
        Map<String, Object> map = new HashMap<>();
        List<Object> list = new ArrayList<>();
        map.put("list", list);
        list.add(map);
        try {
            new JSONObject(map);
            fail("Failure expected on a Map/List cycle");
        } catch (JSONException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("recursion depth limit"));
        }
    }

    public void testPutMapWithMapListCycleIsRejected() {
        Map<String, Object> map = new HashMap<>();
        List<Object> list = new ArrayList<>();
        map.put("list", list);
        list.add(map);
        try {
            new JSONObject().put("map", map);
            fail("Failure expected on a Map/List cycle");
        } catch (JSONException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("recursion depth limit"));
        }
    }

    public void testAlternatingMapListNestingBeyondLimitIsRejected() {
        try {
            new JSONObject(nestedMapsAndLists(JSONObject.getGlobalRecursionDepthLimit() + 100));
            fail("Failure expected on alternating Map/List nesting beyond the recursion depth limit");
        } catch (JSONException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("recursion depth limit"));
        }
    }

    public void testAlternatingMapListNestingWithinLimit() throws JSONException {
        int levels = 100;
        Object current = new JSONObject(nestedMapsAndLists(levels));
        // Every level is converted to a JSONObject or JSONArray
        for (int i = 0; i < levels - 1; i++) {
            if (i % 2 == 0) {
                assertTrue("Level " + i, current instanceof JSONObject);
                current = ((JSONObject) current).get("a");
            } else {
                assertTrue("Level " + i, current instanceof JSONArray);
                current = ((JSONArray) current).get(0);
            }
        }
        assertTrue(current instanceof JSONArray);
        assertEquals(0, ((JSONArray) current).length());
    }

    public void testAlternatingMapListNestingRespectsConfiguredLimit() throws JSONException {
        int originalLimit = JSONObject.getGlobalRecursionDepthLimit();
        JSONObject.setGlobalRecursionDepthLimit(5);
        try {
            // Nesting depths 0 to 5 are allowed
            new JSONObject(nestedMapsAndLists(6));
            try {
                new JSONObject(nestedMapsAndLists(7));
                fail("Failure expected on alternating Map/List nesting beyond the recursion depth limit");
            } catch (JSONException ex) {
                assertTrue(ex.getMessage(), ex.getMessage().contains("recursion depth limit of 5"));
            }
        } finally {
            JSONObject.setGlobalRecursionDepthLimit(originalLimit);
        }
    }

    /**
     * Build a structure of the given number of levels, alternating Maps (at
     * even levels, with the next level under the key "a") and Lists (at odd
     * levels, with the next level as the only element). The innermost level is
     * empty.
     */
    private static Map<String, Object> nestedMapsAndLists(int levels) {
        Object inner = (levels - 1) % 2 == 0 ? new HashMap<String, Object>() : new ArrayList<Object>();
        for (int level = levels - 2; level >= 0; level--) {
            if (level % 2 == 0) {
                Map<String, Object> map = new HashMap<>();
                map.put("a", inner);
                inner = map;
            } else {
                List<Object> list = new ArrayList<>();
                list.add(inner);
                inner = list;
            }
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> root = (Map<String, Object>) inner;
        return root;
    }

    // https://github.com/jettison-json/jettison/issues/105
    public void testOptStringExplicitNull() throws JSONException {
        JSONObject obj = new JSONObject("{\"name\": null, \"other\": \"value\"}");
        assertEquals("Default Name", obj.optString("name", "Default Name"));
        assertNull(obj.optString("name", null));
        assertEquals("", obj.optString("name"));
        assertEquals("value", obj.optString("other", "Default Name"));
        assertEquals("Default Name", obj.optString("missing", "Default Name"));
        // a JSONObject.NULL value that was put explicitly keeps its existing behaviour
        assertEquals("null", new JSONObject().put("name", JSONObject.NULL).optString("name", "Default Name"));
    }

    // https://github.com/jettison-json/jettison/issues/78
    public void testOptReturnsDefaultForWrongType() throws JSONException {
        JSONObject obj = new JSONObject("{\"string\": \"abc\", \"null\": null, "
            + "\"object\": {}, \"array\": []}");
        for (String key : new String[] {"string", "null", "object", "array", "missing"}) {
            assertTrue(key, obj.optBoolean(key, true));
            assertFalse(key, obj.optBoolean(key));
            assertEquals(key, 9.5, obj.optDouble(key, 9.5), 0.0);
            assertTrue(key, Double.isNaN(obj.optDouble(key)));
            assertEquals(key, 7, obj.optInt(key, 7));
            assertEquals(key, 0, obj.optInt(key));
            assertEquals(key, 7L, obj.optLong(key, 7L));
            assertEquals(key, 0L, obj.optLong(key));
        }
    }

    public void testOptConvertsCompatibleValues() throws JSONException {
        JSONObject obj = new JSONObject("{\"bool\": \"TRUE\", \"int\": 42, "
            + "\"intString\": \"42\", \"double\": \"1.5\"}");
        assertTrue(obj.optBoolean("bool", false));
        assertEquals(42, obj.optInt("int", 7));
        assertEquals(42, obj.optInt("intString", 7));
        assertEquals(42L, obj.optLong("intString", 7L));
        assertEquals(1.5, obj.optDouble("double", 9.5), 0.0);
        assertEquals(1, obj.optInt("double", 7));
    }

    public void testGetLongFromString() throws JSONException {
        JSONObject obj = new JSONObject("{\"big\": \"9007199254740993\", \"fraction\": \"1.5\", "
            + "\"exponent\": \"1e3\", \"negative\": \"-42\", \"invalid\": \"abc\"}");
        assertEquals(9007199254740993L, obj.getLong("big"));
        assertEquals(1L, obj.getLong("fraction"));
        assertEquals(1000L, obj.getLong("exponent"));
        assertEquals(-42L, obj.getLong("negative"));
        try {
            obj.getLong("invalid");
            fail("Failure expected on a non-numeric value");
        } catch (JSONException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("is not a number"));
        }
        assertEquals(7L, obj.optLong("invalid", 7L));
        assertEquals(1L, obj.optLong("fraction", 7L));
    }

    // https://github.com/jettison-json/jettison/issues/34
    public void testJavaArrayValues() throws Exception {
        String[] strings = new String[] {"x", "y"};
        JSONObject obj = new JSONObject();
        obj.put("array", strings);
        assertEquals("{\"array\":[\"x\",\"y\"]}", obj.toString());
        // the stored value is unchanged
        assertSame(strings, obj.get("array"));

        obj = new JSONObject();
        obj.put("ints", new int[] {1, 2});
        obj.put("booleans", new boolean[] {true});
        obj.put("nested", new String[][] {{"a"}, {"b", "c"}});
        obj.put("objects", new Object[] {"s", 1, null});
        obj.put("empty", new long[0]);
        String expected = "{\"ints\":[1,2],\"booleans\":[true],\"nested\":[[\"a\"],[\"b\",\"c\"]],"
            + "\"objects\":[\"s\",1,null],\"empty\":[]}";
        assertEquals(expected, obj.toString());

        StringWriter writer = new StringWriter();
        obj.write(writer);
        assertEquals(expected, writer.toString());

        assertEquals(expected, new JSONObject(obj.toString(2)).toString());
    }

    public void testSelfReferencingJavaArray() throws Exception {
        Object[] array = new Object[1];
        array[0] = array;
        JSONObject obj = new JSONObject().put("array", array);
        // toString() swallows the depth limit exception and returns null
        assertNull(obj.toString());
        try {
            obj.toString(2);
            fail("Failure expected on a self-referencing array");
        } catch (JSONException ex) {
            // expected
        }
        try {
            obj.write(new StringWriter());
            fail("Failure expected on a self-referencing array");
        } catch (JSONException ex) {
            // expected
        }
    }

    // https://github.com/jettison-json/jettison/issues/46
    public void testLeadingByteOrderMark() throws JSONException {
        assertEquals("{\"a\":1}", new JSONObject("\uFEFF{\"a\":1}").toString());
        assertEquals("{\"a\":1}", new JSONObject("\uFEFF  {\"a\":1}").toString());
        assertEquals("{\"a\":1}", new JSONObject(new JSONTokener("\uFEFF{\"a\":1}", 10)).toString());
        // a byte order mark is only skipped at the very start of the input
        assertEquals("\uFEFFb", new JSONObject("{\"a\":\"\uFEFFb\"}").getString("a"));
        try {
            new JSONObject("\uFEFF\uFEFF{\"a\":1}");
            fail("Failure expected on a second byte order mark");
        } catch (JSONException ex) {
            // expected
        }
    }

    // https://github.com/jettison-json/jettison/issues/6
    public void testNoOctalOrHexNumbers() throws JSONException {
        JSONObject obj = new JSONObject("{\"octal\": 010, \"octal2\": 0777, \"notOctal\": 09, "
            + "\"negative\": -010, \"zero\": 0, \"fraction\": 0.5, \"hex\": 0x1F, \"hex2\": 0XAB}");
        // leading zeros are read as decimal, not octal
        assertEquals(Integer.valueOf(10), obj.get("octal"));
        assertEquals(Integer.valueOf(777), obj.get("octal2"));
        assertEquals(Integer.valueOf(9), obj.get("notOctal"));
        assertEquals(Integer.valueOf(-10), obj.get("negative"));
        assertEquals(Integer.valueOf(0), obj.get("zero"));
        assertEquals(0.5, obj.getDouble("fraction"), 0.0);
        // hex values are not converted to numbers
        assertEquals("0x1F", obj.get("hex"));
        assertEquals("0XAB", obj.get("hex2"));
        assertEquals(7, obj.optInt("hex", 7));
        // the values are serialized as standard JSON
        assertEquals("{\"octal\":10,\"octal2\":777,\"notOctal\":9,\"negative\":-10,\"zero\":0,"
            + "\"fraction\":0.5,\"hex\":\"0x1F\",\"hex2\":\"0XAB\"}", obj.toString());
    }

    public void testPrettyPrintEscapeForwardSlash() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.setEscapeForwardSlashAlways(false);
        obj.put("a/b", "c/d");
        assertEquals("{\"a/b\":\"c/d\"}", obj.toString());
        // the pretty printed key is escaped in the same way as the compact output
        assertEquals("{\n  \"a/b\": \"c/d\"\n}", obj.toString(2));

        obj.setEscapeForwardSlashAlways(true);
        assertEquals("{\"a\\/b\":\"c\\/d\"}", obj.toString());
        assertEquals("{\n  \"a\\/b\": \"c\\/d\"\n}", obj.toString(2));
    }

    public void testWriteWithIgnoredLastKey() throws Exception {
        JSONObject obj = new JSONObject(Arrays.asList("x"));
        obj.put("a", 1);
        obj.put("b", 2);
        obj.put("x", 3);
        StringWriter writer = new StringWriter();
        obj.write(writer);
        // no trailing ',' after the last entry that is written
        assertEquals("{\"a\":1,\"b\":2}", writer.toString());

        obj = new JSONObject(Arrays.asList("x"));
        obj.put("x", 3);
        obj.put("a", 1);
        writer = new StringWriter();
        obj.write(writer);
        assertEquals("{\"a\":1}", writer.toString());
    }

    public void testMapWithNonStringKeys() throws Exception {
        Map<Object, Object> nested = new LinkedHashMap<Object, Object>();
        nested.put(2L, "y");
        Map<Object, Object> map = new LinkedHashMap<Object, Object>();
        map.put(1, "x");
        map.put(Boolean.TRUE, nested);
        map.put("s", Arrays.asList(1, 2));
        JSONObject obj = new JSONObject(map);

        // the keys are converted to strings, so they can be looked up
        assertTrue(obj.has("1"));
        assertEquals("x", obj.getString("1"));
        assertEquals("y", obj.getJSONObject("true").getString("2"));
        assertEquals(2, obj.getJSONArray("s").length());

        // write() and toString() give the same output
        String expected = "{\"1\":\"x\",\"true\":{\"2\":\"y\"},\"s\":[1,2]}";
        assertEquals(expected, obj.toString());
        StringWriter writer = new StringWriter();
        obj.write(writer);
        assertEquals(expected, writer.toString());
    }

    public void testSerializationWithNullValues() throws Exception {
        JSONObject obj = new JSONObject("{\"a\":null,\"b\":[1,null],\"c\":{\"d\":null}}");
        obj.put("e", JSONObject.NULL);

        JSONObject copy = (JSONObject) serializeAndDeserialize(obj);
        assertEquals(obj.toString(), copy.toString());

        // the null values are resolved to the JSONObject.NULL and EXPLICIT_NULL singletons
        assertSame(JSONObject.EXPLICIT_NULL, copy.get("a"));
        assertSame(JSONObject.NULL, copy.get("e"));
        assertSame(JSONObject.EXPLICIT_NULL, copy.getJSONArray("b").get(1));
        assertTrue(copy.isNull("a"));
        assertEquals("default", copy.optString("a", "default"));
        assertEquals("default", copy.getJSONObject("c").optString("d", "default"));

        JSONArray array = new JSONArray().put(JSONObject.NULL).put(1);
        JSONArray arrayCopy = (JSONArray) serializeAndDeserialize(array);
        assertEquals(array.toString(), arrayCopy.toString());
        assertSame(JSONObject.NULL, arrayCopy.get(0));
    }

    private static Object serializeAndDeserialize(Object object) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        out.writeObject(object);
        out.close();
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        try {
            return in.readObject();
        } finally {
            in.close();
        }
    }

    public void testGetIntAndGetLongClipOutOfRangeValues() throws Exception {
        JSONObject obj = new JSONObject("{\"big\":4294967301,\"small\":-4294967301,\"int\":-42,"
            + "\"string\":\"4294967301\"}");
        obj.put("bigDecimal", new BigDecimal("12345678901234567890.5"));
        obj.put("negBigDecimal", new BigDecimal("-1e100"));
        obj.put("fraction", new BigDecimal("3.7"));
        obj.put("nearMax", new BigDecimal("9223372036854775806"));
        obj.put("bigInteger", new BigInteger("123456789012345678901234567890"));
        obj.put("negBigInteger", new BigInteger("-123456789012345678901234567890"));

        // values outside the int range are clipped, as for strings and doubles, not wrapped
        assertEquals(Integer.MAX_VALUE, obj.getInt("big"));
        assertEquals(Integer.MIN_VALUE, obj.getInt("small"));
        assertEquals(Integer.MAX_VALUE, obj.getInt("string"));
        assertEquals(Integer.MAX_VALUE, obj.getInt("bigDecimal"));
        assertEquals(Integer.MIN_VALUE, obj.getInt("negBigDecimal"));
        assertEquals(Integer.MAX_VALUE, obj.getInt("bigInteger"));
        assertEquals(Integer.MAX_VALUE, obj.optInt("big", 7));
        assertEquals(-42, obj.getInt("int"));
        assertEquals(3, obj.getInt("fraction"));

        // values outside the long range are clipped, values inside it are exact
        assertEquals(Long.MAX_VALUE, obj.getLong("bigDecimal"));
        assertEquals(Long.MIN_VALUE, obj.getLong("negBigDecimal"));
        assertEquals(Long.MAX_VALUE, obj.getLong("bigInteger"));
        assertEquals(Long.MIN_VALUE, obj.getLong("negBigInteger"));
        assertEquals(Long.MAX_VALUE, obj.optLong("bigInteger", 7L));
        assertEquals(9223372036854775806L, obj.getLong("nearMax"));
        assertEquals(4294967301L, obj.getLong("big"));
        assertEquals(3L, obj.getLong("fraction"));
    }

    public void testUnquotedNullKey() throws Exception {
        assertEquals("{\"null\":1}", new JSONObject("{null:1}").toString());
        assertEquals("{\"null\":1}", new JSONObject("{NULL:1}").toString());
        assertEquals(1, new JSONObject("{null:1,\"a\":2}").getInt("null"));
        // consistent with other unquoted keys that are read as values
        assertEquals("{\"true\":1}", new JSONObject("{true:1}").toString());
        // a null value is still read as a null value
        assertTrue(new JSONObject("{\"a\":null}").isNull("a"));
    }
}
