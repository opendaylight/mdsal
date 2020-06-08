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
      * keepalive-interval-seconds <amount> *(10 is used if not set)*
      * max-missed-keepalives <amount> *(5 is used if not set)*

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
      * keepalive-interval-seconds <amount> *(10 is used if not set)*
      * max-missed-keepalives <amount> *(5 is used if not set)*

Switching Primary and Secondary sites
-------------------------------------

Sites can be switched simply by disabling the configurations and enabling
them in the opposite direction.

Example deployment
------------------

Running one ODL instance locally and one in Docker

#. **Run local ODL**
    .. code-block::

      karaf-distribution/bin/karaf

    Karaf Terminal - Start features
      - features-mdsal - core MDSAL features
      - odl-mdsal-exp-replicate-netty - netty replicator
      - odl-restconf-nb-bierman02 - we'll be using Postman to access datastore
      - odl-netconf-clustered-topolog - we will change data of some netconf devices

      .. code-block::

        feature:install features-mdsal odl-mdsal-exp-replicate-netty odl-restconf-nb-bierman02 odl-netconf-clustered-topolog

    Start Source
      .. code-block::

        config:edit org.opendaylight.mdsal.replicate.netty.source
        config:property-set enabled true
        config:update

#. **Run Dockerized Karaf distribution**
    To get access to Karaf Terminal running in Docker you can use:
      .. code-block::

        docker exec -ti $(docker ps -a -q --filter ancestor=<NAME-OF-THE-DOCKER-IMAGE>) /karaf-distribution/bin/karaf

    Start features in the Docker's Karaf Terminal
      .. code-block::

        feature:install features-mdsal odl-mdsal-exp-replicate-netty odl-restconf-nb-bierman02 odl-netconf-clustered-topolog

    Start Sink - Let's say the Docker runs at 172.17.0.2 meaning it will find the local Source is at 172.17.0.1
      .. code-block::

        config:edit org.opendaylight.mdsal.replicate.netty.sink
        config:property-set enabled true
        config:property-set source-host 172.17.0.1
        config:update

#. **Run Postman and try modifying the Source's datastore**
    Put data to the local datastore:
      - Header

        .. code-block::

          PUT http://localhost:8181/restconf/config/network-topology:network-topology/topology/topology-netconf/node/new-netconf-device

      - Body

        .. code-block::

          <node xmlns="urn:TBD:params:xml:ns:yang:network-topology">
            <node-id>new-netconf-device</node-id>
            <host xmlns="urn:opendaylight:netconf-node-topology">127.0.0.1</host>
            <port xmlns="urn:opendaylight:netconf-node-topology">16777</port>
            <username xmlns="urn:opendaylight:netconf-node-topology">admin</username>
            <password xmlns="urn:opendaylight:netconf-node-topology">admin</password>
            <tcp-only xmlns="urn:opendaylight:netconf-node-topology">false</tcp-only>
            <reconnect-on-changed-schema xmlns="urn:opendaylight:netconf-node-topology">false</reconnect-on-changed-schema>
            <connection-timeout-millis xmlns="urn:opendaylight:netconf-node-topology">20000</connection-timeout-millis>
            <max-connection-attempts xmlns="urn:opendaylight:netconf-node-topology">0</max-connection-attempts>
            <between-attempts-timeout-millis xmlns="urn:opendaylight:netconf-node-topology">2000</between-attempts-timeout-millis>
            <sleep-factor xmlns="urn:opendaylight:netconf-node-topology">1.5</sleep-factor>
            <keepalive-delay xmlns="urn:opendaylight:netconf-node-topology">120</keepalive-delay>
          </node>

    Get the data locally
      - Header

        .. code-block::

          GET http://localhost:8181/restconf/config/network-topology:network-topology/

    Get the data from the Docker. The change should be present there.
      - Header

        .. code-block::

          GET http://172.17.0.2:8181/restconf/config/network-topology:network-topology/