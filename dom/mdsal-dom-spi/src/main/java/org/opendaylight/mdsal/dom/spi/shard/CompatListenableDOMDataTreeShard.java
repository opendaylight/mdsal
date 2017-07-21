/*
 * Copyright (c) 2017 Pantheon Technologies, s.ro. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ForwardingObject;
import java.util.Collection;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeShard;
import org.opendaylight.mdsal.dom.spi.store.DOMStoreTreeChangePublisher;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compatibility layer between {@link DOMStoreTreeChangePublisher} and {@link ListenableDOMDataTreeShard}. Required
 * for migration purposes.
 *
 * @author Robert Varga
 *
 * @deprecated This class is scheduled for removal when we remove compatibility with dom.spi.store APIs.
 */
@Deprecated
public final class CompatListenableDOMDataTreeShard extends ForwardingObject implements ListenableDOMDataTreeShard {
    private static final Logger LOG = LoggerFactory.getLogger(CompatListenableDOMDataTreeShard.class);

    private final CompatDOMDataTreeListenerRegistry publisher;
    private final DOMDataTreeShard delegate;

    private CompatListenableDOMDataTreeShard(final DOMDataTreeShard delegate) {
        this.delegate = requireNonNull(delegate);
        checkArgument(delegate instanceof DOMStoreTreeChangePublisher);
        this.publisher = new CompatDOMDataTreeListenerRegistry((DOMStoreTreeChangePublisher) delegate);
    }

    public static ListenableDOMDataTreeShard createIfNeeded(final DOMDataTreeShard delegate) {
        if (delegate instanceof ListenableDOMDataTreeShard) {
            return (ListenableDOMDataTreeShard) delegate;
        }

        LOG.debug("Using compatibility adaptor for {}", delegate);
        return new CompatListenableDOMDataTreeShard(delegate);
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
        return publisher.registerListener(listener, subtrees, allowRxMerges);
    }
}
