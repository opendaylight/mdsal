/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;

class RegistrationTreeNodeTest {
    @Test
    void basicTest() {
        final var pathArgument = new NodeIdentifier(QName.create("", "pathArgument"));
        final var registrationTreeNodeParent = new RegistrationTreeNode<>(null, pathArgument);
        final var registrationTreeNode = new RegistrationTreeNode<>(registrationTreeNodeParent, pathArgument);

        assertEquals(pathArgument, registrationTreeNode.getIdentifier());

        final var registration = new Object();
        assertFalse(registrationTreeNode.getRegistrations().contains(registration));
        registrationTreeNode.addRegistration(registration);
        assertTrue(registrationTreeNode.getRegistrations().contains(registration));
        registrationTreeNode.removeRegistration(registration);
        assertFalse(registrationTreeNode.getRegistrations().contains(registration));
        registrationTreeNode.removeRegistration(registration);

        assertNotNull(registrationTreeNode.ensureChild(pathArgument));
        assertNotNull(registrationTreeNode.getExactChild(pathArgument));

        final var nodeWithValue = new NodeWithValue<>(QName.create("", "testNode"), new Object());
        assertEquals(List.of(), registrationTreeNode.getInexactChildren(nodeWithValue));
        assertEquals(List.of(), registrationTreeNode.getInexactChildren(pathArgument));

        final var nodeWithoutValue = new NodeIdentifier(QName.create("", "testNode"));
        assertNotNull(registrationTreeNode.ensureChild(nodeWithoutValue));
        assertFalse(registrationTreeNode.getInexactChildren(nodeWithValue).isEmpty());

        assertNotNull(registrationTreeNode.toString());
        assertTrue(registrationTreeNode.toString().contains(pathArgument.toString()));
    }
}