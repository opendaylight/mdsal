/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc7895;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.yanglib.api.SchemaContextResolver;
import org.opendaylight.mdsal.yanglib.api.SourceReference;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.ModulesState;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.RevisionUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.module.list.CommonLeafs;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.module.list.Module;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.module.list.Module.ConformanceType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.module.list.module.Submodule;
import org.opendaylight.yangtools.rcf8528.data.util.AbstractMountPointContextFactory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContextFactory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier;
import org.opendaylight.yangtools.rfc8528.data.api.YangLibraryConstants.ContainerName;
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

    private final BindingDataObjectCodecTreeNode<ModulesState> codec;
    private final EffectiveModelContext yangLibContext;
    private final SchemaContextResolver resolver;

    MountPointContextFactoryImpl(final MountPointIdentifier mountId, final SchemaContextResolver resolver,
            final EffectiveModelContext yangLibContext,
            final BindingDataObjectCodecTreeNode<ModulesState> moduleStateCodec) {
        super(mountId);
        this.resolver = requireNonNull(resolver);
        this.yangLibContext = requireNonNull(yangLibContext);
        this.codec = requireNonNull(moduleStateCodec);
    }

    @Override
    protected MountPointContextFactory createContextFactory(final MountPointDefinition mountPoint) {
        return new MountPointContextFactoryImpl(mountPoint.getIdentifier(), resolver, yangLibContext, codec);
    }

    @Override
    protected Optional<SchemaContext> findSchemaForLibrary(final ContainerName containerName) {
        switch (containerName) {
            case RFC7895:
                return Optional.of(yangLibContext);
            default:
                LOG.debug("Unhandled YANG library container {}", containerName);
                return Optional.empty();
        }
    }

    @Override
    protected SchemaContext bindLibrary(final ContainerName containerName, final ContainerNode libData)
            throws YangParserException {
        checkArgument(containerName == ContainerName.RFC7895, "Unsupported container type %s", containerName);
        checkArgument(ModulesState.QNAME.equals(libData.getNodeType()), "Unexpected container %s", libData);

        // DOM-to-Binding magic
        return bindLibrary(verifyNotNull(codec.deserialize(libData)));
    }

    private @NonNull SchemaContext bindLibrary(final @NonNull ModulesState modState) throws YangParserException {
        final List<SourceReference> requiredSources = new ArrayList<>();
        final List<SourceReference> librarySources = new ArrayList<>();

        for (Module module : modState.nonnullModule().values()) {
            final SourceReference modRef = sourceRefFor(module, module.getSchema());

            // TODO: take deviations/features into account

            if (ConformanceType.Import == module.getConformanceType()) {
                librarySources.add(modRef);
            } else {
                requiredSources.add(modRef);
            }

            for (Submodule submodule : module.nonnullSubmodule().values()) {
                // Submodules go to library, as they are pulled in as needed
                librarySources.add(sourceRefFor(submodule, submodule.getSchema()));
            }
        }

        return resolver.resolveSchemaContext(librarySources, requiredSources);
    }

    private static SourceReference sourceRefFor(final CommonLeafs obj, final Uri uri) {
        final SourceIdentifier sourceId = RevisionSourceIdentifier.create(obj.getName().getValue(),
            RevisionUtils.toYangCommon(obj.getRevision()));
        if (uri != null) {
            try {
                return SourceReference.of(sourceId, new URL(uri.getValue()));
            } catch (MalformedURLException e) {
                LOG.debug("Ignoring invalid schema location {}", uri, e);
            }
        }

        return SourceReference.of(sourceId);
    }
}
