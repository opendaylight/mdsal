/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import org.junit.Test;
import org.opendaylight.mdsal.binding.generator.impl.reactor.GeneratorReactor;
import org.opendaylight.mdsal.binding.generator.impl.reactor.TypeBuilderFactory;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug6135Test {
    @Test
    public void bug6135Test() {
        new GeneratorReactor(YangParserTestUtils.parseYangResource("/bug-6135/foo.yang"))
            .execute(TypeBuilderFactory.codegen());
    }
}