/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import org.opendaylight.mdsal.binding.javav2.runtime.context.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.BaseIdentity;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Codec for serialize/deserialize identity.
 */
@Beta
public final class IdentityCodec implements Codec<QName, Class<?>> {

    private final BindingRuntimeContext context;

    /**
     * Prepared binding runtime context for identity codec.
     *
     * @param context
     *            - binding runtime context
     */
    public IdentityCodec(final BindingRuntimeContext context) {
        this.context = requireNonNull(context);
    }

    @Override
    public Class<?> deserialize(final QName input) {
        Preconditions.checkArgument(input != null, "Input must not be null.");
        return context.getIdentityClass(input);
    }

    @Override
    public QName serialize(final Class<?> input) {
        Preconditions.checkArgument(BaseIdentity.class.isAssignableFrom(input));
        return BindingReflections.findQName(input);
    }
}
