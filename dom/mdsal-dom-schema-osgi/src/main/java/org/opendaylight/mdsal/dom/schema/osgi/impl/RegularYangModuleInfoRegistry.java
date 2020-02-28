/*
 * Copyright (c) 2017, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.schema.osgi.impl;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.NoSuchElementException;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.checkerframework.checker.lock.qual.Holding;
import org.opendaylight.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.binding.runtime.spi.ModuleInfoSnapshotBuilder;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update SchemaContext service in Service Registry each time new YangModuleInfo is added or removed.
 */
final class RegularYangModuleInfoRegistry extends YangModuleInfoRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(RegularYangModuleInfoRegistry.class);

    private final ComponentFactory contextFactory;
    private final ModuleInfoSnapshotBuilder moduleInfoRegistry;

    @GuardedBy("this")
    private ComponentInstance currentInstance;
    @GuardedBy("this")
    private ModuleInfoSnapshot currentSnapshot;
    @GuardedBy("this")
    private int generation;

    private volatile boolean ignoreScanner = true;

    RegularYangModuleInfoRegistry(final ComponentFactory contextFactory, final YangParserFactory factory) {
        this.contextFactory = requireNonNull(contextFactory);
        moduleInfoRegistry = new ModuleInfoSnapshotBuilder("binding-dom-codec", factory);
    }

    // Invocation from scanner, we may want to ignore this in order to not process partial updates
    @Override
    void scannerUpdate() {
        if (!ignoreScanner) {
            synchronized (this) {
                updateService();
            }
        }
    }

    @Override
    synchronized void scannerShutdown() {
        ignoreScanner = true;
    }

    @Override
    synchronized void enableScannerAndUpdate() {
        ignoreScanner = false;
        updateService();
    }

    @Override
    synchronized void close() {
        ignoreScanner = true;
        if (currentInstance != null) {
            currentInstance.dispose();
            currentInstance = null;
        }
    }

    @Override
    List<ObjectRegistration<YangModuleInfo>> registerInfos(final List<YangModuleInfo> infos) {
        return moduleInfoRegistry.registerModuleInfos(infos);
    }

    @Holding("this")
    private void updateService() {
        final ModuleInfoSnapshot newSnapshot;
        try {
            newSnapshot = moduleInfoRegistry.build();
        } catch (NoSuchElementException e) {
            LOG.debug("No snapshot available", e);
            return;
        }
        if (newSnapshot.equals(currentSnapshot)) {
            LOG.debug("No update to snapshot");
            return;
        }


        final ComponentInstance newInstance = contextFactory.newInstance(
            OSGiEffectiveModelImpl.props(nextGeneration(), newSnapshot));
        if (currentInstance != null) {
            currentInstance.dispose();
        }
        currentInstance = newInstance;
        currentSnapshot = newSnapshot;
    }

    @Holding("this")
    private long nextGeneration() {
        return generation == -1 ? -1 : ++generation;
    }
}
