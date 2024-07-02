/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import org.opendaylight.mdsal.binding.api.DataTreeCommitCohort;
import org.opendaylight.mdsal.binding.api.DataTreeCommitCohortRegistry;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker.CommitCohortExtension;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;

final class BindingDOMDataTreeCommitCohortRegistryAdapter
        extends AbstractBindingAdapter<CommitCohortExtension> implements DataTreeCommitCohortRegistry {
    BindingDOMDataTreeCommitCohortRegistryAdapter(final AdapterContext codec, final CommitCohortExtension extension) {
        super(codec, extension);
    }

    @Override
    public <D extends DataObject> Registration registerCommitCohort(final LogicalDatastoreType datastore,
            final DataObjectReference<D> subtree, final DataTreeCommitCohort<D> cohort) {
        final var target = subtree.lastStep().type();
        return getDelegate().registerCommitCohort(
            DOMDataTreeIdentifier.of(datastore, currentSerializer().toYangInstanceIdentifier(subtree)),
            new BindingDOMDataTreeCommitCohortAdapter<>(adapterContext(), cohort,
                Augmentation.class.isAssignableFrom(target) ? target : null));
    }
}
