/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl;

import javassist.ClassPool;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.impl.StreamWriterGenerator;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.binding.javav2.runtime.javassist.JavassistUtils;

/**
 * Factory class for creating and initializing the BindingToNormalizedNodeCodec instances.
 */
public class BindingToNormalizedNodeCodecFactory {

    public static final ClassPool CLASS_POOL = ClassPool.getDefault();
    public static final JavassistUtils JAVASSIST = JavassistUtils.forClassPool(CLASS_POOL);

    /**
     * Creates a new BindingToNormalizedNodeCodec instance.
     *
     * @param classLoadingStrategy
     *            - class loading strategy
     *
     * @return the BindingToNormalizedNodeCodec instance
     */
    public static BindingToNormalizedNodeCodec newInstance(final GeneratedClassLoadingStrategy classLoadingStrategy) {
        final BindingNormalizedNodeCodecRegistry codecRegistry =
                new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(JAVASSIST));
        return new BindingToNormalizedNodeCodec(classLoadingStrategy, codecRegistry, true);
    }
}
