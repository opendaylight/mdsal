/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import org.opendaylight.yangtools.concepts.Identifiable;

/**
 * DOM Data Store transaction.
 *
 * <p>See {@link DOMStoreReadTransaction}, {@link DOMStoreWriteTransaction} and {@link DOMStoreReadWriteTransaction}
 * for specific transaction types.
 */
public interface DOMStoreTransaction extends AutoCloseable, Identifiable<Object> {
    /**
     * Unique identifier of the transaction.
     */
    @Override
    Object getIdentifier();

    @Override
    void close();
}
