/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;

abstract class AbstractRuntimeType implements RuntimeType {
    private final @NonNull GeneratedType bindingType;

    AbstractRuntimeType(final GeneratedType bindingType) {
        this.bindingType = requireNonNull(bindingType);
    }

    @Override
    public final GeneratedType bindingType() {
        return bindingType;
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("bindingType", bindingType).toString();
    }
}
