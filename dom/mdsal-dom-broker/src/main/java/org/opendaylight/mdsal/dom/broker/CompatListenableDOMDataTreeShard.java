package org.opendaylight.mdsal.dom.broker;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.opendaylight.mdsal.dom.spi.shard.ListenableDOMDataTreeShard;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTreeChangePublisher;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Compatibility layer between DOMStoreTreeChangePublisher and ListenableDOMDataTreeShard. Required for migration
 * purposes.
 *
 * @author Robert Varga
 *
 * @param <T> Shard type
 */
final class CompatListenableDOMDataTreeShard<T extends DOMStoreTreeChangePublisher & DOMDataTreeShard>
        extends ForwardingObject implements ListenableDOMDataTreeShard {

    private final DOMDataTreeIdentifier prefix;
    private final T delegate;

    CompatListenableDOMDataTreeShard(final DOMDataTreeIdentifier prefix, final T delegate) {
        this.prefix = Preconditions.checkNotNull(prefix);
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

    @Override
    public <L extends DOMDataTreeListener> ListenerRegistration<L> registerListener(final L listener,
            final Collection<DOMDataTreeIdentifier> subtrees, final boolean allowRxMerges) {
        if (subtrees.size() == 1) {
            final DOMDataTreeIdentifier treeId = Iterables.getOnlyElement(subtrees);

            final ListenerRegistration<?> reg = delegate.registerTreeChangeListener(treeId.getRootIdentifier(),
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

        final Collection<ListenerRegistration<?>> regs = new ArrayList<>(subtrees.size());
        final CompatAggregatingTreeListener aggregator = new CompatAggregatingTreeListener(allowRxMerges);
        for (DOMDataTreeIdentifier treeId : subtrees) {
            regs.add(delegate.registerTreeChangeListener(treeId.getRootIdentifier(),
                aggregator.createListener(treeId)));
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
