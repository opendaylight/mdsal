/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.generator.api.BindingRuntimeGenerator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

@Beta
public final class Activator implements BundleActivator {
    @Override
    public void start(final BundleContext context) {
        context.registerService(BindingRuntimeGenerator.class, new DefaultBindingRuntimeGenerator(), null);
    }

    @Override
    public void stop(final BundleContext context) {
        // No-op, service is unregistered automatically
    }
}
