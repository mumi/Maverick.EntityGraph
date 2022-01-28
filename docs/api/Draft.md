

Draft API

Open Questions: 

* Conflict for retrieving an entity through posting a frame vs. creating an entity
* Validate external ABAC (via OPA) compatibility
* Unclear of JSON-LD Frames are powerful enough to express Verifiable Presentations

## Model
Endpoints to manage the schema

---
`GET /api/types`
Lists all configured types currently in schema database.

*Query Parameters:*

* `prefix` focus only on types defined within the schema identified by the prefix
* `limit`
* `page`


*Response*:
It returns a JSON-LD Graph, such as as the schema.org example: https://schema.org/version/latest/schemaorg-current-https.ttl

Supported formats are:

- JSON (as JSON-LD)
- Turtle

--- 
`POST /api/types`
Creates new type definitions.

*Payload:*
A valid type definition. The definition can include as many types as needed

The following example is using the Turtle Syntax

```turtle
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix eagl: <http://av360.org/schema/eagl#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix schema: <https://schema.org/>,

eagl:WikipediaEntry a rdfs:Class ;
    rdfs:label "Wikipedia Entry" ;
    rdfs:comment "A wikipedia entry about a certain topic." ;
    rdfs:subClassOf eagl:LearningUnit .

eagl:hasWikpediaLink a rdf:Property ;
    rdfs:domain eagl:WikipediaEntry
    rdfs:target schema:Url

```

and the following using json-ld (as graph)

```json
{
  "@context": {
    "rdf": "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
    "eagl": "http://av360.org/schema/eagl#",
    "xsd": "http://www.w3.org/2001/XMLSchema#"
  },
  "@graph": [
       {
        "@id": "eagl:WikipediaEntry",
        "@type": "rdfs:Class",
        "rdfs:comment": "A wikipedia entry about a certain topic.",
        "rdfs:label": "Wikipedia Entry",
        "rdfs:subClassOf": {
            "@id": "eagl:LearningUnit"
        }
      }, 
      {
        "@id": "eagl:hasWikpediaLink",
        "@type": "rdfs:Property",
        "rdfs:domain": "eagl:WikipediaEntry",
        "rdfs:target": "schema:Url"
      }
  ]
}
```

## Namespaces
Provides access to the supported schemas identified by namespaces and prefixes

--- 

`GET /api/namespaces`

Provides a list of all supported prefixes. Those are automatically determined from the uploaded types

```json
{
 "@context": {
    "jsonld": "http://www.w3.org/ns/json-ld"
  },
  "@graph": [
      {
        "@Id": "http://esco.eu/model"
        "@Type": "jsonld:Context"
        ":prefix": "esco"
      }, 
      {
        "@Id": "http://av360.io/schema/eagl#"
        "@Type": "jsonld:Context"
        ":prefix": "eagl"
      }
  ]
}
```




## Templates
Endpoints to manage the templates (reusable JSON-LD Frames)

`GET /api/frames?for=esco:Skill`

returns a list of all named templates as json array


*Query Parameters:*

* `for` filter the result to show only templates targetting the given type (either with prefix or fully qualified)


``GET /api/frames/{name}``

returns the template in valid frames syntax (and thus directly reusable)


``POST /api/frames/{name}``

Creates a new template. Example can be found here: https://www.w3.org/TR/json-ld11-framing/#framing

*Query Paramaters:*

* ``verifiable``: if set to true, the template will be checked if it suits the needs for a verifiable presentation, see https://www.w3.org/TR/vc-data-model/#presentations

```json

{
  "@context": {"@vocab": "http://av360.org/schema/eagl#"},
  "@type": "LearningPath",
  "hasItem": {
    "@explicit": true,
    "@type": "WikipediaEntry",
    "hasWikipediaLink": { }
  }
}
```

We use the flag "@explicit" in the example. Any other attributes in the graph will be omitted.

The following flags are supported:

