/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit Test for {@link FixedModuleInfoSchemaContextProvider}.
 *
 * @author Michael Vorburger.ch
 */
public class FixedModuleInfoSchemaContextProviderTest {

    FixedModuleInfoSchemaContextProvider schemaContextProvider = new FixedModuleInfoSchemaContextProvider();

    @Test
    public void testEmptyModuleInfos() {
        assertTrue("moduleInfos.isEmpty", schemaContextProvider.getModuleInfos().isEmpty());
    }

    @Test(expected = RuntimeException.class)
    public void testEmptySchemaContext() {
        schemaContextProvider.getSchemaContext();
    }
}
