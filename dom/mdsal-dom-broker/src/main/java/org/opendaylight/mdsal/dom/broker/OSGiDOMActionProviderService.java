/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.spi.ForwardingDOMActionProviderService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Beta
@Component(immediate = true, service = DOMActionProviderService.class)
public final class OSGiDOMActionProviderService extends ForwardingDOMActionProviderService {
    private DOMActionProviderService delegate;

    @Override
    protected DOMActionProviderService delegate() {
        return verifyNotNull(delegate);
    }

    @Reference
    void bindRpcRouter(final DOMRpcRouterServices router) {
        delegate = router.getActionProviderService();
    }
}
