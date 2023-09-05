/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yang.svc.v1.urn.opendaylight.yang.extension.yang.ext.rev130709.YangModuleInfoImplImpl;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.DelegatedYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@NonNullByDefault
final class Rpcs {
    static final EffectiveModelContext CONTEXT = YangParserTestUtils.parseYangSources(YangParserConfiguration.DEFAULT,
        null,
        new DelegatedYangTextSource(new SourceIdentifier("yang-ext.yang"),
            YangModuleInfoImplImpl.getInstance().getYangTextCharSource()),
        new URLYangTextSource(Rpcs.class.getResource("/rpcs.yang")));

    static final QName FOO = QName.create("rpcs", "foo");
    static final QName BAR = QName.create(FOO, "bar");
    static final QName BAZ = QName.create(FOO, "baz");
    static final QName NAME = QName.create(FOO, "name");
    static final QName INPUT = QName.create(FOO, "input");
    static final QName CTX = QName.create(FOO, "ctx");

    private Rpcs() {
        // Hidden on purpose
    }
}
