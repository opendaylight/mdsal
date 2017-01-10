/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.data;

import com.google.common.annotations.Beta;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.api.BindingTransactionChain;
import org.opendaylight.mdsal.binding.javav2.api.DataBroker;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeListener;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeLoopException;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeProducer;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeService;
import org.opendaylight.mdsal.binding.javav2.api.ReadTransaction;
import org.opendaylight.mdsal.binding.javav2.api.WriteTransaction;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.data.tree.BindingDOMDataTreeServiceAdapter;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.transaction.BindingDOMReadTransactionAdapter;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.transaction.BindingDOMTransactionChainAdapter;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.transaction.BindingDOMWriteTransactionAdapter;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.spi.AbstractForwardedDataBroker;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.spi.builder.BindingDOMAdapterBuilder;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.spi.builder.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.AsyncReadWriteTransaction;
import org.opendaylight.mdsal.common.api.TransactionChainListener;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

/**
 * The DataBrokerImpl simply defers to the DOMDataBroker for all its operations. All transactions and listener
 * registrations are wrapped by the DataBrokerImpl to allow binding aware components to use the DataBroker
 * transparently.
 *
 * <p>
 * Besides this the DataBrokerImpl and it's collaborators also cache data that is already transformed from the
 * binding independent to binding aware format
 *
 */
@Beta
public class BindingDOMDataBrokerAdapter extends AbstractForwardedDataBroker implements DataBroker, DataTreeService {

    public static final Factory<DataBroker> BUILDER_FACTORY = Builder::new;

    private final DataTreeService treeService;

    public BindingDOMDataBrokerAdapter(final DOMDataBroker domDataBroker, final BindingToNormalizedNodeCodec codec) {
        super(domDataBroker, codec);
        final DOMDataTreeService domTreeChange =
                (DOMDataTreeService) domDataBroker.getSupportedExtensions().get(DOMDataTreeService.class);
        if (domTreeChange != null) {
            treeService = BindingDOMDataTreeServiceAdapter.create(domTreeChange, codec);
        } else {
            treeService = null;
        }
    }

    @Override
    public ReadTransaction newReadOnlyTransaction() {
        return new BindingDOMReadTransactionAdapter(getDelegate().newReadOnlyTransaction(), getCodec());
    }

    @Override
    public WriteTransaction newWriteOnlyTransaction() {
        return new BindingDOMWriteTransactionAdapter<>(getDelegate().newWriteOnlyTransaction(), getCodec());
    }

    @Override
    public AsyncReadWriteTransaction<InstanceIdentifier<?>, TreeNode> newReadWriteTransaction() {
        // TODO - placeholder for now
        throw new UnsupportedOperationException();
    }

    @Override
    public BindingTransactionChain createTransactionChain(final TransactionChainListener listener) {
        return new BindingDOMTransactionChainAdapter(getDelegate(), getCodec(), listener);
    }

    private static class Builder extends BindingDOMAdapterBuilder<DataBroker> {

        @Override
        public Set<? extends Class<? extends DOMService>> getRequiredDelegates() {
            return ImmutableSet.of(DOMDataBroker.class);
        }

        @Override
        protected DataBroker createInstance(final BindingToNormalizedNodeCodec codec,
                final ClassToInstanceMap<DOMService> delegates) {
            final DOMDataBroker domDataBroker = delegates.getInstance(DOMDataBroker.class);
            return new BindingDOMDataBrokerAdapter(domDataBroker, codec);
        }

    }

    @Nonnull
    @Override
    public <T extends DataTreeListener> ListenerRegistration<T> registerListener(@Nonnull final T listener,
            @Nonnull final Collection<DataTreeIdentifier<?>> subtrees, final boolean allowRxMerges,
            @Nonnull final Collection<DataTreeProducer> producers) throws DataTreeLoopException {
        if (treeService == null) {
            throw new UnsupportedOperationException("Underlying data broker does not expose DOMDataTreeChangeService.");
        }
        return treeService.registerListener(listener, subtrees, allowRxMerges, producers);
    }

    @Override
    public DataTreeProducer createProducer(final Collection<DataTreeIdentifier<?>> subtrees) {
        return treeService.createProducer(subtrees);
    }
}
