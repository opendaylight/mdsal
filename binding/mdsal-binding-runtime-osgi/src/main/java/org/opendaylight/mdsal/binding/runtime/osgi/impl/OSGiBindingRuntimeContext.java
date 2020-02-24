/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.osgi.impl;

import static com.google.common.base.Verify.verify;

import java.util.Map;
import org.opendaylight.binding.runtime.api.AbstractBindingRuntimeContext;
import org.opendaylight.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.binding.runtime.api.ClassLoadingStrategy;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(factory = "org.opendaylight.mdsal.binding.runtime.osgi.BindingRuntimeContextFactory",
    service = BindingRuntimeContext.class)
public final class OSGiBindingRuntimeContext extends AbstractBindingRuntimeContext {
    static final String DELEGATE_KEY = "org.opendaylight.mdsal.binding.runtime.osgi.BindingRuntimeContextDelegate";

    private static final Logger LOG = LoggerFactory.getLogger(OSGiBindingRuntimeContext.class);

    private BindingRuntimeContext delegate = null;

    @Override
    public ClassLoadingStrategy getStrategy() {
        return delegate.getStrategy();
    }

    @Override
    public BindingRuntimeTypes getTypes() {
        return delegate.getTypes();
    }

    @Activate
    void activate(final Map<String, ?> allProperties) {
        final Object value = allProperties.get(DELEGATE_KEY);
        verify(value instanceof BindingRuntimeContext, "Invalid delegate %s", value);
        delegate = (BindingRuntimeContext) value;
        LOG.debug("{} activated", this);
    }

    @Deactivate
    void deactivate() {
        delegate = null;
        LOG.debug("{} deactivated", this);
    }
}
