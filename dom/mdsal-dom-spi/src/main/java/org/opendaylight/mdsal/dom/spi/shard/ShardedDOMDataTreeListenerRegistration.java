/*
 * Copyright (c) 2017 Pantheon Technologies, s.ro. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.shard;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Map;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeListeningException;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

final class ShardedDOMDataTreeListenerRegistration<T extends DOMDataTreeListener>
        extends AbstractListenerRegistration<T> implements DOMDataTreeListener {
    @GuardedBy("this")
    private final ListenerRegistration<?> localReg;
    @GuardedBy("this")
    private SchemaContext schemaContext;
    @GuardedBy("this")
    private Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> localSubtrees;

    ShardedDOMDataTreeListenerRegistration(final DOMDataTreeListenerRegistry localRegistry,
            final SchemaContext schemaContext, final T userListener,
            final Collection<DOMDataTreeIdentifier> localSubtrees, final boolean allowRxMerges) {
        super(userListener);
        this.schemaContext = requireNonNull(schemaContext);
        this.localReg = localRegistry.registerListener(this, localSubtrees, allowRxMerges);
    }

    synchronized void updateSchemaContext(final SchemaContext schemaContext) {
        this.schemaContext = requireNonNull(schemaContext);
    }

    @Override
    public synchronized void onDataTreeChanged(final Collection<DataTreeCandidate> changes,
            final Map<DOMDataTreeIdentifier, NormalizedNode<?, ?>> subtrees) {
        getInstance().onDataTreeChanged(changes, subtrees);
        this.localSubtrees = ImmutableMap.copyOf(subtrees);
    }

    @Override
    public synchronized void onDataTreeFailed(final Collection<DOMDataTreeListeningException> causes) {
        localReg.close();
        getInstance().onDataTreeFailed(causes);
    }

    @Override
    protected synchronized void removeRegistration() {
        localReg.close();
        // FIXME: upcall to RegistrationTree to remove the registration
    }
}