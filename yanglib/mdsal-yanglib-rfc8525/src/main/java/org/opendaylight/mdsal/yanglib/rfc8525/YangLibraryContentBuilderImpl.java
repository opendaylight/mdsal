/*
 * Copyright (c) 2020 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc8525;

import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.yanglib.api.YangLibraryContentBuilder;
import org.opendaylight.mdsal.yanglib.api.YangLibraryContentBuilderWithLegacy;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.RevisionIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.YangLibrary;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.YangLibraryBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.set.parameters.Module;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.set.parameters.ModuleBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.set.parameters.module.SubmoduleBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.yang.library.parameters.ModuleSet;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.yang.library.parameters.ModuleSetBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.yang.library.parameters.ModuleSetKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.YangIdentifier;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTree;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.data.api.DatastoreIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

final class YangLibraryContentBuilderImpl implements YangLibraryContentBuilder {
    private static final String MODULE_SET_NAME = "ODL_modules";

    private final Map<DatastoreIdentifier, EffectiveModelContext> datastores = new HashMap<>();
    private final BindingDataObjectCodecTreeNode<YangLibrary> codec;
    private final BindingCodecTree codecTree;

    private EffectiveModelContext modelContext;

    YangLibraryContentBuilderImpl(final BindingCodecTree codecTree) {
        this.codecTree = Objects.requireNonNull(codecTree);
        codec = codecTree.getDataObjectCodec(InstanceIdentifier.create(YangLibrary.class));
    }

    @Override
    public YangLibraryContentBuilder defaultContext(final EffectiveModelContext context) {
        modelContext = context;
        return this;
    }

    EffectiveModelContext getModelContext() {
        return modelContext;
    }

    @Override
    public YangLibraryContentBuilderWithLegacy includeLegacy() {
        return new LegacyContentBuilder(this, codecTree);
    }

    @Override
    public YangLibraryContentBuilder addDatastore(final DatastoreIdentifier identifier,
                                                  final EffectiveModelContext context) {
        datastores.put(identifier, context);
        return this;
    }

    @Override
    public ContainerNode formatYangLibraryContent() {
        checkState(modelContext != null, "EffectiveModelContext is required to format YangLibrary content");
        return formatYangLibrary();
    }

    @NonNull ContainerNode formatYangLibrary() {
        // Two-step process: we first build the content and then use hashCode() to generate module-set-id
        final YangLibraryBuilder builder = new YangLibraryBuilder().setContentId("");
        final ModuleSetBuilder moduleSetBuilder = new ModuleSetBuilder()
            .setModule(modelContext.getModules().stream()
                .map(YangLibraryContentBuilderImpl::buildModule)
                .collect(BindingMap.toMap()))
            .setName(MODULE_SET_NAME);
        final ModuleSet moduleSet = moduleSetBuilder.build();

        builder.setModuleSet(Map.of(new ModuleSetKey(moduleSet.getName()), moduleSet));
        return (ContainerNode) codec.serialize(builder.setContentId(String.valueOf(builder.build().hashCode()))
            .build());
    }

    private static Module buildModule(final org.opendaylight.yangtools.yang.model.api.Module module) {
        return new ModuleBuilder()
            .setName(new YangIdentifier(module.getName()))
            .setNamespace(new Uri(module.getQNameModule().namespace().toString()))
            .setRevision(convertRevision(module.getRevision()))
            .setSubmodule(module.getSubmodules().stream()
                .map(submodule -> new SubmoduleBuilder()
                    .setName(new YangIdentifier(submodule.getName()))
                    .setRevision(convertRevision(submodule.getRevision()))
                    .build())
                .collect(BindingMap.toMap()))
            .setFeature(module.getFeatures().stream()
                .map(feat -> new YangIdentifier(feat.getQName().getLocalName()))
                .collect(Collectors.toUnmodifiableSet()))
            .build();
    }

    private static RevisionIdentifier convertRevision(final Optional<Revision> revision) {
        return revision.map(rev -> new RevisionIdentifier(rev.toString())).orElse(null);
    }
}
