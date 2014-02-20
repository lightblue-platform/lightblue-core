lightblue
=========

Project lightblue is designed to enable faster time to market on strategic business demands by increasing availability, resiliency, consistency, and performance of data SOA Services in a scalable, resilient and cloud capable fashion.  It breaks away from traditional modelling approaches of knowing data structures at design time and provides a metadata driven framework with MongoDB support.  Lightblue is based on analysis of existing development models and is driven by a need to reduce maintenance and feature requests from external customers.  Some key features are versioned metadata, flexible plugin architecture, and the ability to aggregate data across various storage technologies (NoSQL, SQL, File, etc).  Red Hat IT is replacing 85% of its existing SOA Services with this one open source project and is reducing complexity, maintenance, support, and infrastructure needs while enhancing flexibility and security in a cloud enabled architecture.

# Licenses

## lightblue

The license of lightblue is [GLPv3](https://www.gnu.org/licenses/gpl.html).  See COPYING in root of project for the full text.

## Dependencies

| Dependency | Why | License
| ---------- | --- | -------
| [Jackson](http://wiki.fasterxml.com/JacksonHome) | Java library for processing JSON documents.  Use in complicated cases where processing is required on the JSON document. |[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
| [Gson](https://code.google.com/p/google-gson/) |Convert Java to and from JSON.  Used in simple cases where processing is 1:1 between Java and the JSON document. | [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
| [JSONassert](https://github.com/skyscreamer/JSONassert) | Used in unit tests to compare JSON documents before and after processing.  Useful for verifying parsing and type conversions (JSON -> Java -> JSON for example) | [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
| [json-schema-validator](https://github.com/fge/json-schema-validator) | Validation of JSON documents against a JSON schema.  Similar to XML Schema validation. | [LGPLv3 or later](https://www.gnu.org/licenses/lgpl.html)
| [Flapdoodle Embedded MongoDB](https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo) | In memory MongoDB used in unit tests to test against a real database. | [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
| [mongo-java-driver](https://github.com/mongodb/mongo-java-driver) | MongoDB driver for Java.  Used for all interactions with MongoDB from Java. | [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
