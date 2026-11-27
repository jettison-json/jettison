/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.jettison.util;

import junit.framework.TestCase;

public class StringIndenterTest extends TestCase {

    public void testIndent() {
        String json = "{\"a\":{\"b\\\"b\":{\"c\\\\\":\"d\"},\"e\":\"f\",\"g\":[1,{\"h\":\"i\"}]}}";
        assertEquals("{\n  \"a\":{\n    \"b\\\"b\":{\n      \"c\\\\\":\"d\"\n    },\n    \"e\":\"f\",\n"
            + "    \"g\":[\n      1,\n      {\n        \"h\":\"i\"\n      }\n    ]\n  }\n}",
            new StringIndenter(json).result());
    }

    public void testIndentationIsCapped() {
        // The indentation of each line used to grow with the depth, so the
        // output of deeply nested input was quadratic in its size: 800 million
        // characters for 40,000
        int depth = 20000;
        StringBuilder json = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            json.append('[');
        }
        for (int i = 0; i < depth; i++) {
            json.append(']');
        }
        String[] lines = new StringIndenter(json.toString()).result().split("\n");
        assertEquals(2 * depth, lines.length);
        for (int i = 0; i < depth; i++) {
            // indented as before up to 500 levels
            assertEquals(indent(Math.min(i, 500)) + "[", lines[i]);
            assertEquals(indent(Math.min(depth - 1 - i, 500)) + "]", lines[depth + i]);
        }
    }

    private static String indent(int levels) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < levels; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }
}
