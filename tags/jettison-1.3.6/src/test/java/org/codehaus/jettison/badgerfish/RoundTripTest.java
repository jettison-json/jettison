package org.codehaus.jettison.badgerfish;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

/**
 * Test large scale object round trips.
 */
public class RoundTripTest extends TestCase {

    // ========================================================================
    // JUnit Tests
    // ========================================================================
    public void testEqualsHashCode() {
        DataSet ds1, ds2, ds3;
        ds1 = new DataSet();
        ds2 = new DataSet();
        ds3 = new DataSet();

        // test null..
        try {
            ds3.setDataPoints(null);
            fail("Suppose to throw null pointer exception..");
        } catch (NullPointerException npe) {
            // success
        }
        // test null..
        try {
            ds3.addDataPoint(null);
            fail("Suppose to throw null pointer exception..");
        } catch (NullPointerException npe) {
            // success
        }

        // standard checks..
        assertTrue(ds1.equals(ds1));
        assertFalse(ds1.equals(null));
        assertFalse(ds1.equals(new Object()));

        // setup the name of the data set..
        ds1.setName("Some name");
        ds2.setName("Some name");

        // add random data points..
        addRandomDataPoints(15, ds1, ds2);
        addRandomDataPoints(15, ds3);

        assertTrue(ds1.equals(ds2));
        assertFalse(ds1.equals(ds3));

        Set set = new HashSet();
        set.add(ds1);
        set.add(ds2);
        set.add(ds3);
        assertTrue(set.size() == 2);
    }

