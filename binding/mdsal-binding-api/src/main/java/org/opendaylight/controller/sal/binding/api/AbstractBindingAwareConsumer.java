/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.sal.binding.api;

import org.osgi.framework.BundleContext;

@Deprecated
public abstract class AbstractBindingAwareConsumer extends AbstractBrokerAwareActivator implements BindingAwareConsumer {

    @Override
    protected final void onBrokerAvailable(final BindingAwareBroker broker, final BundleContext context) {
        broker.registerConsumer(this);
    }

    /**
     * Called when this bundle is started (before
     * {@link #onSessionInitialized(org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ConsumerContext)}
     * so the Framework can perform the bundle-specific activities necessary to start this bundle.
     * This method can be used to register services or to allocate any resources that this bundle
     * needs.
     *
     * <p>
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context The execution context of the bundle being started.
     * @throws Exception If this method throws an exception, this bundle is marked as stopped and
     *         the Framework will remove this bundle's listeners, unregister all services registered
     *         by this bundle, and release all services used by this bundle.
     */
    @Override
    protected void startImpl(final BundleContext context) throws Exception {
        // NOOP
    }
}
