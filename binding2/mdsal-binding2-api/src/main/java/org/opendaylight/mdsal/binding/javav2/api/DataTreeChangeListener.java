/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.EventListener;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;

/**
 * Interface implemented by classes interested in receiving notifications about
 * data tree changes. It provides a cursor-based view of the change, which has potentially
 * lower overhead and allow more flexible consumption of change event.
 */
@Beta
public interface DataTreeChangeListener<T extends TreeNode> extends EventListener {

    /**
     * Invoked when there was data change for the supplied path, which was used
     * to register this listener.
     *
     * <p>
     * This method may be also invoked during registration of the listener if
     * there is any pre-existing data in the conceptual data tree for supplied
     * path. This initial event will contain all pre-existing data as created.
     *
     * <p>
     * A data change event may be triggered spuriously, e.g. such that data before
     * and after compare as equal. Implementations of this interface are expected
     * to recover from such events. Event producers are expected to exert reasonable
     * effort to suppress such events.
     *
     *<p>
     * In other words, it is completely acceptable to observe
     * a {@link TreeNodeModification}, while the state observed before and
     * after- data items compare as equal.
     *
     * @param changes Collection of change events, may not be null or empty.
     */
    void onDataTreeChanged(@Nonnull Collection<? extends DataTreeModification<T>> changes);
}
