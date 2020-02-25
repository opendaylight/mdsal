/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.osgi.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentFactory;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class OSGiBindingRuntimeTest {
    @Mock
    private YangParserFactory parserFactory;
    @Mock
    private BindingRuntimeGenerator generator;
    @Mock
    private ComponentFactory contextFactory;
    @Mock
    private BundleContext bundleContext;

    private OSGiBindingRuntime target;

    @Before
    public void before() {
        target = new OSGiBindingRuntime();
        target.parserFactory = parserFactory;
        target.generator = generator;
        target.contextFactory = contextFactory;
        doReturn(null).when(bundleContext).getServiceReference(any(Class.class));
    }

    @After
    public void after() {
        verifyNoMoreInteractions(parserFactory);
        verifyNoMoreInteractions(generator);
        verifyNoMoreInteractions(contextFactory);
        verifyNoMoreInteractions(bundleContext);
    }

    @Test
    public void testActivate() {
        doReturn(new Bundle[0]).when(bundleContext).getBundles();
        doNothing().when(bundleContext).addBundleListener(any());
        target.activate(bundleContext);
    }

    @Test
    public void testDeactivate() {
        testActivate();
        doNothing().when(bundleContext).removeBundleListener(any());
        target.deactivate();
    }
}
