/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.AbstractQName;

/**
 * Bijective naming strategy backed by {@link BindingMapping#createUniqueJavaIdentifer(AbstractQName)}.
 */
@NonNullByDefault
final class BijectiveNamingStrategy extends ForwardingClassNamingStrategy {
    BijectiveNamingStrategy(final ClassNamingStrategy delegate) {
        super(delegate);
    }

    @Override
    String simpleClassName() {
        return BindingMapping.createUniqueJavaIdentifer(nodeIdentifier());
    }

    @Override
    @NonNull ClassNamingStrategy fallback() {
        // We may have a conflict between statements (i.e. grouping vs. container vs. typedef)
        return new BijectiveWithNamespaceNamingStrategy(this);
    }
}
