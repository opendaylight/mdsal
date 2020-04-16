/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@Component(immediate = true, service = DOMRpcRouterServices.class)
public final class OSGiDOMRpcRouter implements DOMRpcRouterServices {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiDOMRpcRouter.class);

    @Reference
    DOMSchemaService schemaService = null;

    private DOMRpcRouter router;

    @Override
    public DOMActionService getActionService() {
        return router.getActionService();
    }

    @Override
    public DOMActionProviderService getActionProviderService() {
        return router.getActionProviderService();
    }

    @Override
    public DOMRpcService getRpcService() {
        return router.getRpcService();
    }

    @Override
    public DOMRpcProviderService getRpcProviderService() {
        return router.getRpcProviderService();
    }

    @Activate
    void activate() {
        router = DOMRpcRouter.newInstance(schemaService);
        LOG.info("DOM RPC/Action router started");
    }

    @Deactivate
    void deactivate() {
        router.close();
        router = null;
        LOG.info("DOM RPC/Action router stopped");
    }
}
