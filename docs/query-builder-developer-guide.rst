.. _mdsal-binding-dev-guide:

MD-SAL Binding Query Language Developer Guide
=============================================

.. note::

	Reading this section is likely useful as it contains an overview
	of MD-SAL Binding query language in OpenDaylight and a how-to use it for
	retrieving data from data storage.

Retrieving data from storage
----------------------------

MD-SAL has two ways (operations) of retrieving data from storage: read-like and query-like operations.

Read-like operation
~~~~~~~~~~~~~~~~~~~

The method **read** of ReadTransaction interface.

::

	<T extends DataObject> FluentFuture<Optional<T>> read(LogicalDatastoreType store, InstanceIdentifier<T> path);

The method reads data from the provided logical data store located at the provided path.
If the target is a subtree, then the whole subtree is read (and will be accessible from the returned DataObject).
So we are getting DataObject which we need to processed in code for getting relevant data:

::

    FluentFuture<Optional<Foo>> future;
    try (ReadTransaction rtx = getDataBroker().newReadOnlyTransaction()) {
        future = rtx.read(LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(Foo.class));
    }
    Foo haystack = future.get().orElseThrow();
    Object result = null;
    for (System system : haystack.nonnullSystem().values()) {
        if (needle.equals(system.getAlias())) {
            result = system;
            break;
        }
    }

.. note::

	The structure of Foo container is `here`_.

.. _here: https://github.com/opendaylight/mdsal/blob/master/binding/mdsal-binding-test-model/src/main/yang/mdsal-query.yang
	
Query-like operation
~~~~~~~~~~~~~~~~~~~~

The method **execute** of QueryReadTransaction interface.

::

	<T extends DataObject> FluentFuture<QueryResult<T>> execute(LogicalDatastoreType store, QueryExpression<T> query);

The method executes a query on the provided logical data store for getting relevant data.
So we are getting result which we need for future business logic processing.
Before running method execute we need to prepared query with the match predicates.
For example we want to find in Foo container the System with alias **target-needle**:

::

    String needle = "target-needle";
    QueryExpression<System> query = factory.querySubtree(InstanceIdentifier.create(Foo.class))
        .extractChild(System.class)
            .matching()
                .leaf(System::getAlias).valueEquals(needle)
            .build();

The method **querySubtree** creates a new **DescendantQueryBuilder** for a specified root path. It's intermediate query builder stage,
which allows the specification of the query result type to be built up via **extractChild(Class)** and 
**extractChild(Class, Class)** methods. They used to specify which object type to select from the root path.
Once completed, use either **build()** to create a simple query, or **matching()** to transition to specify predicates.
There is a bunch of overloaded methods **leaf** which based on the type of arguments returns specific match builders:

- ValueMatchBuilder methods:

| **ValueMatch<T> nonNull();**
| **ValueMatch<T> isNull();**
| **ValueMatch<T> valueEquals(V value);**

- ComparableMatchBuilder extends ValueMatchBuilder and adds methods:

| **ValueMatch<T> lessThan(V value);**
| **ValueMatch<T> lessThanOrEqual(V value);**
| **ValueMatch<T> greaterThan(V value);**
| **ValueMatch<T> greaterThanOrEqual(V value);**

- StringMatchBuilder extends ValueMatchBuilder and adds methods:

| **ValueMatch<T> startsWith(String str);**
| **ValueMatch<T> endsWith(String str);**
| **ValueMatch<T> contains(String str);**
| **ValueMatch<T> matchesPattern(Pattern pattern);**

After creation of query, we can use it in the method execute of QueryReadTransaction interface:

::

    FluentFuture<QueryResult<System>> future;
    try (ReadTransaction rtx = getDataBroker().newReadOnlyTransaction()) {
        future = ((QueryReadTransaction) rtx).execute(LogicalDatastoreType.CONFIGURATION, query);
    }
    QueryResult<System> result = future.get();