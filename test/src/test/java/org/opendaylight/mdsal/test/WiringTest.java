/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.test;

import org.junit.Rule;
import org.junit.Test;
import org.opendaylight.infrautils.inject.guice.GuiceClassPathBinder;
import org.opendaylight.infrautils.inject.guice.testutils.AnnotationsModule;
import org.opendaylight.infrautils.inject.guice.testutils.GuiceRule;

/**
 * Tests all the "Wiring classes in mdsal.
 *
 * @author Michael Vorburger.ch
 */
public class WiringTest {

    private static final GuiceClassPathBinder CLASS_PATH_BINDER = new GuiceClassPathBinder("org.opendaylight.mdsal");

    public @Rule GuiceRule guice = new GuiceRule(new InMemoryMdsalModule(CLASS_PATH_BINDER), new AnnotationsModule());

    @Test
    public void testWiring() {
    }
}
