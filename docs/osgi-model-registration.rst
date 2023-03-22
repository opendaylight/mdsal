.. _model-registration:

############################
Model Registration with OSGi
############################

.. _overview:

Overview
========

When a feature is installed ``feature:install <feature-name>``, the OSGi framework goes through the related bundles
and searches for all the YANG files, collects them and builds a *SchemaContext*.


.. _service-component-activation:

Service Component Activation - OSGiModelRuntime
===============================================

*OSGiModelRuntime* is identified as a Service Component and is activated via its constructor.
The constructor initializes:

* **YangModuleInfoRegistry** - updates *SchemaContext* each time new *YangModuleInfo* is added/removed
* **YangModuleInfoScanner** - tracks bundles and attempts to retrieve *YangModuleInfo*, which is then fed

into the aforementioned *YangModuleInfoRegistry*

The initialized **YangModuleInfoScanner** (our Bundle Tracker) stores found YANG schemas in the **YangModuleInfoRegistry**
as **RevisionSourceIdentifiers**, which is a simple transfer object that represents revision identifier
for YANG schema (module or submodule), which consists of: *Yang schema name* and *Module revision (optional)*.

All available sources are collected and a new **EffectiveModelContext** is computed.

The computation is performed in **SharedEffectiveModelContextFactory.resolveEntry()**.
Before the computation itself, a **SettableFuture<EffectiveSchemaContext>** is first acquired and only after that
the *resolveEntry()* method is called. It is done this way so that there is no need to worry about races
around **EffectiveModelContext** being garbage-collected just after having computed it
and before acquiring a reference to it.

Process of computing the **EffectiveModelContext**:

1.
 .. note::
    The word source(s) is mentioned many times in this text.
    It refers to the *particular YANG schema* (module or submodule).

 The sources are loaded as **IRSchemaSource**, that has two fields: *symbolic name* and *root statement IRStatement*
 (with keyword being **module** and argument being the module’s **identifier**)

    * IRStatement

    Single YANG statement in its raw string form.
    Statement is composed of:

        * mandatory keyword modeled as *IRKeyword*
        * optional argument modeled as *IRArgument*
        * zero or more nested statements (substatements)

2. After the sources are loaded they are put through **SourceMismatchDetector**, that detects mismatch between requested
Source IDs and IDs that are extracted from parsed source. Also, duplicates are removed if present.

    * Source ID = **SourceIdentifier** of particular YANG  schema.

    * "requested Source IDs" - source IDs acquired in step 1). This **SourceIdentifier** is wrapped in
      the **IRSchemaSource**

    * “IDs extracted from parsed sources“ - source IDs extracted from the **YangModuleInfo** that was retrieved from the
      “bundle tracker“ **YangModuleInfoScanner** (mentioned in the beginning of the page).

3. Sources are then assembled into **EffectiveSchemaContext**

Assembling sources
""""""""""""""""""

**YangParser** is initialized, through which the sources are added into the **BuildGlobalContext**.
After that, the **buildEffective()** method is called. This triggers method **executePhases()**,
which executes these phases:

* *SOURCE_PRE_LINKAGE*
* *SOURCE_LINKAGE*
* *STATEMENT_DEFINITION*
* *FULL_DECLARATION*
* *EFFECTIVE_MODEL*

Each of these phases goes through:

* **startPhase(phase)**

    Firstly it is checked if the previous phase of the current phase matches the finished phase.
    After that the phase is "activated" on each source (**SourceSpecificContext**) - here it is again checked
    if the previous phase matches the finished phase of this source.
    This phase is saved in this source (**SourceSpecificContext**) as *inProgressPhase*.

    It is also saved as *currentPhase* in the **GlobalBuildContext**

* **loadPhaseStatements()**

    Slightly different between the phases (described on each phase).

* **completePhaseActions()**

    Attempting to progress on each source by calling *tryToCompletePhase(currentPhase.executionOrder())* on it.
    The outcome of this method is:
        * *PhaseCompletionProgress.FINISHED* - if no further phase modifiers are present
        * *PhaseCompletionProgress.PROGRESS* - if phase modifiers are present and we were able to progress with them
        * *PhaseCompletionProgress.NO_PROGRESS* - if phase modifier are present, but no progress has been made with them

* **endPhase(phase)**

    The current phase is stored into the *finishedPhase* variable


Description of phases
^^^^^^^^^^^^^^^^^^^^^

* **SOURCE_PRE_LINKAGE**

  * startPhase(phase)
  * loadPhaseStatements()

    * *writePreLinkage()* method is applied on each source, where a new **StatementContextWriter** is passed
      with the current statement definitions = **QNameToStatementDefinition** = Map of available statement definitions
    * The *writePreLinkage()* provides only pre-linkage related statements to supplied writer.
      Only the supplied statements may be written to statement writer.
    * List of all pre-linkage related statements:

      * module
      * submodule
      * namespace
      * import
      * include
      * belongs-to
      * prefix
      * yang-version
      * revision
      * revision-date

    * Each statement (and its substatements) is being processed. This processing of a statement means
      starting the statement on the writer = *writer.startStatement()*
    * The statements relevant to this phase are started, checked if fully defined, stored and ended
      (check how it is done in the SOURCE_LINKAGE phase)

  * completePhaseActions()
  * endPhase(phase)

