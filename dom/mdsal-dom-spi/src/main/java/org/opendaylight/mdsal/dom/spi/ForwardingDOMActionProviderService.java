/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.dom.api.DOMActionImplementation;
import org.opendaylight.mdsal.dom.api.DOMActionInstance;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.yangtools.concepts.Registration;

@NonNullByDefault
public abstract class ForwardingDOMActionProviderService
        extends ForwardingDOMService<DOMActionProviderService, DOMActionProviderService.Extension>
        implements DOMActionProviderService {
    @Override
    public Registration registerActionImplementation(final DOMActionImplementation implementation,
            final Set<DOMActionInstance> instances) {
        return delegate().registerActionImplementation(implementation, instances);
    }
}
