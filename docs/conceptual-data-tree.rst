####################
Conceptual Data Tree
####################

.. _terminology:

Terminology
===========

:Data Tree:
    An instantiated logical tree that represents configuration or operational
    state data of a modeled problem domain (for example, a controller or a
    network)

:Data Tree Consumer:
    A component acting on data, after this data are introduced into one or more
    particular subtrees of a Data Tree.

:Data Tree Identifier:
    A unique identifier for a particular subtree of a Data Tree. It is composed
    of the logical data store type and the instance identifier of the subtree's
    root node. It is represented by a ``DOMDataTreeIdentifier``.

:Data Tree Producer:
    A component responsible for providing data for one or more particular
    subtrees of a Data Tree.

:Data Tree Shard:
    A component responsible for providing storage or access to a particular
    subtree of a Data Tree.

:Shard Layout:
    A longest-prefix mapping between Data Tree Identifiers and Data Tree Shards
    responsible for providing access to a data subtree.

Basic Concepts
==============

Data Tree is a Namespace
------------------------

The concept of a data tree comes from `RFC6020
<https://tools.ietf.org/html/rfc6020>`_. It is is vaguely
split into two instances, configuration and operational. The implicit
assumption is that **config implies oper**, i.e. any configuration data is
also a valid operational data. Further interactions between the two are left
undefined and the YANG language is not strictly extensible in the number and
semantics of these instances, leaving a lot to implementation details. An
outline of data tree use, which is consistent with the current MD-SAL design,
is described in `draft-kwatsen-netmod-opstate
<https://tools.ietf.org/html/draft-kwatsen-netmod-opstate>`_.

The OpenDaylight MD-SAL design makes no inherent assumptions about the
relationship between the configuration and operational data tree instances.
They are treated as separate entities and they are both fully addressable via
the ``DOMDataTreeIdentifier`` objects. It is up to MD-SAL plugins (e.g.
protocol plugins or applications) to maintain this relationship. This reflects
the asynchronous nature of applying configuration and also the fact that the
intended configuration data may be subject to translation (such as template
configuration instantiation).

Both the configuration and operational namespaces (data trees) are instances
of the Conceptual Data Tree. Any data item in the conceptual data tree is
addressed via a ``YangInstanceIdentifier`` object, which is a unique,
hierarchical, content-based identifier. All applications use the identifier
objects to identify data to MD-SAL services, which in turn are expected to
perform proper namespace management such that logical operation connectivity is
maintained.

.. todo::

   Can you reword '...are expected to perform proper namespace management such
   that logical operation connectivity is maintained...' - not clear what you
   mean

.. _identifiers-vs-locators:

Identifiers versus Locators
---------------------------

It is important to note that when we talk about Identifiers and Locators,
we **do not** mean `URIs and URLs
<https://en.wikipedia.org/wiki/Uniform_Resource_Identifier>`_,
but rather URNs and URLs as strictly separate entities. MD-SAL plugins do not
have access to locators and it is the job of MD-SAL services to provide
location independence.

The details of how a particular MD-SAL service achieves location independence
is currently left up to the service's implementation, which leads to the
problem of having MD-SAL services cooperate, such as storing data in different
backends (in-memory, SQL, NoSQL, etc.) and providing unified access to all
available data. Note that data availability is subject to capabilities of a
particular storage engine and its operational state, which leads to the design
decision that a ``YangInstanceIdentifier`` lookup needs to be performed in two
steps:

#. A longest-prefix match is performed to locate the storage backend instance
   for that identifier
#. Masked path elements are resolved by the storage engine

.. _data-tree-shard:

Data Tree Shard
---------------

A process similar to the first step above is performed today by the Distributed
Data Store implementation to split data into Shards. The concept of a Shard as
currently implemented is limited to specifying namespaces, and it does not
allow for pluggable storage engines.

In context of the Conceptual Data Tree, the concept of a Shard is generalized
as the shorthand equivalent of a storage backend instance. A Shard can be
attached at any (even wildcard) ``YangInstanceIdentifier``. This contract is
exposed via the ``DOMShardedDataTree``, which is an MD-SAL SPI class that
implements an ``YangInstanceIdentifier`` -> ``Shard`` registry service. This is
an omnipresent MD-SAL service, Shard Registry, whose visibility scope is a
single OpenDaylight instance (i.e. a cluster member). **Shard Layout** refers
to the mapping information contained in this service.

Federation, Replication and High Availability
---------------------------------------------

Support for various multi-node scenarios is a concern outside of core MD-SAL.
If a particular scenario requires the shard layout to be replicated (either
fully or partially), it is up to Shard providers to maintain an omnipresent
service on each node, which in turn is responsible for dynamically registering
``DOMDataTreeShard`` instances with the Shard Registry service.

Since the Shard Layout is strictly local to a particular OpenDaylight instance,
an OpenDaylight cluster is not strictly consistent in its mapping of
``YangInstanceIdentifier`` to data. When a query for the entire data tree is
executed, the returned result will vary between member instances based on the
differences of their Shard Layouts. This allows each node to project its local
operational details, as well as the partitioning of the data set being worked
on based on workload and node availability.

Partial symmetry of the conceptual data tree can still be maintained to
the extent that a particular deployment requires. For example the Shard
containing the OpenFlow topology can be configured to be registered on all
cluster members, leading to queries into that topology returning consistent
results.

.. _design:

Design
======

.. _design-listener:

Data Tree Listener
------------------

