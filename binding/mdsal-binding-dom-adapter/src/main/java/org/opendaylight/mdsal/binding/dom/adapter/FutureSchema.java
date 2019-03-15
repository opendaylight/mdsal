/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.SettableFuture;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.QNameModule;

abstract class FutureSchema implements AutoCloseable {
    private static final class Waiting extends FutureSchema {
        Waiting(final long time, final TimeUnit unit) {
            super(time, unit);
        }
    }

    private static final class NonWaiting extends FutureSchema {
        NonWaiting(final long time, final TimeUnit unit) {
            super(time, unit);
        }

        @Override
        boolean addPostponedOpAndWait(final FutureSchemaPredicate postponedOp) {
            return false;
        }
    }

    private abstract class FutureSchemaPredicate implements Predicate<BindingRuntimeContext> {
        private final SettableFuture<Void> schemaPromise = SettableFuture.create();

        final boolean waitForSchema() {
            try {
                schemaPromise.get(FutureSchema.this.duration, FutureSchema.this.unit);
                return true;
            } catch (final InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } catch (final TimeoutException e) {
                return false;
            } finally {
                synchronized (FutureSchema.this.postponedOperations) {
                    FutureSchema.this.postponedOperations.remove(this);
                }
            }
        }

        final void unlockIfPossible(final BindingRuntimeContext context) {
            if (!schemaPromise.isDone() && test(context)) {
                schemaPromise.set(null);
            }
        }

        final void cancel() {
            schemaPromise.cancel(true);
        }
    }

    @GuardedBy("postponedOperations")
    private final Set<FutureSchemaPredicate> postponedOperations = new LinkedHashSet<>();
    private final long duration;
    private final TimeUnit unit;

    private volatile BindingRuntimeContext runtimeContext;

    FutureSchema(final long time, final TimeUnit unit) {
        this.duration = time;
        this.unit = requireNonNull(unit);
    }

    static FutureSchema create(final long time, final TimeUnit unit, final boolean waitEnabled) {
        return waitEnabled ? new Waiting(time, unit) : new NonWaiting(time, unit);
    }

    BindingRuntimeContext runtimeContext() {
        final BindingRuntimeContext localRuntimeContext = runtimeContext;
        if (localRuntimeContext != null) {
            return localRuntimeContext;
        }

        if (waitForSchema(Collections.emptyList())) {
            return runtimeContext;
        }

        throw new IllegalStateException("No SchemaContext is available");
    }

    void onRuntimeContextUpdated(final BindingRuntimeContext context) {
        synchronized (postponedOperations) {
            runtimeContext = context;
            for (final FutureSchemaPredicate op : postponedOperations) {
                op.unlockIfPossible(context);
            }
        }
    }

    long getDuration() {
        return duration;
    }

    TimeUnit getUnit() {
        return unit;
    }

    @Override
    public void close() {
        synchronized (postponedOperations) {
            postponedOperations.forEach(FutureSchemaPredicate::cancel);
        }
    }

    boolean waitForSchema(final QNameModule module) {
        return addPostponedOpAndWait(new FutureSchemaPredicate() {
            @Override
            public boolean test(final BindingRuntimeContext input) {
                return input.getSchemaContext().findModule(module).isPresent();
            }
        });
    }

    boolean waitForSchema(final Collection<Class<?>> bindingClasses) {
        return addPostponedOpAndWait(new FutureSchemaPredicate() {
            @Override
            public boolean test(final BindingRuntimeContext context) {
                return bindingClasses.stream().allMatch(clz -> {
                    if (Augmentation.class.isAssignableFrom(clz)) {
                        return context.getAugmentationDefinition(clz) != null;
                    }

                    return context.getSchemaDefinition(clz) != null;
                });
            }
        });
    }

    boolean addPostponedOpAndWait(final FutureSchemaPredicate postponedOp) {
        synchronized (postponedOperations) {
            postponedOperations.add(postponedOp);

            // If the runtimeContext changed, this op may now be satisfied so check it.
            final BindingRuntimeContext context = runtimeContext;
            if (context != null) {
                postponedOp.unlockIfPossible(context);
            }
        }

        return postponedOp.waitForSchema();
    }
}
