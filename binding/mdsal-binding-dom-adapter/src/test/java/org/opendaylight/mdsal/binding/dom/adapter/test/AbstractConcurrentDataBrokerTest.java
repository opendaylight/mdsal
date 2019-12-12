/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test;

/**
 * Abstract base for DataBroker tests.
 *
 * <p>Uses single thread executor for the Serialized DOM DataBroker (instead of the direct executor used by the
 * AbstractDataBrokerTest) in order to allow tests to use the DataBroker concurrently from several threads.
 *
 * @author Michael Vorburger
 * @deprecated Use {@code org.opendaylight.mdsal.binding.testkit.TestKit} instead.
 */
@Deprecated
public abstract class AbstractConcurrentDataBrokerTest extends AbstractBaseDataBrokerTest {
    private final boolean useMTDataTreeChangeListenerExecutor;

    protected AbstractConcurrentDataBrokerTest() {
        this(false);
    }

    protected AbstractConcurrentDataBrokerTest(final boolean useMTDataTreeChangeListenerExecutor) {
        this.useMTDataTreeChangeListenerExecutor = useMTDataTreeChangeListenerExecutor;
    }

    @Override
    protected AbstractDataBrokerTestCustomizer createDataBrokerTestCustomizer() {
        return new ConcurrentDataBrokerTestCustomizer(useMTDataTreeChangeListenerExecutor);
    }
}
