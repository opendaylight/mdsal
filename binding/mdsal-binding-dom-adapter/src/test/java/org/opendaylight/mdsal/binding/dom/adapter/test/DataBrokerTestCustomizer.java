/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * AbstractDataBrokerTestCustomizer implementation that performs synchronous commits via a direct executor.
 * Note that this can cause issues if used in a concurrent manner so it is recommended to use
 * ConcurrentDataBrokerTestCustomizer instead.
 */
public class DataBrokerTestCustomizer extends AbstractDataBrokerTestCustomizer {

    @Override
    public ListeningExecutorService getCommitCoordinatorExecutor() {
        return MoreExecutors.newDirectExecutorService();
    }
}
