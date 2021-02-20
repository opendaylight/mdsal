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
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Enumeration of known strategies for translating a YANG node identifier into a Java package name segment or a Java
 * simple class name.
 */
@NonNullByDefault
abstract class NamingStrategy implements Immutable {
    //  static final class Augment extends NamingStrategy {
    //
    //  }
    /**
     * Return the simple Java class name assigned by this naming strategy.
     *
     * @return Simple class name
     */
    abstract String simpleClassName();

    /**
     * Return the Java package name segment assigned by this naming strategy. The actual package name is formed by
     * concatenating each segment from {@link ModuleGenerator} up to and including the generator for which the package
     * is required.
     *
     * @return Package name segment
     */
    abstract String packageNameSegment();

    abstract @Nullable NamingStrategy fallback();
}
