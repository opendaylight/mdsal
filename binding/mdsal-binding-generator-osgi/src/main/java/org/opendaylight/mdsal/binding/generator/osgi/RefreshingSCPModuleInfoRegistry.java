/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.generator.osgi;

import com.google.common.base.Preconditions;
import java.util.Dictionary;
import java.util.Hashtable;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.generator.api.ModuleInfoRegistry;
import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update SchemaContext service in Service Registry each time new YangModuleInfo is added or removed.
 */
final class RefreshingSCPModuleInfoRegistry implements ModuleInfoRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(RefreshingSCPModuleInfoRegistry.class);

    private final ModuleInfoRegistry moduleInfoRegistry;
    private final SchemaContextProvider schemaContextProvider;
    private final SchemaSourceProvider<YangTextSchemaSource> sourceProvider;
    private final SimpleBindingRuntimeContextSupplier bindingContextProvider;
    private final ClassLoadingStrategy classLoadingStrat;

    private final ServiceRegistration<?> schemaRegistration;

    RefreshingSCPModuleInfoRegistry(final ModuleInfoRegistry moduleInfoRegistry,
        final SchemaContextProvider schemaContextProvider, final ClassLoadingStrategy classLoadingStrat,
        final SchemaSourceProvider<YangTextSchemaSource> sourceProvider,
        final SimpleBindingRuntimeContextSupplier bindingContextProvider,
        final ServiceRegistration<?> schemaRegistration) {

        this.moduleInfoRegistry = moduleInfoRegistry;
        this.schemaContextProvider = schemaContextProvider;
        this.classLoadingStrat = classLoadingStrat;
        this.sourceProvider = sourceProvider;
        this.bindingContextProvider = bindingContextProvider;
        this.schemaRegistration = Preconditions.checkNotNull(schemaRegistration);
    }

    void updateService() {
        final SchemaContext context;
        try {
            context = schemaContextProvider.getSchemaContext();
        } catch (final RuntimeException e) {
            // The ModuleInfoBackedContext throws a RuntimeException if it can't create the schema context.
            LOG.warn("Error updating the schema context", e);
            return;
        }

        this.bindingContextProvider.update(classLoadingStrat, context);

        final Dictionary<String, Object> props = new Hashtable<>(4);
        props.put(BindingRuntimeContext.class.getName(), this.bindingContextProvider.get());
        props.put(SchemaSourceProvider.class.getName(), this.sourceProvider);
        try {
            // send modifiedService event
            schemaRegistration.setProperties(props);
        } catch (IllegalStateException e) {
            // Registration already closed
        }
    }

    @Override
    public ObjectRegistration<YangModuleInfo> registerModuleInfo(final YangModuleInfo yangModuleInfo) {
        final ObjectRegistration<YangModuleInfo> yangModuleInfoObjectRegistration = this.moduleInfoRegistry.registerModuleInfo(yangModuleInfo);
        return new ObjectRegistrationWrapper(yangModuleInfoObjectRegistration);
    }

    private class ObjectRegistrationWrapper implements ObjectRegistration<YangModuleInfo> {
        private final ObjectRegistration<YangModuleInfo> inner;

        ObjectRegistrationWrapper(final ObjectRegistration<YangModuleInfo> inner) {
            this.inner = inner;
        }

        @Override
        public YangModuleInfo getInstance() {
            return this.inner.getInstance();
        }

        @Override
        public void close() throws Exception {
            this.inner.close();
            // send modify event when a bundle disappears
            updateService();
        }

        @Override
        public String toString() {
            return this.inner.toString();
        }
    }
}
