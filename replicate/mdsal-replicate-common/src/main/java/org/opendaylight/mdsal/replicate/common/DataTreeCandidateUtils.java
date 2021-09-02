/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.replicate.common;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteOperations;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreWriteTransaction;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.tree.spi.DataTreeCandidates;

@Beta
public final class DataTreeCandidateUtils {
    private DataTreeCandidateUtils() {
        // Hidden on purpose
    }

    public static void applyToTransaction(final DOMDataTreeWriteOperations transaction,
            final LogicalDatastoreType datastore, final DataTreeCandidate candidate) {
        DataTreeCandidates.applyToModification(new DOMDataBrokerModification(transaction, datastore), candidate);
    }

    public static void applyToTransaction(final DOMStoreWriteTransaction transaction,
            final DataTreeCandidate candidate) {
        DataTreeCandidates.applyToModification(new DOMStoreModification(transaction), candidate);
    }
}
