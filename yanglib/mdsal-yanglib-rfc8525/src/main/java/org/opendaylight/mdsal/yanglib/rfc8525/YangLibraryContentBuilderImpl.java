/*
 * Copyright (c) 2020 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc8525;

import static com.google.common.base.Verify.verifyNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.yanglib.api.LegacyYangLibraryContentBuilder;
import org.opendaylight.mdsal.yanglib.api.YangLibraryContentBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.RevisionIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.YangLibrary;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.YangLibraryBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.set.parameters.Module;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.set.parameters.ModuleBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.set.parameters.module.Submodule;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.set.parameters.module.SubmoduleBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.yang.library.parameters.ModuleSet;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.yang.library.parameters.ModuleSetBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.yang.library.parameters.ModuleSetKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.YangIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.data.api.DatastoreIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

class YangLibraryContentBuilderImpl implements YangLibraryContentBuilder {

    private static final RevisionIdentifier EMPTY_REV = new RevisionIdentifier("1970-01-01");
    private static final String MODULE_SET_NAME = "ODL_modules";

    private EffectiveModelContext modelContext = null;
    private final Map<DatastoreIdentifier, EffectiveModelContext> datastores = new HashMap<>();

    private final BindingCodecTree codecTree;
    private final BindingDataObjectCodecTreeNode<YangLibrary> codec;


    YangLibraryContentBuilderImpl(final BindingCodecTree codecTree) {
        this.codecTree = Objects.requireNonNull(codecTree);
        this.codec = verifyNotNull(codecTree.getSubtreeCodec(InstanceIdentifier.create(YangLibrary.class)));
    }

    @Override
    public YangLibraryContentBuilder setContext(final EffectiveModelContext context) {
        modelContext = context;
        return this;
    }

    EffectiveModelContext getModelContext() {
        return modelContext;
    }

    @Override
    public LegacyYangLibraryContentBuilder includeLegacy() {
        return new LegacyContentBuilder(this, codecTree);
    }

    @Override
    public YangLibraryContentBuilder addDatastore(final DatastoreIdentifier identifier,
                                                  final EffectiveModelContext context) {
        datastores.put(identifier, context);
        return this;
    }

    @Override
    public List<? extends ContainerNode> formatYangLibraryContent() {
        Objects.requireNonNull(modelContext, "EffectiveModelContext is required to formay YangLibrary content");
        return Collections.singletonList((ContainerNode) formatYangLibrary());
    }

    NormalizedNode<?, ?> formatYangLibrary() {
        // Two-step process: we first build the content and then use hashCode() to generate module-set-id
        final YangLibraryBuilder builder = new YangLibraryBuilder().setContentId("");
        final ModuleSetBuilder moduleSetBuilder = new ModuleSetBuilder()
                .setModule(modelContext.getModules().stream()
                        .map(this::buildModule)
                        .collect(Collectors.toUnmodifiableMap(Module::key, Function.identity())))
                .setName(MODULE_SET_NAME);
        final ModuleSet moduleSet = moduleSetBuilder.build();

        builder.setModuleSet(Collections.singletonMap(new ModuleSetKey(moduleSet.getName()), moduleSet));
        return codec.serialize(builder.setContentId(String.valueOf(builder.build().hashCode())).build());
    }

    private Module buildModule(org.opendaylight.yangtools.yang.model.api.Module module) {
        return new ModuleBuilder()
                .setName(new YangIdentifier(module.getName()))
                .setNamespace(new Uri(module.getQNameModule().getNamespace().toString()))
                .setRevision(convertRevision(module.getRevision()))
                .setSubmodule(module.getSubmodules().stream()
                        .map(submodule -> new SubmoduleBuilder()
                                .setName(new YangIdentifier(submodule.getName()))
                                .setRevision(convertRevision(submodule.getRevision()))
                                .build())
                        .collect(Collectors.toUnmodifiableMap(Submodule::key, Function.identity())))
                .setFeature(module.getFeatures().stream()
                        .map(feat -> new YangIdentifier(feat.getQName().getLocalName()))
                        .collect(Collectors.toUnmodifiableList()))
                .build();
    }

    private static RevisionIdentifier convertRevision(final Optional<Revision> revision) {
        return revision.map(rev -> new RevisionIdentifier(rev.toString())).orElse(EMPTY_REV);
    }
}
