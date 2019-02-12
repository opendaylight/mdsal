/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.data.tree;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeCommitCohort;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeCommitCohortRegistry;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCommitCohortRegistration;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCommitCohortRegistry;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.concepts.ObjectRegistration;

/**
 * Data tree commit cohort registry adapter.
 */
@Beta
public final class BindingDOMDataTreeCommitCohortRegistryAdapter implements DataTreeCommitCohortRegistry {

    private final BindingToNormalizedNodeCodec codec;
    private final DOMDataTreeCommitCohortRegistry registry;

    private BindingDOMDataTreeCommitCohortRegistryAdapter(final BindingToNormalizedNodeCodec codec,
            final DOMDataTreeCommitCohortRegistry registry) {
        this.codec = requireNonNull(codec);
        this.registry = requireNonNull(registry);
    }

    /**
     * Create instance of registry of commit cohort.
     *
     * @param codec
     *            - codec for serialize/deserialize
     * @param registry
     *            - dom registry
     * @return binding registry
     */
    public static DataTreeCommitCohortRegistry from(final BindingToNormalizedNodeCodec codec,
            final DOMDataTreeCommitCohortRegistry registry) {
        return new BindingDOMDataTreeCommitCohortRegistryAdapter(codec, registry);
    }

    @Override
    public <D extends TreeNode, T extends DataTreeCommitCohort<D>> ObjectRegistration<T>
            registerCommitCohort(final DataTreeIdentifier<D> subtree, final T cohort) {
        final BindingDOMDataTreeCommitCohortAdapter<D> adapter =
                new BindingDOMDataTreeCommitCohortAdapter<>(codec, cohort);
        final DOMDataTreeIdentifier domPath = codec.toDOMDataTreeIdentifier(subtree);
        final DOMDataTreeCommitCohortRegistration<?> domReg = registry.registerCommitCohort(domPath, adapter);
        return new ObjectRegistration<T>() {

            @Nonnull
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
