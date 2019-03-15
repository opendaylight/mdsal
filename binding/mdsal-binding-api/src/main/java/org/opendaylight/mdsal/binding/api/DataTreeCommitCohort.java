/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.FluentFuture;
import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.DataValidationFailedException;
import org.opendaylight.mdsal.common.api.PostCanCommitStep;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Commit cohort participating in commit of data modification, which can validate data tree
 * modifications, with option to reject supplied modification, and with callbacks describing state
 * of commit.
 *
 * <h2>Performance implications</h2>
 *
 * {@link DataTreeCommitCohort}s are hooked up into commit of data tree changes and MAY
 * negatively affect performance of data broker / store.
 *
 * <p>
 * Implementations of this interface are discouraged, unless you really need ability to veto data
 * tree changes, or to provide external state change in sync with visibility of commited data.
 *
 * <h2>Implementation requirements</h2>
 *
 * <h3>Correctness assumptions</h3> Implementation SHOULD use only provided
 * {@link DataTreeModification} for validation purposes.
 *
 * <p>
 * Use of any other external mutable state is discouraged, implementation MUST NOT use any
 * transaction related APIs on same data broker / data store instance during invocation of
 * callbacks, except ones provided as argument. Note that this MAY BE enforced by some
 * implementations of {@link DataBroker} or Commit coordinator
 *
 * <p>
 * Note that this may be enforced by some implementations of {@link DataTreeCommitCohortRegistry}
 * and such calls may fail.
 *
 * <h3>DataTreeCandidate assumptions</h3> Implementation SHOULD NOT make any assumptions on
 * {@link DataTreeModification} being successfully committed until associated
 * {@link PostCanCommitStep#preCommit()} and
 * {@link org.opendaylight.mdsal.common.api.PostPreCommitStep#commit()} callback was invoked.
 *
 * <h2>Usage patterns</h2>
 *
 * <h3>Data Tree Validator</h3>
 *
 * <p>
 * Validator is implementation, which only validates {@link DataTreeModification} and does not
 * retain any state derived from edited data - does not care if {@link DataTreeModification} was
 * rejected afterwards or transaction was cancelled.
 *
 * <p>
 * Implementation may opt-out from receiving {@code preCommit()}, {@code commit()}, {@code abort()}
 * callbacks by returning {@link PostCanCommitStep#NOOP}.
 *
 * <p>
 * TODO: Provide example and describe more usage patterns
 *
 * @author Tony Tkacik &lt;ttkacik@cisco.com&gt;
 */
@Beta
public interface DataTreeCommitCohort<T extends DataObject> {
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
     * implementations of {@link DataTreeCommitCohortRegistry} and such calls may fail.
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
     * @param modifications the {@link DataTreeModification}s to be validated and committed.
     * @return a FluentFuture which will successfully complete with the user-supplied implementation of
     *         {@link PostCanCommitStep} if all candidates are valid, or a failed future with a
     *         {@link DataValidationFailedException} if and only if a provided
     *         {@link DataTreeModification} instance did not pass validation. Users are encouraged to use
     *         more specific subclasses of this exception to provide additional information about
     *         validation failure reason.
     */
    @CheckReturnValue
    @NonNull FluentFuture<PostCanCommitStep> canCommit(@NonNull Object txId,
            @NonNull Collection<DataTreeModification<T>> modifications);
}
