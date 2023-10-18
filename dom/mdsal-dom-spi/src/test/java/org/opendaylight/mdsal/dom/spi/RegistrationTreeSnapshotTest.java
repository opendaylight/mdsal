/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import java.util.concurrent.locks.Lock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

@ExtendWith(MockitoExtension.class)
class RegistrationTreeSnapshotTest {
    @Mock
    private Lock lock;

    @Test
    void basicTest() throws Exception {
        final var pathArgument = new NodeIdentifier(QName.create("", "pathArgument"));
        final var registrationTreeNode = new RegistrationTreeNode<>(null, pathArgument);
        try (var registrationTreeSnapshot = new RegistrationTreeSnapshot<>(lock, registrationTreeNode)) {
            assertNotNull(registrationTreeSnapshot.getRootNode());
            assertEquals(registrationTreeNode, registrationTreeSnapshot.getRootNode());
            doNothing().when(lock).unlock();
        }
        verify(lock).unlock();
    }
}