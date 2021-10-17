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
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@NonNullByDefault
final class Rpcs {
    static final EffectiveModelContext CONTEXT = YangParserTestUtils.parseYangResource("/rpcs.yang");
    static final QName FOO = QName.create("rpcs", "foo");
    static final QName BAR = QName.create(FOO, "bar");

    private Rpcs() {
        // Hidden on purpose
    }
}
