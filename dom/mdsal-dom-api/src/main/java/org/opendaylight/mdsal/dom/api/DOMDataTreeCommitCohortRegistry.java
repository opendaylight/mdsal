/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.dom.api;

/**
 *
 * Commit Cohort registry for {@link DOMDataTreeWriteTransaction}.
 *
 * See {@link DOMDataTreeCommitCohort} for more details.
 *
 * @author Tony Tkacik &lt;ttkacik@cisco.com&gt;
 *
 */
public interface DOMDataTreeCommitCohortRegistry extends DOMDataBrokerExtension {

    /**
     * Register commit cohort which will participate in three-phase commit protocols of
     * {@link DOMDataTreeWriteTransaction}in data broker associated with this instance of extension.
     *
     * @param path Subtree path on which commit cohort operates.
     * @param cohort Commit cohort
     * @param <T> Cohort subclass
     * @return Registaration object for DOM Data Three Commit cohort.
     */
    <T extends DOMDataTreeCommitCohort> DOMDataTreeCommitCohortRegistration<T> registerCommitCohort(
            DOMDataTreeIdentifier path, T cohort);
}