##################
Incremental Backup
##################

Terminology
===========

:Source:
    Waits for a Sink to connect. After it does, registers a DTCL and starts sending all changes to the Sink.

:Sink:
    Connects to the Source and asks for changes on a particular path in the datastore(root by default).
    All changes received from the Source are applied to the Sink's datastore.

:DTCL:
    Data Tree Change Listener is an object, which is registered on a Node in the datastore andnotified if
    said node(or any of its children) is modified.

Concept
=======

Incremental Backup vs Daexim
----------------------------

The concept of Incremental Backup originated from Daexim drawbacks. Importing
the whole datastore may take a while since it triggers all the DTCLs.
Therefore using Daexim as a mechanism for backup is problematic, since the
export/import process needs to be executed quite frequently to keep the two
sites synchronized.

Incremental Backup simply mirrors the changes made on the primary site to the
secondary site one-by-one. All that's needed is to have a Source on the
primary site, which sends the changes and a Sink on the
secondary site which then applies them. The transport mechanism used is Netty.

Replication (works both for LAN and WAN)
----------------------------------------

Once the Sink is started it tries to connect to the Source's address and port.
Once the connection is established, the Sink sends a request containing a path
in the datastore which needs to be replicated. Source receives this request and
registers DTCL on said path. Any changes the listener receives are then streamed
to the Sink. When Sink receives them he applies them to his datastore.

In case there is a network partition and the connection goes down, the Source unregisters
the listener and simply waits for the Sink to reconnect. When the connection goes UP again
and the Sink reconnects, the Source registers the DTCL again and continues replicating.
Therefore even if there were some changes in the Source's datastore while the connection
was down, when the Sink reconnects and Source registers new DTCL, the current initial state
will be replicated to the Sink. At this point they are synchronized again and the replication
can continue without any issue.

* Features
    * odl-mdsal-exp-replicate-netty
        .. code-block:: xml

          <dependency>
            <groupId>org.opendaylight.mdsal</groupId>
            <artifactId>odl-mdsal-exp-replicate-common</artifactId>
            <classifier>features</classifier>
            <type>xml</type>
          </dependency>
          <dependency>
            <groupId>org.opendaylight.mdsal</groupId>
            <artifactId>odl-mdsal-exp-replicate-netty</artifactId>
            <classifier>features</classifier>
            <type>xml</type>
          </dependency>


Configuration and Installation
------------------------------

#. **Install the features on the primary and secondary site**
    .. code-block::

      feature:install odl-mdsal-exp-replicate-netty odl-mdsal-exp-replicate-common

#. **Enable Source (on the primary site)**
    .. code-block::

      config:edit org.opendaylight.mdsal.replicate.netty.source
      config:property-set enabled true
      config:update

    All configuration options:
      * enabled <true/false>
      * listen-port <port> *(9999 is used if not set)*

#. **Enable Sink (on the secondary site)**
    *In this example the Source is at 172.16.0.2 port 9999*

    .. code-block::

      config:edit org.opendaylight.mdsal.replicate.netty.sink
      config:property-set enabled true
      config:property-set source-host 172.16.0.2
      config:update

    All configuration options:
      * enabled <true/false> *(127.0.0.1 is used if not set)*
      * source-host <address> *(127.0.0.1 is used if not set)*
      * source-port <port> *(9999 is used if not set)*
      * reconnect-delay-millis <reconnect-delay> *(3000 is used if not set)*
      * keepalive-interval-seconds <keepalive-interval> *(10 is used if not set)*

Switching Primary and Secondary sites
-------------------------------------

Sites can be switched simply by disabling the configurations and enabling
them in the opposite direction.
