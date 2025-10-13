/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.trace.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import org.junit.jupiter.api.Test;

class CloseTrackedRegistryTest {
    private static class SomethingClosable extends AbstractCloseTracked<SomethingClosable> implements AutoCloseable {
        SomethingClosable(final CloseTrackedRegistry<SomethingClosable> transactionChainRegistry) {
            super(transactionChainRegistry);
        }

        @Override
        public void close() {
            removeFromTrackedRegistry();
        }
    }

    @Test
    void testDuplicateAllocationContexts() {
        final var registry = new CloseTrackedRegistry<SomethingClosable>(this, "testDuplicateAllocationContexts", true);

        for (int i = 0; i < 100; i++) {
            final var isClosedManyTimes = new SomethingClosable(registry);
            isClosedManyTimes.close();
            someOtherMethodWhichDoesNotClose(registry);
        }
        @SuppressWarnings({ "resource", "unused" })
        final var forgotToCloseOnce = new SomethingClosable(registry);

        final var uniqueNonClosed = registry.getAllUnique();
        assertThat(uniqueNonClosed).hasSize(2).anyMatch(entry ->
            entry.getNumberAddedNotRemoved() == 100 || entry.getNumberAddedNotRemoved() == 1);
        uniqueNonClosed.forEach(entry -> {
            if (entry.getNumberAddedNotRemoved() == 100) {
                assertThat(entry.getStackTraceElements()).anyMatch(
                    element -> element.getMethodName().equals("someOtherMethodWhichDoesNotClose"));
            } else if (entry.getNumberAddedNotRemoved() == 1) {
                assertThat(entry.getStackTraceElements()).anyMatch(
                    element -> element.getMethodName().equals("testDuplicateAllocationContexts"));
            } else {
                fail("Unexpected number of added, not removed: " + entry.getNumberAddedNotRemoved());
            }
        });
    }

    @SuppressWarnings({ "resource", "unused" })
    private static void someOtherMethodWhichDoesNotClose(final CloseTrackedRegistry<SomethingClosable> registry) {
        new SomethingClosable(registry);
    }

    @Test
    void testDebugContextDisabled() {
        final var debugContextDisabledRegistry = new CloseTrackedRegistry<SomethingClosable>(this,
            "testDebugContextDisabled", false);

        @SuppressWarnings({ "unused", "resource" })
        final var forgotToCloseOnce = new SomethingClosable(debugContextDisabledRegistry);

        final var closeRegistryReport = debugContextDisabledRegistry.getAllUnique();
        assertThat(closeRegistryReport).hasSize(1);

        final var closeRegistryReportEntry1 = closeRegistryReport.iterator().next();
        assertEquals(1, closeRegistryReportEntry1.getNumberAddedNotRemoved());
        assertEquals(List.of(), closeRegistryReportEntry1.getStackTraceElements());
    }
}
