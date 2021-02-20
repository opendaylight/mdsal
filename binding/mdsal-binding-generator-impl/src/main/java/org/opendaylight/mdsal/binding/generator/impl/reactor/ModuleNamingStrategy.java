/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.AbstractQName;

@NonNullByDefault
final class ModuleNamingStrategy extends NamingStrategy {
    private final AbstractQName name;

    ModuleNamingStrategy(final AbstractQName name) {
        this.name = requireNonNull(name);
    }

    @Override
    String simpleClassName() {
        return BindingMapping.getClassName(name.getLocalName());
    }

    @Override
    String packageNameSegment() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Nullable NamingStrategy fallback() {
        return null;
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("localName", name.getLocalName());
    }
}
