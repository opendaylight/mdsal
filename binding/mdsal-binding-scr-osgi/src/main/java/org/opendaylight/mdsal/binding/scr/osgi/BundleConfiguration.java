/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.osgi;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.opendaylight.mdsal.binding.api.config.ConfigurationService;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class BundleConfiguration extends AbstractRegistration {
    private static final Logger LOG = LoggerFactory.getLogger(BundleConfiguration.class);

    private final ExecutorService executor;

    BundleConfiguration(final ConfigurationService configService, final Bundle bundle,
            final Map<BundleField, ChildOf<?>> values, final Map<String, Class<? extends DataObject>> types) {
        executor = Executors.newSingleThreadExecutor(newThreadFactory(bundle));

        // FIXME: create services



        // TODO Auto-generated constructor stub
    }

    @Override
    protected void removeRegistration() {
        // FIXME: stop all services

        final var tasks = executor.shutdownNow();
        if (!tasks.isEmpty()) {
            LOG.debug("Terminating with {} outstanding tasks", tasks.size());
        }
    }

    private static ThreadFactory newThreadFactory(final Bundle bundle) {
        return new ThreadFactoryBuilder()
            .setNameFormat("scr-configuration-" + bundle.getBundleId() + "-%d")
            .setDaemon(true)
            .build();
    }
}
