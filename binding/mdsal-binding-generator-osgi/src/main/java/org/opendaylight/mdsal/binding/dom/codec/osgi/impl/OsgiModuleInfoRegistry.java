/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.osgi.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Verify.verify;

import java.util.Dictionary;
import java.util.Hashtable;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.binding.dom.codec.osgi.YangTextAwareBindingRuntimeContext;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.generator.api.ModuleInfoRegistry;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update SchemaContext service in Service Registry each time new YangModuleInfo is added or removed.
 */
final class OsgiModuleInfoRegistry implements ModuleInfoRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(OsgiModuleInfoRegistry.class);

    private final AtomicYangTextAwareBindingRuntimeContext runtimeContext;
    private final SchemaContextProvider schemaContextProvider;
    private final ModuleInfoRegistry moduleInfoRegistry;

    @GuardedBy("this")
    private ServiceRegistration<?> registration;
    @GuardedBy("this")
    private long generation;

    OsgiModuleInfoRegistry(final ModuleInfoRegistry moduleInfoRegistry,
        final SchemaContextProvider schemaContextProvider, final ClassLoadingStrategy classLoadingStrat,
        final SchemaSourceProvider<YangTextSchemaSource> sourceProvider) {

        this.moduleInfoRegistry = checkNotNull(moduleInfoRegistry);
        this.schemaContextProvider = checkNotNull(schemaContextProvider);
        runtimeContext = new AtomicYangTextAwareBindingRuntimeContext(classLoadingStrat, sourceProvider);
    }

    synchronized void updateService() {
        final SchemaContext context;
        try {
            context = schemaContextProvider.getSchemaContext();
        } catch (final RuntimeException e) {
            // The ModuleInfoBackedContext throws a RuntimeException if it can't create the schema context.
            LOG.error("Error updating the schema context", e);
            return;
        }

        try {
            runtimeContext.updateBindingRuntimeContext(context);
        } catch (final RuntimeException e) {
            LOG.error("Error updating binding runtime context", e);
            return;
        }

        if (registration != null) {
            registration.setProperties(updateGeneration());
        }
    }

    synchronized void open(final BundleContext context) {
        verify(registration == null);
        registration = context.registerService(YangTextAwareBindingRuntimeContext.class, runtimeContext,
            updateGeneration());
    }

    synchronized void close() {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
    }

    @GuardedBy("this")
    private Dictionary<String, Long> updateGeneration() {
        final Dictionary<String, Long> ret = new Hashtable<>(1);
        ret.put("generation", generation++);
        return ret;
    }

    @Override
    public ObjectRegistration<YangModuleInfo> registerModuleInfo(final YangModuleInfo yangModuleInfo) {
        return new ObjectRegistrationWrapper(moduleInfoRegistry.registerModuleInfo(yangModuleInfo));
    }

    private class ObjectRegistrationWrapper implements ObjectRegistration<YangModuleInfo> {
        private final ObjectRegistration<YangModuleInfo> inner;

        ObjectRegistrationWrapper(final ObjectRegistration<YangModuleInfo> inner) {
            this.inner = checkNotNull(inner);
        }

        @Override
        public YangModuleInfo getInstance() {
            return inner.getInstance();
        }

        @Override
        public void close() throws Exception {
            try {
                inner.close();
            } finally {
                // send modify event when a bundle disappears
                updateService();
            }
        }

        @Override
        public String toString() {
            return inner.toString();
        }
    }
}
