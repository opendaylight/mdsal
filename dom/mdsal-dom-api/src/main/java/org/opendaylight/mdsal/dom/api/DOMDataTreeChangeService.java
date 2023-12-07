/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * A {@link DOMDataBroker.Extension} which allows users to register for changes to a subtree.
 */
@NonNullByDefault
public interface DOMDataTreeChangeService extends DOMDataBroker.Extension {
    /**
     * Registers a {@link DOMDataTreeChangeListener} to receive notifications when data changes under a given path in
     * the conceptual data tree.
     *
     * <p>
     * You are able to register for notifications for any node or subtree which can be represented using
     * {@link DOMDataTreeIdentifier}.
     *
     * <p>
     * You are able to register for data change notifications for a subtree or leaf even if it does not exist. You will
     * receive notification once that node is created.
     *
     * <p>
     * If there is any pre-existing data in the data tree for the path for which you are registering, you will receive
     * an initial data change event, which will contain all pre-existing data, marked as created.
     *
     * <p>
     * This method returns a {@link Registration} object. To "unregister" your listener for changes call
     * the {@link Registration#close()} method on the returned object.
     *
     * <p>
     * You MUST explicitly unregister your listener when you no longer want to receive notifications. This is especially
     * true in OSGi environments, where failure to do so during bundle shutdown can lead to stale listeners being still
     * registered.
     *
     * @param treeId Data tree identifier of the subtree which should be watched for changes.
     * @param listener Listener instance which is being registered
     * @return A {@link Registration} object, which may be used to unregister your listener using
     *         {@link Registration#close()} to stop delivery of change events.
     * @throws NullPointerException if any of the arguments is null
     */
    Registration registerDataTreeChangeListener(DOMDataTreeIdentifier treeId, DOMDataTreeChangeListener listener);
}
