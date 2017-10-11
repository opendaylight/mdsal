/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl;

import com.google.common.annotations.Beta;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.mdsal.binding.javav2.runtime.context.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.yangtools.yang.common.QNameModule;

@Beta
class FutureSchema implements AutoCloseable {

    private final List<FutureSchemaPredicate> postponedOperations = new CopyOnWriteArrayList<>();
    private final SettableFuture<Void> schemaPromise = SettableFuture.create();
    private final long duration;
    private final TimeUnit unit;

    protected FutureSchema(final long time, final TimeUnit unit) {
        this.duration = time;
        this.unit = unit;
    }

    void onRuntimeContextUpdated(final BindingRuntimeContext context) {
        for (final FutureSchemaPredicate op : postponedOperations) {
            op.unlockIfPossible(context);
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
        for (final FutureSchemaPredicate op : postponedOperations) {
            op.cancel();
        }
    }

    private static boolean isSchemaAvailable(final Class<?> clz, final BindingRuntimeContext context) {
        final Object schema;
        if (Augmentation.class.isAssignableFrom(clz)) {
            schema = context.getAugmentationDefinition(clz);
        } else {
            schema = context.getSchemaDefinition(clz);
        }
        return schema != null;
    }

    boolean waitForSchema(final QNameModule module) {
        final FutureSchemaPredicate postponedOp = new FutureSchemaPredicate() {

            @Override
            public boolean apply(final BindingRuntimeContext input) {
                return input.getSchemaContext().findModule(module).isPresent();
            }
        };
        return postponedOp.waitForSchema();
    }

    boolean waitForSchema(final Collection<Class<?>> bindingClasses) {
        final FutureSchemaPredicate postponedOp = new FutureSchemaPredicate() {

            @Override
            public boolean apply(final BindingRuntimeContext context) {
                for (final Class<?> clz : bindingClasses) {
                    if (!isSchemaAvailable(clz, context)) {
                        return false;
                    }
                }
                return true;
            }
        };
        return postponedOp.waitForSchema();
    }

    private abstract class FutureSchemaPredicate implements Predicate<BindingRuntimeContext> {

        final boolean waitForSchema() {
            try {
                schemaPromise.get(duration, unit);
                return true;
            } catch (final InterruptedException | ExecutionException e) {
                throw Throwables.propagate(e);
            } catch (final TimeoutException e) {
                return false;
            } finally {
                postponedOperations.remove(this);
            }
        }

        final void unlockIfPossible(final BindingRuntimeContext context) {
            if (!schemaPromise.isDone() && apply(context)) {
                schemaPromise.set(null);
            }
        }

        final void cancel() {
            schemaPromise.cancel(true);
        }
    }

}
