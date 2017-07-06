/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.impl;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTypeBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;

public class GeneratedClassLoadingStrategyTest {

    @Test
    public void loadClassTest() throws Exception {
        final Dummy dummy = new Dummy();
        final GeneratedTypeBuilder gtb = new GeneratedTypeBuilderImpl("test", "dummy", new ModuleContext());
        final Class<?> loadClass = dummy.loadClass(gtb.toInstance());
        Assert.assertNotNull(loadClass);
    }

    private class Dummy extends GeneratedClassLoadingStrategy {

        @Override
        public Class<?> loadClass(final String fqcn) throws ClassNotFoundException {
            return Object.class;
        }

    }
}
