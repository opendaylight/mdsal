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
import org.opendaylight.mdsal.dom.spi.AbstractRegistrationTree.Node;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

class AbstractRegistrationTreeTest {
    private final AbstractRegistrationTree<Object> instance = new AbstractRegistrationTree<>() {
        // Nothing else
    };

    @Test
    void basicTest() {
        final var pathArgument = new NodeIdentifier(QName.create("", "pathArgument"));
        final var registrationTreeNodeParent = new Node<>(null, pathArgument);
        final var registrationTreeNode = new Node<>(registrationTreeNodeParent, pathArgument);

        final var registration = new Object();
        instance.takeLock();
        instance.addRegistration(registrationTreeNode, registration);
        assertTrue(registrationTreeNode.getRegistrations().contains(registration));
        instance.releaseLock();

        instance.removeRegistration(registrationTreeNode, registration);
        assertFalse(registrationTreeNode.getRegistrations().contains(registration));

        assertNotNull(instance.findNodeFor(List.of(pathArgument)));
        assertNotNull(instance.takeSnapshot());
    }

    @Test
    void unlockTest() {
        assertThrows(IllegalMonitorStateException.class, instance::releaseLock);
    }
}
