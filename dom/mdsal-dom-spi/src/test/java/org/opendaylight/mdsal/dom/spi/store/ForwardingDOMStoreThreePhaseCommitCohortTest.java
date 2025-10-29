/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ForwardingDOMStoreThreePhaseCommitCohortTest {
    @Mock
    private DOMStoreThreePhaseCommitCohort delegate;

    private ForwardingDOMStoreThreePhaseCommitCohort cohort;

    @BeforeEach
    void beforeEach() {
        cohort = new ForwardingDOMStoreThreePhaseCommitCohort() {
            @Override
            protected DOMStoreThreePhaseCommitCohort delegate() {
                return delegate;
            }
        };
    }

    @Test
    void basicTest() {
        doReturn(null).when(delegate).canCommit();
        assertNotNull(cohort.canCommit());
        verify(delegate).canCommit();

        doReturn(null).when(delegate).preCommit();
        assertNotNull(cohort.preCommit());
        verify(delegate).preCommit();

        doReturn(null).when(delegate).commit();
        assertNotNull(cohort.commit());
        verify(delegate).commit();

        doReturn(null).when(delegate).abort();
        assertNotNull(cohort.abort());
        verify(delegate).abort();
    }
}