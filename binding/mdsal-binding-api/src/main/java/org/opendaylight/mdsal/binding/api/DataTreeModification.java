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
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Represent root of modification.
 *
 * @author Tony Tkacik &lt;ttkacik@cisco.com&gt;
 */
public interface DataTreeModification<T extends DataObject> {
    /**
     * Get the modification root path. This is the path of the root node relative to the root of InstanceIdentifier
     * namespace.
     *
     * @return absolute path of the root node
     */
    @Deprecated
    default DataTreeIdentifier<T> getRootPath() {
        return DataTreeIdentifier.create(datastore(), rootPath().toLegacy());
    }

    LogicalDatastoreType datastore();

    InstanceIdentifier<T> rootPath();

    /**
     * Get the modification root node.
     *
     * @return modification root node
     */
    @NonNull DataObjectModification<T> getRootNode();
}
