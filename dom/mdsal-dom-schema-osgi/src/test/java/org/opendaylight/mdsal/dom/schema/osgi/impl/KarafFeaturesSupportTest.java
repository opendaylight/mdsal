/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.schema.osgi.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.apache.karaf.features.DeploymentListener;
import org.apache.karaf.features.FeaturesService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentFactory;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class KarafFeaturesSupportTest {
    @Mock
    private YangParserFactory parserFactory;
    @Mock
    private ComponentFactory contextFactory;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private ServiceReference<FeaturesService> mockRef;

    private RegularYangModuleInfoRegistry infoRegistry;

    @Before
    public void before() {
        infoRegistry = new RegularYangModuleInfoRegistry(contextFactory, parserFactory);
    }

    @Test
    public void testWrapperWithKaraf() {
        final FeaturesService mockFeatures = mock(FeaturesService.class);
        doNothing().when(mockFeatures).registerListener(any(DeploymentListener.class));
        doReturn(mockFeatures).when(bundleContext).getService(mockRef);
        doReturn(mockRef).when(bundleContext).getServiceReference(FeaturesService.class);

        final YangModuleInfoRegistry wrapped = KarafFeaturesSupport.wrap(bundleContext, infoRegistry);
        assertThat(wrapped, instanceOf(KarafYangModuleInfoRegistry.class));
    }

    @Test
    public void testWrapperWithoutKaraf() {
        doReturn(null).when(bundleContext).getServiceReference(FeaturesService.class);

        final YangModuleInfoRegistry wrapped = KarafFeaturesSupport.wrap(bundleContext, infoRegistry);
        assertSame(infoRegistry, wrapped);
    }
}
