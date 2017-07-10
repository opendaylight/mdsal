/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.runtime.context;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class ModuleInfoBackedContextTest {

    private ModuleInfoBackedContext moduleInfoBackedContext;

    @Before
    public void setup() throws ReactorException, FileNotFoundException, URISyntaxException {
        moduleInfoBackedContext = ModuleInfoBackedContext.create();
    }

    @Test
    public void createTestWithStrategy() {
        assertNotNull(ModuleInfoBackedContext.create(
                GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy()));
    }

    @Test(expected = ClassNotFoundException.class)
    public void loadClassTest() throws ClassNotFoundException {
        moduleInfoBackedContext.loadClass("org.opendaylight.mdsal.gen.javav2.test.rev990939.Dummy");
    }
}
