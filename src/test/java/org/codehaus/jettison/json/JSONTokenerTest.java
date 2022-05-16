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

}
