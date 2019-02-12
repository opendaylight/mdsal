/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.value;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;

@Beta
public abstract class ReflectionBasedCodec extends ValueTypeCodec {

    private final Class<?> typeClass;

    protected ReflectionBasedCodec(final Class<?> typeClass) {
        this.typeClass = requireNonNull(typeClass);
    }

    protected final Class<?> getTypeClass() {
        return typeClass;
    }
}