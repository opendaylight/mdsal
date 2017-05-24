/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.yangtools.concepts.ObjectRegistration;

/**
 * Commit Cohort registry.
 *
 * <p>
 * See {@link DataTreeCommitCohort} for more details.
 *
 */
@Beta
public interface DataTreeCommitCohortRegistry {

    /**
     * Register commit cohort which will participate in three-phase commit protocols of write
     * transaction in data broker associated with this instance of extension.
     *
     * @param subtree Subtree path on which commit cohort operates.
     * @param cohort Commit cohort
     * @param <D> data type
     * @param <T> cohort type
     * @return Registration object for DOM Data Three Commit cohort.
     */
    <D extends TreeNode, T extends DataTreeCommitCohort<D>> ObjectRegistration<T> registerCommitCohort(
        DataTreeIdentifier<D> subtree, T cohort);
}
