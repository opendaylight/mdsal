/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api.ds;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 */
@NonNullByDefault
public interface WriteTransaction extends WriteOperations {

    void commit(CommitCallback callback);

   /**
    * Cancels the transaction. Transactions can only be cancelled if it was not yet committed.
    * Invoking cancel() on failed or already cancelled will have no effect, and transaction is considered cancelled.
    * Invoking cancel() on finished transaction (future returned by {@link #commit()} already successfully completed)
    * will always fail (return false).
    *
    * @return {@code false} if the task could not be cancelled, typically because it has already completed normally;
    *         {@code true} otherwise
    */
   boolean cancel();
}
