Glossary
========

# General
* simple entity - a single persistence unit.. "data that can be written atomically"
* aggregate entity - a collection of simple entities associated through some relationships
* data service - a software service for manipulating a simple entities
* orchestration service - synonymous with "aggregation service"
* aggregation service - a software service for manipulating aggregate entities
* business service - a software service for doing anything outside of data or aggregation service scope.  Typically anything that has business logic requirements.

# Migration
* movable data - data that can be moved from existing datastores to another location
* unmovable data - data that can not be moved from existing datastores
