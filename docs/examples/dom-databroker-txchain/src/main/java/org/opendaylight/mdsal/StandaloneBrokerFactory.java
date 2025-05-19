/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.Map;
import java.util.concurrent.Executors;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.broker.SerializedDOMDataBroker;
import org.opendaylight.mdsal.dom.spi.FixedDOMSchemaService;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStoreFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

public final class StandaloneBrokerFactory {
    private StandaloneBrokerFactory() {
        // hidden on purpose
    }

    public static DOMDataBroker create(final EffectiveModelContext context) {
        final var executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        var schemaSvc = new FixedDOMSchemaService(context);
        final var cfg  = InMemoryDOMDataStoreFactory.create("CONFIGURATION",  schemaSvc);
        final var oper = InMemoryDOMDataStoreFactory.create("OPERATIONAL", schemaSvc);

        final Map<LogicalDatastoreType, DOMStore> stores = Map.of(LogicalDatastoreType.CONFIGURATION, cfg,
                LogicalDatastoreType.OPERATIONAL,  oper);
        return new SerializedDOMDataBroker(stores, executor);
    }
}
