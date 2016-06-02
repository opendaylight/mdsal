/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShardProducer;

final class InMemoryDOMDataTreeShardProducer implements DOMDataTreeShardProducer {

    private final InMemoryDOMDataTreeShard parentShard;
    private final Collection<DOMDataTreeIdentifier> prefixes;

    private InmemoryDOMDataTreeShardWriteTransaction currentTx;
    private InmemoryDOMDataTreeShardWriteTransaction lastSubmittedTx;

    InMemoryDOMDataTreeShardProducer(final InMemoryDOMDataTreeShard parentShard,
            final Collection<DOMDataTreeIdentifier> prefixes) {
        this.parentShard = Preconditions.checkNotNull(parentShard);
        this.prefixes = ImmutableSet.copyOf(prefixes);
    }

    @Override
    public InmemoryDOMDataTreeShardWriteTransaction createTransaction() {
        Preconditions.checkState(currentTx == null || currentTx.isFinished(), "Previous transaction not finished yet.");
        if (lastSubmittedTx != null) {
            currentTx = parentShard.createTransaction(lastSubmittedTx);
        } else {
            currentTx = parentShard.createTransaction(prefixes);
        }
        return currentTx;
    }

    @Override
    public Collection<DOMDataTreeIdentifier> getPrefixes() {
        return prefixes;
    }
}
