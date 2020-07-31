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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.ActionProviderService;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.Action;
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
    public <O extends @NonNull DataObject, P extends @NonNull InstanceIdentifier<O>,
            T extends @NonNull Action<P, ?, ?>, S extends T> @NonNull ObjectRegistration<S> registerImplementation(
                    final Class<T> actionInterface, final S implementation, final LogicalDatastoreType datastore,
                    final Set<@NonNull DataTreeIdentifier<O>> validNodes) {
        return delegate().registerImplementation(actionInterface, implementation, datastore, validNodes);
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
