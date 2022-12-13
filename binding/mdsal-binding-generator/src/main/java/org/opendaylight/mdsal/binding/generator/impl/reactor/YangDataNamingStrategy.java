/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.AbstractQName;

/**
 * Naming strategy for {@code ietf-restconf:yang-data} template which has a generic string not matching YANG identifier.
 */
@NonNullByDefault
final class YangDataNamingStrategy extends ClassNamingStrategy {
    private final String javaIdentifier;

    YangDataNamingStrategy(final String templateName) {
        javaIdentifier = BindingMapping.mapYangDataName(templateName);
    }

    @Override
    AbstractQName nodeIdentifier() {
        // This should never be reached
        throw new UnsupportedOperationException();
    }

    @Override
    String simpleClassName() {
        return javaIdentifier;
    }

    @Override
    @Nullable ClassNamingStrategy fallback() {
        // javaIdentifier is guaranteed to be unique, there is no need for fallback
        return null;
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("javaIdentifier", javaIdentifier);
    }
}
