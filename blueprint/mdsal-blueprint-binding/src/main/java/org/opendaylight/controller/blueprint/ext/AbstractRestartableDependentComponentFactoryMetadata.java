/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.blueprint.ext;

import java.util.concurrent.atomic.AtomicBoolean;
import org.opendaylight.controller.blueprint.BlueprintContainerRestartService;
import org.opendaylight.mdsal.blueprint.common.AbstractDependentComponentFactoryMetadata;

/**
 * Abstract base class for a DependentComponentFactoryMetadata implementation.
 *
 * @author Thomas Pantelis
 */
abstract class AbstractRestartableDependentComponentFactoryMetadata extends AbstractDependentComponentFactoryMetadata {
    private final AtomicBoolean restarting = new AtomicBoolean();

    protected AbstractRestartableDependentComponentFactoryMetadata(final String id) {
        super(id);
    }

    protected void restartContainer() {
        if (restarting.compareAndSet(false, true)) {
            BlueprintContainerRestartService restartService = getOSGiService(BlueprintContainerRestartService.class);
            if (restartService != null) {
                log.debug("{}: Restarting container", logName());
                restartService.restartContainerAndDependents(container().getBundleContext().getBundle());
            }
        }
    }
}
