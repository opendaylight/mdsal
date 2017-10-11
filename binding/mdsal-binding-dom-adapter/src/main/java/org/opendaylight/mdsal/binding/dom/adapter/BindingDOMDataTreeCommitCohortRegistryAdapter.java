/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.base.Preconditions;
import org.opendaylight.mdsal.binding.api.DataTreeCommitCohort;
import org.opendaylight.mdsal.binding.api.DataTreeCommitCohortRegistry;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCommitCohortRegistration;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCommitCohortRegistry;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;

public class BindingDOMDataTreeCommitCohortRegistryAdapter implements DataTreeCommitCohortRegistry {

    private final BindingToNormalizedNodeCodec codec;
    private final DOMDataTreeCommitCohortRegistry registry;

    BindingDOMDataTreeCommitCohortRegistryAdapter(final BindingToNormalizedNodeCodec codec,
            final DOMDataTreeCommitCohortRegistry registry) {
        this.codec = Preconditions.checkNotNull(codec);
        this.registry = Preconditions.checkNotNull(registry);
    }

    static DataTreeCommitCohortRegistry from(final BindingToNormalizedNodeCodec codec,
            final DOMDataTreeCommitCohortRegistry registry) {
        return new BindingDOMDataTreeCommitCohortRegistryAdapter(codec, registry);
    }

    @Override
    public <D extends DataObject, T extends DataTreeCommitCohort<D>> ObjectRegistration<T> registerCommitCohort(
            final DataTreeIdentifier<D> subtree, final T cohort) {
        final BindingDOMDataTreeCommitCohortAdapter<D> adapter =
                new BindingDOMDataTreeCommitCohortAdapter<>(codec, cohort);
        final DOMDataTreeIdentifier domPath = codec.toDOMDataTreeIdentifier(subtree);
        final DOMDataTreeCommitCohortRegistration<?> domReg = registry.registerCommitCohort(domPath, adapter);
        return new ObjectRegistration<T>() {

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
