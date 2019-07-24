/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc7895;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.Collections2;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.mdsal.yanglib.api.YangLibSupport;
import org.opendaylight.mdsal.yanglib.api.YangLibSupportFactory;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621.ModulesState;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@MetaInfServices
@NonNullByDefault
public final class YangModuleLibrarySupportFactory implements YangLibSupportFactory {
    @Override
    public YangLibSupport createYangLibSupport(final YangParserFactory parserFactory)
            throws YangSyntaxErrorException, YangParserException, IOException {
        // TODO Auto-generated method stub
        final YangModuleInfo yangLibModule = $YangModuleInfoImpl.getInstance();
        final EffectiveModelContext context = parserFactory.createParser()
                .addLibSources(Collections2.transform(yangLibModule.getImportedModules(),
                    YangModuleLibrarySupportFactory::createSource))
                .addSource(createSource(yangLibModule))
                .buildEffectiveModel();
        final BindingCodecTree codecTree = new BindingNormalizedNodeCodecRegistry(BindingRuntimeContext.create(
            SimpleStrategy.INSTANCE, context)).getCodecContext();
        return new YangModuleLibrarySupport(context,
            verifyNotNull(codecTree.getSubtreeCodec(InstanceIdentifier.create(ModulesState.class))));
    }

    private static YangTextSchemaSource createSource(final YangModuleInfo info) {
        final QName name = info.getName();
        return YangTextSchemaSource.delegateForByteSource(
            RevisionSourceIdentifier.create(name.getLocalName(), name.getRevision()), info.getYangTextByteSource());
    }
}
