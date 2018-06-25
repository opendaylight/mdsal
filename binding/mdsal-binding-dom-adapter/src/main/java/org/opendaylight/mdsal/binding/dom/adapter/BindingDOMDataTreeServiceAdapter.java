/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.base.Preconditions;
import java.util.Collection;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeListener;
import org.opendaylight.mdsal.binding.api.DataTreeLoopException;
import org.opendaylight.mdsal.binding.api.DataTreeProducer;
import org.opendaylight.mdsal.binding.api.DataTreeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

public final class BindingDOMDataTreeServiceAdapter implements DataTreeService {
    private final DOMDataTreeService delegate;
    private final BindingToNormalizedNodeCodec codec;

    private BindingDOMDataTreeServiceAdapter(final DOMDataTreeService delegate,
            final BindingToNormalizedNodeCodec codec) {
        this.delegate = Preconditions.checkNotNull(delegate, "delegate");
        this.codec = Preconditions.checkNotNull(codec, "codec");
    }

    public static BindingDOMDataTreeServiceAdapter create(final BindingToNormalizedNodeCodec codec,
            final DOMDataTreeService domService) {
        return new BindingDOMDataTreeServiceAdapter(domService, codec);
    }

    static BindingDOMDataTreeServiceAdapter create(final DOMDataTreeService domService,
            final BindingToNormalizedNodeCodec codec) {
        return new BindingDOMDataTreeServiceAdapter(domService, codec);
    }

    @Override
    public DataTreeProducer createProducer(final Collection<DataTreeIdentifier<?>> subtrees) {
        final Collection<DOMDataTreeIdentifier> domSubtrees = codec.toDOMDataTreeIdentifiers(subtrees);
        final DOMDataTreeProducer domChildProducer = delegate.createProducer(domSubtrees);
        return BindingDOMDataTreeProducerAdapter.create(domChildProducer, codec);
    }

    @Override
    public <T extends DataTreeListener> ListenerRegistration<T> registerListener(final T listener,
            final Collection<DataTreeIdentifier<?>> subtrees, final boolean allowRxMerges,
            final Collection<DataTreeProducer> producers) throws DataTreeLoopException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
