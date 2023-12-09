/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import static com.google.common.base.Verify.verifyNotNull;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractAdaptedService<B extends BindingService> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAdaptedService.class);
    static final @NonNull String DELEGATE =
            "org.opendaylight.mdsal.binding.dom.adapter.osgi.AbstractAdaptedService.DELEGATE";

    private final String serviceName;
    final @NonNull B delegate;

    AbstractAdaptedService(final Class<B> bindingService, final Map<String, ?> properties) {
        serviceName = bindingService.getSimpleName();
        delegate = bindingService.cast(verifyNotNull(properties.get(DELEGATE)));
        LOG.info("Binding/DOM adapter for {} activated", serviceName);
    }

    final void stop(final int reason) {
        LOG.info("Binding/DOM adapter for {} deactivated (reason {})", serviceName, reason);
    }
}
