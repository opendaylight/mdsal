/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

/**
 * A {@link BindingService} which allows users to register for changes to a subtree.
 */
@Beta
public interface DataTreeChangeService extends BindingService {

    /**
     * Registers a {@link DataTreeChangeListener} to receive
     * notifications when data changes under a given path in the conceptual data
     * tree.
     *
     * <p>
     * You are able to register for notifications  for any node or subtree
     * which can be represented using {@link DataTreeIdentifier}.
     *
     * <p>
     * You are able to register for data change notifications for a subtree or leaf
     * even if it does not exist. You will receive notification once that node is
     * created.
     *
     * <p>
     * If there is any pre-existing data in the data tree for the path for which you are
     * registering, you will receive an initial data change event, which will
     * contain all pre-existing data, marked as created.
     *
     * <p>
     * This method returns a {@link ListenerRegistration} object. To
     * "unregister" your listener for changes call the {@link ListenerRegistration#close()}
     * method on the returned object.
     *
     * <p>
     * You MUST explicitly unregister your listener when you no longer want to receive
     * notifications. This is especially true in OSGi environments, where failure to
     * do so during bundle shutdown can lead to stale listeners being still registered.
     *
     * @param <T> data tree type
     * @param <L> listener type
     * @param treeId
     *            Data tree identifier of the subtree which should be watched for
     *            changes.
     * @param listener
     *            Listener instance which is being registered
     * @return Listener registration object, which may be used to unregister
     *         your listener using {@link ListenerRegistration#close()} to stop
     *         delivery of change events.
     */
    @Nonnull
    <T extends TreeNode, L extends DataTreeChangeListener<T>> ListenerRegistration<L>
        registerDataTreeChangeListener(@Nonnull DataTreeIdentifier<T> treeId, @Nonnull L listener);

}
