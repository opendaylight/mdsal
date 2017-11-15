/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.test;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.mdsal.binding.javav2.api.DataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@Beta
public class AbstractDataBrokerTest extends AbstractSchemaAwareTest {

    private DataBrokerTestCustomizer testCustomizer;
    private DataBroker dataBroker;
    private DOMDataBroker domBroker;

    @Override
    protected void setupWithSchema(final SchemaContext context) {
        testCustomizer = createDataBrokerTestCustomizer();
        dataBroker = testCustomizer.createDataBroker();
        domBroker = testCustomizer.createDOMDataBroker();
        testCustomizer.updateSchema(context);
        setupWithDataBroker(dataBroker);
    }

    protected void setupWithDataBroker(final DataBroker broker) {
        // Intentionally left No-op, subclasses may customize it
    }

    protected DataBrokerTestCustomizer createDataBrokerTestCustomizer() {
        return new DataBrokerTestCustomizer();
    }

    public DataBroker getDataBroker() {
        return dataBroker;
    }

    public DOMDataBroker getDomBroker() {
        return domBroker;
    }

    protected static final void assertCommit(final ListenableFuture<Void> commit) {
        try {
            commit.get(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new IllegalStateException(e);
        }
    }
}
