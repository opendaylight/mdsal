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

final class BindingDOMDataTreeProducerAdapter extends AbstractBindingAdapter<DOMDataTreeProducer>
        implements DataTreeProducer {
    BindingDOMDataTreeProducerAdapter(final AdapterContext adapterContext, final DOMDataTreeProducer delegate) {
        super(adapterContext, delegate);
    }

    @Override
    public CursorAwareWriteTransaction createTransaction(final boolean isolated) {
        final DOMDataTreeCursorAwareTransaction domTx = getDelegate().createTransaction(isolated);
        return new BindingDOMCursorAwareWriteTransactionAdapter<>(adapterContext(), domTx);
    }

    @Override
    public DataTreeProducer createProducer(final Collection<DataTreeIdentifier<?>> subtrees) {
        final Collection<DOMDataTreeIdentifier> domSubtrees = currentSerializer().toDOMDataTreeIdentifiers(subtrees);
        final DOMDataTreeProducer domChildProducer = getDelegate().createProducer(domSubtrees);
        return new BindingDOMDataTreeProducerAdapter(adapterContext(), domChildProducer);
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
