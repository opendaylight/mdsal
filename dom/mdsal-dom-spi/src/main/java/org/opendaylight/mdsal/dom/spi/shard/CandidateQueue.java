/*
 * Copyright (c) 2017 Pantheon Technologies, s.ro. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

// TODO: move this
@Beta
public final class CandidateQueue implements Identifiable<DOMDataTreeIdentifier> {

    private final Deque<DataTreeCandidate> queue = new ArrayDeque<>();
    private final DOMDataTreeIdentifier identifier;

    private CandidateQueue(final DOMDataTreeIdentifier identifier) {
        this.identifier = Preconditions.checkNotNull(identifier);
    }

    @Override
    public DOMDataTreeIdentifier getIdentifier() {
        return identifier;
    }

    public synchronized void append(final Collection<DataTreeCandidate> candidates) {
        queue.addAll(candidates);
    }

    public synchronized void clear() {
        queue.clear();
    }

    public synchronized void clearAndAppend(final Collection<DataTreeCandidate> candidate) {
        queue.clear();
        queue.addAll(candidate);
    }

    public synchronized List<DataTreeCandidate> collect() {
        final List<DataTreeCandidate> ret = ImmutableList.copyOf(queue);
        queue.clear();
        return ret;
    }

    public static CandidateQueue create(final DOMDataTreeIdentifier treeId, final boolean allowRxMerges) {
        // TODO: do not ignore allowRxMerges, but rather create a dedicated subclass or something
        return new CandidateQueue(treeId);
    }
}
