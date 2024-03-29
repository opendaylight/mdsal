.. _mdsal:

######
MD-SAL
######

.. _overview:

Overview
========

The Model-Driven Service Adaptation Layer (MD-SAL) is message-bus inspired
extensible middleware component that provides messaging and data storage
functionality based on data and interface models defined by application
developers (i.e. user-defined models).

The MD-SAL:

* Defines a **common-layer, concepts, data model building blocks and messaging
  patterns** and provides infrastructure / framework for applications and
  inter-application communication.

.. FIXME: Common integration point / reword this better

* Provide common support for user-defined transport and payload formats,
  including payload serialization and adaptation (e.g. binary, XML or JSON).

The MD-SAL uses **YANG** as the modeling language for both interface and data
definitions, and provides a messaging and data-centric runtime for such
services based on YANG modeling.

The MD-SAL provides two different API types (flavours):

* **MD-SAL Binding:** MD-SAL APIs which extensively uses APIs and classes
  generated from YANG models, which provides compile-time safety.
* **MD-SAL DOM:** (Document Object Model) APIs which uses DOM-like
  representation of data, which makes them more powerful, but provides less
  compile-time safety.

.. note::

   Model-driven nature of the MD-SAL and **DOM**-based APIs allows for
   behind-the-scene API and payload type mediation and transformation
   to facilitate seamless communication between applications - this enables
   for other components and applications to provide connectors / expose
   different set of APIs and derive most of its functionality purely from
   models, which all existing code can benefit from without modification.
   For example **RESTCONF Connector** is an application built on top of MD-SAL
   and exposes YANG-modeled application APIs transparently via HTTP and adds
   support for XML and JSON payload type.

.. _basic-concepts:

Basic concepts
==============

Basic concepts are building blocks which are used by applications, and from
which MD-SAL uses to define messaging patterns and to provide services and
behavior based on developer-supplied YANG models.

:Data Tree: All state-related data are modeled and represented as data tree,
    with possibility to address any element / subtree

    * **Operational Data Tree** - Reported state of the system, published by
      the providers using MD-SAL. Represents a feedback loop for applications
      to observe state of the network / system.
    * **Configuration Data Tree** - Intended state of the system or network,
      populated by consumers, which expresses their intention.

:Instance Identifier: Unique identifier of node / subtree in data tree, which
    provides unambiguous information, how to reference and retrieve node /
    subtree from conceptual data trees.

:Notification: Asynchronous transient event which may be consumed by
    subscribers and they may act upon it.

:RPC: asynchronous request-reply message pair, when request is triggered by
    consumer, send to the provider, which in future replies with reply message.

    .. note::

       In MD-SAL terminology, the term 'RPC' is used to define the input and
       output for a procedure (function) that is to be provided by a provider,
       and mediated by the MD-SAL, that means it may not result in remote call.

.. _messaging-patterns:

Messaging Patterns
==================

MD-SAL provides several messaging patterns using broker derived from
basic concepts, which are intended to transfer YANG modeled data between
applications to provide data-centric integration between applications instead
of API-centric integration.

* **Unicast communication**

  * **Remote Procedure Calls** - unicast between consumer and provider, where
    consumer sends **request** message to provider, which asynchronously
    responds with **reply** message.

* **Publish / Subscribe**

  * **Notifications** - multicast transient message which is published by
    provider and is delivered to subscribers.

  * **Data Change Events** - multicast asynchronous event, which is sent by
    data broker if there is change in conceptual data tree, and is delivered to
    subscribers.

* **Transactional access to Data Tree**

  * Transactional **reads** from conceptual **data tree** - read-only
    transactions with isolation from other running transactions.
  * Transactional **modification** to conceptual **data tree** - write
    transactions with isolation from other running transactions.
  * **Transaction chaining**

Table of Contents
=================

.. toctree::
   :maxdepth: 1

   architecture
   conceptual-data-tree
   incremental-backup
   binding-query-user-guide
   query-binding-language-developer-guide
   model-registration
