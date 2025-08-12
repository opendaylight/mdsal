/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import com.google.common.util.concurrent.ForwardingExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ExecutorService;

/**
 * A forwarding Executor used by unit tests for DataChangeListener notifications.
 *
 * @author Thomas Pantelis
 */
final class TestDCLExecutorService extends ForwardingExecutorService {
    // Start with a same thread executor to avoid timing issues during test setup.
    private volatile ExecutorService currentExecutor = MoreExecutors.newDirectExecutorService();

    // The real executor to use when test setup is complete.
    private final ExecutorService postSetupExecutor;

    TestDCLExecutorService(final ExecutorService postSetupExecutor) {
        this.postSetupExecutor = postSetupExecutor;
    }

    @Override
    protected ExecutorService delegate() {
        return currentExecutor;
    }

    void afterTestSetup() {
        // Test setup complete - switch to the real executor.
        currentExecutor = postSetupExecutor;
    }
}