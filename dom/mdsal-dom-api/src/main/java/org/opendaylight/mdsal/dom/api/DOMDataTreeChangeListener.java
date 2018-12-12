/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import java.util.Collection;
import java.util.EventListener;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

/**
 * Interface implemented by classes interested in receiving notifications about data tree changes. It provides
 * a cursor-based view of the change.
 */
public interface DOMDataTreeChangeListener extends EventListener {
    /**
     * Invoked when there was data change for the supplied path, which was used to register this listener.
     *
     * <p>
     * This method may be also invoked during registration of the listener if there is any pre-existing data
     * in the conceptual data tree for supplied path. This initial event will contain all pre-existing data as created.
     *
     * <p>
     * Note: If there is no pre-existing data, the method {@link #onEmptyInitialData} will be invoked.
     *
     * <p>
     * A data change event may be triggered spuriously, e.g. such that data before and after compare as equal.
     * Implementations of this interface are expected to recover from such events. Event producers are expected to exert
     * reasonable effort to suppress such events. In other words, it is completely acceptable to observe
     * a {@link org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode}, which reports
     * a {@link org.opendaylight.yangtools.yang.data.api.schema.tree.ModificationType} other than UNMODIFIED, while
     * the before- and after- data items compare as equal.
     *
     * @param changes Collection of change events, may not be null or empty.
     * @throws NullPointerException if {@code changes} is null
     */
    void onDataTreeChanged(@NonNull Collection<DataTreeCandidate> changes);

    /**
     * Invoked only once during registration of the listener if there was no data in the conceptual data tree
     * for the supplied path, which was used to register this listener, and after this {@link #onDataTreeChanged}
     * would always be invoked for data changes.
     *
     * <p>
     * Default implementation does nothing and is appropriate for users who do not care about ascertaining
     * initial stat.
     */
    default void onEmptyInitialData() {
        //no-op
    }
}
