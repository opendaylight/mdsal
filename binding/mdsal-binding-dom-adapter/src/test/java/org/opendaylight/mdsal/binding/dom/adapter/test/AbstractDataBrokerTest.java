/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test;

import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Abstract base for DataBroker tests. Note that it performs synchronous commits via a direct executor which can cause
 * issues if used in a concurrent manner so it is recommended to use AbstractConcurrentDataBrokerTest instead.
 *
 * @deprecated Use {@code org.opendaylight.mdsal.binding.testkit.TestKit} instead.
 */
@Deprecated
public class AbstractDataBrokerTest extends AbstractBaseDataBrokerTest {
    @Override
    protected AbstractDataBrokerTestCustomizer createDataBrokerTestCustomizer() {
        return new DataBrokerTestCustomizer();
    }

    @Override
    protected void setupWithSchema(final SchemaContext context) {
        super.setupWithSchema(context);
        setupWithDataBroker(getDataBroker());
    }

    protected void setupWithDataBroker(final DataBroker dataBroker) {
        // Intentionally left No-op, subclasses may customize it
    }
}
