# Security Policy

## Supported Versions

Security fixes are made on the `master` branch and released in the next
1.5.x version. Only the latest release receives security fixes; please
upgrade before reporting an issue.

| Version          | Supported          |
| ---------------- | ------------------ |
| Latest 1.5.x     | :white_check_mark: |
| Older than 1.5.x | :x:                |

## Reporting a Vulnerability

Please **do not** report security vulnerabilities through public GitHub
issues, pull requests or discussions.

Report them privately using GitHub's
[private vulnerability reporting](https://github.com/jettison-json/jettison/security/advisories/new)
("Report a vulnerability" under the repository's **Security** tab).

Please include:

- the affected Jettison version (or commit);
- the API or code path involved (for example `JSONObject(String)`,
  `MappedXMLStreamReader`, `MappedDOMDocumentParser`);
- a minimal input or test case that reproduces the issue;
- the impact you observed (for example stack overflow, excessive memory or
  CPU use, information disclosure) and any configuration needed to trigger it.

We aim to acknowledge reports within a few working days, and will keep you
informed as we work on a fix. Once a fix is released we will publish a GitHub
Security Advisory and request a CVE where appropriate. We are happy to credit
reporters who wish to be named.

## Threat Model

Jettison is a library. Its security properties depend on how an application
uses and configures it. The following describes what the project does and
does not treat as a vulnerability.

### In scope

The primary untrusted input is **JSON text** supplied by a remote party and
parsed by Jettison, either directly (`JSONObject`, `JSONArray`,
`JSONTokener`) or through the StAX/DOM conversion layers (Mapped and
BadgerFish `XMLInputFactory`, `XMLStreamReader` and `DOMDocumentParser`
implementations).

Issues of interest include:

- **Denial of service**: input that causes a `StackOverflowError`,
  unbounded memory or CPU use, or an infinite loop, despite the default
  limits being in effect.
- **Bypasses of the built-in limits**: input that exceeds the recursion
  depth, array length or object key limits without being rejected.
- **XML injection or XXE**: JSON input that causes Jettison to produce or
  process XML in a way that resolves external entities, injects markup, or
  reads local or remote resources.
- **Information disclosure**: sensitive data exposed through Jettison's own
  output or exceptions.

### Out of scope

- Data structures constructed by the application itself (for example cyclic
  objects built with `put()` and then serialized). Jettison assumes such
  structures are acyclic.
- Configuration controlled by the application or the JVM operator, such as
  system properties (`jettison.mapped.typeconverter.class`,
  `jettison.json.jsontokener.use_bigdecimal`) or limits raised via
  `setGlobalRecursionDepthLimit` / `setGlobalArrayLengthLimit`.
- Vulnerabilities in the underlying StAX or DOM implementation (for example
  the JDK or Woodstox), unless Jettison uses them in an unsafe way.
- Resource consumption that is proportional to the size of the input. Limit
  request body sizes at the application or container level.
- Acceptance of lenient, non-standard JSON syntax (single-quoted strings,
  comments, `=` / `=>` / `;` separators, hexadecimal numbers), which is
  long-standing, documented behaviour.

## Security Limits

Jettison applies the following default limits when parsing JSON:

| Limit                           | Default     | Configure with                                   |
| ------------------------------- | ----------- | ------------------------------------------------ |
| Nesting (recursion) depth       | 500         | `JSONObject.setGlobalRecursionDepthLimit(int)`   |
| `JSONArray` length              | 1,000,000   | `JSONArray.setGlobalArrayLengthLimit(int)`       |
| Keys per `JSONObject` (parsing) | 1,000,000   | `new JSONTokener(String, int threshold)`         |

These limits are JVM-wide (except the per-tokener key threshold). Set them
once at application startup, and prefer lowering rather than raising them
when handling untrusted input.
