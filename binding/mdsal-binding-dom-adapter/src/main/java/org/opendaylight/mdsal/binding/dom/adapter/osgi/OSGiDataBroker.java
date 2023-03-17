/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import com.google.common.annotations.Beta;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.InstanceIdentifier;
import org.opendaylight.mdsal.binding.api.InstanceWildcard;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.binding.api.TransactionChainListener;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Beta
@Component(factory = OSGiDataBroker.FACTORY_NAME)
public final class OSGiDataBroker extends AbstractAdaptedService<DataBroker> implements DataBroker {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME = "org.opendaylight.mdsal.binding.dom.adapter.osgi.OSGiDataBroker";

    public OSGiDataBroker() {
        super(DataBroker.class);
    }

    @Override
    public ReadTransaction newReadOnlyTransaction() {
        return delegate().newReadOnlyTransaction();
    }

    @Override
    public ReadWriteTransaction newReadWriteTransaction() {
        return delegate().newReadWriteTransaction();
    }

    @Override
    public WriteTransaction newWriteOnlyTransaction() {
        return delegate().newWriteOnlyTransaction();
    }

    @Override
    public <T extends DataObject, L extends DataTreeChangeListener<T>>
            ListenerRegistration<L> registerDataTreeChangeListener(final DataTreeIdentifier<T> treeId,
                    final L listener) {
        return delegate().registerDataTreeChangeListener(treeId, listener);
    }

    @Override
    public @NonNull <T extends DataObject, L extends DataTreeChangeListener<T>> ListenerRegistration<L> registerDataTreeChangeListener(
            @NonNull LogicalDatastoreType store, @NonNull InstanceIdentifier<T> path,
            @NonNull L listener) {
        return delegate().registerDataTreeChangeListener(store, path, listener);
    }

    @Override
    public @NonNull <T extends DataObject, L extends DataTreeChangeListener<T>> ListenerRegistration<L> registerDataTreeChangeListener(
            @NonNull LogicalDatastoreType store, @NonNull InstanceWildcard<T> path,
            @NonNull L listener) {
        return delegate().registerDataTreeChangeListener(store, path, listener);
    }

    @Override
    public TransactionChain createTransactionChain(@NonNull final TransactionChainListener listener) {
        return delegate().createTransactionChain(listener);
    }

    @Override
    public TransactionChain createMergingTransactionChain(final TransactionChainListener listener) {
        return delegate().createMergingTransactionChain(listener);
    }

    @Activate
    void activate(final Map<String, ?> properties) {
        start(properties);
    }

    @Deactivate
    void deactivate(final int reason) {
        stop(reason);
    }
}
