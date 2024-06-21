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
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataBroker.CommitCohortExtension;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.concepts.Registration;

final class BindingDOMDataTreeCommitCohortRegistryAdapter
        extends AbstractBindingAdapter<CommitCohortExtension> implements DataTreeCommitCohortRegistry {
    BindingDOMDataTreeCommitCohortRegistryAdapter(final AdapterContext codec, final CommitCohortExtension extension) {
        super(codec, extension);
    }

    @Override
    public <D extends DataObject> Registration registerCommitCohort(final DataTreeIdentifier<D> subtree,
            final DataTreeCommitCohort<D> cohort) {
        final var target = subtree.path().getTargetType();
        final var adapter = new BindingDOMDataTreeCommitCohortAdapter<>(adapterContext(), cohort,
            Augmentation.class.isAssignableFrom(target) ? target : null);
        final var domPath = currentSerializer().toDOMDataTreeIdentifier(subtree);
        return getDelegate().registerCommitCohort(domPath, adapter);
    }
}
