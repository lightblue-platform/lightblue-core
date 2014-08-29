# Associations

This document describes how Lightblue evaluates associations between
entities. Associations allow injecting one entity into another as an
array field. For instance, order entity and customer entity are
business entities, and every order includes a customer:

```
order : {
  ....
  "customerId":{ "type":"string" },
  "customer": { "type":"reference",
                "entity" : "customer",
                "version" : "1.0.0",     
                "query" : { "field":"_id", "op":"=", "rfield":"$parent.customerId" }
   }
```

In this example, the customer field of the order entity is populated
using customers whose id match order.customerId

## Considerations:

### Query plan depends both on association queries and the search query.

Let A be defined as:

```
A: { 
     "b" : { "type":"reference",
             "entity":"B",
             "query": { "field":"a_id", "op":"=", "rfield":"$parent._id"}
           } 
  }
```

Here, the field 'a_id' is a field of entity B.

If the retrieval criteria is

```
   { "field":"_id", "op":"=", "rvalue":<value> }
```

then the optimum retrieval is:

```
   retrieve A using  {"field":"_id","op":"=","rvalue":<value>}
     retrieve B using {"field":"a_id","op":"=","rvalue":<_id> }
```

If the retrieval criteria is

```
   { "field":"b.someField","op":"=","rvalue":<value> }
```

then the optimum retrieval is:

```
    retrieve B using {"field":"someField","op":"=","rvalue":<value>}
      retrieve A using {"field":"_id","op":"=","rvalue":<b.a_id>}
```

### Sorting requests need to be considered

A sort request can contain fields from only one entity. Otherwise,
we'd have to build the result set and sort it ourselves. For
simplicity, lets assume that sort request can only contain fields from
the root entity.

### Query plan depends on the projection request

The requested projection and query determine what parts of entity
association tree will be retrieved. The composite metadata that will
be used to build the query plan must be constructed to include all the
fields requested by the projection, and all the fields referred to in
the request query.

## Composite Metadata

Composite metedata is a tree computed from the metadata of the entity
that is being retrieved by extending that entity using the fields of
associated entities. The composite metadata computation should take
into account the request projection and request query fields, so all
the queried and projected fields should be in the composite
metadata. The root node of the composite metadata corresponds to the
requested entity. Child nodes of the composite metadata correspond to
the entities reached via a reference field.

Example:

```
A : {
       "b" : { "type":"reference",
               "entity": "B",
               ...
             },
       "c" : { "type":"reference",
               "entity": "C",
               ..
             },
       "a" : { "type":"reference",
               "entity":"A",
                ...
             },
       ...
    }
```

Here, the composite metadata is a tree:

<pre>
  N_1 (A)
  |
  +-- N_2 (B)
  |
  +-- N_3 (C)
  |
  +-- N_4 (A)
      |
      +-- N_5 (B)
      .
      .
</pre>

The  depth  of the  composite  metadata  depends  on what  fields  are
requested.   That means,  a  separate instance  of composite  metadata
needs to be constructed for each retrieval request. Assuming the above
metadata definition is given:

<pre>
req: { "field":"b.someField", "op":"=", "rvalue":<value> }
projection: { "a.a.c" }
Composite Metadata: 


   N_1 (A)
    |
    +-- N_2 (B)
    |
    +-- N_4 (A)
         |
         +-- N_7 (A)
              |
              +-- N_9 (C)
</pre>      

As shown above, an entity may appear more than once in the composite
metadata. 

Implementation considerations: 
 - Composite metadata should provide an EntityScheme instance containing 
   all the fields. Ideally,  composite metadata should extend EntityMetadata.
 - Composite metadata should provide additional APIs to expose the tree 
   structure of entities.

## Query Plans

A query plan is a tree where each node contains a node of composite
metadata, and a query that will be executed to retrieve entities for
the entity of that node. We will construct different query plans with
different node orderings, and score them to pick the best retrieval
strategy.

<pre>
   (N_1, Q_1)
    |
    +-- (N_2, Q_2)
    |    |
    |    +-- (N_3, Q_3)
    |
    +-- (N4, Q_4)
</pre>

