/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.Map;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeListener;
import org.opendaylight.mdsal.binding.api.DataTreeLoopException;
import org.opendaylight.mdsal.binding.api.DataTreeProducer;
import org.opendaylight.mdsal.binding.api.DataTreeService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Beta
@Component(factory = OSGiDataTreeService.FACTORY_NAME)
public final class OSGiDataTreeService extends AbstractAdaptedService<DataTreeService> implements DataTreeService {
    // OSGi DS Component Factory name
    static final String FACTORY_NAME = "org.opendaylight.mdsal.binding.dom.adapter.osgi.OSGiDataTreeService";

    public OSGiDataTreeService() {
        super(DataTreeService.class);
    }

    @Override
    public DataTreeProducer createProducer(final Collection<DataTreeIdentifier<?>> subtrees) {
        return delegate().createProducer(subtrees);
    }

    @Override
    public <T extends DataTreeListener> ListenerRegistration<T> registerListener(final T listener,
            final Collection<DataTreeIdentifier<?>> subtrees, final boolean allowRxMerges,
            final Collection<DataTreeProducer> producers) throws DataTreeLoopException {
        return delegate().registerListener(listener, subtrees, allowRxMerges, producers);
    }

    @Activate
    void activate(final Map<String, ?> properties) {
        start(properties);
    }

    @Deactivate
    void deactivate() {
        stop();
    }
}
