/*
 * Copyright (c) 2018 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.fail;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.concepts.Registration;
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

    protected static final class ModificationCollector<T extends DataObject> implements AutoCloseable {
        private final TestListener<T> listener;
        private final Registration reg;

        private ModificationCollector(final TestListener<T> listener, final Registration reg) {
            this.listener = requireNonNull(listener);
            this.reg = requireNonNull(reg);
        }

        @SafeVarargs
        public final void verifyModifications(final Matcher<T>... inOrder) {
            final var matchers = new ArrayDeque<>(Arrays.asList(inOrder));
            final var changes = listener.awaitChanges(matchers.size());

            while (!changes.isEmpty()) {
                final var mod = changes.pop();
                final var matcher = matchers.pop();
                if (!matcher.apply(mod)) {
                    final var rootNode = mod.getRootNode();
                    fail("Received unexpected notification: type: %s, path: %s, before: %s, after: %s".formatted(
                        rootNode.getModificationType(), mod.getRootPath().path(), rootNode.getDataBefore(),
                        rootNode.getDataAfter()));
                    return;
                }
            }

            final var count = listener.changeCount();
            if (count != 0) {
                throw new AssertionError("Expected no more changes, %s remain".formatted(count));
            }
        }

        @Override
        public void close() {
            reg.close();
        }
    }

    private static final class TestListener<T extends DataObject> implements DataTreeChangeListener<T> {
        private final Deque<DataTreeModification<T>> accumulatedChanges = new ArrayDeque<>();

        private boolean synced;

        @Override
        public synchronized void onDataTreeChanged(final List<DataTreeModification<T>> changes) {
            accumulatedChanges.addAll(changes);
            synced = true;
        }

        @Override
        public synchronized void onInitialData() {
            synced = true;
        }

        void awaitSync() {
            final var sw = Stopwatch.createStarted();

            do {
                synchronized (this) {
                    if (synced) {
                        return;
                    }
                }

                Uninterruptibles.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
            } while (sw.elapsed(TimeUnit.SECONDS) < 5);

            throw new AssertionError("Failed to achieve initial sync");
        }

        Deque<DataTreeModification<T>> awaitChanges(final int expectedCount) {
            final var ret = new ArrayDeque<DataTreeModification<T>>(expectedCount);
            final var sw = Stopwatch.createStarted();
            int remaining = expectedCount;

            do {
                synchronized (this) {
                    while (remaining != 0) {
                        final var change = accumulatedChanges.poll();
                        if (change == null) {
                            break;
                        }

                        remaining--;
                        ret.add(change);
                    }
                }

                if (remaining == 0) {
                    return ret;
                }
                Uninterruptibles.sleepUninterruptibly(100, TimeUnit.MILLISECONDS);
            } while (sw.elapsed(TimeUnit.SECONDS) < 5);

            throw new AssertionError("Expected %s changes, received only %s".formatted(expectedCount, ret.size()));
        }

        synchronized int changeCount() {
            return accumulatedChanges.size();
        }
    }

    protected AbstractDataTreeChangeListenerTest() {
        super(true);
    }

    protected final <T extends DataObject> @NonNull ModificationCollector<T> createCollector(
            final LogicalDatastoreType store, final InstanceIdentifier<T> path) {
        final var listener = new TestListener<T>();
        final var reg = getDataBroker().registerDataTreeChangeListener(DataTreeIdentifier.of(store, path), listener);
        listener.awaitSync();
        return new ModificationCollector<>(listener, reg);
    }

    public static <T extends DataObject> @NonNull Matcher<T> match(final ModificationType type,
            final InstanceIdentifier<T> path, final DataMatcher<T> checkDataBefore,
            final DataMatcher<T> checkDataAfter) {
        return modification -> type == modification.getRootNode().getModificationType()
                && path.equals(modification.getRootPath().path())
                && checkDataBefore.apply(modification.getRootNode().getDataBefore())
                && checkDataAfter.apply(modification.getRootNode().getDataAfter());
    }

    public static <T extends DataObject> @NonNull Matcher<T> match(final ModificationType type,
            final InstanceIdentifier<T> path, final T expDataBefore, final T expDataAfter) {
        return match(type, path, dataBefore -> Objects.equals(expDataBefore, dataBefore),
            (DataMatcher<T>) dataAfter -> Objects.equals(expDataAfter, dataAfter));
    }

    public static <T extends DataObject> @NonNull Matcher<T> added(final InstanceIdentifier<T> path, final T data) {
        return match(ModificationType.WRITE, path, null, data);
    }

    public static <T extends DataObject> @NonNull Matcher<T> replaced(final InstanceIdentifier<T> path,
            final T dataBefore, final T dataAfter) {
        return match(ModificationType.WRITE, path, dataBefore, dataAfter);
    }

    public static <T extends DataObject> @NonNull Matcher<T> deleted(final InstanceIdentifier<T> path,
            final T dataBefore) {
        return match(ModificationType.DELETE, path, dataBefore, null);
    }

    public static <T extends DataObject> @NonNull Matcher<T> subtreeModified(final InstanceIdentifier<T> path,
            final T dataBefore, final T dataAfter) {
        return match(ModificationType.SUBTREE_MODIFIED, path, dataBefore, dataAfter);
    }
}
