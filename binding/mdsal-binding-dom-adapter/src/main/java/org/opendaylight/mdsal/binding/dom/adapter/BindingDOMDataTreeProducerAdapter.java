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
import org.opendaylight.mdsal.binding.api.CursorAwareWriteTransaction;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeProducer;
import org.opendaylight.mdsal.binding.api.DataTreeProducerException;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerBusyException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;

class BindingDOMDataTreeProducerAdapter implements DataTreeProducer {

    private final DOMDataTreeProducer delegate;
    private final BindingToNormalizedNodeCodec codec;

    protected BindingDOMDataTreeProducerAdapter(final DOMDataTreeProducer delegate,
            final BindingToNormalizedNodeCodec codec) {
        super();
        this.delegate = Preconditions.checkNotNull(delegate);
        this.codec = codec;
    }

    @Override
    public CursorAwareWriteTransaction createTransaction(final boolean isolated) {
        final DOMDataTreeCursorAwareTransaction domTx = delegate.createTransaction(isolated);
        return new BindingDOMCursorAwareWriteTransactionAdapter<>(domTx, codec);
    }

    static DataTreeProducer create(final DOMDataTreeProducer domProducer,
            final BindingToNormalizedNodeCodec codec) {
        return new BindingDOMDataTreeProducerAdapter(domProducer, codec);
    }

    @Override
    public DataTreeProducer createProducer(final Collection<DataTreeIdentifier<?>> subtrees) {
        final Collection<DOMDataTreeIdentifier> domSubtrees = codec.toDOMDataTreeIdentifiers(subtrees);
        final DOMDataTreeProducer domChildProducer = delegate.createProducer(domSubtrees);
        return BindingDOMDataTreeProducerAdapter.create(domChildProducer, codec);
    }

    @Override
    public void close() throws DataTreeProducerException {
        try {
            delegate.close();
        } catch (final DOMDataTreeProducerBusyException e) {
            throw new DataTreeProducerException(e.getMessage(), e);
        } catch (final DOMDataTreeProducerException e) {
            throw new DataTreeProducerException(e.getMessage(), e);
        }
    }

}
