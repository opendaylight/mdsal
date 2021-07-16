/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.config;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.Collection;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.Cont;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.ContBuilder;
import org.opendaylight.yang.gen.v1.urn.test.rev170101.TestData;

/**
 * @author nite
 *
 */
public final class ExampleTestImplementation implements ConfigurationListener<Cont>, AutoCloseable {
    private final ImplementedModule<TestData> impl;

    public ExampleTestImplementation(final ConfigurationService service) throws ImplementationException {
        impl = service.implementModule(TestData.class)
            .addInitialConfiguration(new ContBuilder().build(), this)
            .startImplementation(MoreExecutors.directExecutor());
    }

    @Override
    public void onDataTreeChanged(final Collection<DataTreeModification<Cont>> changes) {
        // process events
    }

    @Override
    public void close() {
        impl.close();
    }
}