A Data Tree Listener is a data consumer, for example a process that wants
to act on data after it has been introduced to the Conceptual Data Tree.

A Data Tree Listener implements the :mdsal-apidoc:`DOMDataTreeListener
<DOMDataTreeListener.html>` interface and registers itself using
:mdsal-apidoc:`DOMDataTreeService <DOMDataTreeService.html>`.

A Data Tree Listener may register for multiple subtrees. Each time it is
invoked it will be provided with the current state of all subtrees that it
is registered for.

.. todo:: Consider linking / inlining interface

.DOMDataTreeListener interface signature

.. code-block:: java

   public interface DOMDataTreeListener extends EventListener {

       void onDataTreeChanged(Collection<DataTreeCandidate> changes, // (1)
           Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> subtrees);

       void onDataTreeFailed(Collection<DOMDataTreeListeningException> causes); // (2)
   }

#. Invoked when the data tree to which the Data Tree Listener is subscribed
   to changed. `changes` contains the collection of changes, `subtrees`
   contains the current state of all subtrees to which the listener is
   registered.
#. Invoked when a subtree listening failure occurs. For example, a failure
   can be triggered when a connection to an external subtree source is
   broken.


.. _design-producer:

Data Tree Producer
------------------

A Data Tree Producer represents source of data in system. Data TreeProducer
implementations are not required to implement a specific interface, but
use a :mdsal-apidoc:`DOMDataTreeProducer <DOMDataTreeProducer.html>` instance
to publish data (i.e. to modify the Conceptual Data Tree).

A Data Tree Producer is exclusively bound to one or more subtrees of the
Conceptual Data Tree, i.e. binding a Data Tree Producer to a subtree prevents
other Data Tree Producers from modifying the subtree.

* A failed Data Tree Producer still holds a claim to the namespace to which
  it is bound (i.e. the exclusive lock of the subtree) until it is closed.

:mdsal-apidoc:`DOMDataTreeProducer <DOMDataTreeProducer.html>` represents a
Data Tree Producer context

* allows transactions to be submitted to subtrees specified at creation
  time
* at any given time there may be a single transaction open.
* once a transaction is submitted, it will proceed to be committed
  asynchronously.

.. todo:: Consider linking / inlining interface

.DOMDataTreeProducer interface signature

.. code-block:: java

   public interface DOMDataTreeProducer extends DOMDataTreeProducerFactory, AutoCloseable {
       DOMDataWriteTransaction createTransaction(boolean isolated); // (1)
       DOMDataTreeProducer createProducer(Collection<DOMDataTreeIdentifier> subtrees); // (2)
   }

#. Allocates a new transaction. All previously allocated transactions must
   have been either submitted or canceled. Setting `isolated` to `true`
   disables state compression for this transaction.
#. Creates a sub-producer for the provided `subtrees`. The parent producer
   loses the ability to access the specified paths until the resulting child
   producer is closed.

.. _design-shard:

Data Tree Shard
---------------

- **A Data Tree Shard** is always bound to either the ``OPERATIONAL``, or the
  ``CONFIG`` space, never to both at the same time.

- **Data Tree Shards** may be nested, the parent shard must be aware of
  sub-shards and execute every request in context of a self-consistent view of
  sub-shards liveness. Data requests passing through it must be multiplexed
  with sub-shard creations/deletions. In other words, if an application creates
  a transaction rooted at the parent Shard and attempts to access data residing
  in a sub-shard, the parent Shard implementation must coordinate with the
  sub-shard implementation to provide the illusion that the data resides in the
  parent shard. In the case of a transaction running concurrently with
  sub-shard creation or deletion, these operations need to execute atomically
  with respect to each other, which is to say that the transactions must
  completely execute as if the sub-shard creation/deletion occurred before the
  transaction started or as if the transaction completed before the sub-shard
  creation/deletion request was executed. This requirement can also be
  satisfied by the Shard implementation preventing transactions from
  completing. A Shard implementation may choose to abort any open transactions
  prior to executing a sub-shard operation.

- **Shard Layout** is local to an OpenDaylight instance.

- **Shard Layout** is modified by agents (registering / unregistering Data Tree
  Shards) in order to make the Data Tree Shard and the underlaying data
  available to plugins and applications executing on that particular
  OpenDaylight instance.

Registering a Shard with the Conceptual Data Tree
-------------------------------------------------

.. note:: Namespace in this context means a Data Tree Identifier prefix.

#. **Claim a namespace** - An agent that is registering a shard must prove that
   it has sufficient rights to modify the subtree where the shard is going to be
   attached. A namespace for the shard is claimed by binding a Data Tree
   Producer instance to same subtree where the shard will be bound. The Data
   Tree Producer must not have any open child producers, and it should not have
   any outstanding transactions.

#. **Create a shard instance** - Once a namespace is claimed, the agent creates
   a shard instance.

#. **Attach shard** - The agent registers the created shard instance and
   provides in the reigstration the Data Tree Producer instance to verify the
   namespace claim. The newly created Shard is checked for its ability to
   cooperate with its parent shard. If the check is successful, the newly
   created Shard is attached to its parent shard and recorded in the Shard
   layout.

#. **Remove namespace claim** (optional) - If the Shard is providing storage for
   applications, the agent should close the Data Tree Producer instance to make
   the subtree available to applications.

.. important::

   Steps 1, 2 and 3 may fail, and the recovery strategy depends
   on which step failed and on the failure reason.

.. todo:: Describe possible failures and recovery scenarios
