/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.dom.api.DOMTransactionChainListener;
import org.opendaylight.mdsal.dom.spi.ForwardingDOMDataBroker;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * An implementation of a {@link DOMDataBroker} which throws clearer errors than
 * the typical NPE when there are no schemas; this is useful at the very least
 * in development, if not also sometimes in production.
 *
 * @author Michael Vorburger.ch
 */
final class CheckingDOMDataBroker extends ForwardingDOMDataBroker implements DOMDataTreeChangeService {

    // intentionally just package local for now; move somewhere shared, some time later.

    private final DOMDataBroker delegate;
    private final DOMSchemaService schemaService;

    CheckingDOMDataBroker(DOMDataBroker delegate, DOMSchemaService schemaService) {
        this.delegate = requireNonNull(delegate, "delegate");
        this.schemaService = requireNonNull(schemaService, "schemaService");
    }

    @Override
    protected DOMDataBroker delegate() {
        return delegate;
    }

    protected void check() {
        SchemaContext globalContext = schemaService.getGlobalContext();
        if (globalContext == null) {
            throw new IllegalStateException(
                    "There currently is *NO* SchemaContext; this is most likely a bug in your configuration; "
                            + "schemaService = " + schemaService);
        }
        if (globalContext.getModules().isEmpty()) {
            throw new IllegalStateException(
                    "There are currently *NO* YANG modules available; this is most likely a bug in your configuration; "
                            + "schemaService = " + schemaService);
        }
    }

    @Override
    public DOMDataTreeReadWriteTransaction newReadWriteTransaction() {
        check();
        return super.newReadWriteTransaction();
    }

    @Override
    public DOMDataTreeReadTransaction newReadOnlyTransaction() {
        check();
        return super.newReadOnlyTransaction();
    }

    @Override
    public DOMDataTreeWriteTransaction newWriteOnlyTransaction() {
        check();
        return super.newWriteOnlyTransaction();
    }

    @Override
    public DOMTransactionChain createTransactionChain(DOMTransactionChainListener listener) {
        check();
        return super.createTransactionChain(listener);
    }

    @Override // copy/paste from PingPongDataBroker
    public <L extends DOMDataTreeChangeListener> ListenerRegistration<L>
            registerDataTreeChangeListener(DOMDataTreeIdentifier treeId, L listener) {
        DOMDataTreeChangeService treeService = delegate.getExtensions().getInstance(DOMDataTreeChangeService.class);
        if (treeService != null) {
            return treeService.registerDataTreeChangeListener(treeId, listener);
        }

        throw new UnsupportedOperationException("Delegate " + delegate + " does not support required functionality");
    }
}
