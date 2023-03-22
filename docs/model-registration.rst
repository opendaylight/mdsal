
############################                                                                                                     
Registering Models with OSGi
############################

The OSGi framework activates the *OSGiModelRuntime* component, who initiates:

* **YangModuleInfoScanner** - tracks bundles and attempts to retrieve *YangModuleInfo*, which it then feeds to *YangModuleInfoRegistry*
* **YangModuleInfoRegistry** - updates *SchemaContext* each time new *YangModuleInfo* is added/removed

The diagram below shows the process of registering the modules found in a recently tracked bundle.
The **YangModuleInfoScanner** collects the *ModuleInfo*-s from that bundle and forwards them to the **YangModuleInfoRegistry**.
From there the **ModuleInfoSnapshotResolver** checks for existing registration of each *YangModuleInfo*
and if exists, it reuses, otherwise creates a fresh registration with the help of *YangTextSchemaContextResolver*.

After each *ModuleInfo* is registered, the **YangModuleInfoScanner** initiates a snapshot update
on the **YangModuleInfoRegistry**. During the snapshot capture, the *EffectiveSchemaContext*
is updated and preserved within that snapshot.

.. uml::

  @startuml
    boundary " " <<OSGi Tracker>>
    participant OSGiModelRuntime as "OSGiModelRuntime"
    participant YangModuleInfoScanner as "YangModuleInfoScanner"
    participant YangModuleInfoRegistry as "YangModuleInfoRegistry"
    participant ModuleInfoSnapshotResolver as "ModuleInfoSnapshotResolver"
    participant YangTextSchemaContextResolver as "YangTextSchemaContextResolver"

    -> OSGiModelRuntime : OSGi Component activation
    activate OSGiModelRuntime

    OSGiModelRuntime -> YangModuleInfoScanner: Initialize YangModuleInfoScanner as BundleTracker
    activate YangModuleInfoScanner

    OSGiModelRuntime -> YangModuleInfoRegistry: Initialize YangModuleInfoRegistry
    activate YangModuleInfoRegistry

    deactivate OSGiModelRuntime
    deactivate YangModuleInfoScanner
    deactivate YangModuleInfoRegistry

    " " -> YangModuleInfoScanner : Bundle registered
    activate YangModuleInfoScanner

    YangModuleInfoScanner -> YangModuleInfoScanner: Load the services \nfrom the bundle \nthat was recently tracked.

    YangModuleInfoScanner -> YangModuleInfoScanner: Collect YangModuleInfo-s \nfrom loaded services

    YangModuleInfoScanner -> YangModuleInfoRegistry: Register collected ModuleInfos
    activate YangModuleInfoRegistry

    YangModuleInfoRegistry -> ModuleInfoSnapshotResolver: Register ModuleInfos
    activate ModuleInfoSnapshotResolver

    alt No previous Registration for the ModuleInfo => create a fresh Registration
        ModuleInfoSnapshotResolver -> YangTextSchemaContextResolver : Register provided ModuleInfo
        activate YangTextSchemaContextResolver

        YangTextSchemaContextResolver --> ModuleInfoSnapshotResolver: Return fresh Registration
        deactivate YangTextSchemaContextResolver

        ModuleInfoSnapshotResolver -> YangModuleInfoRegistry: Return fresh Registration
    else Registration for provided ModuleInfo exists
        ModuleInfoSnapshotResolver -> YangModuleInfoRegistry : Return existing Registration
        deactivate ModuleInfoSnapshotResolver
    end

    YangModuleInfoRegistry -> YangModuleInfoScanner: Return Registration

    YangModuleInfoScanner -> YangModuleInfoRegistry : Scanner update

    YangModuleInfoRegistry -> ModuleInfoSnapshotResolver : Take a snapshot
    activate ModuleInfoSnapshotResolver

    ModuleInfoSnapshotResolver -> YangTextSchemaContextResolver : Ask for new schema context
    activate YangTextSchemaContextResolver

    YangTextSchemaContextResolver -> YangTextSchemaContextResolver : Create EffectiveModelContext \nfrom registered sources

    YangTextSchemaContextResolver -> ModuleInfoSnapshotResolver : Return created schema context
    deactivate YangTextSchemaContextResolver

    ModuleInfoSnapshotResolver -> YangModuleInfoRegistry : Return a snapshot with the created schema context
    deactivate ModuleInfoSnapshotResolver

    YangModuleInfoRegistry -> YangModuleInfoRegistry : Save the snapshot

    YangModuleInfoRegistry -> YangModuleInfoScanner : Done
    deactivate YangModuleInfoRegistry

    YangModuleInfoScanner -> " " : Return Registration
    deactivate YangModuleInfoRegistry


    deactivate YangModuleInfoRegistry
    deactivate YangModuleInfoScanner
  @enduml

The diagram below shows all relevant components and their relations.

.. uml::

  @startuml
    class "OSGiModuleRuntime" as runtime {
      void deactivate()
    }
    class "YangModuleInfoScanner" as bundleTracker {
      Registration addingBundle()
      void modifiedBundle()
      void removedBundle()
    }
    class "YangModuleInfoRegistry" as registry {
      Registration registerBundle()
      void scanerUpdate()
      void enableScannerAndUpdate()
      void scannerShutdown()
    }
    class "ModuleInfoSnapshotResolver" as resolver {
      List<Registration> registerModuelInfos()
      ModuleInfoSnapshot takeSnapshot()
    }
    class "YangTextSchemaContextResolver" as textResolver {
      YangTextSchemaSourceRegistration registerSource()
      EffectiveModelContext getEffectiveModelContext()
    }

    runtime -- bundleTracker : initiates
    runtime -- registry : initiates

    registry -- resolver
    resolver -- textResolver
  @enduml
