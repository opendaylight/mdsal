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

/**
 * Secondary naming strategy: we use {@link StatementNamespace#appendSuffix(String)} on top of CamelCaseNamingStrategy.
 */

@NonNullByDefault
final class CamelCaseWithNamespaceNamingStrategy extends ForwardingClassNamingStrategy {
    CamelCaseWithNamespaceNamingStrategy(final CamelCaseNamingStrategy delegate) {
        super(delegate);
    }

    @Override
    String simpleClassName() {
        return namespace().appendSuffix(delegate().simpleClassName());
    }

    @Override
    @NonNull ClassNamingStrategy fallback() {
        // TODO: We might do an intermediate step: since we are assigning 14 different statements into the default
        //       namespace (which did not add a suffix), we can try to assign a statement-derived suffix. To make
        //       things easier, we use two-characters: AC, AD, AU, AX, CA, CH, CO, IP, LE, LI, LL, NO, OP, RP.
        return new BijectiveNamingStrategy(delegate());
    }
}
