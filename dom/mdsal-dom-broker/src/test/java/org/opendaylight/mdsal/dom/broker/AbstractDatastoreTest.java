/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

public abstract class AbstractDatastoreTest {
    static EffectiveModelContext SCHEMA_CONTEXT;

    @BeforeAll
    public static void beforeClass() {
        SCHEMA_CONTEXT = TestModel.createTestContext();
    }

    @AfterAll
    public static void afterClass() {
        SCHEMA_CONTEXT = null;
    }
}
