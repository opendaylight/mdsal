/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.mdsal.binding.api;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.mdsal.common.api.DataValidationFailedException;
import org.opendaylight.mdsal.common.api.PostCanCommitStep;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * **
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
 * <h3>Correctness assumptions</h3> Implementation SHOULD use only provided
 * {@link DataTreeModification} for validation purposes.
 *
 * Use of any other external mutable state is discouraged, implementation MUST NOT use any
 * transaction related APIs on same data broker / data store instance during invocation of
 * callbacks, except ones provided as argument. Note that this MAY BE enforced by some
 * implementations of {@link DataBroker} or Commit coordinator
 *
 * Note that this may be enforced by some implementations of {@link DOMDataTreeCommitCohortRegistry}
 * and such calls may fail.
 *
 * <h3>DataTreeCandidate assumptions</h3> Implementation SHOULD NOT make any assumptions on
 * {@link DataTreeModification} being successfully committed until associated
 * {@link PostCanCommitStep#preCommit()} and
 * {@link org.opendaylight.mdsal.common.api.PostPreCommitStep#commit()} callback was invoked.
 *
 *
 * <h2>Usage patterns</h2>
 *
 * <h3>Data Tree Validator</h3>
 *
 * Validator is implementation, which only validates {@link DataTreeModification} and does not
 * retain any state derived from edited data - does not care if {@link DataDataTreeModification} was
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
public interface DataTreeCommitCohort<T extends DataObject> {

    CheckedFuture<PostCanCommitStep, DataValidationFailedException> canCommit(Object txId,
            DataTreeModification<T> modification);

}
