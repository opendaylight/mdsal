
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.mdsal.binding.api.DataTreeCommitCohort;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.DataValidationFailedException;
import org.opendaylight.mdsal.common.api.PostCanCommitStep;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCandidate;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCommitCohort;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

class BindingDOMDataTreeCommitCohortAdapter<T extends DataObject> implements DOMDataTreeCommitCohort {

    private final BindingToNormalizedNodeCodec codec;
    private final DataTreeCommitCohort<T> cohort;

    BindingDOMDataTreeCommitCohortAdapter(BindingToNormalizedNodeCodec codec, DataTreeCommitCohort<T> cohort) {
        this.codec = Preconditions.checkNotNull(codec);
        this.cohort = Preconditions.checkNotNull(cohort);
    }

    @Override
    public CheckedFuture<PostCanCommitStep, DataValidationFailedException> canCommit(Object txId,
            DOMDataTreeCandidate candidate, SchemaContext ctx) {
        DataTreeModification<T> modification = LazyDataTreeModification.create(codec, candidate);
        return cohort.canCommit(txId, modification);
    }
}
