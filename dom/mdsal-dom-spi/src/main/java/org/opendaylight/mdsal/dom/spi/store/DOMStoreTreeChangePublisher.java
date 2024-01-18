/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMDataBroker.DataTreeChangeExtension;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * Interface implemented by DOMStore implementations which allow registration of {@link DOMDataTreeChangeListener}
 * instances.
 */
public interface DOMStoreTreeChangePublisher {
    /**
     * Registers a {@link DOMDataTreeChangeListener} to receive notifications when data changes under a given path in
     * the conceptual data tree. See
     * {@link DataTreeChangeExtension#registerTreeChangeListener(DOMDataTreeIdentifier, DOMDataTreeChangeListener)} for
     * full semantics.
     *
     * @param treeId Data tree identifier of the subtree which should be watched for changes.
     * @param listener Listener instance which is being registered
     * @return A {@link Registration} registration object, which may be used to unregister your listener using
     *         {@link Registration#close()} to stop delivery of change events.
     */
    @NonNull Registration registerTreeChangeListener(@NonNull YangInstanceIdentifier treeId,
        @NonNull DOMDataTreeChangeListener listener);

    /**
     * Registers a {@link DOMDataTreeChangeListener} to receive notifications when data changes under a given path in
     * the conceptual data tree. See {@link DataTreeChangeExtension#registerLegacyTreeChangeListener(
     * DOMDataTreeIdentifier, DOMDataTreeChangeListener)} for full semantics.
     *
     * @param treeId Data tree identifier of the subtree which should be watched for changes.
     * @param listener Listener instance which is being registered
     * @return A {@link Registration} registration object, which may be used to unregister your listener using
     *         {@link Registration#close()} to stop delivery of change events.
     * @deprecated Legacy support class
     */
    @Deprecated(since = "13.0.0", forRemoval = true)
    @NonNull Registration registerLegacyTreeChangeListener(@NonNull YangInstanceIdentifier treeId,
        @NonNull DOMDataTreeChangeListener listener);
}
