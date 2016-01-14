/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.CheckedFuture;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.common.api.DataValidationFailedException;
import org.opendaylight.mdsal.common.api.PostCanCommitStep;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 *
 * Commit cohort participating in commit of data modification, which can validate data tree
 * modifications, with option to reject supplied modification, and with callbacks describing state
 * of commit.
 *
 * <h2>Performance implications</h2>
 *
 * {@link DOMDataTreeCommitCohort}s are hooked up into commit of data tree changes and MAY
 * negatively affect performance of data broker / store.
 *
 * Implementations of this interface are discouraged, unless you really need ability to veto data
 * tree changes, or to provide external state change in sync with visibility of commited data.
 *
 *
 * <h2>Implementation requirements</h2>
 *
 * <h3>Correctness assumptions</h3> Implementation SHOULD use only {@link DOMDataTreeCandidate} and
 * provided {@link SchemaContext} for validation purposes.
 *
 * Use of any other external mutable state is discouraged, implementation MUST NOT use any
 * transaction related APIs on same data broker / data store instance during invocation of
 * callbacks, except ones provided as argument. Note that this MAY BE enforced by some
 * implementations of {@link DOMDataBroker} or DOMDataCommitCoordinator
 *
 * Note that this may be enforced by some implementations of {@link DOMDataTreeCommitCohortRegistry}
 * and such calls may fail.
 *
 * <h3>Correct model usage</h3> If implementation is performing YANG-model driven validation
 * implementation SHOULD use provided schema context.
 *
 * Any other instance of {@link SchemaContext} obtained by other means, may not be valid for
 * associated DOMDataTreeCandidate and it may lead to incorrect validation or processing of provided
 * data.
 *
 * <h3>DataTreeCandidate assumptions</h3> Implementation SHOULD NOT make any assumptions on
 * {@link DOMDataTreeCandidate} being successfully committed until associated
 * {@link PostCanCommitStep#preCommit()} and
 * {@link org.opendaylight.mdsal.common.api.PostPreCommitStep#commit()} callback was invoked.
 *
 *
 * <h2>Usage patterns</h2>
 *
 * <h3>Data Tree Validator</h3>
 *
 * Validator is implementation, which only validates {@link DOMDataTreeCandidate} and does not
 * retain any state derived from edited data - does not care if {@link DOMDataTreeCandidate} was
 * rejected afterwards or transaction was cancelled.
 *
 * Implementation may opt-out from receiving {@code preCommit()}, {@code commit()}, {@code abort()}
 * callbacks by returning {@link PostCanCommitStep#NOOP}.
 *
 * TODO: Provide example and describe more usage patterns
 *
 * @author Tony Tkacik &lt;ttkacik@cisco.com&gt;
 *
 */
@Beta
public interface DOMDataTreeCommitCohort {

    /**
     * Validates supplied data tree candidate and associates cohort-specific steps with data broker
     * transaction.
     *
     * If {@link DataValidationFailedException} is thrown by implementation, commit of supplied data
     * will be prevented, with the DataBroker transaction providing the thrown exception as the
     * cause of failure.
     *
     * Note the implementations are expected to do validation and processing asynchronous.
     *
     * Implementations SHOULD do processing fast, and are discouraged SHOULD NOT block on any
     * external resources.
     *
     * Implementation MUST NOT access any data transaction related APIs during invocation of
     * callback. Note that this may be enforced by some implementations of
     * {@link DOMDataTreeCommitCohortRegistry} and such calls may fail.
     *
     * Implementation MAY opt-out from implementing other steps by returning
     * {@link PostCanCommitStep#NOOP}. Otherwise implementation MUST return instance of
     * {@link PostCanCommitStep}, which will be used to invoke
     * {@link org.opendaylight.mdsal.common.api.PostPreCommitStep#commit()} or
     * {@link PostCanCommitStep#abort()} based on accepting data by data broker and or other commit
     * cohorts.
     *
     * @param txId Transaction identifier. SHOULD be used only for reporting and correlation.
     *        Implementation MUST NOT use {@code txId} for validation.
     * @param candidate Data Tree candidate to be validated and committed.
     * @param ctx Schema Context to which Data Tree candidate should conform.
     * @return Checked future which will successfully complete with user-supplied implementation of
     *         {@link PostCanCommitStep} if data are valid, or failed check future with
     *         {@link DataValidationFailedException} if and only if provided
     *         {@link DOMDataTreeCandidate} did not pass validation. Users are encouraged to use
     *         more specific subclasses of this exception to provide additional information about
     *         validation failure reason.
     */
    @Nonnull
    CheckedFuture<PostCanCommitStep, DataValidationFailedException> canCommit(@Nonnull Object txId,
            @Nonnull DOMDataTreeCandidate candidate, @Nonnull SchemaContext ctx);
}
