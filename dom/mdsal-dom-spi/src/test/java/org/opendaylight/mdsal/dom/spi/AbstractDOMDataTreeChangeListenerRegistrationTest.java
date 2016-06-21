/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;

public class AbstractDOMDataTreeChangeListenerRegistrationTest extends AbstractDOMDataTreeChangeListenerRegistration {

    private static final DOMDataTreeChangeListener DOM_DATA_TREE_CHANGE_LISTENER =
            mock(DOMDataTreeChangeListener.class);

    @Test
    public void basicTest() throws Exception {
        AbstractDOMDataTreeChangeListenerRegistration abstractDOMDataTreeChangeListenerRegistration =
                new AbstractDOMDataTreeChangeListenerRegistrationTest();
        assertEquals(DOM_DATA_TREE_CHANGE_LISTENER, abstractDOMDataTreeChangeListenerRegistration.getInstance());
    }

    public AbstractDOMDataTreeChangeListenerRegistrationTest() {
        super(DOM_DATA_TREE_CHANGE_LISTENER);
    }

    @Override
    protected void removeRegistration() {
        // NOOP
    }
}