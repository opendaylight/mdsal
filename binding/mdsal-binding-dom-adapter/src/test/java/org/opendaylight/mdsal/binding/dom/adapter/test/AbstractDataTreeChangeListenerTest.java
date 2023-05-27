/*
 * Copyright (c) 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test;

import static org.junit.Assert.fail;

import com.google.common.util.concurrent.SettableFuture;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Abstract base that provides a DTCL for verification.
 *
 * @author Thomas Pantelis
 */
public class AbstractDataTreeChangeListenerTest extends AbstractConcurrentDataBrokerTest {
    @FunctionalInterface
    protected interface Matcher<T extends DataObject> {

        boolean apply(DataTreeModification<T> modification);
    }

    @FunctionalInterface
    protected interface DataMatcher<T extends DataObject> {

        boolean apply(T data);
    }

    protected static final class TestListener<T extends DataObject> implements DataTreeChangeListener<T> {
        private final SettableFuture<List<DataTreeModification<T>>> future = SettableFuture.create();
        private final List<DataTreeModification<T>> accumulatedChanges = new ArrayList<>();
        private final Deque<Matcher<T>> matchers;
        private final int expChangeCount;

        private TestListener(final List<Matcher<T>> matchers) {
            this.matchers = new ArrayDeque<>(matchers);
            expChangeCount = this.matchers.size();
        }

        @Override
        public void onDataTreeChanged(final Collection<DataTreeModification<T>> changes) {
            synchronized (accumulatedChanges) {
                accumulatedChanges.addAll(changes);
                if (expChangeCount == accumulatedChanges.size()) {
                    future.set(List.copyOf(accumulatedChanges));
                }
            }
        }

        public List<DataTreeModification<T>> changes() {
            try {
                final var changes = future.get(5, TimeUnit.SECONDS);
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                return changes;
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                throw new AssertionError(String.format(
                    "Data tree change notifications not received. Expected: %s. Actual: %s - %s",
                        expChangeCount, accumulatedChanges.size(), accumulatedChanges), e);
            }
        }

        public void verify() {
            final var changes = new ArrayDeque<>(changes());
            while (!changes.isEmpty()) {
                final var mod = changes.pop();
                final var matcher = matchers.peek();
                if (matcher == null || !matcher.apply(mod)) {
                    final var rootNode = mod.getRootNode();
                    fail("Received unexpected notification: type: %s, path: %s, before: %s, after: %s".formatted(
                        rootNode.getModificationType(), mod.getRootPath().getRootIdentifier(), rootNode.getDataBefore(),
                        rootNode.getDataAfter()));
                    return;
                }

                matchers.pop();
            }

            if (!matchers.isEmpty()) {
                fail("Unsatisfied matchers " + matchers);
            }
        }

        public boolean hasChanges() {
            synchronized (accumulatedChanges) {
                return !accumulatedChanges.isEmpty();
            }
        }
    }

    protected AbstractDataTreeChangeListenerTest() {
        super(true);
    }

    @SafeVarargs
    protected final <T extends DataObject> TestListener<T> createListener(final LogicalDatastoreType store,
            final InstanceIdentifier<T> path, final Matcher<T>... matchers) {
        final var listener = new TestListener<>(Arrays.asList(matchers));
        getDataBroker().registerDataTreeChangeListener(DataTreeIdentifier.create(store, path), listener);
        return listener;
    }

    public static <T extends DataObject> Matcher<T> match(final ModificationType type, final InstanceIdentifier<T> path,
            final DataMatcher<T> checkDataBefore, final DataMatcher<T> checkDataAfter) {
        return modification -> type == modification.getRootNode().getModificationType()
                && path.equals(modification.getRootPath().getRootIdentifier())
                && checkDataBefore.apply(modification.getRootNode().getDataBefore())
                && checkDataAfter.apply(modification.getRootNode().getDataAfter());
    }

    public static <T extends DataObject> Matcher<T> match(final ModificationType type, final InstanceIdentifier<T> path,
            final T expDataBefore, final T expDataAfter) {
        return match(type, path, dataBefore -> Objects.equals(expDataBefore, dataBefore),
            (DataMatcher<T>) dataAfter -> Objects.equals(expDataAfter, dataAfter));
    }

    public static <T extends DataObject> Matcher<T> added(final InstanceIdentifier<T> path, final T data) {
        return match(ModificationType.WRITE, path, null, data);
    }

    public static <T extends DataObject> Matcher<T> replaced(final InstanceIdentifier<T> path, final T dataBefore,
            final T dataAfter) {
        return match(ModificationType.WRITE, path, dataBefore, dataAfter);
    }

    public static <T extends DataObject> Matcher<T> deleted(final InstanceIdentifier<T> path, final T dataBefore) {
        return match(ModificationType.DELETE, path, dataBefore, null);
    }

    public static <T extends DataObject> Matcher<T> subtreeModified(final InstanceIdentifier<T> path,
            final T dataBefore, final T dataAfter) {
        return match(ModificationType.SUBTREE_MODIFIED, path, dataBefore, dataAfter);
    }
}
