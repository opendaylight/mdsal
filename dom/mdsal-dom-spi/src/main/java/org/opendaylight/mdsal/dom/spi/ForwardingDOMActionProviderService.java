/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi;

import com.google.common.annotations.Beta;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMActionImplementation;
import org.opendaylight.mdsal.dom.api.DOMActionInstance;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMActionProviderServiceExtension;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

@Beta
@NonNullByDefault
public abstract class ForwardingDOMActionProviderService
        extends ForwardingDOMExtensibleService<DOMActionProviderService, DOMActionProviderServiceExtension>
        implements DOMActionProviderService {
    @Override
    public <T extends DOMActionImplementation> ObjectRegistration<T> registerActionImplementation(
            final T implementation, final Set<DOMActionInstance> instances) {
        return delegate().registerActionImplementation(implementation, instances);
    }

    @Override
    public <T extends DOMActionImplementation> ObjectRegistration<T> registerActionImplementation(
            final T implementation, final Absolute type, final Set<LogicalDatastoreType> datastores) {
        return delegate().registerActionImplementation(implementation, type, datastores);
    }
}
