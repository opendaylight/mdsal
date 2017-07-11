/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.dom.adapter.test;

import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.api.WriteTransaction;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.mdsal.gen.javav2.urn.test.rev170627.data.MyCont;
import org.opendaylight.mdsal.gen.javav2.urn.test.rev170627.dto.MyContBuilder;

public class BasicLeafValueTest extends AbstractDataBrokerTest {

    private static final InstanceIdentifier<MyCont> MY_CONT_NODE_PATH
            = InstanceIdentifier.create(MyCont.class);

    @Ignore
    @Test
    public void testMyContLeafNode() throws TransactionCommitFailedException {
        final WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        final MyContBuilder hello = new MyContBuilder().setMyLeaf("hello");
        writeTx.put(LogicalDatastoreType.OPERATIONAL, MY_CONT_NODE_PATH, hello.build());
        writeTx.submit().checkedGet();
    }
}
