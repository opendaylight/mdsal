/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;

/**
 * Interface implemented by {@link DOMStoreTransaction}s which are backed by a {@link DataTreeSnapshot}.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public interface SnapshotBackedTransaction extends DOMStoreTransaction {
    /**
     * Returns the {@link DataTreeSnapshot} underlying this transaction. If this transaction is no longer open,
     * {@link Optional#empty()} is returned.
     *
     * @return DataTreeSnapshot attached to this transaction if this transaction is still open.
     */
    Optional<DataTreeSnapshot> getSnapshot();
}
