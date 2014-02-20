Specification: Service REST API / Metadata
==========================================

# Glossary
For a definition of terms see the [Glossary](Glossary).

# Structure
* Descriptive name is the Header
* First paragraph is a short description of the API
* Request section contains info on what the request looks like
* Response section contains info on what the response looks like, including errors where applicable
* Example section contains a successful example and an unsuccessful example.

# Common

## Error Response Object
See [error JSON-schema](https://raw.github.com/bserdar/lightblue/master/query-api/src/main/resources/json-schema/error/error.json) for details.

Each response section for documenting errors will include mention of the expected error codes only.  The fields are:
* object_type is always "error"
* context is the place in processing at which the code failed
* msg is a descriptive message that should help a developer understand the error

There are some common error codes that could be returned on any request and they are:
* metadata:ConfigurationNotFound - configuration for the metadata manager was not found
* metadata:ConfigurationNotValid - configuration for the metadata manager is not valid
* rest-metadata:RestError - generic error when no other specific error can be determined

    {
        "object_type": "error",
        "context": "error/context/path",
        "errorCode": "DESCRIPTIVE_ERROR_CODE",
        "msg": "User friendly error message."
    }

# GET

## Get Entity Names
Get names of all defined entities.  There is no paging for this request, all entity names are returned in one response.

### Request
No parameters to pass, this request has no variations
> GET /metadata/names

### Response: Success
JSON document that is an array of strings.  Each element in the array is an entity name.

## Get Versions for Entity
Get a list of all available versions of a given entity.

### Request
One parameter as a path param of the request.
> GET /metadata/{entityName}/versions

### Response: Success
JSON document that is an array of version objects (see [#definitions/version](https://raw.github.com/bserdar/lightblue/master/metadata/src/main/resources/json-schema/metadata/common.json)).

### Response: Errors
Additional error codes:
* metadata:NoEntityName - no entity name specified

## Get Metadata for Entity Version
Get metadata details for the specified version of an entity.

### Request
Two path parameters on the request.
> GET /metadata/{entityName}/{version}

### Response: Success
If the requested version of the entity exists a JSON document matching the [metadata JSON-schema](https://raw.github.com/bserdar/lightblue/master/metadata/src/main/resources/json-schema/metadata/metadata.json) is returned.

If the requested version of the entity does not exist no result is returned (empty document).

### Response: Errors
Additional error codes:
* metadata:NoEntityName - no entity name specified
* metadata:NoEntityVersion - no entity version specified

# PUT

## Create New Metadata
Create a new entity by defining a new metadata.

### Request
Two path params on the request representing the entity name and version.
Body of request is a JSON document matching the [metadata JSON-schema](https://raw.github.com/bserdar/lightblue/master/metadata/src/main/resources/json-schema/metadata/metadata.json)
> PUT /metadata/{entityName}/{version}
>
> {metadata JSON document}

### Response: Success
On success returns the stored metadata document for the newly created version of the entity.

### Response: Errors
Additional error codes:
* metadata:NoEntityName - no entity name specified
* metadata:NoEntityVersion - no entity version specified
* rest-metadata:NoNameMatch - entity name on the path does not match entity name in the json document
* rest-metadata:NoVersionMatch - version on the path does not match the version in the json document
* metadata:DuplicateEntityInfo - entity info for this entity already exists
* metadata:DuplicateSchema - schema for this entity with this version already exists

## Create New Schema
Create a new schema, representing a new version of an existing entity.

### Request
Two path params on the request representing the entity name and version.
Body of request is a JSON document matching the [schema JSON-schema](https://raw.github.com/bserdar/lightblue/master/metadata/src/main/resources/json-schema/metadata/schema.json)
> PUT /metadata/{entityName}/{version}
>
> {schema JSON document}

### Response: Success
On success returns the [metadata JSON document](https://raw.github.com/bserdar/lightblue/master/metadata/src/main/resources/json-schema/metadata/metadata.json) for the newly created version of the entity.  Note that this is *not* the schema JSON document, it is the metadata JSON document (entity info + schema).

### Response: Errors
Additional error codes:
* metadata:NoEntityName - no entity name specified
* metadata:NoEntityVersion - no entity version specified
* rest-metadata:NoNameMatch - entity name on the path does not match entity name in the json document
* rest-metadata:NoVersionMatch - version on the path does not match the version in the json document
* metadata:DuplicateSchema - schema for this entity with this version already exists

## Update Entity Info
Create an existing entity's info.

### Request
One path param on the request representing the entity name.
Body of request is a JSON document matching the [enttyInfo JSON-schema](https://raw.github.com/bserdar/lightblue/master/metadata/src/main/resources/json-schema/metadata/entityInfo.json)
> PUT /metadata/{entityName}
>
> {entityInfo JSON document}

### Response: Success
On success returns the [metadata JSON document](https://raw.github.com/bserdar/lightblue/master/metadata/src/main/resources/json-schema/metadata/metadata.json) with the default version for the given entity.  Note that this is *not* the entityInfo JSON document, it is the metadata JSON document (entity info + schema).

### Response: Errors
Additional error codes:
* metadata:NoEntityName - no entity name specified
* rest-metadata:NoNameMatch - entity name on the path does not match entity name in the json document
* metadata:MissingEntityInfo - entity info does not exist and therefore cannot be updated

## Update Schema Status
Update the status of a a version of an entity.  For example, to disable an old version of an entity.

### Request
Three path params to represent entity name, version, and new status.  Payload is a comment representing why the status was changed.
> PUT /metadata/{entityName}/{version}/{status}
>
> Change comment

Values for {status} are defined in the [schema JSON-schema](https://raw.github.com/bserdar/lightblue/master/metadata/src/main/resources/json-schema/metadata/schema.json), see #definitions/status/properties/value/enum.

### Response: Success
The [metadata JSON document](https://raw.github.com/bserdar/lightblue/master/metadata/src/main/resources/json-schema/metadata/metadata.json) for the given entity name and version.

### Response: Errors
Additional error codes:
* metadata:NoEntityName - no entity name specified
* metadata:NoEntityVersion - no entity version specified
* rest-metadata:NoNameMatch - entity name on the path does not match entity name in the json document
* rest-metadata:NoVersionMatch - version on the path does not match the version in the json document
* metadata:MissingSchema - schema does not exist for given entity name + version and therefore cannot be updated

# POST

## Set Default Version for Entity
Set the default version for the given entity to the specified version.

### Request
Two path params to represent entity name and version.
> POST /metadata/{entityName}/{version}/default

### Response: Success
The [metadata JSON document](https://raw.github.com/bserdar/lightblue/master/metadata/src/main/resources/json-schema/metadata/metadata.json) for the given entity name and version.

### Response: Errors
Additional error codes:
* metadata:NoEntityName - no entity name specified
* metadata:NoEntityVersion - no entity version specified
* metadata:MissingEntityInfo - entity info does not exist
* metadata:MissingSchema - schema does not exist for given entity name + version

