/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;

public class DatastoreTest {

    @Test
    public void testDatastore() {
        assertThat(Datastore.toType(Datastore.CONFIGURATION)).isEqualTo(LogicalDatastoreType.CONFIGURATION);
        assertThat(Datastore.toType(Datastore.OPERATIONAL)).isEqualTo(LogicalDatastoreType.OPERATIONAL);
        try {
            Datastore.toType(null);
            Assert.fail("Expected Datastore.toType(null) to throw NullPointerException");
        } catch (NullPointerException e) {
            // OK, this is what we're expecting
        }

        assertThat(Datastore.toClass(LogicalDatastoreType.CONFIGURATION)).isEqualTo(Datastore.CONFIGURATION);
        assertThat(Datastore.toClass(LogicalDatastoreType.OPERATIONAL)).isEqualTo(Datastore.OPERATIONAL);
        try {
            Datastore.toClass(null);
            Assert.fail("Expected Datastore.toClass(null) to throw NullPointerException");
        } catch (NullPointerException e) {
            // OK, this is what we're expecting
        }
    }

}
