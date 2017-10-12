/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.common.api.DataValidationFailedException;
import org.opendaylight.mdsal.common.api.MappingCheckedFuture;
import org.opendaylight.mdsal.common.api.PostCanCommitStep;
import org.opendaylight.yangtools.util.concurrent.ExceptionMapper;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.LoggerFactory;

/**
 * Commit cohort participating in commit of data modification, which can validate data tree
 * modifications, with option to reject supplied modification, and with callbacks describing state
 * of commit.
 *
 * <h2>Performance implications</h2>
 * {@link DOMDataTreeCommitCohort}s are hooked up into commit of data tree changes and MAY
 * negatively affect performance of data broker / store.
 * Implementations of this interface are discouraged, unless you really need ability to veto data
 * tree changes, or to provide external state change in sync with visibility of committed data.
 *
 * <h2>Implementation requirements</h2>
 * <h3>Correctness assumptions</h3> Implementation SHOULD use only the {@link DOMDataTreeCandidate} instances and
 * provided {@link SchemaContext} for validation purposes.
 * Use of any other external mutable state is discouraged, implementation MUST NOT use any
 * transaction related APIs on same data broker / data store instance during invocation of
 * callbacks, except ones provided as argument. Note that this MAY BE enforced by some
 * implementations of {@link DOMDataBroker} or DOMDataCommitCoordinator
 * Note that this may be enforced by some implementations of {@link DOMDataTreeCommitCohortRegistry}
 * and such calls may fail.
 * <h3>Correct model usage</h3> If implementation is performing YANG-model driven validation
 * implementation SHOULD use provided schema context.
 * Any other instance of {@link SchemaContext} obtained by other means, may not be valid for the
 * associated DOMDataTreeCandidates and it may lead to incorrect validation or processing of provided
 * data.
 * <h3>DataTreeCandidate assumptions</h3> Implementation SHOULD NOT make any assumptions on a
 * {@link DOMDataTreeCandidate} being successfully committed until associated
 * {@link PostCanCommitStep#preCommit()} and
 * {@link org.opendaylight.mdsal.common.api.PostPreCommitStep#commit()} callback was invoked.
 * <h2>Usage patterns</h2>
 * <h3>Data Tree Validator</h3>
 * Validator is implementation, which only validates {@link DOMDataTreeCandidate} instances and does not
 * retain any state derived from edited data - does not care if a {@link DOMDataTreeCandidate} was
 * rejected afterwards or transaction was cancelled.
 * Implementation may opt-out from receiving {@code preCommit()}, {@code commit()}, {@code abort()}
 * callbacks by returning {@link PostCanCommitStep#NOOP}.
 *
 * <p>
 * @author Tony Tkacik
 */
// TODO: Provide example and describe more usage patterns
@Beta
public interface DOMDataTreeCommitCohort {

    /**
     * DO NOT implement or invoke this method. It is deprecated in favor of
     * {@link #canCommit(Object, Collection, SchemaContext)} and only exists for backwards compatibility. The
     * default implementation returns {@link PostCanCommitStep#NOOP_SUCCESS_FUTURE} and is invoked by the
     * default implementation of {@link #canCommit(Object, Collection, SchemaContext)}.
     *
     * @deprecated Implement and invoke {@link #canCommit(Object, Collection, SchemaContext)} instead.
     */
    @Deprecated
    @Nonnull
    default CheckedFuture<PostCanCommitStep, DataValidationFailedException> canCommit(@Nonnull final Object txId,
            @Nonnull final DOMDataTreeCandidate candidate, @Nonnull final SchemaContext ctx) {
        LoggerFactory.getLogger(getClass()).error(
                "The default implementation of DOMDataTreeCommitCohort#canCommit(Object, DOMDataTreeCandidate, "
                + "SchemaContext) was invoked on {}", getClass());
        return PostCanCommitStep.NOOP_SUCCESS_FUTURE;
    }

    /**
     * Validates supplied data tree candidates and associates cohort-specific steps with data broker
     * transaction.
     * If {@link DataValidationFailedException} is thrown by implementation, commit of supplied data
     * will be prevented, with the DataBroker transaction providing the thrown exception as the
     * cause of failure.
     * Note the implementations are expected to do validation and processing asynchronous.
     * Implementations SHOULD do processing fast, and are discouraged SHOULD NOT block on any
     * external resources.
     * Implementation MUST NOT access any data transaction related APIs during invocation of
     * callback. Note that this may be enforced by some implementations of
     * {@link DOMDataTreeCommitCohortRegistry} and such calls may fail.
     * Implementation MAY opt-out from implementing other steps by returning
     * {@link PostCanCommitStep#NOOP}. Otherwise implementation MUST return instance of
     * {@link PostCanCommitStep}, which will be used to invoke
     * {@link org.opendaylight.mdsal.common.api.PostPreCommitStep#commit()} or
     * {@link PostCanCommitStep#abort()} based on accepting data by data broker and or other commit
     * cohorts.
     *
     * @param txId Transaction identifier. SHOULD be used only for reporting and correlation.
     *        Implementation MUST NOT use {@code txId} for validation.
     * @param candidates Data Tree candidates to be validated and committed.
     * @param ctx Schema Context to which Data Tree candidate should conform.
     * @return Checked future which will successfully complete with the user-supplied implementation of
     *         {@link PostCanCommitStep} if all candidates are valid, or a failed checked future with a
     *         {@link DataValidationFailedException} if and only if a provided
     *         {@link DOMDataTreeCandidate} instance did not pass validation. Users are encouraged to use
     *         more specific subclasses of this exception to provide additional information about
     *         validation failure reason.
     */
    @Nonnull
    default CheckedFuture<PostCanCommitStep, DataValidationFailedException> canCommit(@Nonnull final Object txId,
            @Nonnull final Collection<DOMDataTreeCandidate> candidates, @Nonnull final SchemaContext ctx) {
        LoggerFactory.getLogger(getClass()).warn("DOMDataTreeCommitCohort implementation {} should override "
                + "canCommit(Object, Collection, SchemaContext)", getClass());

        // For backwards compatibility, the default implementation is to invoke the deprecated
        // canCommit(Object, DOMDataTreeCandidate, SchemaContext) method for each DOMDataTreeCandidate and return the
        // last PostCanCommitStep.
        List<ListenableFuture<PostCanCommitStep>> futures = new ArrayList<>();
        for (DOMDataTreeCandidate candidate : candidates) {
            futures.add(canCommit(txId, candidate, ctx));
        }

        final ListenableFuture<PostCanCommitStep> resultFuture = Futures.transform(Futures.allAsList(futures),
            input -> input.get(input.size() - 1), MoreExecutors.directExecutor());
        return MappingCheckedFuture.create(resultFuture, new DataValidationFailedExceptionMapper("canCommit",
                Iterables.getLast(candidates).getRootPath()));
    }

    /**
     * An ExceptionMapper that translates an Exception to a DataValidationFailedException.
     */
    class DataValidationFailedExceptionMapper extends ExceptionMapper<DataValidationFailedException> {
        private final DOMDataTreeIdentifier failedTreeId;

        public DataValidationFailedExceptionMapper(final String opName, final DOMDataTreeIdentifier failedTreeId) {
            super(opName, DataValidationFailedException.class);
            this.failedTreeId = failedTreeId;
        }

        @Override
        protected DataValidationFailedException newWithCause(final String message, final Throwable cause) {
            return new DataValidationFailedException(DOMDataTreeIdentifier.class, failedTreeId, message, cause);
        }
    }
}