* **SOURCE_LINKAGE**

  * startPhase(phase)
  * loadPhaseStatements()

    * on each source the *writeLinkage()* method is applied, where a new **StatementContextWriter** is passed alongside
      the current statement definitions = **QNameToStatementDefinition** = Map of available statement definitions.
    * Other parameters are passed to the method:

      * pre-linkage prefixes = *PrefixToModule* = Pre-linkage map of source-specific prefixes to namespaces,
      * YANG version = *YangVersion*

    * The *writeLinkage()* provides only linkage related statements (list of those below) to supplied writer
      based on specified YANG version. Only the supplied statements may be written to statement writer.
    * Each statement (and its substatements) is being processed. This processing of a statement means starting
      the statement on the writer = *writer.startStatement()*.

      This method is supplied with:

      * child identifier (unique among siblings)
      * name (fully qualified name of statement)
      * argument (string representation of value as appeared in source, null if not present)
      * reference (identifier of location in source, which will be used for reporting in case
        of statement processing error)

    * List of all linkage related statements:

      * <all statements from previous phases => SOURCE_PRE_LINKAGE>
      * description
      * reference
      * contact
      * organization

    * After being started, the statement is further processed, where it is checked if it is fully defined
      and it is stored - *writer.storeStatement()* - this ensures the statement is not null
      and the size of the map where the children are stored is updated with the supplied *expectedChildren*
      (which is first checked if it is a valid number ... i.e. >= 0).
    * Each started statement must be ended - *writer.endStatement()* - this call exits the current phase
      for this statement by performing final actions of the current phase on this statement.
    * For **SOURCE_LINKAGE** it is adding the keys (of type according to namespaces relevant
      for this phase - ModuleNamespace, ModuleNamespaceForBelongsTo, ...)

  * completePhaseActions()
  * endPhase(phase)

* **STATEMENT_DEFINITION**

  * startPhase(phase)
  * loadPhaseStatements()

    * on each source the *writeLinkageAndStatementDefinitions()* method is applied, where a new
      **StatementContextWriter** is passed alongside the current statement definitions = **QNameToStatementDefinition**
      = Map of available statement definitions.
    * Other parameters are passed to the method:

      * prefixes = *PrefixToModule* = map of source-specific import and belongs to prefixes to namespaces,
      * YANG version = *YangVersion*
    * The *writeLinkageAndStatementDefinitions()* provides only linkage and language extension statements
      to supplied writer based on specified YANG version.
      Only the supplied statements may be written to statement writer.
    * List of all statements related to this phase:

      * <all statements from previous phases => SOURCE_PRE_LINKAGE, SOURCE_LINKAGE>
      * yin-element
      * argument
      * extension
      * typedef
      * identity
      * default
      * status
      * type
      * units
      * require
      * bit
      * path
      * enum
      * length
      * pattern
      * modifier
      * range
      * key
      * container
      * grouping
      * list
      * unique
      * action
      * rpc
      * input
      * output
      * notification
      * fraction
      * base

    * The supplied statements are again processed the same way as in described in the **SOURCE_LINKAGE**

  * completePhaseActions()
  * endPhase(phase)

* **FULL_DECLARATION**

  * startPhase(phase)
  * loadPhaseStatements()

    * on each source the *writeFull()* method is applied, where a new **StatementContextWriter** is passed alongside
      the current statement definitions = **QNameToStatementDefinition** = Map of available statement definitions.
    * Other parameters are passed to the method:

      * prefixes = *PrefixToModule* = map of source-specific import and belongs to prefixes to namespaces,
      * YANG version = *YangVersion*

    * The *writeFull()* provides every statement present in this statement source to supplied writer
      based on specified YANG version.
      Only the supplied statements may be written to statement writer.
    * List of all statements related to this phase (all the statements):

      * <all statements from previous phases => SOURCE_PRE_LINKAGE, SOURCE_LINKAGE, STATEMENT_DEFINITION>
      * leaf
      * config
      * deviation
      * choice
      * case
      * must
      * mandatory
      * anyxml
      * anydata
      * if-feature
      * uses
      * error-message
      * error-app-tag
      * leaf-list
      * presence
      * max-elements
      * min-elements
      * ordered-by
      * when
      * augment
      * refine
      * feature
      * position
      * value

    * extensions added into the FULL_DECLARATION phase:

      * OpenDaylight extensions:

        * augment-identifier
        * context-instance
        * context-reference
        * instance-target
        * rpc-context-reference

      * RFC 6241 extension:

        * get-filter-element-attributes

      * RFC 6536 extensions:

        * default-deny-all
        * default-deny-write

      * RFC 6643 extensions:

        * display-hint
        * max-access
        * defval
        * implied
        * alias
        * oid
        * subid

      * RFC 7952 extension:

        * annotation

      * RFC 8040 extension:

        * yang-data

      * RFC 8528 extension:

        * mount-point

      * RFC 8639 extension:

        * subscription-state-notification

      * OpenConfig extensions (except openconfig-version):

        * encrypted-value
        * hashed-value

    * The supplied statements are again processed the same way as in described in the **SOURCE_LINKAGE**

  * completePhaseActions()
  * endPhase(phase)

* **EFFECTIVE_MODEL**

  * startPhase(phase)
  * loadPhaseStatements()
  * completePhaseActions()

    * The phase is completed in the same way as the previous ones (described in the beginning),
      which results in having acquired the effective model.
    * The sources are released.

  * endPhase(phase)


After all phases are executed the *transformEffective()* method is called. First the it is checked if
the *finishedPhase* == **EFFECTIVE_MODEL**. After that assertion a root statement (as **DeclaredStatement**)
and root effective statement (as **EffectiveStatement**) are extracted from each source.
These extracted statements are used to populate *Lists* that are then passed as parameters to method
*create()* in **EffectiveSchemaContext** which creates new effective schema context.
Before calling the *create()* method, any mutable statements are sealed,
which is the last step of statement parser processing.
The mutable statement is finished and made immutable.
After this, any further modifications of that current are not allowed.
