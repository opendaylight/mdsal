/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import java.util.Collection;
import org.opendaylight.mdsal.binding.api.CursorAwareWriteTransaction;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeProducer;
import org.opendaylight.mdsal.binding.api.DataTreeProducerException;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCursorAwareTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducer;
import org.opendaylight.mdsal.dom.api.DOMDataTreeProducerException;

class BindingDOMDataTreeProducerAdapter extends AbstractBindingAdapter<DOMDataTreeProducer>
        implements DataTreeProducer {

    BindingDOMDataTreeProducerAdapter(final BindingToNormalizedNodeCodec codec, final DOMDataTreeProducer delegate) {
        super(codec, delegate);
    }

    @Override
    public CursorAwareWriteTransaction createTransaction(final boolean isolated) {
        final DOMDataTreeCursorAwareTransaction domTx = getDelegate().createTransaction(isolated);
        return new BindingDOMCursorAwareWriteTransactionAdapter<>(domTx, getCodec());
    }

    static DataTreeProducer create(final DOMDataTreeProducer domProducer, final BindingToNormalizedNodeCodec codec) {
        return new BindingDOMDataTreeProducerAdapter(codec, domProducer);
    }

    @Override
    public DataTreeProducer createProducer(final Collection<DataTreeIdentifier<?>> subtrees) {
        final Collection<DOMDataTreeIdentifier> domSubtrees = getCodec().toDOMDataTreeIdentifiers(subtrees);
        final DOMDataTreeProducer domChildProducer = getDelegate().createProducer(domSubtrees);
        return BindingDOMDataTreeProducerAdapter.create(domChildProducer, getCodec());
    }

    @Override
    public void close() throws DataTreeProducerException {
        try {
            getDelegate().close();
        } catch (final DOMDataTreeProducerException e) {
            throw new DataTreeProducerException(e.getMessage(), e);
        }
    }
}
