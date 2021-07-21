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
import org.opendaylight.yangtools.yang.common.AbstractQName;

/**
 * A naming strategy which is forwarding some requests to a delegate.
 */
@NonNullByDefault
abstract sealed class ForwardingClassNamingStrategy extends ClassNamingStrategy permits BijectiveNamingStrategy,
    BijectiveWithNamespaceNamingStrategy, CamelCaseWithNamespaceNamingStrategy {
    private final ClassNamingStrategy delegate;

    ForwardingClassNamingStrategy(final ClassNamingStrategy delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    final AbstractQName nodeIdentifier() {
        return delegate.nodeIdentifier();
    }

    @Override
    final StatementNamespace namespace() {
        return delegate.namespace();
    }

    final ClassNamingStrategy delegate() {
        return delegate;
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("delegate", delegate);
    }
}
