/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.spi.ForwardingDOMRpcProviderService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Beta
@Component(immediate = true, service = DOMRpcProviderService.class)
public final class OSGiDOMRpcProviderService extends ForwardingDOMRpcProviderService {
    private final @NonNull DOMRpcProviderService delegate;

    @Activate
    public OSGiDOMRpcProviderService(@Reference final DOMRpcRouterServices router) {
        delegate = router.getRpcProviderService();
    }

    @Override
    protected DOMRpcProviderService delegate() {
        return delegate;
    }
}
