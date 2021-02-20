/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.AbstractQName;

/**
 * Enumeration of known strategies for translating a YANG node identifier into a Java package name segment or a Java
 * simple class name.
 */
@NonNullByDefault
enum NamingStrategy {
    // FIXME: MDSAL-503: add 'UNIQUE' strategy
    //        The algorithm needs to essentially fall back to using escape-based translation scheme, where each
    //        localName results in a unique name, while not conflicting with any possible preferredName. The exact
    //        mechanics for that are TBD, but note the keys we get in toAssign already have a namespace-specific
    //        suffix. A requirement for that mapping is that it must not rely on definition order.
    //
    //        But there is another possible step: since we are assigning 14 different statements into the default
    //        namespace (which did not add a suffix), we can try to assign a statement-derived suffix. To make
    //        things easier, we use two-characters: AC, AD, AU, AX, CA, CH, CO, IP, LE, LI, LL, NO, OP, RP.
    NAMESPACED(null) {
        @Override
        String simpleClassName(final AbstractQName localName, final StatementNamespace namespace) {
            return namespace.appendSuffix(CLASSIC.simpleClassName(localName, namespace));
        }

        @Override
        String packageNameSegment(final AbstractQName localName, final StatementNamespace namespace) {
            return namespace.appendSuffix(CLASSIC.packageNameSegment(localName, namespace));
        }
    },
    CLASSIC(NAMESPACED) {
        @Override
        String simpleClassName(final AbstractQName localName, final StatementNamespace namespace) {
            return BindingMapping.getClassName(localName.getLocalName());
        }

        @Override
        String packageNameSegment(final AbstractQName localName, final StatementNamespace namespace) {
            // Replace dashes with dots, as dashes are not allowed in package names
            return localName.getLocalName().replace('-', '.');
        }
    };

    private final @Nullable NamingStrategy nextStrategy;

    NamingStrategy(final @Nullable NamingStrategy nextStrategy) {
        this.nextStrategy = nextStrategy;
    }

    @Nullable NamingStrategy nextStrategy() {
        return nextStrategy;
    }

    /**
     * Return the simple Java class name assigned by this naming strategy for the specified {@code localName}.
     *
     * @param localName Local name to process
     * @param namespace {@link StatementNamespace} to use (if needed)
     * @return Simple class name
     */
    abstract String simpleClassName(AbstractQName localName, StatementNamespace namespace);

    /**
     * Return the Java package name segment assigned by this naming strategy for the specified {@code localName}. The
     * actual package name is formed by concatenating each segment from {@link ModuleGenerator} up to and including the
     * generator for which the package is required.
     *
     * @param localName Local name to process
     * @param namespace {@link StatementNamespace} to use (if needed)
     * @return Package name segment
     */
    abstract String packageNameSegment(AbstractQName localName, StatementNamespace namespace);
}