    public void testSerializeDeserialize() throws Exception {
        DataSet ds = new DataSet();
        ds.setName("Some Name!");
        // addRandomDataPoints(50, ds);
        addRandomDataPoints(90, ds);
        assertRoundTrip(ds, new DataSet());
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================
    public File serialize(File f, XMLStreamObject t) throws Exception {
        XMLStreamWriter xwrt = new BadgerFishXMLOutputFactory().createXMLStreamWriter(new FileWriter(f));
        xwrt.writeStartDocument();
        t.toXMLStream(xwrt);
        xwrt.writeEndDocument();
        xwrt.flush();
        xwrt.close();
        return f;
    }

    public XMLStreamObject deserialize(File f, XMLStreamObject t) throws Exception {
        XMLStreamReader xrdr = new BadgerFishXMLInputFactory().createXMLStreamReader(new FileReader(f));
        t.fromXMLStream(xrdr);
        return t;
    }

    public void assertRoundTrip(XMLStreamObject instance, XMLStreamObject newInstance) throws Exception {
        File f = File.createTempFile("json", ".txt");
        try {
            serialize(f, instance);
            XMLStreamObject t = deserialize(f, newInstance);
            assertEquals(instance, t);
        } finally {
            f.delete();
        }
    }

    /**
     * Adds the number of random data points to the data set.
     * 
     * @param size
     * @param ds
     */
    void addRandomDataPoints(int size, DataSet ds) {
        addRandomDataPoints(size, ds, null);
    }

    /**
     * Adds the number of random data point to both data sets.
     */
    void addRandomDataPoints(int size, DataSet d1, DataSet d2) {
        Random rnd = new Random(System.currentTimeMillis());
        for (int i = 0; i < size; i++) {
            String label = "Label--" + i;
            Number value = new Double(rnd.nextDouble());
            d1.addDataPoint(new DataPoint(label, value));
            if (d2 != null) {
                d2.addDataPoint(new DataPoint(label, value));
            }
        }
    }

    /**
     * Simple model class to represent a data point in the chart.
     */
    public static class DataPoint implements XMLStreamObject {
        // =======================================================================
        // Fields
        // =======================================================================
        URI uri;
        Color color;
        String label;
        Number value;

        // =======================================================================
        // Constructors
        // =======================================================================

        /**
         * Default constructor.
         */
        public DataPoint() {

        }

        /**
         * When only a value exists.
         */
        public DataPoint(Number value) {
            this(null, null, value, null);
        }

        /**
         * When only a label and value exist for the data point.
         */
        public DataPoint(String label, Number value) {
            this(null, label, value, null);
        }

        /**
         * When only a label, value, and color exist for the data point.
         */
        public DataPoint(String label, Number value, Color color) {
            this(null, label, value, color);
        }

        /**
         * Simple constructor to set all properties.
         * 
         * @param uri used for image maps.
         * @param label used to label each point.
         * @param value used as the range value for the point.
         * @param color used to change the color of the point.
         */
        public DataPoint(URI uri, String label, Number value, Color color) {
            if (uri != null) {
                setURI(uri);
            }
            setColor(color);
            setLabel(label);
            setValue(value);
        }

        // =======================================================================
        // Color Property
        // =======================================================================
        /**
         * Color of the data point. This could be a pie slice or a bar in a
         * chart.
         */
        public Color getColor() {
            return color;
        }

        /**
         * Value can be null to signify default value. Otherwise must be a
         * color.
         */
        public void setColor(Color color) {
            this.color = color;
        }

        /**
         * Sets the color of the data point based on the string received.
         * 
         * @throws NumberFormatException if the string does not conform to a
         *             number color string.
         * @throws NullPointerException if the string is null.
         * @param color
         */
        public void setColor(String color) {
            this.color = Color.decode(color);
        }

        // =======================================================================
        // Label Property
        // =======================================================================
        /**
         * Label for the slice or bar.
         */
        public String getLabel() {
            return label;
        }

        /**
         * Sets the label. Blank input will result in a 'null' value.
         */
        public void setLabel(String label) {
            this.label = (StringUtil.isBlank(label)) ? null : label;
        }

        // =======================================================================
        // Value Property
        // =======================================================================
        /**
         * Actual value of the bar or slice.
         */
        public Number getValue() {
            return value;
        }

        /**
         * Sets the range value for this data point.
         * 
         * @throws NullPointerException if the parameter provided is null.
         * @param value Any number preferably a Double.
         */
        public void setValue(Number value) {
            if (value == null) {
                NullPointerException npe = new NullPointerException();
                throw npe;
            }
            this.value = value;
        }

        /**
         * Set the value based on the string. A Double is used for the basis.
         * 
         * @throws NumberFormatException if value is not a valid number.
         * @see java.lang.Double
         */
        void setValue(String value) {
            // use double since it has a fairly large range
            this.value = Double.valueOf(value);
        }

        // =======================================================================
        // URI Property
        // =======================================================================
        /**
         * Links for image maps etc..
         */
        public URI getURI() {
            return this.uri;
        }

        /**
         * Sets the URI for the data point.
         * 
         * @throws NullPointerException if value is null.
         */
        public void setURI(URI value) {
            if (value == null) {
                NullPointerException npe = new NullPointerException();
                throw npe;
            }
            this.uri = value;
        }

        /**
         * Sets a URI for a image map parameter.
         * 
         * @throws IllegalArgumentException - If the given string violates RFC
         *             2396 or is blank.
         */
        public void setURI(String value) {
            final String ERROR = "Invalid URI value!";
            if (StringUtil.isBlank(value)) {
                throw new IllegalArgumentException(ERROR);
            }
            this.uri = java.net.URI.create(value);
        }

        // =======================================================================
        // Object Overrides
        // =======================================================================

        /**
         * Determine if the object is equal.
         */
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof DataPoint)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            DataPoint dp = (DataPoint)obj;
            EqualsBuilder bld = new EqualsBuilder();
            bld.append(getColor(), dp.getColor());
            bld.append(getLabel(), dp.getLabel());
            bld.append(getValue(), dp.getValue());
            bld.append(getURI(), dp.getURI());
            return bld.isEquals();
        }

        /**
         * Must implement hashCode if implementing equals.
         */
        public int hashCode() {
            HashCodeBuilder bld = new HashCodeBuilder();
            bld.append(getColor());
            bld.append(getLabel());
            bld.append(getValue());
            bld.append(getURI());
            return bld.toHashCode();
        }

        // =======================================================================
        // XML
        // =======================================================================

        static final String ELEMENT = "DataPoint";
        static final String VALUE = "value";
        static final String COLOR = "color";
        static final String LABEL = "label";
        static final String URI = "uri";

