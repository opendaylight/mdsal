/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.schema.service.osgi;

import java.util.Hashtable;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class SchemaServiceActivator implements BundleActivator {
    private ServiceRegistration<DOMSchemaService> schemaServiceReg;
    private OsgiBundleScanningSchemaService schemaService;

    @Override
    public void start(final BundleContext context) {
        schemaService = OsgiBundleScanningSchemaService.createInstance(context);
        schemaServiceReg = context.registerService(DOMSchemaService.class,
                schemaService, new Hashtable<>());
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        schemaServiceReg.unregister();
        schemaService.close();
    }
}
