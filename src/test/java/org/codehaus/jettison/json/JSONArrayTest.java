package org.codehaus.jettison.json;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

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

}