        /**
         * Value is represented by a Double on parsing.
         * 
         * @see com.sun.idm.xml.XMLStreamObject#fromXMLStream(javax.xml.stream.XMLStreamReader)
         */
        public void fromXMLStream(XMLStreamReader xrdr) throws XMLStreamException {
            // clear out all variables that may not be there..
            uri = null;
            label = null;
            color = null;
            do {
                int tag = xrdr.getEventType();
                if (XMLStreamReader.START_ELEMENT == tag && ELEMENT.equals(xrdr.getLocalName())) {
                    int count = xrdr.getAttributeCount();
                    for (int x = 0; x < count; x++) {
                        String name = xrdr.getAttributeLocalName(x);
                        String value = xrdr.getAttributeValue(x);
                        if (VALUE.equals(name)) {
                            setValue(value);
                        } else if (LABEL.equals(name)) {
                            setLabel(value);
                        } else if (COLOR.equals(name)) {
                            setColor(value);
                        } else if (URI.equals(name)) {
                            setURI(value);
                        }
                    }
                    // found it so we're done here..
                    break;
                }
            } while (xrdr.next() != XMLStreamReader.END_DOCUMENT);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.sun.idm.xml.XMLStreamObject#toXMLStream(javax.xml.stream.XMLStreamWriter)
         */
        public void toXMLStream(XMLStreamWriter xwrt) throws XMLStreamException {
            xwrt.writeStartElement(ELEMENT);
            if (getColor() != null) {
                String color = ColorUtil.colorToString(getColor());
                xwrt.writeAttribute(COLOR, color);
            }
            if (StringUtil.isNotBlank(getLabel())) {
                xwrt.writeAttribute(LABEL, getLabel());
            }
            if (getURI() != null) {
                xwrt.writeAttribute(URI, getURI().toString());
            }
            xwrt.writeAttribute(VALUE, getValue().toString());
            xwrt.writeEndElement();
        }
    }

    /**
     * Simple interface that should the object supports streaming XML data
     * in/out.
     */
    public interface XMLStreamObject {
        /**
         * Writes the object to the stream. This stream writer has support for
         * various formats including XML, FastInfoSet and JSON.
         * 
         * @param wrt Writer for streaming the content through.
         * @throws XMLStreamException
         */
        public void toXMLStream(XMLStreamWriter wrt) throws XMLStreamException;

        /**
         * Reads the object from the stream this is an example of pull parsing.
         * It can be used on various formats a well. Formats include XML,
         * FastInfo Set, and JSON.
         * 
         * @param rdr Reader for streaming the content through.
         * @throws XMLStreamException if the content is cut off or malformed.
         */
        public void fromXMLStream(XMLStreamReader rdr) throws XMLStreamException;
    }

    /**
     * Simple model to represent a chart's data set.
     */
    public class DataSet implements XMLStreamObject {
        /**
         * Name of the data set or category depending on the translation.
         */
        String name;
        /**
         * Use a 'Set' to avoid any duplicates.
         */
        Set dataPoints;

        /**
         * Empty constructor for just using the set methods.
         */
        public DataSet() {
            // set the defaults..
            this(null, new HashSet());
        }

        /**
         * Set the name and data points for the data set.
         */
        public DataSet(String name, Set dataPoints) {
            setName(name);
            setDataPoints(dataPoints);
        }

        /**
         * Get the group of data points.
         */
        public Set getDataPoints() {
            return dataPoints;
        }

        /**
         * Set the group of data points. Using a set to avoid duplicates.
         * 
         * @throws NullPointerException if value is null.
         */
        public void setDataPoints(Set value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.dataPoints = value;
        }

        /**
         * Add a data point to the set of data points..
         * 
         * @throws NullPointerException if value is null.
         */
        public void addDataPoint(DataPoint value) {
            if (value == null) {
                throw new NullPointerException();
            }
            this.dataPoints.add(value);
        }

        /**
         * Get the name/category of the data set.
         */
        public String getName() {
            return name;
        }

        /**
         * Set the name of the data set.
         */
        public void setName(String value) {
            this.name = StringUtil.isBlank(value) ? null : value;
        }

