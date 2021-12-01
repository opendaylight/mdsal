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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.api.ActionService;
import org.opendaylight.mdsal.binding.api.ActionSpec;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Beta
@NonNullByDefault
@Component(factory = OSGiActionService.FACTORY_NAME)
public final class OSGiActionService extends AbstractAdaptedService<ActionService> implements ActionService {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME = "org.opendaylight.mdsal.binding.dom.adapter.osgi.OSGiActionService";

    public OSGiActionService() {
        super(ActionService.class);
    }

    @Override
    public <P extends DataObject, A extends Action<InstanceIdentifier<P>, ?, ?>> A getActionHandle(
            final ActionSpec<A, P> spec, final Set<@NonNull DataTreeIdentifier<P>> validNodes) {
        return delegate().getActionHandle(spec, validNodes);
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
