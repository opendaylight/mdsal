/*
 * Copyright (c) 2020 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc8525;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.yanglib.api.LegacyYangLibraryContentBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.ModulesState;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.ModulesStateBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.RevisionIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.CommonLeafs;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.Module;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.ModuleBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.module.Submodule;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.module.list.module.SubmoduleBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.YangIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

class LegacyContentBuilder implements LegacyYangLibraryContentBuilder {

    private static final CommonLeafs.Revision EMPTY_REV = new CommonLeafs.Revision("");

    private YangLibraryContentBuilderImpl delegate;
    private final BindingDataObjectCodecTreeNode<ModulesState> legacyCodec;

    LegacyContentBuilder(final YangLibraryContentBuilderImpl delegate, final BindingCodecTree codecTree) {
        this.delegate = Objects.requireNonNull(delegate);
        this.legacyCodec = verifyNotNull(codecTree.getSubtreeCodec(InstanceIdentifier.create(ModulesState.class)));
    }

    @Override
    public LegacyContentBuilder setContext(final EffectiveModelContext context) {
        delegate.setContext(context);
        return this;
    }

    @Override
    public List<? extends ContainerNode> formatYangLibraryContent() {
        return List.of((ContainerNode) delegate.formatYangLibrary(),
                (ContainerNode) formatModulesState(Objects.requireNonNull(delegate.getModelContext())));
    }

    @VisibleForTesting
    NormalizedNode<?, ?> formatModulesState(final EffectiveModelContext context) {
        // Two-step process: we first build the content and then use hashCode() to generate module-set-id
        final ModulesStateBuilder builder = new ModulesStateBuilder().setModuleSetId("")
                .setModule(context.getModules().stream()
                        .map(module -> new ModuleBuilder()
                                .setName(new YangIdentifier(module.getName()))
                                .setNamespace(new Uri(module.getNamespace().toString()))
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
                                .setConformanceType(Module.ConformanceType.Implement)
                                .build())
                        .collect(Collectors.toUnmodifiableMap(Module::key, Function.identity())));

        return legacyCodec.serialize(builder.setModuleSetId(String.valueOf(builder.build().hashCode())).build());
    }

    private static CommonLeafs.Revision convertRevision(final Optional<Revision> revision) {
        return revision.map(rev -> new CommonLeafs.Revision(new RevisionIdentifier(rev.toString()))).orElse(EMPTY_REV);
    }
}
