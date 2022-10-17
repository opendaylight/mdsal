/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Operational;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Running;

public class DatastoresTest {
    @Test
    public void testDatastoreToType() {
        assertEquals(LogicalDatastoreType.CONFIGURATION, Datastores.typeOf(Running.VALUE));
        assertEquals(LogicalDatastoreType.OPERATIONAL, Datastores.typeOf(Operational.VALUE));
    }

    @Test
    public void testDatastoreToClass() {
        assertEquals(Running.VALUE, Datastores.ofType(LogicalDatastoreType.CONFIGURATION));
        assertEquals(Operational.VALUE, Datastores.ofType(LogicalDatastoreType.OPERATIONAL));
        assertThrows(NullPointerException.class, () -> Datastores.ofType(null));
    }
}
