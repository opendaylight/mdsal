/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory.impl;

import java.util.List;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;

/**
 * A {@link TreeChangePublisher}
 */
final class DirectTreeChangePublisher extends TreeChangePublisher {
    @Override
    protected void notifyListener(final Reg registration, final List<DataTreeCandidate> changes) {
        registration.listener().onDataTreeChanged(changes);
    }

    @Override
    void close() {
        // no-op
    }
}
