/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import com.google.common.collect.ImmutableSet;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.spi.AbstractDOMDataTreeChangeListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Abstract base class for {@link DOMStoreTreeChangePublisher} implementations.
 */
public abstract class AbstractDOMStoreTreeChangePublisher
    extends AbstractDOMDataTreeChangePublisher implements DOMStoreTreeChangePublisher {

    @Override
    public <L extends DOMDataTreeChangeListener> AbstractDOMDataTreeChangeListenerRegistration<L>
        registerTreeChangeListener(final YangInstanceIdentifier treeId, final L listener) {
        return registerTreeChangeListener(listener, ImmutableSet.of(treeId));
    }
}
