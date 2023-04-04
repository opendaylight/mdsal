/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FluentFuture;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.DataValidationFailedException;
import org.opendaylight.mdsal.common.api.PostCanCommitStep;
import org.opendaylight.yangtools.util.concurrent.ExceptionMapper;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

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
 * provided {@link EffectiveModelContext} for validation purposes.
 * Use of any other external mutable state is discouraged, implementation MUST NOT use any
 * transaction related APIs on same data broker / data store instance during invocation of
 * callbacks, except ones provided as argument. Note that this MAY BE enforced by some
 * implementations of {@link DOMDataBroker} or DOMDataCommitCoordinator
 * Note that this may be enforced by some implementations of {@link DOMDataTreeCommitCohortRegistry}
 * and such calls may fail.
 * <h3>Correct model usage</h3> If implementation is performing YANG-model driven validation
 * implementation SHOULD use provided schema context.
 * Any other instance of {@link EffectiveModelContext} obtained by other means, may not be valid for the
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
 * @author Tony Tkacik
 */
// TODO: Provide example and describe more usage patterns
@Beta
public interface DOMDataTreeCommitCohort {

    /**
     * Validates the supplied data tree modifications and associates the cohort-specific steps with data broker
     * transaction.
     *
     * <p>
     * If {@link DataValidationFailedException} is thrown by implementation, the commit of the supplied data
     * will be prevented, with the DataBroker transaction providing the thrown exception as the cause of failure.
     *
     * <p>
     * Note the implementations are expected to do validation and processing asynchronous. Implementations SHOULD do
     * processing fast, and are discouraged from blocking on any external resources. Implementation MUST NOT access
     * any data transaction related APIs during invocation of the callback. Note that this may be enforced by some
     * implementations of {@link DOMDataTreeCommitCohortRegistry} and such calls may fail.
     *
     * <p>
     * Implementation MAY opt-out from implementing other steps by returning
     * {@link PostCanCommitStep#NOOP}. Otherwise implementation MUST return instance of
     * {@link PostCanCommitStep}, which will be used to invoke
     * {@link org.opendaylight.mdsal.common.api.PostPreCommitStep#commit()} or
     * {@link PostCanCommitStep#abort()} based on accepting data by data broker and or other commit cohorts.
     *
     * @param txId Transaction identifier. SHOULD be used only for reporting and correlation.
     *        Implementation MUST NOT use {@code txId} for validation.
     * @param candidates Data Tree candidates to be validated and committed.
     * @param ctx Schema Context to which Data Tree candidate should conform.
     * @return a FluentFuture which will successfully complete with the user-supplied implementation of
     *         {@link PostCanCommitStep} if all candidates are valid, or a failed future with a
     *         {@link DataValidationFailedException} if and only if a provided
     *         {@link DOMDataTreeCandidate} instance did not pass validation. Users are encouraged to use
     *         more specific subclasses of this exception to provide additional information about
     *         validation failure reason.
     */
    @NonNull FluentFuture<PostCanCommitStep> canCommit(@NonNull Object txId,
            @NonNull EffectiveModelContext ctx, @NonNull Collection<DOMDataTreeCandidate> candidates);

    /**
     * An ExceptionMapper that translates an Exception to a DataValidationFailedException.
     */
    class DataValidationFailedExceptionMapper extends ExceptionMapper<DataValidationFailedException> {
        private final @NonNull DOMDataTreeIdentifier failedTreeId;

        public DataValidationFailedExceptionMapper(final String opName, final DOMDataTreeIdentifier failedTreeId) {
            super(opName, DataValidationFailedException.class);
            this.failedTreeId = requireNonNull(failedTreeId);
        }

        @Override
        protected DataValidationFailedException newWithCause(final String message, final Throwable cause) {
            return new DataValidationFailedException(DOMDataTreeIdentifier.class, failedTreeId, message, cause);
        }
    }
}
