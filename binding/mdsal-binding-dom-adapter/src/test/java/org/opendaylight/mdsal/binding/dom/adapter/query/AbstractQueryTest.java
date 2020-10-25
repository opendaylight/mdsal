/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opendaylight.mdsal.binding.api.query.QueryFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.impl.DefaultBindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;

public class AbstractQueryTest {
    static BindingCodecTree CODEC;

    final QueryFactory factory = new DefaultQueryFactory(CODEC);

    @BeforeClass
    public static final void beforeClass() {
        CODEC = new DefaultBindingCodecTreeFactory().create(BindingRuntimeHelpers.createRuntimeContext());
    }

    @AfterClass
    public static final void afterClass() {
        CODEC = null;
    }
}
