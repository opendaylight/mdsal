/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTreeChangePublisher;
import org.opendaylight.yangtools.concepts.Identifiable;

/**
 * A {@link DOMStore} and a {@link DOMStoreTreeChangePublisher} implementation storing data on Java heap.
 */
public sealed interface InMemoryDOMStore
        extends AutoCloseable, DOMStore, DOMStoreTreeChangePublisher, Identifiable<String>
        permits InMemoryDOMDataStore {
    @Override
    void close();
}
