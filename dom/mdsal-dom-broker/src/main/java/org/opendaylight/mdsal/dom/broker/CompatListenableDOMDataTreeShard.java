/*
 * Copyright (c) 2017 Pantheon Technologies, s.ro. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.opendaylight.mdsal.dom.spi.shard.ListenableDOMDataTreeShard;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTreeChangePublisher;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Compatibility layer between {@link DOMStoreTreeChangePublisher} and {@link ListenableDOMDataTreeShard}. Required
 * for migration purposes.
 *
 * @author Robert Varga
 *
 * @deprecated This class is scheduled for removal when we remove compatibility with dom.spi.store APIs.
 */
@Deprecated
final class CompatListenableDOMDataTreeShard extends ForwardingObject implements ListenableDOMDataTreeShard {

    private final DOMDataTreeShard delegate;

    CompatListenableDOMDataTreeShard(final DOMDataTreeShard delegate) {
        Preconditions.checkArgument(delegate instanceof DOMStoreTreeChangePublisher);
        this.delegate = Preconditions.checkNotNull(delegate);
    }

    @Override
    protected DOMDataTreeShard delegate() {
        return delegate;
    }

    @Override
    public void onChildAttached(final DOMDataTreeIdentifier prefix, final DOMDataTreeShard child) {
        delegate.onChildAttached(prefix, child);
    }

    @Override
    public void onChildDetached(final DOMDataTreeIdentifier prefix, final DOMDataTreeShard child) {
        delegate.onChildDetached(prefix, child);
    }

    private <L extends DOMDataTreeChangeListener> ListenerRegistration<L> registerTreeChangeListener(
            @Nonnull final YangInstanceIdentifier treeId, @Nonnull final L listener) {
        return ((DOMStoreTreeChangePublisher) delegate).registerTreeChangeListener(treeId, listener);
    }

    @Override
    public <L extends DOMDataTreeListener> ListenerRegistration<L> registerListener(final L listener,
            final Collection<DOMDataTreeIdentifier> subtrees, final boolean allowRxMerges) {
        if (subtrees.size() == 1) {
            final DOMDataTreeIdentifier treeId = Iterables.getOnlyElement(subtrees);

            final ListenerRegistration<?> reg = registerTreeChangeListener(treeId.getRootIdentifier(),
                changes -> {
                    final Optional<NormalizedNode<?, ?>> last = Iterables.getLast(changes).getRootNode().getDataAfter();
                    if (last.isPresent()) {
                        listener.onDataTreeChanged(changes, ImmutableMap.of(treeId, last.get()));
                    } else {
                        listener.onDataTreeChanged(changes, ImmutableMap.of());
                    }
                });
            return new AbstractListenerRegistration<L>(listener) {
                @Override
                protected void removeRegistration() {
                    reg.close();
                }
            };
        }

        final int size = subtrees.size();
        final Collection<ListenerRegistration<?>> regs = new ArrayList<>(size);
        final CompatDOMDataTreeListener aggregator = new CompatDOMDataTreeListener(size, allowRxMerges);
        for (DOMDataTreeIdentifier treeId : subtrees) {
            regs.add(registerTreeChangeListener(treeId.getRootIdentifier(), aggregator.createListener(treeId)));
        }

        aggregator.start(listener);
        return new AbstractListenerRegistration<L>(listener) {
            @Override
            protected void removeRegistration() {
                regs.forEach(ListenerRegistration::close);
            }
        };
    }
}
