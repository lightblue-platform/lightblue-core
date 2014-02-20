Specification: Service REST API / General
=========================================

# Glossary
For a definition of terms see the [Glossary](../GLOSSARY.md).

# Authentication
Authentication is not the responsibility of lightblue.  Any request that calls a REST application is assumed to be authenticated.  This means something like the container or a local proxy (httpd) is intercepting all requests and verifying authentication before passing the request on.  Examples of how to do this may be added to this document later.

# Authorization
For each request lightblue requires authorization information about the client.  This is captured as a set of "roles" associated with that client.  Management of those roles is outside the scope of lightblue.  Lightblue will simply look for roles in a specific location on each request.  Examples of how to do this may be added to this document later.

## Roles
The roles for a client are expected to exist in a HTTP header called <TBD>.  If the header is not set or if the header has no value it is assumed the client has no roles.  Note that metadata can be created with no access restrictions.  In this case, requests from clients without any roles in a header will be successful.