        /**
         * Determine if two objects values are equal.
         */
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof DataSet)) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            DataSet ds = (DataSet)obj;
            EqualsBuilder bld = new EqualsBuilder();
            bld.append(getName(), ds.getName());
            bld.append(getDataPoints(), ds.getDataPoints());
            return bld.isEquals();
        }

        /**
         * Must override hashCode if using equals.
         */
        public int hashCode() {
            HashCodeBuilder bld = new HashCodeBuilder();
            bld.append(getName());
            bld.append(getDataPoints());
            return bld.toHashCode();
        }

        // =======================================================================
        // XMLStreamObject Interface
        // =======================================================================
        static final String ELEMENT = "dataset";
        private static final String NAME = "name";

        /*
         * (non-Javadoc)
         * 
         * @see com.sun.idm.xml.XMLStreamObject#fromXMLStream(javax.xml.stream.XMLStreamReader)
         */
        public void fromXMLStream(XMLStreamReader xrdr) throws XMLStreamException {
            // clear the current data..
            name = null;
            dataPoints.clear();
            // optimization..
            boolean found = false;
            do {
                int tag = xrdr.getEventType();
                if (XMLStreamReader.START_ELEMENT == tag) {
                    String name = xrdr.getLocalName();
                    // find the start element
                    if (!found && ELEMENT.equals(name)) {
                        found = true;
                        // get the various attributes..
                        int count = xrdr.getAttributeCount();
                        for (int x = 0; x < count; x++) {
                            name = xrdr.getAttributeLocalName(x);
                            String value = xrdr.getAttributeValue(x);
                            if (NAME.equals(name)) {
                                setName(value);
                            }
                        }
                    } else if (found && DataPoint.ELEMENT.equals(name)) {
                        DataPoint dp = new DataPoint();
                        dp.fromXMLStream(xrdr);
                        dataPoints.add(dp);
                    }
                } else if (found && XMLStreamReader.END_ELEMENT == tag && ELEMENT.equals(xrdr.getLocalName())) {
                    // we're done so stop parsing..
                    break;
                }
            } while (xrdr.next() != XMLStreamReader.END_DOCUMENT);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.sun.idm.xml.XMLStreamObject#toXMLStream(javax.xml.stream.XMLStreamWriter)
         */
        public void toXMLStream(XMLStreamWriter xwrt) throws XMLStreamException {
            xwrt.writeStartElement(ELEMENT);
            // write data set attributes
            if (name != null) {
                xwrt.writeAttribute(NAME, getName());
            }
            // write out each data point
            for (Iterator iter = getDataPoints().iterator(); iter.hasNext();) {
                DataPoint dp = (DataPoint)iter.next();
                dp.toXMLStream(xwrt);
            }
            xwrt.writeEndElement();
        }
    }

    public static class ColorUtil {
        /**
         * Create the hex string representing a color.
         */
        public static String colorToString(Color color) {
            int rgb = color.getRGB();
            String value = Integer.toString(rgb);
            return value;
        }

        /**
         * Create a color object from a hex string.
         */
        public static Color stringToColor(String color) {
            Color ret = Color.WHITE;
            try {
                ret = Color.decode(color);
            } catch (NumberFormatException e) {
                // do nothing..
            }
            return ret;
        }
    }

    /**
     * String utilities..
     */
    public static class StringUtil {
        /**
         * <pre>
         *  StringUtil.isEmpty(null)               = true
         *  StringUtil.isEmpty(&quot;&quot;)       = true
         *  StringUtil.isEmpty(&quot; &quot;)      = false
         *  StringUtil.isEmpty(&quot;bob&quot;)    = false
         *  StringUtil.isEmpty(&quot; bob &quot;)  = false
         * </pre>
         */
        public static boolean isEmpty(String val) {
            return (val == null) ? true : "".equals(val) ? true : false;
        }

        public static boolean isNotEmpty(String val) {
            return !isEmpty(val);
        }

        /**
         * <pre>
         *      StringUtil.isBlank(null)                = true
         *      StringUtil.isBlank(&quot;&quot;)        = true
         *      StringUtil.isBlank(&quot; &quot;)       = true
         *      StringUtil.isBlank(&quot;bob&quot;)     = false
         *      StringUtil.isBlank(&quot;  bob  &quot;) = false
         * </pre>
         */
        public static boolean isBlank(String val) {
            return (val == null) ? true : isEmpty(val.trim());
        }

        public static boolean isNotBlank(String val) {
            return !isBlank(val);
        }

    }

    public static class EqualsBuilder {

        private boolean _equals = true;

        public EqualsBuilder() {
            // do nothing for now.
        }

        public EqualsBuilder appendSuper(boolean value) {
            // quick exit if already false..
            if (isEquals()) {
                setEquals(value);
            }
            return this;
        }

        public EqualsBuilder append(Object lhs, Object rhs) {
            // quick exit if already false..
            if (isEquals() == false) {
                return this;
            }
            // identity check..
            if (lhs == rhs) {
                return this;
            }
            // this will check if one is null and the other is not..
            if (lhs == null || rhs == null) {
                setEquals(false);
                return this;
            }
            // check that they are the same class..
            Class rhsClass = rhs.getClass();
            Class lhsClass = lhs.getClass();
            if (rhsClass != lhsClass) {
                setEquals(false);
            } else if (!lhsClass.isArray()) {
                // The simple case, not an array, just test the element
                setEquals(lhs.equals(rhs));
            } else if (lhs instanceof Set) {
                // get the arrays for each an test them..
            }
            // 'Switch' on type of array, to dispatch to the correct handler
            // This handles multi dimensional arrays of the same depth
            else if (lhs instanceof long[]) {
                append((long[])lhs, (long[])rhs);
            } else if (lhs instanceof int[]) {
                append((int[])lhs, (int[])rhs);
            } else if (lhs instanceof short[]) {
                append((short[])lhs, (short[])rhs);
            } else if (lhs instanceof char[]) {
                append((char[])lhs, (char[])rhs);
            } else if (lhs instanceof byte[]) {
                append((byte[])lhs, (byte[])rhs);
            } else if (lhs instanceof double[]) {
                append((double[])lhs, (double[])rhs);
            } else if (lhs instanceof float[]) {
                append((float[])lhs, (float[])rhs);
            } else if (lhs instanceof boolean[]) {
                append((boolean[])lhs, (boolean[])rhs);
            } else {
                // Not an array of primitives
                append((Object[])lhs, (Object[])rhs);
            }
            return this;
        }

        public EqualsBuilder appendIgnoreCase(String lhs, String rhs) {
            if (isEquals() && lhs.equals(rhs)) {
                if (rhs == null) {
                    setEquals(false);
                } else if (lhs.equalsIgnoreCase(rhs)) {
                    setEquals(false);
                }
            }
            return this;
        }

        public EqualsBuilder append(long lhs, long rhs) {
            if (isEquals() && lhs != rhs) {
                setEquals(false);
            }
            return this;
        }

        public EqualsBuilder append(int lhs, int rhs) {
            if (isEquals() && lhs != rhs) {
                setEquals(false);
            }
            return this;
        }

        public EqualsBuilder append(short lhs, short rhs) {
            if (isEquals() && lhs != rhs) {
                setEquals(false);
            }
            return this;
        }

        public EqualsBuilder append(char lhs, char rhs) {
            if (isEquals() && lhs != rhs) {
                setEquals(false);
            }
            return this;
        }

        public EqualsBuilder append(byte lhs, byte rhs) {
            if (isEquals() && lhs != rhs) {
                setEquals(false);
            }
            return this;
        }

        public EqualsBuilder append(double lhs, double rhs) {
            if (_equals == false) {
                return this;
            }
            return append(Double.doubleToLongBits(lhs), Double.doubleToLongBits(rhs));
        }

        public EqualsBuilder append(float lhs, float rhs) {
            if (_equals == false) {
                return this;
            }
            return append(Float.floatToIntBits(lhs), Float.floatToIntBits(rhs));
        }

        public EqualsBuilder append(boolean lhs, boolean rhs) {
            if (isEquals() && lhs != rhs) {
                setEquals(false);
            }
            return this;
        }

        public EqualsBuilder append(Object[] lhs, Object[] rhs) {
            if (isEquals() && lhs != rhs) {
                if (lhs == null || rhs == null) {
                    setEquals(false);
                } else if (lhs.length != rhs.length) {
                    setEquals(false);
                } else {
                    for (int i = 0; i < lhs.length && isEquals(); ++i) {
                        append(lhs[i], rhs[i]);
                    }
                }
            }
            return this;
        }

        public EqualsBuilder append(long[] lhs, long[] rhs) {
            if (isEquals() && lhs != rhs) {
                if (lhs == null || rhs == null) {
                    setEquals(false);
                } else if (lhs.length != rhs.length) {
                    setEquals(false);
                } else {
                    for (int i = 0; i < lhs.length && isEquals(); ++i) {
                        append(lhs[i], rhs[i]);
                    }
                }
            }
            return this;
        }

        public EqualsBuilder append(int[] lhs, int[] rhs) {
            if (isEquals() && lhs != rhs) {
                if (lhs == null || rhs == null) {
                    setEquals(false);
                } else if (lhs.length != rhs.length) {
                    setEquals(false);
                } else {
                    for (int i = 0; i < lhs.length && isEquals(); ++i) {
                        append(lhs[i], rhs[i]);
                    }
                }
            }
            return this;
        }

        public EqualsBuilder append(short[] lhs, short[] rhs) {
            if (isEquals() && lhs != rhs) {
                if (lhs == null || rhs == null) {
                    setEquals(false);
                } else if (lhs.length != rhs.length) {
                    setEquals(false);
                } else {
                    for (int i = 0; i < lhs.length && isEquals(); ++i) {
                        append(lhs[i], rhs[i]);
                    }
                }
            }
            return this;
        }

        public EqualsBuilder append(char[] lhs, char[] rhs) {
            if (isEquals() && lhs != rhs) {
                if (lhs == null || rhs == null) {
                    setEquals(false);
                } else if (lhs.length != rhs.length) {
                    setEquals(false);
                } else {
                    for (int i = 0; i < lhs.length && isEquals(); ++i) {
                        append(lhs[i], rhs[i]);
                    }
                }
            }
            return this;
        }

        public EqualsBuilder append(byte[] lhs, byte[] rhs) {
            if (isEquals() && lhs != rhs) {
                if (lhs == null || rhs == null) {
                    setEquals(false);
                } else if (lhs.length != rhs.length) {
                    setEquals(false);
                } else {
                    for (int i = 0; i < lhs.length && isEquals(); ++i) {
                        append(lhs[i], rhs[i]);
                    }
                }
            }
            return this;
        }

        public EqualsBuilder append(double[] lhs, double[] rhs) {
            if (isEquals() && lhs != rhs) {
                if (lhs == null || rhs == null) {
                    setEquals(false);
                } else if (lhs.length != rhs.length) {
                    setEquals(false);
                } else {
                    for (int i = 0; i < lhs.length && isEquals(); ++i) {
                        append(lhs[i], rhs[i]);
                    }
                }
            }
            return this;
        }

        public EqualsBuilder append(float[] lhs, float[] rhs) {
            if (isEquals() && lhs != rhs) {
                if (lhs == null || rhs == null) {
                    setEquals(false);
                } else if (lhs.length != rhs.length) {
                    setEquals(false);
                } else {
                    for (int i = 0; i < lhs.length && isEquals(); ++i) {
                        append(lhs[i], rhs[i]);
                    }
                }
            }
            return this;
        }

        public EqualsBuilder append(boolean[] lhs, boolean[] rhs) {
            if (isEquals() && lhs != rhs) {
                if (lhs == null || rhs == null) {
                    setEquals(false);
                } else if (lhs.length != rhs.length) {
                    setEquals(false);
                } else {
                    for (int i = 0; i < lhs.length && isEquals(); ++i) {
                        append(lhs[i], rhs[i]);
                    }
                }
            }
            return this;
        }

        public boolean isEquals() {
            return _equals;
        }

        protected void setEquals(boolean value) {
            _equals = value;
        }
    }

    public static class HashCodeBuilder {

        private final int _constant;

        /**
         * Running total used to produce hash code.
         */
        private int _total = 0;

        public HashCodeBuilder() {
            _constant = 37;
            _total = 17;
        }

        public HashCodeBuilder append(boolean value) {
            _total = _total * _constant + (value ? 0 : 1);
            return this;
        }

        public HashCodeBuilder append(boolean[] array) {
            if (array == null) {
                _total = _total * _constant;
            } else {
                for (int i = 0; i < array.length; i++) {
                    append(array[i]);
                }
            }
            return this;
        }

        public HashCodeBuilder append(byte value) {
            _total = _total * _constant + value;
            return this;
        }

        public HashCodeBuilder append(byte[] array) {
            if (array == null) {
                _total = _total * _constant;
            } else {
                for (int i = 0; i < array.length; i++) {
                    append(array[i]);
                }
            }
            return this;
        }

        public HashCodeBuilder append(char value) {
            _total = _total * _constant + value;
            return this;
        }

        public HashCodeBuilder append(char[] array) {
            if (array == null) {
                _total = _total * _constant;
            } else {
                for (int i = 0; i < array.length; i++) {
                    append(array[i]);
                }
            }
            return this;
        }

        public HashCodeBuilder append(double value) {
            return append(Double.doubleToLongBits(value));
        }

        public HashCodeBuilder append(double[] array) {
            if (array == null) {
                _total = _total * _constant;
            } else {
                for (int i = 0; i < array.length; i++) {
                    append(array[i]);
                }
            }
            return this;
        }

        public HashCodeBuilder append(float value) {
            _total = _total * _constant + Float.floatToIntBits(value);
            return this;
        }

        public HashCodeBuilder append(float[] array) {
            if (array == null) {
                _total = _total * _constant;
            } else {
                for (int i = 0; i < array.length; i++) {
                    append(array[i]);
                }
            }
            return this;
        }

        public HashCodeBuilder append(int value) {
            _total = _total * _constant + value;
            return this;
        }

        public HashCodeBuilder append(int[] array) {
            if (array == null) {
                _total = _total * _constant;
            } else {
                for (int i = 0; i < array.length; i++) {
                    append(array[i]);
                }
            }
            return this;
        }

        public HashCodeBuilder append(long value) {
            _total = _total * _constant + ((int)(value ^ (value >> 32)));
            return this;
        }

        public HashCodeBuilder append(long[] array) {
            if (array == null) {
                _total = _total * _constant;
            } else {
                for (int i = 0; i < array.length; i++) {
                    append(array[i]);
                }
            }
            return this;
        }

        public HashCodeBuilder append(Object object) {
            if (object == null) {
                _total = _total * _constant;
            } else {
                if (!object.getClass().isArray()) {
                    // the simple case, not an array, just the element
                    _total = _total * _constant + object.hashCode();
                } else {
                    // 'Switch' on type of array, to dispatch to the correct
                    // handler
                    // This handles multi dimensional arrays
                    if (object instanceof long[]) {
                        append((long[])object);
                    } else if (object instanceof int[]) {
                        append((int[])object);
                    } else if (object instanceof short[]) {
                        append((short[])object);
                    } else if (object instanceof char[]) {
                        append((char[])object);
                    } else if (object instanceof byte[]) {
                        append((byte[])object);
                    } else if (object instanceof double[]) {
                        append((double[])object);
                    } else if (object instanceof float[]) {
                        append((float[])object);
                    } else if (object instanceof boolean[]) {
                        append((boolean[])object);
                    } else {
                        // Not an array of primitives
                        append((Object[])object);
                    }
                }
            }
            return this;
        }

        public HashCodeBuilder append(Object[] array) {
            if (array == null) {
                _total = _total * _constant;
            } else {
                for (int i = 0; i < array.length; i++) {
                    append(array[i]);
                }
            }
            return this;
        }

        public HashCodeBuilder append(short value) {
            _total = _total * _constant + value;
            return this;
        }

        public HashCodeBuilder append(short[] array) {
            if (array == null) {
                _total = _total * _constant;
            } else {
                for (int i = 0; i < array.length; i++) {
                    append(array[i]);
                }
            }
            return this;
        }

        public HashCodeBuilder appendSuper(int value) {
            _total = _total * _constant + value;
            return this;
        }

        public int toHashCode() {
            return _total;
        }
    }
}
