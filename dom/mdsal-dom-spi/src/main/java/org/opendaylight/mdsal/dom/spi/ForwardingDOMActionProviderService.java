/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import com.google.common.annotations.Beta;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ForwardingObject;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMActionImplementation;
import org.opendaylight.mdsal.dom.api.DOMActionInstance;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMActionProviderServiceExtension;
import org.opendaylight.yangtools.concepts.ObjectRegistration;

@Beta
public abstract class ForwardingDOMActionProviderService extends ForwardingObject implements DOMActionProviderService {
    @Override
    public ClassToInstanceMap<DOMActionProviderServiceExtension> getExtensions() {
        return delegate().getExtensions();
    }

    @Override
    public <T extends @NonNull DOMActionImplementation> ObjectRegistration<T> registerActionImplementation(
            final T implementation, final Set<@NonNull DOMActionInstance> instances) {
        return delegate().registerActionImplementation(implementation, instances);
    }

    @Override
    protected abstract @NonNull DOMActionProviderService delegate();
}
