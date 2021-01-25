
########################
Binding query User Guide
########################

Feature Overview
========================
Query language based API for work with YANG based models.
MD-SAL component provides a binding query language to interact with the underlying data store.
This API provides an easy and type-safe mechanism for retrieving and processing data from generated DOM based on queries.
On the DOM layer the expression can be transmitted, and it gives the possibility to move the execution to
the storage backend. This can reduce app/backend interchange data.
This API is a part of the MD-SAL component and can be found inside the `org.opendaylight.mdsal.binding.api.query` package.

Query structure
========================

* *QueryExpression* - Built sequence-based expression. QueryExpression is similar to an SQL query expression. While SQL operates on tables and rows, QueryExpression operates on a subtree. Creates by *QueryFactory*.
* *QueryExecutor* - Interface to execute query expression and retrieve execution result.
* *QueryResult* - Result execution of *QueryExpression* by *QueryExecutor*.
    Query result will contain Objects which can be represented using the next methods:
        - *stream*  Returns sequential Stream of values from the query result.
        - *parallelStream* Returns parallel Stream of values from the query result.
        - *getValues* Returns List of generic Objects from the query result.
        - *getItems* Returns List of Items(Object and *InstanceIdentifier*) from query result.

Query Usage
========================
A QueryExpression is built up of three items, which specify *what* to search, looking for *something* matching
a *predicate*. This is similar in structure to being an SQL query:
FROM *what* SELECT *something* WHERE *predicate*.

*Query execution workflow:*

    Query Factory -> Root Path -> Query Builder -> (Extract child node -> Matcher) -> build

    *Query Factory* - Primary entry point for creating Query.

    *Root Path* - Specify Subtree root path for start from the query. This corresponds to the *what* part of a query.
    Just as with SQL tables, this path has to point to at most one item.

    *Query Builder* - Intermediate builder stage, which allows providing a specification of the query. On query completed
    call _build_ method finalize the creation of simple query.

    *Extract child node* - Add a child path component to the query specification of what needs to be extracted.
    This constitutes an intermediate step of specifying the *something* part of *what* needs be found.

    Child node can be specified in the next ways:
            - using child container *.class*;
            - using an exact match in a keyed list using *List* and *Key* types;
            - using child case *.class* and child *.class*;

    *Matcher* - Specify a matching pattern for request using Leaf's getter method and appropriate matcher

This constitutes the *predicate* part of the query. Every candidate has to match this pattern.

Leaf's value can be retrieved passing method reference from container type.

Query engine supports `Empty, string, int8, int16, int32, int64, uint8, uint16, uint32, uint64, Identity, TypeObject` leaf's value types. Appropriate MatchBuilder pattern applied according to leaf's value type.

*Example:*

Create a simple executor:
::

    QueryExecutor executor = SimpleQueryExecutor.builder(CODEC)
        .add(new FooBuilder()
            .setSystem(BindingMap.of(
                new SystemBuilder().setName("SystemOne").setAlarms(BindingMap.of(
                    new AlarmsBuilder()
                        .setId(Uint64.ZERO)
                        .setCritical(Empty.getInstance())
                        .setAffectedUsers(BindingMap.of()).build(),
                    new AlarmsBuilder()
                        .setId(Uint64.ONE)
                        .setAffectedUsers(BindingMap.of()).build()))
                    .build(),
                new SystemBuilder().setName("SystemTwo").setAlarms(BindingMap.of(
                    new AlarmsBuilder()
                        .setId(Uint64.ZERO)
                        .setCritical(Empty.getInstance())
                        .setAffectedUsers(BindingMap.of(
                        )).build())).build()))
            .build())
        .build();

Create query expression and execute it using executor above:
::

    QueryExpression<System> query = new DefaultQueryFactory(CODEC).querySubtree(InstanceIdentifier.create(Foo.class))
        .extractChild(System.class)
        .matching()
        .leaf(System::getName).contains("One")
        .build();
    final QueryResult result = executor.executeQuery(query);
    List items = result.getItems();

This expression will retrieve System node with name containing "One" from DOM tree.
::

    QueryExpression<Alarms> query
    = new DefaultQueryFactory(CODEC).querySubtree(InstanceIdentifier.create(Foo.class))
        .extractChild(System.class)
        .extractChild(Alarms.class)
        .matching()
        .leaf(Alarms::getId).valueEquals(Uint64.ZERO)
        .build();
    final QueryResult result = executor.executeQuery(query);
    List items = result.getItems();

The result of this query expression will be a list of two items - Alarms with Id of ZERO.

