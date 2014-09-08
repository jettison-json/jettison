package org.codehaus.jettison.json;

import junit.framework.TestCase;

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
}
