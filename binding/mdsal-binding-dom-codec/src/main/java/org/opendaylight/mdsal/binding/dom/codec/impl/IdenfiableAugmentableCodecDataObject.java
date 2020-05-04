/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

/**
 * A base class for {@link DataObject}s which are also {@link Augmentable} and {@link Identifiable}, backed by
 * {@link DataObjectCodecContext}. While this class is public, it not part of API surface and is an implementation
 * detail. The only reason for it being public is that it needs to be accessible by code generated at runtime.
 *
 * @param <I> Identifier type
 * @param <T> DataObject type
 */
public abstract class IdenfiableAugmentableCodecDataObject<I extends Identifier<T>,
        T extends DataObject & Augmentable<T> & Identifiable<I>>
        extends AugmentableCodecDataObject<T> implements Identifiable<I> {

    private static final VarHandle CACHED_KEY;

    static {
        try {
            CACHED_KEY = MethodHandles.lookup().findVarHandle(IdenfiableAugmentableCodecDataObject.class,
                "cachedKey", Identifier.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // Used via VarHandle
    @SuppressWarnings("unused")
    private volatile Identifier<?> cachedKey;

    protected IdenfiableAugmentableCodecDataObject(final DataObjectCodecContext<T, ?> context,
            final NormalizedNodeContainer<?, ?, ?> data) {
        super(context, data);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final I key() {
        return (I) codecKey(CACHED_KEY);
    }
}
