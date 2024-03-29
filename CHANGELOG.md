# Change log
All notable changes to this project are documented in this file following the [Keep a CHANGELOG](http://keepachangelog.com) conventions. 

Issues reported on [GitHub](https://github.com/authzforce/core/issues) are referenced in the form of `[GH-N]`, where N is the issue number. Issues reported on [OW2](https://jira.ow2.org/browse/AUTHZFORCE/) are mentioned in the form of `[OW2-N]`, where N is the issue number.

## 3.0.0
### Changed
- Upgraded parent project: 9.1.0:
  - **Migrated to Java 17** (as the minimum required JRE version from now on)
- Upgraded dependencies: 
  - slf4j-api: 2.0.7 
  - **Jakarta XML Binding** (JAXB): (javax.xml.bind-api replaced with) jakarta.xml.bind-api: **4.0.0**
  - **Jakarta Rest-API** (JAX-RS): (javax.ws.rs-api replaced with) jakarta.ws.rs-api: **3.0.0**
  - authzforce-ce-xacml-json-model: 4.1.0


## 2.0.4
### Fixed
- CVEs affecting dependencies by upgrading:
  - parent project (authzforce-ce-parent): 8.5.0
  - slf4j-api: 1.7.36
  - authzforce-ce-xacml-json-model: 3.0.5
  - spring-core: 5.3.29
  - json: 20230227
  - org.everit.json.schema, renamed everit-json-schema: 1.14.2
- `XacmlAttributeId` enum: added missing value for standard XACML 3.0 Core attribute ID: `urn:oasis:names:tc:xacml:2.0:resource:target-namespace (used for <Content> processing).


## 2.0.3
### Fixed
- CVE-2021-22696 and CVE-2021-3046 fixed by upgrading **authzforce-ce-parent to v8.0.3**
- Dependency fixes:
	- authzforce-ce-xacml-json-model to 3.0.4: fixes authzforce/server#64 (loading schemas in offline mode failed)


## 2.0.2
### Fixed
- CVE-2021-22118: updated Spring version to 5.2.15
  - Upgraded parent version to 8.0.2


## 2.0.1
### Fixed
- Upgraded authzforce-ce-xacml-json-model to 3.0.2: fixes issue with method `XacmlJsonUtils#canonicalizeResponse()` when comparing similar XACML/JSON responses (linked to https://github.com/stleary/JSON-java/issues/589 )


## 2.0.0
### Changed
- Upgraded supported JRE to Java 11. Java 8 no longer supported
- Upgraded parent project: 8.0.0
- Upgraded Jakarta RESTful Web Services API to 2.1.6
- Upgraded JAXB (Jakarta XML Binding) to 2.3.3
- upgraded authzforce-ce-xacml-json-model to 3.0.0


## 1.6.0
### Changed
- Upgraded parent project: 7.6.1
	- Upgraded dependency slf4j-api: 1.7.30
- BadRequestExceptionMapper: new constructor argument to configured the verbosity of returned BadRequest messages in terms of depth of the returned error stacktrace. The higher the verbosity, the more error info and the easier for clients to troubleshoot.


### Fixed
- #1 : CVE-2018-8088 affecting slf4j


## 1.5.0
### Changed
- Upgraded parent project: 7.6.0
- Upgraded dependency `authzforce-ce-xacml-json-model`: 2.3.0


## 1.4.0
### Changed
- Upgraded dependency `authzforce-ce-xacml-json-model` version: 2.2.0

### Fixed
- Throwing RuntimeException instead of BadRequestException when XACML/JSON Request not valid (against JSON schema)


## 1.3.1
### Fixed
- CVE affecting Spring 4.3.18: upgraded parent and dependencies to depend on Spring v4.3.20:
	- authzforce-ce-parent: 7.5.1
	- authzforce-ce-xacml-json-model: 2.1.1
	

## 1.3.0
### Changed
- Maven parent project version (authzforce-ce-parent): 7.5.0
- Dependency version: authzforce-ce-xacml-json-model: 2.1.0:
	- Spring: 4.3.18 (fixes CVE)
- Copyright company name


## 1.2.0
### Changed
- Parent project version (authzforce-ce-parent): 7.3.0
- Dependency authzforce-ce-xacml-json-model: 2.0.0


## 1.1.0
### Changed
- Parent project version: 7.0.0 -> 7.1.0
- Dependency version: authzforce-ce-xacml-json-model: 1.0.0 -> 1.1.0
	- org.everit.json.schema: 1.6.0 -> 1.6.1
	- guava: 21.0 -> 22.0
	- json: 20170516 -> 20171018


## 1.0.0
Initial release on GitHub

