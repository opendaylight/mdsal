/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractAdaptedService<B extends BindingService> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAdaptedService.class);
    static final @NonNull String DELEGATE =
            "org.opendaylight.mdsal.binding.dom.adapter.osgi.AbstractAdaptedService.DELEGATE";

    private final Class<B> bindingService;

    private @Nullable B delegate;

    AbstractAdaptedService(final Class<B> bindingService) {
        this.bindingService = requireNonNull(bindingService);
    }

    final void start(final Map<String, ?> properties) {
        delegate = bindingService.cast(verifyNotNull(properties.get(DELEGATE)));
        LOG.info("Binding/DOM adapter for {} activated", bindingService.getSimpleName());
    }

    final void stop(final int reason) {
        delegate = null;
        LOG.info("Binding/DOM adapter for {} deactivated (reason {})", bindingService.getSimpleName(), reason);
    }

    final @NonNull B delegate() {
        return verifyNotNull(delegate);
    }
}
