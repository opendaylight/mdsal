/*
 * Copyright (c) 2017, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.schema.osgi.impl;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
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
            final YangParserFactory factory) {
        return KarafFeaturesSupport.wrap(ctx, new RegularYangModuleInfoRegistry(contextFactory, factory));
    }

    // Invocation from scanner, we may want to ignore this in order to not process partial updates
    abstract void scannerUpdate();

    abstract void scannerShutdown();

    abstract void enableScannerAndUpdate();

    abstract void close();

    abstract List<ObjectRegistration<YangModuleInfo>> registerInfos(List<YangModuleInfo> infos);
}
