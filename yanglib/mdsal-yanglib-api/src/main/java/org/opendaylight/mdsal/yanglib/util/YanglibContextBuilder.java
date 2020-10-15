/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

public final class YanglibContextBuilder {

    private YanglibContextBuilder() {

    }

    public static EffectiveModelContext buildContext(YangParserFactory parserFactory, Class<?>... clazz)
            throws Exception {
        final YangParser parser = parserFactory.createParser();

        for (Class<?> aclazz : clazz) {
            final YangModuleInfo moduleInfo = BindingReflections.getModuleInfo(aclazz);

            Set<YangModuleInfo> infos = new HashSet<>();
            flatDependencies(infos, moduleInfo);

            for (YangModuleInfo info : infos) {
                final YangTextSchemaSource source =
                        YangTextSchemaSource.delegateForByteSource(sourceIdentifierFrom(info),
                                info.getYangTextByteSource());
                try {
                    parser.addSource(source);
                } catch (YangSyntaxErrorException | IOException e) {
                    throw new YangParserException("Failed to add source for " + moduleInfo, e);
                }
            }
        }

        return parser.buildEffectiveModel();
    }

    static void flatDependencies(final Set<YangModuleInfo> set, final YangModuleInfo moduleInfo) {
        if (set.add(moduleInfo)) {
            for (YangModuleInfo dep : moduleInfo.getImportedModules()) {
                flatDependencies(set, dep);
            }
        }
    }

    private static SourceIdentifier sourceIdentifierFrom(final YangModuleInfo moduleInfo) {
        final QName name = moduleInfo.getName();
        return RevisionSourceIdentifier.create(name.getLocalName(), name.getRevision());
    }
}
