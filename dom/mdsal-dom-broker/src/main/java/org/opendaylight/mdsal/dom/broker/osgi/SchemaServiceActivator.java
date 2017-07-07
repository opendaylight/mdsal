/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker.osgi;

import java.util.Hashtable;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.broker.DOMRpcRouter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchemaServiceActivator implements BundleActivator {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaServiceActivator.class);

    private ServiceRegistration<DOMSchemaService> schemaServiceReg;
    private OsgiBundleScanningSchemaService schemaService;
    private ServiceRegistration<DOMRpcProviderService> domRpcProviderService;
    private ServiceRegistration<DOMRpcService> domRpcService;

    @Override
    public void start(final BundleContext context) {
        LOG.info("Start SchemaServiceActivator.");
        schemaService = OsgiBundleScanningSchemaService.createInstance(context);
        schemaServiceReg = context.registerService(DOMSchemaService.class,
                schemaService, new Hashtable<>());
        final DOMRpcRouter rpcProviderService = DOMRpcRouter.newInstance(schemaService);
        domRpcProviderService = context.registerService(DOMRpcProviderService.class, rpcProviderService, null);
        domRpcService = context.registerService(DOMRpcService.class, rpcProviderService, null);
    }

    @Override
    public void stop(final BundleContext context) {
        domRpcProviderService.unregister();
        domRpcService.unregister();
        schemaServiceReg.unregister();
        schemaService.close();
    }
}
