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
                "query" : { "field":"_id", "op":"=", "rfield":"$parent.customerId" },
                "projection": { "field":"*","include":true,"recursive":true},
                "sort" : { "$parent._id":"asc" }
   }
```

  * type: "reference": Identifies the field as a reference type.
  * entity: The name of the entity referred from the current entity
  * version: The particular version of the entity that is being referenced
  * query: A query to select the referenced entity. The query expression is 
    expected to relate some fields of the referenced entity with some fields 
    of the referencing entity. The query is evaluated relative to the current
    field. So, a field reference of the form "fieldName" refers to a field in 
    the referenced entity. A field reference of the form "$parent.fieldName" 
    refers to a field in the referencing entity.
  * projection: Determines which fields of the referenced entity will be
    injected into the referencing entity.
  * sort: An optional sort to order the referenced entity instances

In this example, the customer field of the order entity is populated
using customers whose _id match order.customerId. All fields of the
matching customers are injected into the order entity. If there are
multiple customers matching that criteria, they are sorted by their
_id, in ascending order.

## Considerations:

### Reference projection is optional

If no projection is given, assume all fields are included,
recursively. Note that recursive inclusions don't cross entity
boundaries.

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

Composite metedata is a directed tree. The requested entity is at the
root of the composite metadata. Every entity arrived by following an
association is another node in the composite metadata, and the edge
points to the destination of the association. The composite metadata
computation should take into account the request projection and
request query fields, so all the queried and projected fields should
be in the composite metadata. The root node of the composite metadata
corresponds to the requested entity. Child nodes of the composite
metadata correspond to the entities reached via a reference field.

Cyclic references are possible. We deal with these as follows:
  * Composite metadata is computed based on the request projection and
    request query. Composite metadata will be constructed with sufficient 
    depth to retrieve all required fields, and nothing more.
  * The interpretation of recursive inclusion projections will be changed to
    not cross entity boundaries. So, in the example below if the request 
    projection is {"field":"*","include":true,"recursive":true}, the projection
    will not include contents of "b", "c", and "a". To include "a", the request
    must ask for
```
 [ {"field":"*","include":true,"recursive":true},
      {"field":"a.*","include":"true","recursive":true} ]
```
    This will include only one level of "a". To include "a.a", another projection 
    must be added:
```
 [ {"field":"*","include":true,"recursive":true},
      {"field":"a.*","include":true,"recursive":true},
      {"field":"a.a.*","include":true,"recursive":true}  ]
```


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
 - Composite metadata should provide an EntitySchema instance containing 
   all the fields. Ideally,  composite metadata should extend EntityMetadata.
 - CompositeMetadata implementation should provide additional APIs to expose 
   the tree structure of entities.

## Query Plans

A query plan is a directed graph and a set of constraints of the form
(X before Y) where X and Y are nodes. Each node contains a node of
composite metadata, and a query that will be executed to retrieve
entities for the entity of that node. We will construct different
query plans with different node orderings, and score them to pick the
best retrieval strategy. The additional constraints will be used to
order the execution of unrelated nodes.

The underlying undirected graph of a query plan is isomorphic to the
underlying undirected graph of its composite metadata. In other words,
query plan may reverse the direction of edges of the composite
metadata, but cannot add or remove edges.

<pre>
   (N_1, Q_1) --+ 
                |
                +--> (N_3, Q_3) --> (N_4, Q_4)
                |
   (N_2, Q_2) --+

   (N_1 before N_2)
</pre>

In the above query plan, entities for N_1 and N_2 are retrieved first,
then using those results, a query to retrieve N_3 is written and
executed. For every N_3 retrieved, N_4 queries are evaluated and
executed.

"before" constraints: "before" constraints determine the ordering of
independent nodes. In the above example, there are no structural
constraints on wheter to evaluate node N_1 first or node N_2
first. Different query plans can add "before" constraints to force a
particular ordering. In the above query plan with "before" constraint
(N_1 before N_2), node N_1 is to be evaluated before node N_2.

Entities are retrieved starting from the source nodes (nodes with no
incoming edges). Execution continues until all the sink nodes of the
query plan are retrieved. A node is evaluated after all the ancestors
of it are evaluated.

The queries assigned to each node are the clauses of the conjunctive
normal form of all queries. A query Q is assigned to a node N if Q has
variables referring to N, and all other variables of Q refer to an
ancestor of N.

Future consideration: Paths starting from different sources can be
executed in parallel, depending on the before constraints.

Implementation considerations: Some queries can be converted to $in
expressions for bulk retrieval, or retrival can be performed one by
one.


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

If 
```
Q: { "$and" : [ q_1, ... {"$and": [ x, y, ... ] } ] }
```
then Q can be written as
```
   { "$and": [ q_1, ..., x, y, ... ] }
```

  2) Multiple values in an $or can be written as $in

If 
```
Q : { "$or" : [ ..., {"field":X, "op":"=","rvalue":v1},... {"field":X, "op":"=","rvalue":v2}, ... ] }
```
then Q can be written as
```
   { "$or" : [ ..., {"$in" : {"field":X,"values":[v1,v2]} },... ] }
