/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.osgi.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import org.opendaylight.mdsal.binding.generator.api.ModuleInfoRegistry;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

/**
 * Update SchemaContext service in Service Registry each time new YangModuleInfo is added or removed.
 */
final class OsgiModuleInfoRegistry implements ModuleInfoRegistry {

    private final ModuleInfoRegistry moduleInfoRegistry;

    OsgiModuleInfoRegistry(final ModuleInfoRegistry moduleInfoRegistry) {
        this.moduleInfoRegistry = checkNotNull(moduleInfoRegistry);
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
        @SuppressWarnings("checkstyle:illegalCatch")
        public void close() {
            inner.close();
        }

        @Override
        public String toString() {
            return inner.toString();
        }
    }
}
