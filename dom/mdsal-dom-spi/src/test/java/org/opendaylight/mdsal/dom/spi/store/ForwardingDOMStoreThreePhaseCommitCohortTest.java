/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Test;
import org.mockito.Mock;

public class ForwardingDOMStoreThreePhaseCommitCohortTest extends ForwardingDOMStoreThreePhaseCommitCohort {

    @Mock(name = "domStoreThreePhaseCommitCohort")
    private DOMStoreThreePhaseCommitCohort domStoreThreePhaseCommitCohort;

    @Test
    public void basicTest() throws Exception {
        initMocks(this);

        doReturn(null).when(domStoreThreePhaseCommitCohort).canCommit();
        this.canCommit();
        verify(domStoreThreePhaseCommitCohort).canCommit();

        doReturn(null).when(domStoreThreePhaseCommitCohort).preCommit();
        this.preCommit();
        verify(domStoreThreePhaseCommitCohort).preCommit();

        doReturn(null).when(domStoreThreePhaseCommitCohort).commit();
        this.commit();
        verify(domStoreThreePhaseCommitCohort).commit();

        doReturn(null).when(domStoreThreePhaseCommitCohort).abort();
        this.abort();
        verify(domStoreThreePhaseCommitCohort).abort();
    }

    @Override
    protected DOMStoreThreePhaseCommitCohort delegate() {
        return domStoreThreePhaseCommitCohort;
    }
}