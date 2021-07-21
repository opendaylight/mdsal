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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingIdentityCodec;
import org.opendaylight.mdsal.yanglib.api.SchemaContextResolver;
import org.opendaylight.mdsal.yanglib.api.SourceReference;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Operational;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.LegacyRevisionUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.ModulesState;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.RevisionIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.RevisionUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.YangLibrary;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.CommonLeafs;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.Module.ConformanceType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.yang.library.parameters.Datastore;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.yang.library.parameters.DatastoreKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.yang.library.parameters.ModuleSet;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.yang.library.parameters.SchemaKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.YangIdentifier;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContextFactory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier;
import org.opendaylight.yangtools.rfc8528.data.api.YangLibraryConstants.ContainerName;
import org.opendaylight.yangtools.rfc8528.data.util.AbstractMountPointContextFactory;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MountPointContextFactoryImpl extends AbstractMountPointContextFactory {
    private static final Logger LOG = LoggerFactory.getLogger(MountPointContextFactoryImpl.class);

    @SuppressWarnings("deprecation")
    private final BindingDataObjectCodecTreeNode<ModulesState> legacyCodec;
    private final BindingDataObjectCodecTreeNode<YangLibrary> codec;
    private final BindingIdentityCodec identityCodec;
    private final EffectiveModelContext yangLibContext;
    private final SchemaContextResolver resolver;

    MountPointContextFactoryImpl(final MountPointIdentifier mountId, final SchemaContextResolver resolver,
            final EffectiveModelContext yangLibContext,
            final BindingIdentityCodec identityCodec,
            final BindingDataObjectCodecTreeNode<YangLibrary> codec,
            @SuppressWarnings("deprecation")
            final BindingDataObjectCodecTreeNode<ModulesState> legacyCodec) {
        super(mountId);
        this.resolver = requireNonNull(resolver);
        this.identityCodec = requireNonNull(identityCodec);
        this.yangLibContext = requireNonNull(yangLibContext);
        this.codec = requireNonNull(codec);
        this.legacyCodec = requireNonNull(legacyCodec);
    }

    @Override
    protected MountPointContextFactory createContextFactory(final MountPointDefinition mountPoint) {
        return new MountPointContextFactoryImpl(mountPoint.getIdentifier(), resolver, yangLibContext, identityCodec,
            codec, legacyCodec);
    }

    @Override
    protected Optional<EffectiveModelContext> findSchemaForLibrary(final ContainerName containerName) {
        return switch (containerName) {
            case RFC7895, RFC8525 -> Optional.of(yangLibContext);
        };
    }

    @Override
    protected EffectiveModelContext bindLibrary(final ContainerName containerName, final ContainerNode libData)
            throws YangParserException {
        return switch (containerName) {
            case RFC7895 -> bindLibrary(verifyNotNull(legacyCodec.deserialize(libData)));
            case RFC8525 -> bindLibrary(verifyNotNull(codec.deserialize(libData)));
        };
    }

    private @NonNull EffectiveModelContext bindLibrary(final @NonNull YangLibrary yangLib) throws YangParserException {
        final var datastores = yangLib.nonnullDatastore();
        checkArgument(!datastores.isEmpty(), "No datastore defined");

        final var requiredSources = new ArrayList<SourceReference>();
        final var librarySources = new ArrayList<SourceReference>();
        final var supportedFeatures = new HashSet<QName>();
        final var moduleSet = findModuleSet(yangLib, findSchemaName(datastores, Operational.QNAME));
        for (var modSet : yangLib.nonnullModuleSet().values()) {
            if (moduleSet.remove(modSet.getName())) {
                fillModules(librarySources, requiredSources, supportedFeatures, modSet);
            }
        }
        checkArgument(moduleSet.isEmpty(), "Failed to resolve module sets %s", moduleSet);

        return resolver.resolveSchemaContext(librarySources, requiredSources, supportedFeatures);
    }

    @SuppressWarnings("deprecation")
    private @NonNull EffectiveModelContext bindLibrary(final @NonNull ModulesState modState)
            throws YangParserException {
        final var requiredSources = new ArrayList<SourceReference>();
        final var librarySources = new ArrayList<SourceReference>();
        final var supportedFeatures = new HashSet<QName>();

        for (var module : modState.nonnullModule().values()) {
            final var modRef = sourceRefFor(module, module.getSchema());

            final var namespace = XMLNamespace.of(module.require¤Namespace().getValue());
            for (var feature : module.require¤Feature()) {
                supportedFeatures.add(QName.create(namespace, feature.getValue()).intern());
            }

            // TODO: take deviations into account

            if (ConformanceType.Import == module.getConformanceType()) {
                librarySources.add(modRef);
            } else {
                requiredSources.add(modRef);
            }

            for (var submodule : module.nonnullSubmodule().values()) {
                // Submodules go to library, as they are pulled in as needed
                librarySources.add(sourceRefFor(submodule, submodule.getSchema()));
            }
        }

        return resolver.resolveSchemaContext(librarySources, requiredSources, supportedFeatures);
    }

    private String findSchemaName(final Map<DatastoreKey, Datastore> datastores, final QName qname) {
        final var it = datastores.values().iterator();
        final var ds = it.next();

        // FIXME: This is ugly, but it is the most compatible thing we can do without knowing the exact requested
        //        datastore
        if (it.hasNext() && !qname.equals(identityCodec.fromBinding(ds.getName()))) {
            do {
                final Datastore next = it.next();
                if (qname.equals(identityCodec.fromBinding(ds.getName()))) {
                    return next.getSchema();
                }
            } while (it.hasNext());
        }

        return ds.getSchema();
    }

    @SuppressWarnings("deprecation")
    private static SourceReference sourceRefFor(final CommonLeafs obj, final Uri uri) {
        final var sourceId = new SourceIdentifier(Unqualified.of(obj.getName().getValue()),
            LegacyRevisionUtils.toYangCommon(obj.getRevision()).orElse(null));
        if (uri != null) {
            try {
                return SourceReference.of(sourceId, new URL(uri.getValue()));
            } catch (MalformedURLException e) {
                LOG.debug("Ignoring invalid schema location {}", uri, e);
            }
        }

        return SourceReference.of(sourceId);
    }

    private static HashSet<String> findModuleSet(final YangLibrary yangLib, final String schemaName) {
        final var schema = yangLib.nonnullSchema().get(new SchemaKey(schemaName));
        if (schema == null) {
            throw new IllegalArgumentException("Failed to find moduleSet for " + schemaName);
        }
        return new HashSet<>(schema.getModuleSet());
    }

    private static void fillModules(final List<SourceReference> librarySources,
            final List<SourceReference> requiredSources, final Set<QName> supportedFeatures, final ModuleSet modSet) {
        // TODO: take deviations/features into account

        for (var mod : modSet.nonnullImportOnlyModule().values()) {
            fillSource(librarySources, mod.getName(), RevisionUtils.toYangCommon(mod.getRevision()),
                mod.getLocation());
            mod.nonnullSubmodule().values().forEach(sub -> {
                fillSource(librarySources, sub.getName(), toYangCommon(sub.getRevision()), sub.getLocation());
            });
        }
        for (var mod : modSet.nonnullModule().values()) {
            fillSource(requiredSources, mod.getName(), toYangCommon(mod.getRevision()), mod.getLocation());
            final var namespace = XMLNamespace.of(mod.require¤Namespace().getValue());
            mod.require¤Feature().forEach(
                feature -> supportedFeatures.add(QName.create(namespace, feature.getValue())));
            mod.nonnullSubmodule().values().forEach(sub -> {
                fillSource(librarySources, sub.getName(), toYangCommon(sub.getRevision()), sub.getLocation());
            });
        }
    }

    private static void fillSource(final List<SourceReference> sources, final YangIdentifier sourceName,
            final Optional<Revision> revision, final Set<Uri> uris) {
        final var sourceId = new SourceIdentifier(Unqualified.of(sourceName.getValue()), revision.orElse(null));
        final SourceReference sourceRef;
        if (uris != null && uris.isEmpty()) {
            final var locations = new ArrayList<URL>();
            for (Uri uri : uris) {
                try {
                    locations.add(new URL(uri.getValue()));
                } catch (MalformedURLException e) {
                    LOG.debug("Ignoring invalid schema location {}", uri, e);
                }
            }
            sourceRef = SourceReference.of(sourceId, locations);
        } else {
            sourceRef = SourceReference.of(sourceId);
        }

        sources.add(sourceRef);
    }

    private static Optional<Revision> toYangCommon(final @Nullable RevisionIdentifier revisionIdentifier) {
        return revisionIdentifier == null ? Optional.empty() : Optional.of(Revision.of(revisionIdentifier.getValue()));
    }
}
