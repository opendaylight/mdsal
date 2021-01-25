######
Binding query User Guide
######

Feature Overview
========
Query language based API for work with YANG based models.
Provides an easy and type-safe mechanism for retrieving and proceed data from generated DOM based on queries.
On the DOM layer the expression can be transmitted, and it gives the possibility to move the execution to
the storage backend. This can reduce app/backend interchange data.
The feature presented as OSGi component activated on main service start.

Query structure
========

* *QueryExpression* - Built sequence-based expression. Creates by _QueryFactory_.

* *QueryExecutor* - Interface to execute query expression and retrieve execution result.

* *QueryResult* - Result execution of _QueryExpression_ by _QueryExecutor_.
    Query result will contain Objects which can be represented using the next methods:
        - _stream_  Returns sequential Stream of values from the query result.
        - _parallelStream_ Returns parallel Stream of values from the query result.
        - _getValues_ Returns List of generic Objects from the query result.
        - _getItems_ Returns List of Items(Object and _InstanceIdentifier_) from query result.

Query Usage
========

*Query expression build workflow:*

    Query Factory -> Root Path -> Query Builder -> (Extract child node -> Matcher) -> build

    *Query Factory* - Primary entry point for creating Query. //TODO codecs

    *Root Path* - Specify Subtree root path for start from the query.

    *Query Builder* - Intermediate builder stage, which allows providing a specification of the query. On query completed
    call _build_ method finalize the creation of simple query.

    *Extract child node* - Add a child path component to the query specification of what needs to be extracted
        Child node can be specified in the next ways:
            - using child container _.class_;
            - using an exact match in a keyed list using _List_ and _Key_ types;
            - using child case _.class_ and child _.class_;

    *Matcher* - Specify a matching pattern for request using Leaf's getter method and //TODO matcher
        Leaf's value can be retrieved passing method reference from container type.
        Query engine supports `Empty, string, int8, int16, int32, int64, uint8, uint16, uint32, uint64, Identity,
        TypeObject` leaf's value types. Appropriate MatchBuilder pattern applied according to leaf's value type.

*Example:*
Create a simple executor:

`QueryExecutor executor = SimpleQueryExecutor.builder(CODEC)
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
`
Create query expression and execute it using executor above:
`
QueryExpression<System> query = new DefaultQueryFactory(CODEC).querySubtree(InstanceIdentifier.create(Foo.class))
    .extractChild(System.class)
    .matching()
    .leaf(System::getName).contains("One")
    .build();
final QueryResult result = executor.executeQuery(query);
List items = result.getItems();
`
This expression will retrieve System node with name containing "One" from DOM tree.

`
QueryExpression<Alarms> query = new DefaultQueryFactory(CODEC).querySubtree(InstanceIdentifier.create(Foo.class))
    .extractChild(System.class)
    .extractChild(Alarms.class)
    .matching()
    .leaf(Alarms::getId).valueEquals(Uint64.ZERO)
    .build();
final QueryResult result = executor.executeQuery(query);
List items = result.getItems();
`

Result of this query expression will be list of two items - Alarms with Id of ZERO.

