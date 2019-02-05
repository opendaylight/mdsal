/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public abstract class AbstractBaseDataBrokerTest extends AbstractSchemaAwareTest {

    private static final int ASSERT_COMMIT_DEFAULT_TIMEOUT = 5000;

    private AbstractDataBrokerTestCustomizer testCustomizer;
    private DataBroker dataBroker;
    private DOMDataBroker domBroker;

    protected abstract AbstractDataBrokerTestCustomizer createDataBrokerTestCustomizer();

    public AbstractDataBrokerTestCustomizer getDataBrokerTestCustomizer() {
        if (testCustomizer == null) {
            throw new IllegalStateException("testCustomizer not yet set by call to createDataBrokerTestCustomizer()");
        }
        return testCustomizer;
    }

    @Override
    protected void setupWithSchema(final SchemaContext context) {
        testCustomizer = createDataBrokerTestCustomizer();
        dataBroker = testCustomizer.createDataBroker();
        domBroker = testCustomizer.getDOMDataBroker();
        testCustomizer.updateSchema(context);
    }

    public DataBroker getDataBroker() {
        return dataBroker;
    }

    public DOMDataBroker getDomBroker() {
        return domBroker;
    }

    protected static final void assertCommit(final ListenableFuture<?> commit) {
        assertCommit(commit, ASSERT_COMMIT_DEFAULT_TIMEOUT);
    }

    protected static final void assertCommit(final ListenableFuture<?> commit, long timeoutInMS) {
        try {
            commit.get(timeoutInMS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new IllegalStateException(e);
        }
    }
}