```

  3) $in can be extended

If 
```
Q:  { "$or" : [ ..., {"$in" : {"field":X,"values":[v1,v2]} }, ...  {"field":X, "op":"=","rvalue":v3}, ... }
```
then Q can be written as
```
   { "$or" : [ ..., {"$in": [ "field":X,"values":[v1,v2,v3]} },... ] }
```

  4) $or with a single expression can be eliminated

If 
```
Q: { "$or" : [ q ] }
``` 
then Q can be written as q

  5) $not $or can be converted to $and

If 
```
Q: { "$not" : { "$or" : [ q_1, q_2, ... ] ] } 
```
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

Notation: X < Y means X is an ancestor of Y in query plan. 

### Algorithm

Enumerate all permutations of edge directions of composite metadata. For each option:
 * Assign queries to each node
 * Rewrite queries based on the query plan
    * Determine node ordering based on queries with multiple fields
    * Eliminate query plan if there are queries that cannot be rewritten.
 * Score query plan, and keep the best score

Complexity: If there are N nodes in composite metadata, there are
(N-1) edges in the query plan. Query plans are obtained by flipping
edges, so there are 2^(N-1) distinct query plans. This is the baseline
complexity. Further query plans can be created based on query
structure.

Example: The above example has 3 nodes, so 4 distinct query plans:

1)
<pre>
A --+--> B
    |
    +--> C
</pre>

2)
<pre>
B --> A --> C
</pre>

3)
<pre>
C --> A --> B
</pre>

4)
<pre>
C--+
   |
   +--> A
   |
B--+
</pre>


#### Query assignments

Below, let nodeOf(f) denote the query plan node of the path f. In the
above example, nodeOf("c._id")=C. For a query Q, nodesOf(Q) is the
union of nodeOf(f) for all fields f in Q.

 * If q is one of
<pre>
{ "field":f, "op":o, "rvalue":r }
{ "field":f, "regex":... }
{ "array":f, "contains":... }
</pre>
then q is assigned to nodeOf(f)

 * If q is 
<pre>
 { "field":f, "op":o, "rfield":g }
</pre>
then 
     * If nodeOf(f) < nodeOf(g), q is assigned to nodeOf(g)
     * If nodeOf(g) < nodeOf(f), q is assigned to nodeOf(f)
     * otherwise, generate a copy of the query plan. In the first copy, 
       add (nodeOf(f) before nodeOf(g)) and assign q to nodeOf(g). In 
       the second copy, add (nodeOf(g) before nodeOf(f)) and assign q 
       to nodeOf(f).

 * If q is
<pre>
{ "array":f, "elemtMatch":R }
</pre>
then we work with the node set nodeOf(f) and nodesOf(R). For every 
possible ordering of the nodes of the query:
   * Create a copy of the query plan.
   * Assign query to the highest node with respect to <
   * Add "before" constraints for all unrelated nodes

Warning: This process has the potential to become too costly to be
practical. Need some heuristics...

We can't filter the results of a search. All entries returned from a
seach should either appear in the result set, or be fed into another
search criteria. So: if there is a query associated with a node that
would require filtering out the results obtained from one of the
ancestors of that node, that query plan will be eliminated. 

Assume N is a node with a query q with a value clause (a clause that
defines a relation between a field and a value).  If M < N, M has to
be retrieved first with no queries. Then, N will be retrieved with a
query, and the result set for M needs to be filtered out based on the
matching elements in N. So, such query plans should be discarded.

That means, nodes with queries with value clauses must be ordered to
be before nodes without queries with value clauses.

### Query rewriting

If a query clause q depends on multiple nodes, that clause needs to be
rewritten. Assume nodesOf(q) is {N_1, N_2, ... }. q is associated with
all of these nodes. The clause q is rewritten for each node as follows:

Assume we're rewriting q for node N:

  * If there is a field f in q with nodeOf(f) < N, then replace the
    clause containing f with a value clause, where the value of f is
    bound to the value retrieved by the execution of node
    nodeOf(f). If f is already a value clause, eliminate query plan,
    as this requires manual filtering of ancestors.

  * If there is a field f in q with N < nodeOf(f), then eliminate
    query plan, it cannot be evaluated.

  * Otherwise, add a constraint that all nodes other than N must be
    retrieved before N. Once all queries are processed, test the
    constraints for conflicts, i.e. an implication that for nodes X,
    Y, (X before Y) and (Y before X).


### Node ordering

During query rewriting, we collect constraints of the form (X before
Y) for nodes X and Y. These constraints will be used to order the
execution of nodes that are not of the same lineage. If all
constraints cannot be satisfied, then the query plan is not viable,
and discarded.

Some observations:
  * Node ordering is essentially assigning indexes 1, 2, ... to each 
    node. 
  * For any node N, index of N is always larger than the number of 
    its descendants.
  * For any node N, index of N is always one more that the maximum
    index of its descendants.

These should limit the search space. The first ordering that satisfies
all the constraints wins.


### Scoring Query Plans

The score assigned to the query plan should reflect the effort of
executing it. So smaller score means better performance. We will
select the query plan with the smallest score.

Ideally, score should reflect how quickly the next item in the
resultset can be retrieved. For instance, a query that retrieves all
elements of a collection without any filtering should have the same,
or similar score to a query that retrieves one row using a unique
index.

Heuristics for scoring a query plan:
  * Only include nodes with value clauses. If node, only include the first node.
  * Node scoring, in increasing order
     * Node with no queries
     * Node with value query on fields with unique index
     * Node with value query on field with non-unique index
     * Node with value query on field without index
     * Node without value query
  * Final score: sum ( (numNodes - nodeIndex) * nodeScore )

