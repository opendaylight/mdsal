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
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeCommitCohort;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeModification;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.modification.LazyDataTreeModification;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.PostCanCommitStep;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCandidate;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCommitCohort;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Data tree commit cohort adapter.
 *
 * @param <T>
 *            - {@link TreeNode} type
 */
@Beta
public class BindingDOMDataTreeCommitCohortAdapter<T extends TreeNode> implements DOMDataTreeCommitCohort {

    private final BindingToNormalizedNodeCodec codec;
    private final DataTreeCommitCohort<T> cohort;

    public BindingDOMDataTreeCommitCohortAdapter(final BindingToNormalizedNodeCodec codec,
            final DataTreeCommitCohort<T> cohort) {
        this.codec = requireNonNull(codec);
        this.cohort = requireNonNull(cohort);
    }

    @Nonnull
    @Override
    public FluentFuture<PostCanCommitStep> canCommit(Object txId, SchemaContext ctx,
            Collection<DOMDataTreeCandidate> candidates)  {
        final Collection<DataTreeModification<T>> modifications = candidates.stream().map(
            candidate -> LazyDataTreeModification.<T>create(codec, candidate)).collect(Collectors.toList());
        final SettableFuture<PostCanCommitStep> resultFuture = SettableFuture.create();

        cohort.canCommit(txId, modifications, (ex, pccs) -> {
            if (ex != null) {
                resultFuture.setException(ex);
            } else {
                resultFuture.set(pccs);
            }
        });

        return resultFuture;
    }
}

