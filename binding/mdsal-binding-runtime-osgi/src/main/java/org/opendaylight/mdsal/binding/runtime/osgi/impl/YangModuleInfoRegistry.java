/*
 * Copyright (c) 2017, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.osgi.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentFactory;

/**
 * Update SchemaContext service in Service Registry each time new YangModuleInfo is added or removed.
 */
abstract class YangModuleInfoRegistry {
    static @NonNull YangModuleInfoRegistry create(final BundleContext ctx, final ComponentFactory contextFactory,
            final YangParserFactory factory, final BindingRuntimeGenerator generator) {
        return KarafFeaturesSupport.wrap(ctx, new RegularYangModuleInfoRegistry(contextFactory, factory, generator));
    }

    // Invocation from scanner, we may want to ignore this in order to not process partial updates
    abstract void scannerUpdate();

    abstract void enableScannerAndUpdate();

    abstract void close();

    abstract ObjectRegistration<YangModuleInfo> registerInfo(YangModuleInfo yangModuleInfo);
}
