/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc8525;

import com.google.common.annotations.Beta;
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
import org.opendaylight.mdsal.yanglib.api.YangLibraryContentBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.ModulesState;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.YangLibrary;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.MountPointLabel;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContextFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Beta
@NonNullByDefault
@Singleton
@Component
public final class YangLibrarySupport implements YangLibSupport {
    private static final Revision REVISION = YangLibrary.QNAME.getRevision().orElseThrow();

    private final BindingDataObjectCodecTreeNode<YangLibrary> codec;
    @SuppressWarnings("deprecation")
    private final BindingDataObjectCodecTreeNode<ModulesState> legacyCodec;
    private final BindingIdentityCodec identityCodec;
    private final EffectiveModelContext modelContext;
    private final BindingCodecTree codecTree;

    @Inject
    @Activate
    public YangLibrarySupport(@Reference final YangParserFactory parserFactory,
            @Reference final BindingRuntimeGenerator generator, @Reference final BindingCodecTreeFactory codecFactory)
                throws YangParserException {
        final ModuleInfoSnapshot snapshot = new ModuleInfoSnapshotBuilder(parserFactory)
                .add(YangLibrary.class)
                .build();
        modelContext = snapshot.modelContext();

        codecTree = codecFactory.create(new DefaultBindingRuntimeContext(
            generator.generateTypeMapping(modelContext), snapshot));

        identityCodec = codecTree.getIdentityCodec();
        codec = codecTree.getDataObjectCodec(InstanceIdentifier.create(YangLibrary.class));
        legacyCodec = codecTree.getDataObjectCodec(InstanceIdentifier.create(ModulesState.class));
    }

    @Override
    public MountPointContextFactory createMountPointContextFactory(final MountPointLabel label,
            final SchemaContextResolver resolver) {
        return new MountPointContextFactoryImpl(label, resolver, modelContext, identityCodec, codec, legacyCodec);
    }

    @Override
    public Revision implementedRevision() {
        return REVISION;
    }

    @Override
    public YangLibraryContentBuilder newContentBuilder() {
        return new YangLibraryContentBuilderImpl(codecTree);
    }
}
