[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.opendaylight.mdsal/mdsal-artifacts/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.opendaylight.mdsal/mdsal-artifacts)
[![Javadocs](https://www.javadoc.io/badge/org.opendaylight.mdsal/mdsal-docs.svg)](https://www.javadoc.io/doc/org.opendaylight.mdsal/mdsal-docs)
[![License](https://img.shields.io/badge/License-EPL%201.0-blue.svg)](https://opensource.org/licenses/EPL-1.0)

# MD-SAL

## Overview

The Model-Driven Service Adaptation Layer (MD-SAL) is message-bus inspired
extensible middleware component that provides messaging and data storage
functionality based on data and interface models defined by application developers
(i.e. user-defined models).

The MD-SAL:

* Defines a *common-layer, concepts, data model building blocks and messaging
   patterns* and provides infrastructure / framework for applications and
   inter-application communication.

// FIXME: Common integration point / reword this better
* Provide common support for user-defined transport and payload formats, including
   payload serialization and adaptation (e.g. binary, XML or JSON).

The MD-SAL uses *YANG* as the modeling language for both interface and data
definitions, and provides a messaging and data-centric runtime for such services
based on YANG modeling.

The MD-SAL provides two different API types (flavours): +

Binding:: 
  MD-SAL APIs which extensively uses APIs and classes generated
  from YANG models, which provides compile-time safety and allows developers
  to use more natural way to work with data.
DOM::
  (Document Object Model) APIs which uses DOM-like
  representation of data, which makes them more powerful, but provides less
  compile-time safety.

NOTE: Model-driven nature of the MD-SAL and *DOM*-based APIs allows for
behind-the-scene API and payload type mediation and transformation
to facilitate seamless communication between applications - this enables
for other components and applications to provide connectors / expose different
set of APIs and derive most of its functionality purely from models, which
all existing code can benefit from without modification.
For example *RESTCONF Connector* is an application built on top of MD-SAL
and exposes YANG-modeled application APIs transparently via HTTP and adds support
for XML and JSON payload type.

## Contributing

MD-SAL is part of http://opendaylight.org[OpenDaylight Project], where we
use http://git.opendaylight.org[Gerrit] for incoming patch reviews.
