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
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecServices;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@Component(immediate = true, service = AdapterContext.class)
public final class OSGiBlockingBindingNormalizer implements AdapterContext {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiBlockingBindingNormalizer.class);

    @Reference(updated = "update")
    volatile BindingDOMCodecServices codec = null;

    private volatile CurrentAdapterSerializer serializer;

    @Override
    public CurrentAdapterSerializer currentSerializer() {
        return verifyNotNull(serializer);
    }

    @Activate
    void activate() {
        serializer = new CurrentAdapterSerializer(codec);
        LOG.info("Binding/DOM adapter activated");
    }

    @Deactivate
    void deactivate() {
        serializer = null;
        LOG.info("Binding/DOM adapter deactivated");
    }

    void update() {
        serializer = new CurrentAdapterSerializer(codec);
        LOG.info("Binding/DOM adapter updated");
    }
}