Entities are retrieved using a depth-first traversal of the query
plan. The query that will be evaluated for a node N is the conjunction
of all queries containing the variables of N and the entities of all
ancestors of N. If a query clause contains variables referring to the
ancestors of N, that query clause will be rewritten to replace that
variable with a value.

Implementation considerations: Some queries can be converted to $in
expressions for bulk retrieval, or retrival can be performed one by
one.

In the above tree, the execution goes like this:
 - Q_1 is run, and an instance of entityOf(N_1) is retrieved. Remember, 
   Q_1 is a query that only depends on fields of entityOf(N_1).
 - Q_2 is a query that depends on only fields from entityOf(N_1) and entityOf(N_2). 
   So, Q_2 is rewritten to replace fields in entityOf(N_1) with values, and 
   N_2 values are retrieved. 
 - ... and so on


### Rewriting queries:

FieldComparisonExpression instances need to be rewritten as
ValueComparisonExpressions once one of the fields can be replaced with
a value. That happens after an instance of an containing that field is
retrieved.

```
{ "field":<field1>, "op":<op>, "rfield":<field2> }
```

Assume field1 refers to a field of entityOf(N_i), and field2 refers to
a field of an entity of an ancestor of N_i (i.e. field2 values are
known). Then, the query component can be written as a
ValueComparisonExpression:

```
  {"field":"<field1>, "op":<operator>, "rvalue":<value> }
```

(if field and rfield are interchanged and if the operation is not
commutative, operator should be negated as well, i.e. > should become
<)

### Processing query expressions

Ideally, we'd like to deal with queries in conjunctive normal form.

```
   Q: { "$and": [ q_1, q_2, q_3, ... ] }
```

If the request query and all the associations queries are in
conjunctive normal form, all we need to worry about is the query
clauses q_1, q_2,..., and associate these clauses to the nodes of the
query plan. To reformat the queries in conjunctive normal form, we
will apply the following rewrite rules, and obtain several equivalent
queries.

These are the query rewrite rules:

 1) Nested $and's can be combined:

If Q: { "$and" : [ q_1, ... {"$and": [ x, y, ... ] } ] }

then Q can be written as
```
   { "$and": [ q_1, ..., x, y, ... ] }
```

  2) Multiple values in an $or can be written as $in

If Q : { "$or" : [ ..., {"field":X, "op":"=","rvalue":v1},... {"field":X, "op":"=","rvalue":v2}, ... ] }

then Q can be written as
```
   { "$or" : [ ..., {"$in" : {"field":X,"values":[v1,v2]} },... ] }
```

  3) $in can be extended

If Q:  { "$or" : [ ..., {"$in" : {"field":X,"values":[v1,v2]} }, ...  {"field":X, "op":"=","rvalue":v3}, ... }

then Q can be written as

```
   { "$or" : [ ..., {"$in": [ "field":X,"values":[v1,v2,v3]} },... ] }
```


  4) $or with a single expression can be eliminated

If Q: { "$or" : [ q ] }, then Q can be written as q

  5) $not $or can be converted to $and

If Q: { "$not" : { "$or" : [ q_1, q_2, ... ] ] } 

then Q can be written as
```
   {"$and" : [ not(q_1), not(q_2),  ... ] }
```

provided not(q_i) can be performed for all q_i.


Anything contained in an $or operator is useless for query plan
construction. There is no way to efficiently retrieve something like
that, unless underlying data collection is small.

## Query plan construction

We are given composite metadata C, the request query RQ, requested
projection RP, and requested sort RS, we have to construct query plans
and score them.

This is the entity definition for the following examples:

```
A : {
  ....
  "_id":{"type":"uid" },
  "b_id":{ "type":"string" },
  "b": { "type":"reference",
         "entity" : "B",
         "query" : { "field":"_id", "op":"=", "rfield":"$parent.b_id" }
   }
  "c_id":{ "type":"string" },
  "c": { "type":"reference",
         "entity" : "C",
         "query" : { "field":"_id", "op":"=", "rfield":"$parent.c_id" }
   }
}
```

Notation: X < Y means X is an ancestor of Y. That is, X is retrieved
before Y, and that the two are related.

