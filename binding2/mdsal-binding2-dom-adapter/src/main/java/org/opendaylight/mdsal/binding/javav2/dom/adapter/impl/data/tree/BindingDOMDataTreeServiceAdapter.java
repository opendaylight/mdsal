/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.data.tree;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeListener;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeLoopException;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeProducer;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeService;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

/**
 * Data tree service adapter.
 *
 */
@Beta
public final class BindingDOMDataTreeServiceAdapter implements DataTreeService {

    private final DOMDataTreeService delegate;
    private final BindingToNormalizedNodeCodec codec;

    private BindingDOMDataTreeServiceAdapter(final DOMDataTreeService delegate,
            final BindingToNormalizedNodeCodec codec) {
        this.delegate = requireNonNull(delegate, "delegate");
        this.codec = requireNonNull(codec, "codec");
    }

    /**
     * Create instance of data tree service adapter.
     *
     * @param domService
     *            - data tree service
     * @param codec
     *            - codec foer serialize/deserialize
     * @return instance of data tree service adapter
     */
    public static BindingDOMDataTreeServiceAdapter create(final DOMDataTreeService domService,
            final BindingToNormalizedNodeCodec codec) {
        return new BindingDOMDataTreeServiceAdapter(domService, codec);
    }

    @Override
    public DataTreeProducer createProducer(final Collection<DataTreeIdentifier<?>> subtrees) {
        final Collection<DOMDataTreeIdentifier> domSubtrees = codec.toDOMDataTreeIdentifiers(subtrees);
        final DOMDataTreeProducer domChildProducer = delegate.createProducer(domSubtrees);
        return BindingDOMDataTreeProducerAdapter.create(domChildProducer, codec);
    }

    @Nonnull
    @Override
    public <T extends DataTreeListener> ListenerRegistration<T> registerListener(@Nonnull final T listener,
            @Nonnull final Collection<DataTreeIdentifier<?>> subtrees, final boolean allowRxMerges,
            @Nonnull final Collection<DataTreeProducer> producers) throws DataTreeLoopException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
