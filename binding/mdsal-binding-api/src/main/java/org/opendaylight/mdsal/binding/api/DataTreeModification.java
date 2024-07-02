/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;

/**
 * A modification to a datastore subtree.
 */
public interface DataTreeModification<T extends DataObject> {
    /**
     * Return the {@link LogicalDatastoreType} on which this change occurred.
     *
     * @return the logical datastore type.
     */
    @NonNull LogicalDatastoreType datastore();

    /**
     * Return the {@link DataObjectIdentifier} of the root path. This is the path of the root node relative to the root
     * of {@link DataObjectIdentifier} namespace.
     *
     * @return the {@link DataObjectIdentifier} of the root path
     */
    @NonNull DataObjectIdentifier<T> path();

    /**
     * Get the modification root path. This is the path of the root node relative to the root of InstanceIdentifier
     * namespace.
     *
     * @return absolute path of the root node
     * @deprecated Use {@link #datastore()} and {@link #path()} instead.
     */
    @Deprecated(since = "14.0.0", forRemoval = true)
    default @NonNull DataTreeMatch<T> getRootPath() {
        return DataTreeMatch.of(datastore(), path().toLegacy());
    }

    /**
     * Get the modification root node.
     *
     * @return modification root node
     */
    @NonNull DataObjectModification<T> getRootNode();
}
