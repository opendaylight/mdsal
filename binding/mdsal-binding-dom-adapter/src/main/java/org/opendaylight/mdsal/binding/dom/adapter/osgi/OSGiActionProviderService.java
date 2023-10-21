/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import com.google.common.annotations.Beta;
import java.util.Map;
import java.util.Set;
import org.opendaylight.mdsal.binding.api.ActionProviderService;
import org.opendaylight.mdsal.binding.api.ActionSpec;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.BindingDataObjectIdentifier;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Beta
@Component(factory = OSGiActionProviderService.FACTORY_NAME)
public final class OSGiActionProviderService extends AbstractAdaptedService<ActionProviderService>
        implements ActionProviderService {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME = "org.opendaylight.mdsal.binding.dom.adapter.osgi.OSGiActionProviderService";

    public OSGiActionProviderService() {
        super(ActionProviderService.class);
    }

    @Override
    public <P extends DataObject, A extends Action<? extends BindingDataObjectIdentifier<P>, ?, ?>, S extends A>
        ObjectRegistration<S> registerImplementation(final ActionSpec<A, P> spec, final S implementation,
            final LogicalDatastoreType datastore, final Set<? extends InstanceIdentifier<P>> validNodes) {
        return delegate().registerImplementation(spec, implementation, datastore, validNodes);
    }

    @Activate
    void activate(final Map<String, ?> properties) {
        start(properties);
    }

    @Deactivate
    void deactivate(final int reason) {
        stop(reason);
    }
}
