/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class DataObjectModificationTest {

    @Test
    public void basicTest() throws Exception {
        assertNotNull(DataObjectModification.ModificationType.values());
    }
}