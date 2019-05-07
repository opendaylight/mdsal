/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.eos.dom.simple;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.eos.dom.api.DOMEntityOwnershipService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

@Beta
public class Activator implements BundleActivator {
    private ServiceRegistration<?> reg;

    @Override
    public void start(final BundleContext context) {
        reg = context.registerService(DOMEntityOwnershipService.class, new SimpleDOMEntityOwnershipService(), null);
    }

    @Override
    public void stop(final BundleContext context) {
        if (reg != null) {
            reg.unregister();
            reg = null;
        }
    }
}
