Jettison is a Java library for converting XML to JSON and vice-versa with the help of StAX (https://en.wikipedia.org/wiki/StAX).
It implements XMLStreamWriter and XMLStreamReader and supports Mapped and BadgerFish conventions. Latest release is 1.4.0.

For example, with a Mapped convention, JAXB processes JAXB beans and emits XMLStreamWriter events which are processed by Jettison
with the XML data being converted to JSON. Likewise, when it reads JSON, it reports XMLStreamReader events for JAXB to populate JAXB
beans.

Note improving and supporting the Mapped convention code is the main focus of this project. However the pull requests from 
BadgerFish convention users are welcomed. 

Jettison was originally created by Dan Diephouse and hosted at Codehause.

## Security limits

Jettison includes default parser safety limits to help prevent excessive memory/stack usage from deeply nested or extremely large JSON payloads:

- Default recursion depth limit: `500` (configured in `JSONObject`).
- Default `JSONArray` length limit: `1,000,000` elements.

These can be adjusted globally at application startup:

- `JSONObject.setGlobalRecursionDepthLimit(int)`
- `JSONArray.setGlobalArrayLengthLimit(int)`


