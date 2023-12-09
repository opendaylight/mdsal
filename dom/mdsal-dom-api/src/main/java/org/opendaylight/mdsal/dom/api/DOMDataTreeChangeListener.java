/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;

/**
 * Interface implemented by classes interested in receiving notifications about data tree changes. It provides
 * a cursor-based view of the change.
 */
public interface DOMDataTreeChangeListener {
    /**
     * Invoked when there was data change for the supplied path, which was used to register this listener.
     *
     * <p>
     * This method may be also invoked during registration of the listener if there is any pre-existing data
     * in the conceptual data tree for supplied path. This initial event will contain all pre-existing data as created.
     *
     * <p>
     * Note: If there is no pre-existing data, the method {@link #onInitialData} will be invoked.
     *
     * <p>
     * A data change event may be triggered spuriously, e.g. such that data before and after compare as equal.
     * Implementations of this interface are expected to recover from such events. Event producers are expected to exert
     * reasonable effort to suppress such events. In other words, it is completely acceptable to observe
     * a {@link org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode}, which reports
     * a {@link org.opendaylight.yangtools.yang.data.tree.api.ModificationType} other than UNMODIFIED, while
     * the before- and after- data items compare as equal.
     *
     * @param changes List of change events, may not be null or empty.
     * @throws NullPointerException if {@code changes} is null
     */
    void onDataTreeChanged(@NonNull List<DataTreeCandidate> changes);

    /**
     * Invoked only once during registration of the listener if there was no data in the conceptual data tree
     * for the supplied path, which was used to register this listener, and after this
     * {@link #onDataTreeChanged(List)} would always be invoked for data changes.
     */
    void onInitialData();
}
