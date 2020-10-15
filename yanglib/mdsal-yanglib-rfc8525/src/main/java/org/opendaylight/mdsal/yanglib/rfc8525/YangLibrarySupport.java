/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc8525;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingIdentityCodec;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.mdsal.binding.runtime.api.DefaultBindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.mdsal.binding.runtime.spi.ModuleInfoSnapshotBuilder;
import org.opendaylight.mdsal.yanglib.api.SchemaContextResolver;
import org.opendaylight.mdsal.yanglib.api.YangLibSupport;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.ModulesState;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.ModulesStateBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.YangLibrary;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.YangLibraryBuilder;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContextFactory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.data.api.DatastoreIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;

@Beta
@NonNullByDefault
@Singleton
public final class YangLibrarySupport implements YangLibSupport {
    private static final Revision REVISION = YangLibrary.QNAME.getRevision().orElseThrow();

    private final BindingDataObjectCodecTreeNode<YangLibrary> codec;
    @SuppressWarnings("deprecation")
    private final BindingDataObjectCodecTreeNode<ModulesState> legacyCodec;
    private final BindingIdentityCodec identityCodec;
    private final EffectiveModelContext context;

    @Inject
    public YangLibrarySupport(final YangParserFactory parserFactory, final BindingRuntimeGenerator generator,
            final BindingCodecTreeFactory codecFactory) throws YangParserException {
        final ModuleInfoSnapshot snapshot = new ModuleInfoSnapshotBuilder(parserFactory)
                .add(YangLibrary.class)
                .build();
        context = snapshot.getEffectiveModelContext();

        final BindingCodecTree codecTree = codecFactory.create(new DefaultBindingRuntimeContext(
            generator.generateTypeMapping(context), snapshot));

        this.identityCodec = codecTree.getIdentityCodec();
        this.codec = verifyNotNull(codecTree.getSubtreeCodec(InstanceIdentifier.create(YangLibrary.class)));
        this.legacyCodec = verifyNotNull(codecTree.getSubtreeCodec(InstanceIdentifier.create(ModulesState.class)));
    }

    @Override
    public MountPointContextFactory createMountPointContextFactory(final MountPointIdentifier mountId,
            final SchemaContextResolver resolver) {
        return new MountPointContextFactoryImpl(mountId, resolver, context, identityCodec, codec, legacyCodec);
    }

    @Override
    public Revision implementedRevision() {
        return REVISION;
    }

    @Override
    public List<? extends ContainerNode> formatSchema(final EffectiveModelContext context,
            final Set<DatastoreIdentifier> datastores) {
        return List.of((ContainerNode) formatYangLibrary(context, datastores),
            (ContainerNode) formatModulesState(context));
    }

    private NormalizedNode<?, ?> formatModulesState(final EffectiveModelContext context) {
        // Two-step process: we first build the content and then use hashCode() to generate module-set-id
        final ModulesState noId = new ModulesStateBuilder()
            .setModuleSetId("")
            // TODO Auto-generated method stub
            .build();


        return legacyCodec.serialize(new ModulesStateBuilder(noId)
            .setModuleSetId(String.valueOf(noId.hashCode()))
            .build());
    }

    private NormalizedNode<?, ?> formatYangLibrary(final EffectiveModelContext context,
            final Set<DatastoreIdentifier> datastores) {
        // Two-step process: we first build the content and then use hashCode() to generate content-id
        final YangLibrary noId = new YangLibraryBuilder()
            .setContentId("")
            // TODO Auto-generated method stub
            .build();

        return codec.serialize(new YangLibraryBuilder(noId).setContentId(String.valueOf(noId.hashCode())).build());
    }
}
