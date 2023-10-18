/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;

class AbstractDOMDataTreeChangeListenerRegistrationTest
        extends AbstractDOMDataTreeChangeListenerRegistration<DOMDataTreeChangeListener> {
    private static final DOMDataTreeChangeListener DOM_DATA_TREE_CHANGE_LISTENER =
            mock(DOMDataTreeChangeListener.class);

    AbstractDOMDataTreeChangeListenerRegistrationTest() {
        super(DOM_DATA_TREE_CHANGE_LISTENER);
    }

    @Test
    void basicTest() {
        try (var reg = new AbstractDOMDataTreeChangeListenerRegistrationTest()) {
            assertEquals(DOM_DATA_TREE_CHANGE_LISTENER, reg.getInstance());
        }
    }

    @Override
    protected void removeRegistration() {
        // NOOP
    }
}