* [@embed](https://www.w3.org/TR/json-ld11-framing/#object-embed-flag) (default is "@once", but can also be "@never" or "@always")
* [@explicit](https://www.w3.org/TR/json-ld11-framing/#explicit-inclusion-flag) (if true, not mentioned attributes will be omitted)
* @omitDefault
* @omitGraph for single node objects

## Queries
Support for querying the graph

---
`POST /api/query`

like Get, but with a frame in post body. Query Parameters are equivalent with the exception of the ``presentation``-parameter 

``GET /api/query?type=eagl:LearningUnit&expand=true``

returns entities of given type

*Supported query parameters:*

* ``type``: the type of the requested entity
* ``expand``: include all entities, whose type is a subclassof the given type
* ``presentation``: name of the template which should be used to construct the results
* ``query``: a RSQL query (optional)
* ``limit``:
* ``page``:

In this example, the type `eagl:LearningUnit` is abstract. There are no direct instances in the graph, we only have WikipediaEntries and YoutubeVideos. By adding the ``expand``-flag, we define that we are interested in all entities whose type inherits from the query parameter.

Query support comes through attribute matching in the frames.




## Entities
Managing entities

---
``GET /api/entities/{id}``

*Supported query parameters:*
Returns an invidual item

* ``proof``: whether to include a proof (signature)
* ``presentation``: use a named template to construct a specific view on the entity



``POST /api/entity``

Payload has to be a valid json-ld document with a @type and ideally @id (could be automatically generated)

```json
{
  "@context": {
    "rdf": "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
    "eagl": "http://av360.org/schema/eagl#",
    "xsd": "http://www.w3.org/2001/XMLSchema#", 
    "sdo:" "https://schema.org/"

  },
  "@graph": [
       {
        "@type": "eagl:WikipediaEntry",
        .. other values
        "eagl:hasWikipediaLink": {
            "@type": "sdo:url",
            "url": "http://en.wikipedia.org/..", 
            "language": "en-US"
        }
  ]
}
```

If it includes a proof, it will be verified (and should signed in a credential chain)


### Partial updates
Partial updates are crucial in the graph, and natively supported. "@id" is required


#### Adding an embedded object

Scenario: adding a new wikipedia link in a different language

Conflicts can arise: 
 - if the embedded object with exactly the same values (but different ids) already exists
 - if a constraint is violated (e.g. only lang de and en are supported)
 - 
```json
{
  "@context": {
    "rdf": "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
    "eagl": "http://av360.org/schema/eagl#",
    "xsd": "http://www.w3.org/2001/XMLSchema#", 
    "sdo:" "https://schema.org/"
  },
  "@graph": [
       {
        "@type": "sdo:url",
        "@id": "df87da74-7e9b-11ec-90d6-0242ac120003"
        "url": "http://de.wikipedia.org/..", 
        "language": "de-DE"
       }, 
       {
        "@type": "eagl:WikipediaEntry",
        "@id": "684416b4-2ea3-48b0-b55c-6a2410e2baec", 
        "eagl:hasWikipediaLink": {
            "@id": "df87da74-7e9b-11ec-90d6-0242ac120003"
        }
  ]
}
```

#### Deleting an embedded object

Is the same request again via HTTP DELETE. 

Conflicts can arise: 
 - if the request does not include all edges pointing to the deleted object (means, we have dangling edges)
 - if the user is not allowed to modify entities, who have edges pointing to the deleted object

```json
{
  "@context": {
    "rdf": "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
    "eagl": "http://av360.org/schema/eagl#",
    "xsd": "http://www.w3.org/2001/XMLSchema#", 
    "sdo:" "https://schema.org/"
  },
  "@graph": [
       {
        "@type": "sdo:url",
        "@id": "df87da74-7e9b-11ec-90d6-0242ac120003"
        "url": "http://de.wikipedia.org/..", 
        "language": "de-DE"
       }, 
       {
        "@type": "eagl:WikipediaEntry",
        "@id": "684416b4-2ea3-48b0-b55c-6a2410e2baec", 
        "eagl:hasWikipediaLink": {
            "@id": "df87da74-7e9b-11ec-90d6-0242ac120003"
        }
  ]
}
```

#### Changing an attribute value


