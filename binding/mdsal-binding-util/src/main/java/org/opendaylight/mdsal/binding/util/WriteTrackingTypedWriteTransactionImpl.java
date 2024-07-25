/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.datastores.rev180214.Datastore;

/**
 * Write typed transaction which keeps track of writes.
 */
final class WriteTrackingTypedWriteTransactionImpl<D extends Datastore>
        extends TypedWriteTransactionImpl<D, WriteTransaction> implements WriteTrackingTransaction {

    // This is volatile to ensure we get the latest value; transactions aren't supposed to be used in multiple threads,
    // but the cost here is tiny (one read penalty at the end of a transaction) so we play it safe
    private volatile boolean written;

    WriteTrackingTypedWriteTransactionImpl(final D datastore, final WriteTransaction realTx) {
        super(datastore, realTx);
    }

    @Override
    public boolean isWritten() {
        return written;
    }

    @Override
    void postOperation() {
        written = true;
    }
}
