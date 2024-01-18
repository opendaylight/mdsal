/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@NonNullByDefault
@Component(factory = OSGiDataBroker.FACTORY_NAME)
public final class OSGiDataBroker extends AbstractAdaptedService<DataBroker> implements DataBroker {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME = "org.opendaylight.mdsal.binding.dom.adapter.osgi.OSGiDataBroker";

    @Activate
    public OSGiDataBroker(final Map<String, ?> properties) {
        super(DataBroker.class, properties);
    }

    @Deactivate
    void deactivate(final int reason) {
        stop(reason);
    }

    @Override
    public ReadTransaction newReadOnlyTransaction() {
        return delegate.newReadOnlyTransaction();
    }

    @Override
    public ReadWriteTransaction newReadWriteTransaction() {
        return delegate.newReadWriteTransaction();
    }

    @Override
    public WriteTransaction newWriteOnlyTransaction() {
        return delegate.newWriteOnlyTransaction();
    }

    @Override
    public TransactionChain createTransactionChain() {
        return delegate.createTransactionChain();
    }

    @Override
    public TransactionChain createMergingTransactionChain() {
        return delegate.createMergingTransactionChain();
    }

    @Override
    public <T extends DataObject> Registration registerTreeChangeListener(final DataTreeIdentifier<T> treeId,
            final DataTreeChangeListener<T> listener) {
        return delegate.registerTreeChangeListener(treeId, listener);
    }

    @Override
    @Deprecated(since = "13.0.0", forRemoval = true)
    public <T extends DataObject> Registration registerLegacyTreeChangeListener(final DataTreeIdentifier<T> treeId,
            final DataTreeChangeListener<T> listener) {
        return delegate.registerLegacyTreeChangeListener(treeId, listener);
    }
}
