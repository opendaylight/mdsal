/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

class AbstractRegistrationTreeTest extends AbstractRegistrationTree<Object> {
    @Test
    void basicTest() throws Exception {
        final var pathArgument = new NodeIdentifier(QName.create("", "pathArgument"));
        final var registrationTreeNodeParent = new RegistrationTreeNode<>(null, pathArgument);
        final var registrationTreeNode = new RegistrationTreeNode<>(registrationTreeNodeParent, pathArgument);

        final var registration = new Object();
        takeLock();
        addRegistration(registrationTreeNode, registration);
        assertTrue(registrationTreeNode.getRegistrations().contains(registration));
        releaseLock();

        removeRegistration(registrationTreeNode, registration);
        assertFalse(registrationTreeNode.getRegistrations().contains(registration));

        assertNotNull(findNodeFor(List.of(pathArgument)));
        assertNotNull(takeSnapshot());
    }

    @Test
    void unlockTest() {
        assertThrows(IllegalMonitorStateException.class, this::releaseLock);
    }
}
