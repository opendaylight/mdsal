/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.util.ClassLoaderUtils;

public abstract class GeneratedClassLoadingStrategy implements ClassLoadingStrategy {
    private static final class AlwaysFailClassLoadingStrategy extends GeneratedClassLoadingStrategy {
        static final @NonNull AlwaysFailClassLoadingStrategy INSTANCE = new AlwaysFailClassLoadingStrategy();

        @Override
        public Class<?> loadClass(final String fullyQualifiedName) throws ClassNotFoundException {
            throw new ClassNotFoundException(fullyQualifiedName);
        }
    }

    private static final class TCCLClassLoadingStrategy extends GeneratedClassLoadingStrategy {
        static final @NonNull TCCLClassLoadingStrategy INSTANCE = new TCCLClassLoadingStrategy();

        @Override
        public Class<?> loadClass(final String fullyQualifiedName) throws ClassNotFoundException {
            return ClassLoaderUtils.loadClassWithTCCL(fullyQualifiedName);
        }
    }

    protected GeneratedClassLoadingStrategy() {

    }

    public static final @NonNull GeneratedClassLoadingStrategy getTCCLClassLoadingStrategy() {
        return TCCLClassLoadingStrategy.INSTANCE;
    }

    public static final @NonNull GeneratedClassLoadingStrategy getAlwaysFailClassLoadingStrategy() {
        return AlwaysFailClassLoadingStrategy.INSTANCE;
    }

    @Override
    public Class<?> loadClass(final Type type) throws ClassNotFoundException {
        return loadClass(type.getFullyQualifiedName());
    }
}
