/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.osgi.impl;

import static java.util.Objects.requireNonNull;

import java.util.Dictionary;
import java.util.Hashtable;
import org.checkerframework.checker.lock.qual.GuardedBy;
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

/**
 * Update SchemaContext service in Service Registry each time new YangModuleInfo is added or removed.
 */
final class OsgiModuleInfoRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(OsgiModuleInfoRegistry.class);

    private final ComponentFactory contextFactory;
    private final BindingRuntimeGenerator generator;
    private final ModuleInfoBackedContext moduleInfoRegistry;

    @GuardedBy("this")
    private ComponentInstance currentInstance;

    OsgiModuleInfoRegistry(final ComponentFactory contextFactory, final YangParserFactory factory,
            final BindingRuntimeGenerator generator) {
        this.contextFactory = requireNonNull(contextFactory);
        this.generator = requireNonNull(generator);

        moduleInfoRegistry = ModuleInfoBackedContext.create("binding-dom-codec", factory,
            // FIXME: This is the fallback strategy, it should not be needed
            GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy());
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    synchronized void updateService() {
        final EffectiveModelContext context;
        try {
            context = moduleInfoRegistry.getEffectiveModelContext();
        } catch (final RuntimeException e) {
            // The ModuleInfoBackedContext throws a RuntimeException if it can't create the schema context.
            LOG.error("Error updating the schema context", e);
            return;
        }
        LOG.trace("Assembled context {}", context);

        final ComponentInstance newInstance = contextFactory.newInstance(createProperties(context));
        if (currentInstance != null) {
            currentInstance.dispose();
        }
        currentInstance = newInstance;
    }

    synchronized void close() {
        if (currentInstance != null) {
            currentInstance.dispose();
            currentInstance = null;
        }
    }

    ObjectRegistration<YangModuleInfo> registerInfo(final YangModuleInfo yangModuleInfo) {
        return moduleInfoRegistry.registerModuleInfo(yangModuleInfo);
    }

    private Dictionary<String, ?> createProperties(final EffectiveModelContext context) {
        final Dictionary<String, BindingRuntimeContext> ret = new Hashtable<>(2);
        ret.put(OSGiBindingRuntimeContext.DELEGATE_KEY,
            // FIXME: MDSAL-392: UGH, this should be a snapshot
            DefaultBindingRuntimeContext.create(generator.generateTypeMapping(context), moduleInfoRegistry));
        return ret;
    }
}
