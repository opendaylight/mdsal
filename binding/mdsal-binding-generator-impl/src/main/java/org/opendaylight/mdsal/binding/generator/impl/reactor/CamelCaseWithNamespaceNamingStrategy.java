/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
final class CamelCaseWithNamespaceNamingStrategy extends NamingStrategy {
    private final CamelCaseNamingStrategy delegate;

    CamelCaseWithNamespaceNamingStrategy(final CamelCaseNamingStrategy delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    String simpleClassName() {
        return delegate.namespace().appendSuffix(delegate.simpleClassName());
    }

    @Override
    String packageNameSegment() {
        return delegate.namespace().appendSuffix(delegate.packageNameSegment());
    }

    @Override
    @Nullable NamingStrategy fallback() {
        // FIXME: MDSAL-503: add a BijectiveNamingStrategy
        //        The algorithm needs to essentially fall back to using escape-based translation scheme, where each
        //        localName results in a unique name, while not conflicting with any possible preferredName. The exact
        //        mechanics for that are TBD. A requirement for that mapping is that it must not rely on definition
        //        order.
        //
        //        But there is another possible step: since we are assigning 14 different statements into the default
        //        namespace (which did not add a suffix), we can try to assign a statement-derived suffix. To make
        //        things easier, we use two-characters: AC, AD, AU, AX, CA, CH, CO, IP, LE, LI, LL, NO, OP, RP.
        return null;
    }
}
