/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.binding.DataObjectStep;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

abstract sealed class CommonDataObjectCodecPrototype<T extends CompositeRuntimeType> implements CodecContextSupplier
        permits AugmentationCodecPrototype, DataObjectCodecPrototype {
    private static final VarHandle INSTANCE;

    static {
        try {
            INSTANCE = MethodHandles.lookup().findVarHandle(CommonDataObjectCodecPrototype.class,
                "instance", CommonDataObjectCodecContext.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull T type;
    private final @NonNull CodecContextFactory factory;
    private final @NonNull DataObjectStep<?> step;

    // Accessed via INSTANCE
    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile CommonDataObjectCodecContext<?, T> instance;

    CommonDataObjectCodecPrototype(final DataObjectStep<?> step, final T type, final CodecContextFactory factory) {
        this.step = requireNonNull(step);
        this.type = requireNonNull(type);
        this.factory = requireNonNull(factory);
    }

    final @NonNull T getType() {
        return type;
    }

    final @NonNull CodecContextFactory getFactory() {
        return factory;
    }

    final @NonNull Class<?> getBindingClass() {
        return step.type();
    }

    final @NonNull DataObjectStep<?> getBindingArg() {
        return step;
    }

    abstract @NonNull NodeIdentifier getYangArg();

    @Override
    public final CommonDataObjectCodecContext<?, T> get() {
        final var existing = (CommonDataObjectCodecContext<?, T>) INSTANCE.getAcquire(this);
        return existing != null ? existing : loadInstance();
    }

    private @NonNull CommonDataObjectCodecContext<?, T> loadInstance() {
        final var tmp = createInstance();
        final var witness = (CommonDataObjectCodecContext<?, T>) INSTANCE.compareAndExchangeRelease(this, null, tmp);
        return witness == null ? tmp : witness;
    }

    // This method must allow concurrent loading, i.e. nothing in it may have effects outside of the loaded object
    abstract @NonNull CommonDataObjectCodecContext<?, T> createInstance();
}
