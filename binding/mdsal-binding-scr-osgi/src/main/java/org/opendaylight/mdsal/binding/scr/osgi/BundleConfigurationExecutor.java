/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.osgi;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import org.opendaylight.yangtools.concepts.AbstractRegistration;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class BundleConfigurationExecutor extends AbstractRegistration implements Executor {
    private static final Logger LOG = LoggerFactory.getLogger(BundleConfigurationExecutor.class);

    private final ExecutorService service;

    BundleConfigurationExecutor(final Bundle bundle) {
        service = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setNameFormat("scr-configuration-" + bundle.getBundleId() + "-%d")
            .setDaemon(true)
            .build());
    }

    @Override
    public void execute(final Runnable command) {
        // A bit of trickiness: we can have an asynchronous closure. We check twice because we want to side-step
        // exceptions being thrown. We may still race on with removeRegistration(), though, hence if we get an exception
        // check again to suppress it.
        if (notClosed()) {
            try {
                service.execute(command);
            } catch (RejectedExecutionException e) {
                if (notClosed()) {
                    throw e;
                } else {
                    LOG.debug("Ignored rejected execution of {}", command, e);
                }
            }
        } else {
            LOG.debug("Suppressed execution of {}", command);
        }
    }

    @Override
    protected void removeRegistration() {
        final var tasks = service.shutdownNow();
        if (!tasks.isEmpty()) {
            LOG.debug("Terminating with {} outstanding tasks", tasks.size());
        }
    }
}
