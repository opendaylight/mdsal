/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter;

import org.opendaylight.mdsal.binding.javav2.api.RpcActionProviderService;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.BindingToNormalizedNodeCodecFactory;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation.BindingDOMOperationProviderServiceAdapter;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.runtime.context.ModuleInfoBackedContext;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingActivator implements BundleActivator {

    private static final Logger LOG = LoggerFactory.getLogger(BindingActivator.class);

    private ServiceRegistration<BindingToNormalizedNodeCodec> codecService;
    private ServiceRegistration<RpcActionProviderService> registerService;

    private ListenerRegistration<SchemaContextListener> registerSchemaContextListener;

    @Override
    public void start(final BundleContext context) {
        LOG.info("Start BindingActivator.");
        final BindingToNormalizedNodeCodec codec = BindingToNormalizedNodeCodecFactory
                .newInstance(ModuleInfoBackedContext.create());
        codecService = context.registerService(BindingToNormalizedNodeCodec.class, codec, null);

        final ServiceReference<DOMRpcProviderService> domRpcServiceRef =
                context.getServiceReference(DOMRpcProviderService.class);

        if (domRpcServiceRef != null) {
            final DOMRpcProviderService domRpcProviderService = context.getService(domRpcServiceRef);
            final RpcActionProviderService operationServiceAdapter =
                    new BindingDOMOperationProviderServiceAdapter(domRpcProviderService, codec);
            registerService = context.registerService(RpcActionProviderService.class,
                    operationServiceAdapter, null);
        } else {
            registerService = null;
            LOG.error("Missing DOMRpcProviderService service.");
        }

        final ServiceReference<DOMSchemaService> schemaService = context.getServiceReference(DOMSchemaService.class);
        if (schemaService != null) {
            registerSchemaContextListener = context.getService(schemaService).registerSchemaContextListener(codec);
        } else {
            registerSchemaContextListener = null;
            LOG.error("Missing DOMSchemaService service.");
        }
    }

    @Override
    public void stop(final BundleContext context) {
        codecService.unregister();
        if (registerService != null) {
            registerService.unregister();
        }
        if (registerSchemaContextListener != null) {
            registerSchemaContextListener.close();
        }
    }
}
