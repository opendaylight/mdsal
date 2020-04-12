/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.dom.adapter.spi.AdapterFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@Component(immediate = true, service = AdapterFactory.class)
// TODO: once we have constructor injection, unify this with BindingAdapterFactory
public final class OSGiBindingAdapterFactory extends AbstractAdapterFactory {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiBindingAdapterFactory.class);

    @Reference
    AdapterContext codec = null;

    @Override
    AdapterContext codec() {
        return verifyNotNull(codec);
    }

    @Activate
    @SuppressWarnings("static-method")
    void activate() {
        LOG.info("Binding Adapter Factory activated");
    }

    @Deactivate
    @SuppressWarnings("static-method")
    void deactivate() {
        LOG.info("Binding Adapter Factory deactivated");
    }
}
