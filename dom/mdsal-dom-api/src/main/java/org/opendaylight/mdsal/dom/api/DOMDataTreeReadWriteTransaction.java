/*
 * Copyright (c) 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

/**
 * A transaction that provides read/write access to a logical data store.
 *
 * <p>
 * For more information on usage and examples, please see the documentation in {@link DOMDataTreeReadTransaction}
 * and {@link DOMDataTreeWriteTransaction}.
 */
// FIXME: 4.0.0: extend DOMDataTreeReadOperations instead of DOMDataTreeReadTransaction
public interface DOMDataTreeReadWriteTransaction extends DOMDataTreeWriteTransaction, DOMDataTreeReadTransaction {
    /**
     * This method is an API design mistake. Users should not use it and use {@link #cancel()} instead. Implementations
     * should not override it and rely instead of its default implementation, which calls {@link #cancel()}.
     *
     * @deprecated Use {@link #cancel()} instead.
     */
    @Deprecated
    @Override
    default void close() {
        cancel();
    }
}
