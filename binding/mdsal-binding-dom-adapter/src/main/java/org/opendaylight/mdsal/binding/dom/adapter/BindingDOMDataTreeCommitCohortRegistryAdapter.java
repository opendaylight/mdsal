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
import org.opendaylight.mdsal.dom.api.DOMDataTreeCommitCohortRegistry;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;

final class BindingDOMDataTreeCommitCohortRegistryAdapter
        extends AbstractBindingAdapter<DOMDataTreeCommitCohortRegistry> implements DataTreeCommitCohortRegistry {
    BindingDOMDataTreeCommitCohortRegistryAdapter(final AdapterContext codec,
            final DOMDataTreeCommitCohortRegistry registry) {
        super(codec, registry);
    }

    @Override
    public <D extends DataObject, T extends DataTreeCommitCohort<D>> ObjectRegistration<T> registerCommitCohort(
            final DataTreeIdentifier<D> subtree, final T cohort) {
        final var target = subtree.path().getTargetType();
        final var adapter = new BindingDOMDataTreeCommitCohortAdapter<>(adapterContext(), cohort,
            Augmentation.class.isAssignableFrom(target) ? target : null);
        final var domPath = currentSerializer().toDOMDataTreeIdentifier(subtree);
        final var domReg = getDelegate().registerCommitCohort(domPath, adapter);
        return new ObjectRegistration<>() {
            @Override
            public T getInstance() {
                return cohort;
            }

            @Override
            public void close() {
                domReg.close();
            }
        };
    }
}
