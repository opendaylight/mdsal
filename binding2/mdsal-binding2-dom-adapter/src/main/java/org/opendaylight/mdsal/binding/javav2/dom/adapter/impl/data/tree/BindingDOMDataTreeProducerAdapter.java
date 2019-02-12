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
import org.opendaylight.mdsal.binding.javav2.api.CursorAwareWriteTransaction;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeProducer;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeProducerException;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerException;

/**
 * Producer adapter.
 */
@Beta
public final class BindingDOMDataTreeProducerAdapter implements DataTreeProducer {

    private final DOMDataTreeProducer delegate;
    private final BindingToNormalizedNodeCodec codec;

    private BindingDOMDataTreeProducerAdapter(final DOMDataTreeProducer delegate,
            final BindingToNormalizedNodeCodec codec) {
        this.delegate = requireNonNull(delegate);
        this.codec = codec;
    }

    /**
     * Create instance of producer data tree.
     *
     * @param domProducer
     *            - DOM producer
     * @param codec
     *            - codec for serialize/deserialize
     * @return instance of producer
     */
    public static DataTreeProducer create(final DOMDataTreeProducer domProducer,
            final BindingToNormalizedNodeCodec codec) {
        return new BindingDOMDataTreeProducerAdapter(domProducer, codec);
    }

    @Nonnull
    @Override
    public CursorAwareWriteTransaction createTransaction(final boolean isolated) {
        final DOMDataTreeCursorAwareTransaction domTx = delegate.createTransaction(isolated);
        return new BindingDOMCursorAwareWriteTransactionAdapter<>(domTx, codec);
    }

    @Nonnull
    @Override
    public DataTreeProducer createProducer(@Nonnull final Collection<DataTreeIdentifier<?>> subtrees) {
        final Collection<DOMDataTreeIdentifier> domSubtrees = codec.toDOMDataTreeIdentifiers(subtrees);
        final DOMDataTreeProducer domChildProducer = delegate.createProducer(domSubtrees);
        return BindingDOMDataTreeProducerAdapter.create(domChildProducer, codec);
    }

    @Override
    public void close() throws DataTreeProducerException {
        try {
            delegate.close();
        } catch (final DOMDataTreeProducerException e) {
            throw new DataTreeProducerException(e.getMessage(), e);
        }
    }
}

