/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.osgi.impl;

import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.lock.qual.GuardedBy;
import org.checkerframework.checker.lock.qual.Holding;
import org.opendaylight.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.binding.runtime.api.DefaultBindingRuntimeContext;
import org.opendaylight.binding.runtime.spi.GeneratedClassLoadingStrategy;
import org.opendaylight.binding.runtime.spi.ModuleInfoBackedContext;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class RegularYangModuleInfoRegistry extends YangModuleInfoRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(RegularYangModuleInfoRegistry.class);

    private final ComponentFactory contextFactory;
    private final BindingRuntimeGenerator generator;
    private final ModuleInfoBackedContext moduleInfoRegistry;

    @GuardedBy("this")
    private ComponentInstance currentInstance;
    @GuardedBy("this")
    private int generation;

    private volatile boolean ignoreScanner = true;

    RegularYangModuleInfoRegistry(final ComponentFactory contextFactory, final YangParserFactory factory,
            final BindingRuntimeGenerator generator) {
        this.contextFactory = requireNonNull(contextFactory);
        this.generator = requireNonNull(generator);

        moduleInfoRegistry = ModuleInfoBackedContext.create("binding-dom-codec", factory,
            // FIXME: This is the fallback strategy, it should not be needed
            GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy());
    }

    @Override
    ObjectRegistration<YangModuleInfo> registerInfo(final YangModuleInfo yangModuleInfo) {
        return moduleInfoRegistry.registerModuleInfo(yangModuleInfo);
    }

    @Override
    synchronized void enableScannerAndUpdate() {
        ignoreScanner = false;
        updateService();
    }

    @Override
    void scannerUpdate() {
        if (!ignoreScanner) {
            synchronized (this) {
                updateService();
            }
        }
    }

    @Override
    synchronized void close() {
        ignoreScanner = true;
        if (currentInstance != null) {
            currentInstance.dispose();
            currentInstance = null;
        }
    }

    @Holding("this")
    @SuppressWarnings("checkstyle:illegalCatch")
    private void updateService() {
        final EffectiveModelContext context;
        try {
            context = moduleInfoRegistry.getEffectiveModelContext();
        } catch (final RuntimeException e) {
            // The ModuleInfoBackedContext throws a RuntimeException if it can't create the schema context.
            LOG.error("Error updating the schema context", e);
            return;
        }
        LOG.trace("Assembled context {}", context);

        final BindingRuntimeContext delegate = DefaultBindingRuntimeContext.create(
            generator.generateTypeMapping(context), moduleInfoRegistry);

        final ComponentInstance newInstance = contextFactory.newInstance(
            BindingRuntimeContextImpl.props(nextGeneration(), delegate));
        if (currentInstance != null) {
            currentInstance.dispose();
        }
        currentInstance = newInstance;
    }

    @Holding("this")
    private long nextGeneration() {
        return generation == -1 ? -1 : ++generation;
    }
}