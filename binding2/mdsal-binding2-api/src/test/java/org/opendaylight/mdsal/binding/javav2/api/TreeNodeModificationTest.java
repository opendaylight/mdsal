/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TreeNodeModificationTest {

    @Test
    public void notificationMethodEnumTest() throws Exception {
        assertNotNull(TreeNodeModification.ModificationType.values());
    }
}
