/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.binding.api.ActionProviderService;
import org.opendaylight.mdsal.binding.api.ActionSpec;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.binding.Action;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(factory = OSGiActionProviderService.FACTORY_NAME)
public final class OSGiActionProviderService extends AbstractAdaptedService<ActionProviderService>
        implements ActionProviderService {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME = "org.opendaylight.mdsal.binding.dom.adapter.osgi.OSGiActionProviderService";

    @Activate
    public OSGiActionProviderService(final Map<String, ?> properties) {
        super(ActionProviderService.class, properties);
    }

    @Deactivate
    void deactivate(final int reason) {
        stop(reason);
    }

    @Override
    public <P extends DataObject, A extends Action<? extends DataObjectIdentifier<P>, ?, ?>>
            Registration registerImplementation(final ActionSpec<A, P> spec, final A implementation,
                final LogicalDatastoreType datastore, final Set<? extends DataObjectIdentifier<P>> validNodes) {
        return delegate.registerImplementation(spec, implementation, datastore, validNodes);
    }
}
