/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.locks.Lock;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

public class RegistrationTreeSnapshotTest {
    @Test
    public void basicTest() throws Exception {
        final Lock lock = mock(Lock.class);
        final NodeIdentifier pathArgument = new NodeIdentifier(QName.create("", "pathArgument"));
        final RegistrationTreeNode<?> registrationTreeNode = new RegistrationTreeNode<>(null, pathArgument);
        final RegistrationTreeSnapshot<?> registrationTreeSnapshot =
                new RegistrationTreeSnapshot<>(lock, registrationTreeNode);

        assertNotNull(registrationTreeSnapshot.getRootNode());
        assertEquals(registrationTreeNode, registrationTreeSnapshot.getRootNode());

        doNothing().when(lock).unlock();
        registrationTreeSnapshot.close();
        verify(lock).unlock();
    }
}