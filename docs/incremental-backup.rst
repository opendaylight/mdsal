##################
Incremental Backup
##################

Terminology
===========

:Backup Producer:
    Feature running on the primary site (which needs to be backed-up). It collects
    all the changes made in the datastore and sends them to the Backup Consumer
    after a connection is established.

:Backup Consumer:
    Feature running as a server on the secondary site. It listens for the changes
    sent by Backup Producer and applies them to the local datastore.

:DTCL:
    Data Tree Change Listener is an object, which is registered on a Node in the
    datastore and notified if said node(or any of its children) is modified.

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
secondary site one-by-one. All that's needed is to have a Producer on the
primary site, which collects and sends the changes and a Consumer on the
secondary site which then applies them. The transport mechanism can be chosen
according to the environment in which the two sites operate. Currently two
transports are supported: Kafka and Netty (both are described bellow)

Replication over LAN
--------------------

Kafka was chosen as the transport system for LAN replication since it is a
powerful tool and can be tweaked independently by the user. If the user
chooses to start multiple secondary sites, it can be done easily since
Kafka takes care of the message replication and distribution.

* Features
    * odl-mdsal-kafka-backup-producer
        .. code-block:: xml

            <dependency>
              <groupId>org.opendaylight.mdsal</groupId>
              <artifactId>odl-mdsal-kafka-backup-producer</artifactId>
              <version>${mdsal-version}</version>
              <classifier>features</classifier>
              <type>xml</type>
              <scope>runtime</scope>
            </dependency>

    * odl-mdsal-kafka-backup-consumer
        .. code-block:: xml

            <dependency>
              <groupId>org.opendaylight.mdsal</groupId>
              <artifactId>odl-mdsal-kafka-backup-consumer</artifactId>
              <version>${mdsal-version}</version>
              <classifier>features</classifier>
              <type>xml</type>
              <scope>runtime</scope>
            </dependency>


Replication over WAN
--------------------

Using Kafka in WAN environment has it's drawbacks. Other transport systems
were discussed and Netty was chosen. It provides peer-to-peer streaming and
guarantees the delivery order.

* Features
    * odl-mdsal-netty-backup-producer
        .. code-block:: xml

            <dependency>
              <groupId>org.opendaylight.mdsal</groupId>
              <artifactId>odl-mdsal-netty-backup-producer</artifactId>
              <version>${mdsal-version}</version>
              <classifier>features</classifier>
              <type>xml</type>
              <scope>runtime</scope>
            </dependency>

    * odl-mdsal-netty-backup-consumer
        .. code-block:: xml

            <dependency>
              <groupId>org.opendaylight.mdsal</groupId>
              <artifactId>odl-mdsal-netty-backup-consumer</artifactId>
              <version>${mdsal-version}</version>
              <classifier>features</classifier>
              <type>xml</type>
              <scope>runtime</scope>
            </dependency>


Configuration and Installation
------------------------------

For both the Kafka and Netty replication the installation and usage is the
same.

#. **Configure the Producer and Consumer**
    * Kafka Producer - configuration file "kafkaProducer.json"
        .. code-block:: json

           {
              "kafka-connection":{
                "kafka-host": "172.17.0.1",
                "kafka-port": 9092
              },
              "kafka-messaging": {
                "topic": "testTopic1",
                "partition" : 0,
                "key": "testKey"
              },
              "kafka-producer-properties": {
                "retries" : 0,
                "linger.ms" : 1,
                "max.block.ms" : 1000
              }
           }

        ``kafka-connection`` - host and port of the running Kafka instance

        ``kafka-messaging`` - messaging configuration

        ``kafka-producer-properties`` - properties exposed by the `Kafka Producer API
        <https://docs.confluent.io/current/installation/configuration/producer-configs.html#cp-config-producer>`_

    * Kafka Consumer - configuration file "kafkaConsumer.json"
        .. code-block:: json

            {
              "kafka-connection":{
                "kafka-host": "172.17.0.1",
                "kafka-port": 9092
              },
              "kafka-messaging": {
                "topic": "testTopic1"
              },
              "kafka-consumer-properties": {
                "group.id" : "test",
                "enable.auto.commit": "true",
                "auto.commit.interval.ms" : 3000
              }
            }

        ``kafka-connection`` - host and port of the running Kafka instance

        ``kafka-messaging`` - messaging configuration

        ``kafka-consumer-properties`` - properties exposed by the `Kafka Consumer API
        <https://docs.confluent.io/current/installation/configuration/consumer-configs.html>`_

    * Netty Producer - configuration file "nettyProducer.json"
        .. code-block:: json

            {
              "netty-connection":{
                "consumer-address": "172.17.0.2",
                "consumer-port": 9999,
                "connection-retry-interval-ms": 1000
              }
            }

        ``netty-connection`` - address and port of the Netty Consumer plus
        some additional options which will be added as the development continues

    * Netty Consumer - configuration file "nettyConsumer.json"
        .. code-block:: json

            {
              "netty-consumer":{
                "listening-port": 9999,
                "enable-server-auto-restart": false
              }
            }

        ``netty-consumer`` - the port on which the Consumer listens plus
        some additional options which will be added as the development continues

#. **Install Features**
    .. note:: Do not mix Netty and Kafka producers/consumers.

    * install the Consumer on the secondary site
        * feature:install odl-mdsal-kafka-backup-consumer
        * feature:install odl-mdsal-netty-backup-consumer

    * install the Producer on the primary site
        * feature:install odl-mdsal-kafka-backup-producer
        * feature:install odl-mdsal-netty-backup-producer

    As soon and the features are installed they will start the connection
    process. Nothing else is needed for the replication to begin.

#. **Modify the primary's site datastore / check Karaf logs**
    Any modification to the primary site should be replicated on the
    secondary site if the connection between Producer and Consumer
    was established. Connection status can be seen in Karaf log.

Switching Primary and Secondary sites
-------------------------------------

Sites can be switched simply by uninstalling the features and installing
them in the opposite direction.

Troubleshooting
---------------

In case the modifications don't appear on the secondary site, check
the following cases.

* Kafka Replication
    * Kafka Instance is running and reachable from both sites
    * Kafka Consumer and Provider are configured correctly.
    * Check the Karaf log to see, if the features started successfully
      and the connection was established.

* Netty Replication
    * Make sure the two sites can reach one-another (ping)
    * Make sure the Consumer is running. In case the connection is very
      unstable, it might be necessary to set the config parameter
      "enable-server-auto-restart" to true.
    * Check the Karaf log to see, if the features started successfully
      and the connection was established.
