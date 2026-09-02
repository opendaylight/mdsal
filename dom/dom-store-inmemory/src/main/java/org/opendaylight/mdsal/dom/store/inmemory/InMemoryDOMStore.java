/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTreeChangePublisher;
import org.opendaylight.mdsal.dom.store.inmemory.impl.InMemoryDOMStoreImpl;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * A {@link DOMStore} and a {@link DOMStoreTreeChangePublisher} implementation storing data on Java heap.
 */
public sealed interface InMemoryDOMStore
        extends AutoCloseable, DOMStore, DOMStoreTreeChangePublisher, Identifiable<String>
        permits InMemoryDOMStoreImpl {
    @Override
    @Deprecated(since = "13.0.0", forRemoval = true)
    default Registration registerLegacyTreeChangeListener(final YangInstanceIdentifier treeId,
            final DOMDataTreeChangeListener listener) {
        return registerTreeChangeListener(treeId, listener);
    }

    @Override
    void close();
}
