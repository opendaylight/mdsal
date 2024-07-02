/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;

/**
 * Commit Cohort registry. See {@link DataTreeCommitCohort} for more details.
 */
public interface DataTreeCommitCohortRegistry {
    /**
     * Register commit cohort which will participate in three-phase commit protocols of write transaction in data broker
     * associated with this instance of extension.
     *
     * @param datastore datastore to attach to
     * @param subtree a {@link DataObjectReference} on which commit cohort operates
     * @param cohort Commit cohort
     * @return Registration object for DOM Data Three Commit cohort.
     */
    <D extends DataObject> @NonNull Registration registerCommitCohort(@NonNull LogicalDatastoreType datastore,
        @NonNull DataObjectReference<D> subtree, @NonNull DataTreeCommitCohort<D> cohort);

    /**
     * Register commit cohort which will participate in three-phase commit protocols of write transaction in data broker
     * associated with this instance of extension.
     *
     * @param subtree Subtree path on which commit cohort operates.
     * @param cohort Commit cohort
     * @return Registration object for DOM Data Three Commit cohort.
     * @deprecated Use {@link #registerCommitCohort(LogicalDatastoreType, DataObjectReference, DataTreeCommitCohort)}
     *             instead.
     */
    @Deprecated(since = "14.0.0", forRemoval = true)
    default <D extends DataObject> @NonNull Registration registerCommitCohort(
            final @NonNull DataTreeMatch<D> subtree, final @NonNull DataTreeCommitCohort<D> cohort) {
        return registerCommitCohort(subtree.datastore(), subtree.path().toReference(), cohort);
    }
}
