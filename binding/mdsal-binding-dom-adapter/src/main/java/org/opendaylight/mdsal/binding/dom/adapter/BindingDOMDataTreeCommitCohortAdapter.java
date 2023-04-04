/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.util.concurrent.FluentFuture;
import java.util.Collection;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.binding.api.DataTreeCommitCohort;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.PostCanCommitStep;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCandidate;
import org.opendaylight.mdsal.dom.api.DOMDataTreeCommitCohort;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

final class BindingDOMDataTreeCommitCohortAdapter<T extends DataObject>
        extends AbstractBindingAdapter<DataTreeCommitCohort<T>> implements DOMDataTreeCommitCohort {
    BindingDOMDataTreeCommitCohortAdapter(final AdapterContext codec, final DataTreeCommitCohort<T> cohort) {
        super(codec, cohort);
    }

    @Override
    public FluentFuture<PostCanCommitStep> canCommit(final Object txId,
            final EffectiveModelContext ctx, final Collection<DOMDataTreeCandidate> candidates) {
        final Collection<DataTreeModification<T>> modifications = candidates.stream()
                .map(candidate -> LazyDataTreeModification.<T>create(currentSerializer(), candidate))
                .collect(Collectors.toList());
        return getDelegate().canCommit(txId, modifications);
    }
}
