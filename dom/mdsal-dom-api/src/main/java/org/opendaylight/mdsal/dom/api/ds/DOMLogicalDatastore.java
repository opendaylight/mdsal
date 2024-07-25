/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.ds;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;

/**
 * A datastore which has which has a {@link LogicalDatastoreType}.
 */
public sealed interface DOMLogicalDatastore extends DOMDatastore
        permits DOMConfigurationDatastore, DOMOperationalDatastore {
    /**
     * Returns the {@link LogicalDatastoreType}.
     *
     * @return the {@link LogicalDatastoreType}
     */
    @NonNull LogicalDatastoreType type();
}
