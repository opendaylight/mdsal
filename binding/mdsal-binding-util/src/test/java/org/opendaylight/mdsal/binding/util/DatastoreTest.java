/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;

public class DatastoreTest {
    @Test
    public void testDatastoreToType() {
        assertEquals(LogicalDatastoreType.CONFIGURATION, Datastore.toType(Datastore.CONFIGURATION));
        assertEquals(LogicalDatastoreType.OPERATIONAL, Datastore.toType(Datastore.OPERATIONAL));
        assertThrows(NullPointerException.class, () -> Datastore.toType(null));
    }

    @Test
    public void testDatastoreToClass() {
        assertEquals(Datastore.CONFIGURATION, Datastore.toClass(LogicalDatastoreType.CONFIGURATION));
        assertEquals(Datastore.OPERATIONAL, Datastore.toClass(LogicalDatastoreType.OPERATIONAL));
        assertThrows(NullPointerException.class, () -> Datastore.toClass(null));
    }
}
