/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc7895;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.mdsal.binding.runtime.api.DefaultBindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.mdsal.binding.runtime.spi.ModuleInfoSnapshotBuilder;
import org.opendaylight.mdsal.yanglib.api.SchemaContextResolver;
import org.opendaylight.mdsal.yanglib.api.YangLibSupport;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.ModulesState;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContextFactory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;

@Beta
@NonNullByDefault
@Singleton
public final class YangModuleLibrarySupport implements YangLibSupport {
    private final BindingDataObjectCodecTreeNode<ModulesState> codec;
    private final EffectiveModelContext context;

    @Inject
    public YangModuleLibrarySupport(final YangParserFactory parserFactory, final BindingRuntimeGenerator generator,
            final BindingCodecTreeFactory codecFactory) throws YangParserException, IOException {
        final ModuleInfoSnapshotBuilder builder = new ModuleInfoSnapshotBuilder("yanglib", parserFactory);
        builder.registerModuleInfos(List.of($YangModuleInfoImpl.getInstance()));
        final ModuleInfoSnapshot snapshot = builder.build();
        context = snapshot.getEffectiveModelContext();

        final BindingCodecTree codecTree = codecFactory.create(new DefaultBindingRuntimeContext(
            generator.generateTypeMapping(context), snapshot));

        this.codec = verifyNotNull(codecTree.getSubtreeCodec(InstanceIdentifier.create(ModulesState.class)));
    }

    @Override
    public MountPointContextFactory createMountPointContextFactory(final MountPointIdentifier mountId,
            final SchemaContextResolver resolver) {
        return new MountPointContextFactoryImpl(mountId, resolver, context, codec);
    }
}
