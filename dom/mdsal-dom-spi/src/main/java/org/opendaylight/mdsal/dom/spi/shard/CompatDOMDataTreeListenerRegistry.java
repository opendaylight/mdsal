/*
 * Copyright (c) 2017 Pantheon Technologies, s.ro. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTreeChangePublisher;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Compatibility bridge between {@link DOMDataTreeListenerRegistry} and {@link DOMStoreTreeChangePublisher}.
 */
@Beta
@Deprecated
public final class CompatDOMDataTreeListenerRegistry implements DOMDataTreeListenerRegistry {

    private final DOMStoreTreeChangePublisher publisher;

    public CompatDOMDataTreeListenerRegistry(final DOMStoreTreeChangePublisher publisher) {
        this.publisher = requireNonNull(publisher);
    }

    @Override
    public <T extends DOMDataTreeListener> ListenerRegistration<T> registerListener(final T listener,
            final Collection<DOMDataTreeIdentifier> subtrees, final boolean allowRxMerges) {
        if (subtrees.size() == 1) {
            final DOMDataTreeIdentifier treeId = Iterables.getOnlyElement(subtrees);

            final ListenerRegistration<?> reg = publisher.registerTreeChangeListener(treeId.getRootIdentifier(),
                changes -> {
                    final Optional<NormalizedNode<?, ?>> last = Iterables.getLast(changes).getRootNode().getDataAfter();
                    if (last.isPresent()) {
                        listener.onDataTreeChanged(changes, ImmutableMap.of(treeId, last.get()));
                    } else {
                        listener.onDataTreeChanged(changes, ImmutableMap.of());
                    }
                });
            return new AbstractListenerRegistration<T>(listener) {
                @Override
                protected void removeRegistration() {
                    reg.close();
                }
            };
        }

        final int size = subtrees.size();
        final Collection<ListenerRegistration<?>> regs = new ArrayList<>(size);
        final DOMDataTreeChangeListenerAggregator aggregator = new DOMDataTreeChangeListenerAggregator(size,
            allowRxMerges);
        for (DOMDataTreeIdentifier treeId : subtrees) {
            regs.add(publisher.registerTreeChangeListener(treeId.getRootIdentifier(),
                aggregator.createListener(treeId)));
        }

        return aggregator.start(listener, regs);
    }
}
