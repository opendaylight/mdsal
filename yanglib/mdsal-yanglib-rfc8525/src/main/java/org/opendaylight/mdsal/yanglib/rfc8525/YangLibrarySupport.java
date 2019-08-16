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
import com.google.common.collect.Collections2;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Reference;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingIdentityCodec;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.mdsal.yanglib.api.SchemaContextResolver;
import org.opendaylight.mdsal.yanglib.api.YangLibSupport;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.ModulesState;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev190104.YangLibrary;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContextFactory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@Beta
@NonNullByDefault
@Singleton
public final class YangLibrarySupport implements YangLibSupport {
    private final BindingDataObjectCodecTreeNode<YangLibrary> codec;
    @SuppressWarnings("deprecation")
    private final BindingDataObjectCodecTreeNode<ModulesState> legacyCodec;
    private final BindingIdentityCodec identityCodec;
    private final EffectiveModelContext context;

    @Inject
    public YangLibrarySupport(final @Reference YangParserFactory parserFactory)
            throws YangParserException, IOException {
        final YangModuleInfo yangLibModule = $YangModuleInfoImpl.getInstance();

        // FIXME: DEFAULT_MODE should not be necessary, but it seems blueprint is still b0rked
        context = parserFactory.createParser(StatementParserMode.DEFAULT_MODE)
                .addLibSources(Collections2.transform(yangLibModule.getImportedModules(),
                    YangLibrarySupport::createSource))
                .addSource(createSource(yangLibModule))
                .buildEffectiveModel();
        final BindingCodecTree codecTree = new BindingNormalizedNodeCodecRegistry(BindingRuntimeContext.create(
            SimpleStrategy.INSTANCE, context)).getCodecContext();

        this.identityCodec = codecTree.getIdentityCodec();
        this.codec = verifyNotNull(codecTree.getSubtreeCodec(InstanceIdentifier.create(YangLibrary.class)));
        this.legacyCodec = verifyNotNull(codecTree.getSubtreeCodec(InstanceIdentifier.create(ModulesState.class)));
    }

    @Override
    public MountPointContextFactory createMountPointContextFactory(final MountPointIdentifier mountId,
            final SchemaContextResolver resolver) {
        return new MountPointContextFactoryImpl(mountId, resolver, context, identityCodec, codec, legacyCodec);
    }

    private static YangTextSchemaSource createSource(final YangModuleInfo info) {
        final QName name = info.getName();
        return YangTextSchemaSource.delegateForByteSource(
            RevisionSourceIdentifier.create(name.getLocalName(), name.getRevision()), info.getYangTextByteSource());
    }
}
