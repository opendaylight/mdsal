/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;

/**
 * The {@value LogicalDatastoreType#OPERATIONAL} DOM datastore.
 */
@NonNullByDefault
public non-sealed interface DOMOperationalDatastore extends DOMLogicalDatastore {
    @Override
    default LogicalDatastoreType type() {
        return LogicalDatastoreType.OPERATIONAL;
    }
}
