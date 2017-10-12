/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.data.tree;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeCommitCohort;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeModification;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.modification.LazyDataTreeModification;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.DataValidationFailedException;
import org.opendaylight.mdsal.common.api.MappingCheckedFuture;
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
        this.codec = Preconditions.checkNotNull(codec);
        this.cohort = Preconditions.checkNotNull(cohort);
    }

    @Nonnull
    @Override
    public CheckedFuture<PostCanCommitStep, DataValidationFailedException> canCommit(@Nonnull final Object txId,
            @Nonnull final DOMDataTreeCandidate candidate, @Nonnull final SchemaContext ctx) {
        final DataTreeModification<T> modification = LazyDataTreeModification.create(codec, candidate);
        final List<CheckedFuture<PostCanCommitStep, DataValidationFailedException>> futures = new ArrayList<>();
        final BiConsumer<DataValidationFailedException, PostCanCommitStep> biConsumer = (ex, cc) -> {
            final ListenableFuture<PostCanCommitStep> immediateFuture = Futures.immediateFuture(cc);
            futures.add(MappingCheckedFuture.create(immediateFuture,
                            new DataValidationFailedExceptionMapper("canCommit", candidate.getRootPath())));
        };
        final ListenableFuture<PostCanCommitStep> resultFuture =
                Futures.transform(Futures.allAsList(futures), input -> input.get(input.size() - 1),
                        MoreExecutors.directExecutor());
        cohort.canCommit(txId, modification, biConsumer);
        return MappingCheckedFuture.create(resultFuture,
                new DataValidationFailedExceptionMapper("canCommit", candidate.getRootPath()));
    }
}

