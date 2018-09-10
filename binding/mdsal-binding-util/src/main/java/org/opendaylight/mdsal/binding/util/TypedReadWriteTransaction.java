/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;

/**
 * Read-write transaction which is specific to a single logical datastore (configuration or operational). Designed
 * for use with {@link ManagedNewTransactionRunner} (it doesn’t support explicit cancel or commit operations).
 *
 * @param <D> The logical datastore handled by the transaction.
 * @see ReadWriteTransaction
 */
public interface TypedReadWriteTransaction<D extends Datastore>
        extends TypedReadTransaction<D>, TypedWriteTransaction<D> {
}
