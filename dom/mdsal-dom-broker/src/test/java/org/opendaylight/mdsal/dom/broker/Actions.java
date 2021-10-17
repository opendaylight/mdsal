/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@NonNullByDefault
final class Actions {
    static final EffectiveModelContext CONTEXT = YangParserTestUtils.parseYangResource("/actions.yang");
    static final QName FOO = QName.create("actions", "foo");
    static final QName BAR = QName.create(FOO, "bar");
    static final QName BAZ = QName.create(FOO, "baz");
    static final QName INPUT = QName.create(FOO, "input");
    static final QName OUTPUT = QName.create(FOO, "output");
    static final Absolute BAZ_TYPE = Absolute.of(FOO, BAZ);

    private Actions() {
        // Hidden on purpose
    }
}