Valid relation sets: The valid relation sets of composite metadata is
constructed for each leaf node X as follows:

  - X is in the valid relation set of X
  - Ancestors of X are in the valid relation set of X

That is, the nodes of all maximal paths from the root to a leaf forms
a valid relation set. Within a query plan, only relations between the
members of a valid relation set is allowed. If there is a relation
between two members of different valid relation sets, that query plan
is non-viable.

In the above example, (A,B) and (A,C) are valids relation sets. That
means, any relation between A and B, and A and C form viable query
plans. A relation containing fields from different relations sets form
nonviable query plans, such as B<C or C<B.

The overview of the algorithm is:

  1) Convert RQ to conjunctive normal form to get CRQ. Also, convert
  all association queries to their conjunctive normal forms.
  
  2) For each clause:

    - If a clause refers to one variable of a node in composite
      metadata, associate that clause with the query plan node
      corresponding to that composite metadata node.

Example:

The clause 

```
 {"field":"_id", "op":"=", "rvalue":<value> } 
```

will be associated to the query plan node for "A".


The clause

```
  {"field":"b.someField","op":"=","rvalue":<value>}
```

will be associated the the query plan node for the field "b".

     - If a clause has two fields referring to different entities,

Example:

The clause

```
 { "field":"b.a_id","op":"=","rfield":"a._id"}
```

generates two query plans. The first one has query
 
```
 { "field":"b.a_id", "op":"=", "rvalue":<value> }
```

associated with node for "b", and "A" is retrieved before "B". The
second one has query

```
 { "field":"a._id", "op":"=", "rvalue":<value> }
```

associated with node "a", and "B" is retrieved before "A".

  3) Eliminate query plans that are not viable. A query plan is not
  viable if one of the following is correct:
    - there exists nodes X and Y such that X<Y and Y<X, or
    - X<Y and X and Y are in different valid relation sets.

Note: Take into account transitivity. If X<Y<Z and A<B<C and Y<A, then
X<A, X<B, X<C, Y<B, Y<C are all true.

  4) Iterate and score all viable query plans.

### Scoring Query Plans

The score assigned to the query plan should reflect the effort of
executing it. So smaller score means better performance. We will
select the query plan with the smallest score.

#### Scoring nodes:

Scores given to individual nodes, in decreasing order:

  - No associated node query
  - A disjunction clause, score decreases with decreasing number of distinct fields
  - A conjunction clause, score decreases with increasing number of indexed fields
  - Fields with unique indexes only
  
Scoring algorithm starts from the root, and processes the query plan
in a depth first manner. The score of the node is the base score
multiplied with (maxdepth - depth+1). The final score of the query plan is the sum of
the scores of all nodes.

In these examples, assume the scores are 40, 30, 20, 10.

Example 1:

Request query: { "field":"b.someValue", "op":"=", "rvalue":<value> }

Query plan:
  A  
  |
  +-- B  { "field":"a_id", "op":"=", "rvalue":<$parent.id>}
         { "field":"b.someValue", "op":"=", "rvalue":<value> }

Node A score: 40 * 2
Node B score: 20 * 1

Total score: 100

Query plan:
  B  { "field":"b.someValue", "op":"=", "rvalue":<value> }
  |
  +-- A  { "field":"_id", "op":"=", "rvalue":<$parent.a_id>}

Node B score: 20 * 2
Node A score: 10 * 1

Total score: 50

Second query plan wins.

Example 2:

Request query: { "field":"someValue", "op":"=", "rvalue":<value> }

Query plan:
  A  { "field":"someValue", "op":"=", "rvalue":<value> }
  |
  +-- B  { "field":"a_id", "op":"=", "rvalue":<$parent.id>}

Node A score: 20 * 2
Node B score: 10 * 1

Total score: 50

Query plan:
  B  
  |
  +-- A  { "field":"_id", "op":"=", "rvalue":<$parent.a_id>}
         { "field":"someValue", "op":"=", "rvalue":<value> }

Node B score: 40 * 2
Node A score: 20 * 1

Total score: 100

First query plan wins.

