package org.codehaus.jettison.json;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
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
}
