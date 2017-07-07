/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.yangtools.util.ClassLoaderUtils;

@Beta
public abstract class GeneratedClassLoadingStrategy implements ClassLoadingStrategy {

    private static final GeneratedClassLoadingStrategy TCCL_STRATEGY = new TCCLClassLoadingStrategy();

    @Override
    public Class<?> loadClass(final Type type) throws ClassNotFoundException {
        return loadClass(type.getFullyQualifiedName());
    }

    @Override
    public abstract Class<?> loadClass(String fqcn) throws ClassNotFoundException;

    public static GeneratedClassLoadingStrategy getTCCLClassLoadingStrategy() {
        return TCCL_STRATEGY;
    }

    private static final class TCCLClassLoadingStrategy extends GeneratedClassLoadingStrategy {
        @Override
        public Class<?> loadClass(final String fullyQualifiedName) throws ClassNotFoundException {
            return ClassLoaderUtils.loadClassWithTCCL(fullyQualifiedName);
        }
    }
}
