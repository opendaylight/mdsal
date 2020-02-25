/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.osgi.impl;

import static java.util.Objects.requireNonNull;

import org.apache.karaf.features.DeploymentEvent;
import org.apache.karaf.features.DeploymentListener;
import org.apache.karaf.features.FeaturesService;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class KarafYangModuleInfoRegistry extends YangModuleInfoRegistry implements DeploymentListener {
    private static final Logger LOG = LoggerFactory.getLogger(KarafYangModuleInfoRegistry.class);

    private final RegularYangModuleInfoRegistry delegate;

    private boolean scannerEnabled = false;
    private boolean updatesEnabled = false;

    KarafYangModuleInfoRegistry(final FeaturesService features, final RegularYangModuleInfoRegistry delegate) {
        this.delegate = requireNonNull(delegate);
        features.registerListener(this);
    }

    @Override
    public synchronized void deploymentEvent(final DeploymentEvent event) {
        LOG.debug("Features service indicates {}", event);
        switch (event) {
            case DEPLOYMENT_STARTED:
            case BUNDLES_INSTALLED:
                updatesEnabled = false;
                LOG.debug("BindingRuntimeContext updates disabled");
                break;
            case BUNDLES_RESOLVED:
            case DEPLOYMENT_FINISHED:
            default:
                updatesEnabled = true;
                LOG.debug("BindingRuntimeContext updates enabled");
                if (scannerEnabled) {
                    delegate.enableScannerAndUpdate();
                }
        }
    }

    @Override
    synchronized void scannerUpdate() {
        if (updatesEnabled) {
            delegate.scannerUpdate();
        }
    }

    @Override
    synchronized void enableScannerAndUpdate() {
        scannerEnabled = true;
        if (updatesEnabled) {
            delegate.enableScannerAndUpdate();
        }
    }

    @Override
    synchronized ObjectRegistration<YangModuleInfo> registerInfo(final YangModuleInfo yangModuleInfo) {
        return delegate.registerInfo(yangModuleInfo);
    }

    @Override
    synchronized void close() {
        delegate.close();
    }
}
