/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.trace.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.Test;

public class CloseTrackedRegistryTest {

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
    public void testDuplicateAllocationContexts() {
        final CloseTrackedRegistry<SomethingClosable> registry =
                new CloseTrackedRegistry<>(this, "testDuplicateAllocationContexts", true);

        for (int i = 0; i < 100; i++) {
            SomethingClosable isClosedManyTimes = new SomethingClosable(registry);
            isClosedManyTimes.close();
            someOtherMethodWhichDoesNotClose(registry);
        }
        @SuppressWarnings({ "resource", "unused" })
        SomethingClosable forgotToCloseOnce = new SomethingClosable(registry);

        Set<CloseTrackedRegistryReportEntry<SomethingClosable>> uniqueNonClosed = registry.getAllUnique();
        assertThat(uniqueNonClosed, hasSize(2));
        assertContains(uniqueNonClosed, entry ->
            entry.getNumberAddedNotRemoved() == 100 || entry.getNumberAddedNotRemoved() == 1);
        uniqueNonClosed.forEach(entry -> {
            if (entry.getNumberAddedNotRemoved() == 100) {
                assertContains(entry.getStackTraceElements(),
                    element -> element.getMethodName().equals("someOtherMethodWhichDoesNotClose"));
            } else if (entry.getNumberAddedNotRemoved() == 1) {
                assertContains(entry.getStackTraceElements(),
                    element -> element.getMethodName().equals("testDuplicateAllocationContexts"));
            } else {
                fail("Unexpected number of added, not removed: " + entry.getNumberAddedNotRemoved());
            }
        });
    }

    // FIXME: use a Matcher
    private static <T> void assertContains(final Collection<T> collection, final Predicate<T> predicate) {
        assertTrue("Iterable did not contain any element matching predicate", collection.stream().anyMatch(predicate));
    }

    @SuppressWarnings({ "resource", "unused" })
    private static void someOtherMethodWhichDoesNotClose(final CloseTrackedRegistry<SomethingClosable> registry) {
        new SomethingClosable(registry);
    }

    @Test
    @SuppressWarnings({ "unused", "resource" })
    public void testDebugContextDisabled() {
        final CloseTrackedRegistry<SomethingClosable> debugContextDisabledRegistry =
                new CloseTrackedRegistry<>(this, "testDebugContextDisabled", false);

        SomethingClosable forgotToCloseOnce = new SomethingClosable(debugContextDisabledRegistry);

        Set<CloseTrackedRegistryReportEntry<SomethingClosable>>
            closeRegistryReport = debugContextDisabledRegistry.getAllUnique();
        assertThat(closeRegistryReport, hasSize(1));

        CloseTrackedRegistryReportEntry<SomethingClosable>
            closeRegistryReportEntry1 = closeRegistryReport.iterator().next();
        assertEquals(1, closeRegistryReportEntry1.getNumberAddedNotRemoved());
        assertEquals(List.of(), closeRegistryReportEntry1.getStackTraceElements());
    }
}
