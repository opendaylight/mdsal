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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
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
    protected static final class TestListener<T extends DataObject> implements DataTreeChangeListener<T> {
        private final SettableFuture<List<DataTreeModification<T>>> future = SettableFuture.create();
        private final List<DataTreeModification<T>> accumulatedChanges = new ArrayList<>();
        private final Function<DataTreeModification<T>, Boolean>[] matchers;
        private final int expChangeCount;

        private TestListener(final Function<DataTreeModification<T>, Boolean>[] matchers) {
            expChangeCount = matchers.length;
            this.matchers = matchers;
        }

        @Override
        public void onDataTreeChanged(final List<DataTreeModification<T>> changes) {
            synchronized (accumulatedChanges) {
                accumulatedChanges.addAll(changes);
                if (expChangeCount == accumulatedChanges.size()) {
                    future.set(new ArrayList<>(accumulatedChanges));
                }
            }
        }

        @Override
        public void onInitialData() {
            // No-op
        }

        public List<DataTreeModification<T>> changes() {
            try {
                final List<DataTreeModification<T>> changes = future.get(5, TimeUnit.SECONDS);
                Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                return changes;
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                throw new AssertionError(String.format(
                    "Data tree change notifications not received. Expected: %s. Actual: %s - %s",
                        expChangeCount, accumulatedChanges.size(), accumulatedChanges), e);
            }
        }

        public void verify() {
            List<DataTreeModification<T>> changes = new ArrayList<>(changes());
            Iterator<DataTreeModification<T>> iter = changes.iterator();
            while (iter.hasNext()) {
                DataTreeModification<T> dataTreeModification = iter.next();
                for (Function<DataTreeModification<T>, Boolean> matcher: matchers) {
                    if (matcher.apply(dataTreeModification)) {
                        iter.remove();
                        break;
                    }
                }
            }

            if (!changes.isEmpty()) {
                DataTreeModification<T> mod = changes.iterator().next();
                fail(String.format("Received unexpected notification: type: %s, path: %s, before: %s, after: %s",
                        mod.getRootNode().getModificationType(), mod.getRootPath().getRootIdentifier(),
                        mod.getRootNode().getDataBefore(), mod.getRootNode().getDataAfter()));
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
            final InstanceIdentifier<T> path, final Function<DataTreeModification<T>, Boolean>... matchers) {
        TestListener<T> listener = new TestListener<>(matchers);
        getDataBroker().registerDataTreeChangeListener(DataTreeIdentifier.create(store, path), listener);
        return listener;
    }

    public static <T extends DataObject> Function<DataTreeModification<T>, Boolean> match(
            final ModificationType type, final InstanceIdentifier<T> path, final Function<T, Boolean> checkDataBefore,
            final Function<T, Boolean> checkDataAfter) {
        return modification -> type == modification.getRootNode().getModificationType()
                && path.equals(modification.getRootPath().getRootIdentifier())
                && checkDataBefore.apply(modification.getRootNode().getDataBefore())
                && checkDataAfter.apply(modification.getRootNode().getDataAfter());
    }

    public static <T extends DataObject> Function<DataTreeModification<T>, Boolean> match(final ModificationType type,
            final InstanceIdentifier<T> path, final T expDataBefore, final T expDataAfter) {
        return match(type, path, dataBefore -> Objects.equals(expDataBefore, dataBefore),
            (Function<T, Boolean>) dataAfter -> Objects.equals(expDataAfter, dataAfter));
    }

    public static <T extends DataObject> Function<DataTreeModification<T>, Boolean> added(
            final InstanceIdentifier<T> path, final T data) {
        return match(ModificationType.WRITE, path, null, data);
    }

    public static <T extends DataObject> Function<DataTreeModification<T>, Boolean> replaced(
            final InstanceIdentifier<T> path, final T dataBefore, final T dataAfter) {
        return match(ModificationType.WRITE, path, dataBefore, dataAfter);
    }

    public static <T extends DataObject> Function<DataTreeModification<T>, Boolean> deleted(
            final InstanceIdentifier<T> path, final T dataBefore) {
        return match(ModificationType.DELETE, path, dataBefore, null);
    }

    public static <T extends DataObject> Function<DataTreeModification<T>, Boolean> subtreeModified(
            final InstanceIdentifier<T> path, final T dataBefore, final T dataAfter) {
        return match(ModificationType.SUBTREE_MODIFIED, path, dataBefore, dataAfter);
    }
}
