/*
 * Copyright (c) 2018 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.test;

import javassist.ClassPool;
import org.junit.Before;
import org.opendaylight.mdsal.binding.dom.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.generator.util.JavassistUtils;

public abstract class AbstractBindingCodecTest extends AbstractBindingRuntimeTest {

    protected BindingNormalizedNodeCodecRegistry registry;

    @Before
    public void before() {
        final JavassistUtils utils = JavassistUtils.forClassPool(ClassPool.getDefault());
        this.registry = new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(utils));
        this.registry.onBindingRuntimeContextUpdated(getRuntimeContext());
    }
}
