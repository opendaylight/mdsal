/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;

/**
 * Interfaces exposed from DOMRpcRouter.
 */
@Beta
@NonNullByDefault
interface DOMRpcRouterServices {
    DOMActionService getActionService();

    DOMActionProviderService getActionProviderService();

    DOMRpcService getRpcService();

    DOMRpcProviderService getRpcProviderService() ;
}
