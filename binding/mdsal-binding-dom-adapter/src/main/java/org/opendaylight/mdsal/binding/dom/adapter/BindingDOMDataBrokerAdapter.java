/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeChangeService;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.InstanceIdentifier;
import org.opendaylight.mdsal.binding.api.InstanceWildcard;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.binding.api.TransactionChainListener;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * The DataBrokerImpl simply defers to the DOMDataBroker for all its operations. All transactions and listener
 * registrations are wrapped by the DataBrokerImpl to allow binding aware components to use the DataBroker
 * transparently.
 *
 * <p>
 * Besides this the DataBrokerImpl and it's collaborators also cache data that is already transformed from the binding
 * independent to binding aware format.
 */
@VisibleForTesting
public class BindingDOMDataBrokerAdapter extends AbstractBindingAdapter<@NonNull DOMDataBroker> implements DataBroker,
        DataTreeChangeService {

    static final Factory<DataBroker> BUILDER_FACTORY = Builder::new;
    private final DataTreeChangeService treeChangeService;

    public BindingDOMDataBrokerAdapter(final AdapterContext adapterContext, final DOMDataBroker domDataBroker) {
        super(adapterContext, domDataBroker);
        final DOMDataTreeChangeService domTreeChange = domDataBroker.getExtensions()
                .getInstance(DOMDataTreeChangeService.class);
        treeChangeService = domTreeChange == null ? null
                : new BindingDOMDataTreeChangeServiceAdapter(adapterContext, domTreeChange);
    }

    @Override
    public ReadTransaction newReadOnlyTransaction() {
        return new BindingDOMReadTransactionAdapter(adapterContext(), getDelegate().newReadOnlyTransaction());
    }

    @Override
    public WriteTransaction newWriteOnlyTransaction() {
        return new BindingDOMWriteTransactionAdapter<>(adapterContext(), getDelegate().newWriteOnlyTransaction());
    }

    @Override
    public ReadWriteTransaction newReadWriteTransaction() {
        return new BindingDOMReadWriteTransactionAdapter(adapterContext(), getDelegate().newReadWriteTransaction());
    }

    @Override
    public TransactionChain createTransactionChain(final TransactionChainListener listener) {
        return new BindingDOMTransactionChainAdapter(getDelegate()::createTransactionChain, adapterContext(), listener);
    }

    @Override
    public TransactionChain createMergingTransactionChain(final TransactionChainListener listener) {
        return new BindingDOMTransactionChainAdapter(getDelegate()::createMergingTransactionChain, adapterContext(),
            listener);
    }

    private static class Builder extends BindingDOMAdapterBuilder<DataBroker> {
        Builder(final AdapterContext adapterContext) {
            super(adapterContext);
        }

        @Override
        public Set<? extends Class<? extends DOMService>> getRequiredDelegates() {
            return ImmutableSet.of(DOMDataBroker.class);
        }

        @Override
        protected DataBroker createInstance(final ClassToInstanceMap<DOMService> delegates) {
            return new BindingDOMDataBrokerAdapter(adapterContext(), delegates.getInstance(DOMDataBroker.class));
        }
    }

    @Override
    public @NonNull <T extends DataObject, L extends DataTreeChangeListener<T>> ListenerRegistration<L>
            registerDataTreeChangeListener(
            final DataTreeIdentifier<T> treeId, final L listener) {
        if (treeChangeService == null) {
            throw new UnsupportedOperationException("Underlying data broker does not expose DOMDataTreeChangeService.");
        }
        return treeChangeService.registerDataTreeChangeListener(treeId, listener);
    }


    @Override
    public @NonNull <T extends DataObject, L extends DataTreeChangeListener<T>> ListenerRegistration<L> registerDataTreeChangeListener(
            final @NonNull LogicalDatastoreType store, final @NonNull InstanceIdentifier<T> path,
            final @NonNull L listener) {
        if (treeChangeService == null) {
            throw new UnsupportedOperationException("Underlying data broker does not expose DOMDataTreeChangeService.");
        }
        return treeChangeService.registerDataTreeChangeListener(store, path, listener);
    }

    @Override
    public @NonNull <T extends DataObject, L extends DataTreeChangeListener<T>> ListenerRegistration<L> registerDataTreeChangeListener(
            final @NonNull LogicalDatastoreType store,final @NonNull InstanceWildcard<T> path,
            final @NonNull L listener) {
        if (treeChangeService == null) {
            throw new UnsupportedOperationException("Underlying data broker does not expose DOMDataTreeChangeService.");
        }
        return treeChangeService.registerDataTreeChangeListener(store, path, listener);
    }
}
