/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter;

import org.opendaylight.mdsal.binding.javav2.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.javav2.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.broker.DOMRpcRouter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Binding2Activator implements BundleActivator {

    @Override
    public void start(final BundleContext context) throws Exception {
        context.registerService(ClassLoadingStrategy.class.getName(),
                GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(), null);
        context.registerService(DOMRpcProviderService.class.getName(), new DOMRpcRouter(), null);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
    }
}
