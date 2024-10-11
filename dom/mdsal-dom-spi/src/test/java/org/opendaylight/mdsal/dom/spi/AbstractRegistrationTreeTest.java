/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

public class AbstractRegistrationTreeTest extends AbstractRegistrationTree<Object> {
    @Test
    public void basicTest() {
        final NodeIdentifier pathArgument = new NodeIdentifier(QName.create("", "pathArgument"));
        final Node<Object> registrationTreeNodeParent = new Node<>(null, pathArgument);
        final Node<Object> registrationTreeNode =
                new Node<>(registrationTreeNodeParent, pathArgument);

        final Object registration = new Object();
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
    public void unlockTest() {
        assertThrows(IllegalMonitorStateException.class, this::releaseLock);
    }
}
