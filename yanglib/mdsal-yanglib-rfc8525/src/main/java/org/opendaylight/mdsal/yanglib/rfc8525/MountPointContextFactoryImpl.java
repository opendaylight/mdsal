/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc8525;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.yanglib.api.SchemaContextResolver;
import org.opendaylight.mdsal.yanglib.api.SourceReference;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Operational;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.ModulesState;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.RevisionIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.YangLibrary;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.CommonLeafs;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.CommonLeafsRevisionBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.Module.ConformanceType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.set.parameters.ImportOnlyModule;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.set.parameters.ImportOnlyModuleRevisionBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.set.parameters.Module;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.yang.library.parameters.Datastore;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.yang.library.parameters.ModuleSet;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.yang.library.parameters.Schema;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.YangIdentifier;
import org.opendaylight.yangtools.rcf8528.data.util.AbstractMountPointContextFactory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContextFactory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier;
import org.opendaylight.yangtools.rfc8528.data.api.YangLibraryConstants.ContainerName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MountPointContextFactoryImpl extends AbstractMountPointContextFactory {
    private static final Logger LOG = LoggerFactory.getLogger(MountPointContextFactoryImpl.class);

    @SuppressWarnings("deprecation")
    private final BindingDataObjectCodecTreeNode<ModulesState> legacyCodec;
    private final BindingDataObjectCodecTreeNode<YangLibrary> codec;
    private final EffectiveModelContext yangLibContext;
    private final SchemaContextResolver resolver;

    MountPointContextFactoryImpl(final MountPointIdentifier mountId, final SchemaContextResolver resolver,
            final EffectiveModelContext yangLibContext,
            final BindingDataObjectCodecTreeNode<YangLibrary> codec,
            final BindingDataObjectCodecTreeNode<ModulesState> legacyCodec) {
        super(mountId);
        this.resolver = requireNonNull(resolver);
        this.yangLibContext = requireNonNull(yangLibContext);
        this.codec = requireNonNull(codec);
        this.legacyCodec = requireNonNull(legacyCodec);
    }

    @Override
    protected MountPointContextFactory createContextFactory(final MountPointDefinition mountPoint) {
        return new MountPointContextFactoryImpl(mountPoint.getIdentifier(), resolver, yangLibContext, codec,
            legacyCodec);
    }

    @Override
    protected Optional<SchemaContext> findSchemaForLibrary(final ContainerName containerName) {
        switch (containerName) {
            case RFC7895:
            case RFC8525:
                return Optional.of(yangLibContext);
            default:
                LOG.debug("Unhandled YANG library container {}", containerName);
                return Optional.empty();
        }
    }

    @Override
    protected SchemaContext bindLibrary(final ContainerName containerName, final ContainerNode libData)
            throws YangParserException {
        switch (containerName) {
            case RFC7895:
                return bindLibrary(verifyNotNull(legacyCodec.deserialize(libData)));
            case RFC8525:
                return bindLibrary(verifyNotNull(codec.deserialize(libData)));
            default:
                throw new IllegalStateException("Unhandled container type " + containerName);
        }
    }

    private @NonNull SchemaContext bindLibrary(final @NonNull YangLibrary yangLib) throws YangParserException {
        final List<Datastore> datastores = yangLib.nonnullDatastore();
        checkArgument(!datastores.isEmpty(), "No datastore defined");

        final Iterator<Datastore> it = datastores.iterator();
        final Datastore ds = it.next();
        final QName dsQName = BindingReflections.findQName(ds.getName());

        // This is the default if we do not have enough context
        final String schemaName;
        if (!Operational.QNAME.equals(dsQName)) {
            // FIXME: try to find a better one
            schemaName = ds.getSchema();
        } else {
            schemaName = ds.getSchema();
        }

        final List<SourceReference> requiredSources = new ArrayList<>();
        final List<SourceReference> librarySources = new ArrayList<>();
        final HashSet<String> moduleSet = findModuleSet(yangLib, schemaName);
        for (ModuleSet modSet : yangLib.nonnullModuleSet()) {
            if (moduleSet.remove(modSet.getName())) {
                fillModules(librarySources, requiredSources, modSet);
            }
        }
        checkArgument(moduleSet.isEmpty(), "Failed to resolve module sets %s", moduleSet);

        return resolver.resolveSchemaContext(librarySources, requiredSources);
    }

    private static HashSet<String> findModuleSet(final YangLibrary yangLib, final String schemaName) {
        for (Schema schema : yangLib.nonnullSchema()) {
            if (schemaName.equals(schema.getName())) {
                return new HashSet<>(schema.getModuleSet());
            }
        }
        throw new IllegalArgumentException("Failed to find moduleSet for " + schemaName);
    }

    private static void fillModules(final List<SourceReference> librarySources,
            final List<SourceReference> requiredSources, final ModuleSet modSet) {
        // TODO: take deviations/features into account

        for (ImportOnlyModule mod : modSet.nonnullImportOnlyModule()) {
            fillSource(librarySources, mod.getName(), ImportOnlyModuleRevisionBuilder.toYangCommon(mod.getRevision()),
                mod.getLocation());
            mod.nonnullSubmodule().forEach(sub -> {
                fillSource(librarySources, sub.getName(), toYangCommon(sub.getRevision()), sub.getLocation());
            });
        }
        for (Module mod : modSet.nonnullModule()) {
            fillSource(requiredSources, mod.getName(), toYangCommon(mod.getRevision()), mod.getLocation());
            mod.nonnullSubmodule().forEach(sub -> {
                fillSource(librarySources, sub.getName(), toYangCommon(sub.getRevision()), sub.getLocation());
            });
        }
    }

    private static void fillSource(final List<SourceReference> sources, final YangIdentifier sourceName,
            final Optional<Revision> revision, final List<Uri> uris) {
        final SourceIdentifier sourceId = RevisionSourceIdentifier.create(sourceName.getValue(), revision);
        sources.add(uris == null || uris.isEmpty()
                ? SourceReference.of(sourceId)
                        : SourceReference.of(sourceId, URI.create(uris.get(0).getValue())));
    }

    private static Optional<Revision> toYangCommon(final @Nullable RevisionIdentifier revisionIdentifier) {
        return revisionIdentifier == null ? Optional.empty() : Optional.of(Revision.of(revisionIdentifier.getValue()));
    }

    @SuppressWarnings("deprecation")
    private @NonNull SchemaContext bindLibrary(final @NonNull ModulesState modState) throws YangParserException {
        final List<SourceReference> requiredSources = new ArrayList<>();
        final List<SourceReference> librarySources = new ArrayList<>();

        for (org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.
                Module module : modState.nonnullModule()) {
            final SourceReference modRef = sourceRefFor(module, module.getSchema());

            // TODO: take deviations/features into account

            if (ConformanceType.Import == module.getConformanceType()) {
                librarySources.add(modRef);
            } else {
                requiredSources.add(modRef);
            }

            for (org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.
                    module.Submodule submodule : module.nonnullSubmodule()) {
                // Submodules go to library, as they are pulled in as needed
                librarySources.add(sourceRefFor(submodule, submodule.getSchema()));
            }
        }

        return resolver.resolveSchemaContext(librarySources, requiredSources);
    }

    @SuppressWarnings("deprecation")
    private static SourceReference sourceRefFor(final CommonLeafs obj, final Uri uri) {
        final SourceIdentifier sourceId = RevisionSourceIdentifier.create(obj.getName().getValue(),
            CommonLeafsRevisionBuilder.toYangCommon(obj.getRevision()));
        return uri == null ? SourceReference.of(sourceId) : SourceReference.of(sourceId, URI.create(uri.getValue()));
    }
}
