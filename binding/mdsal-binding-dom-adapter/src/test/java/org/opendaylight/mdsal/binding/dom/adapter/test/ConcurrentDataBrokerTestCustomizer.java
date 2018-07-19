/*
 * Copyright (c) 2017 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executors;

/**
 * AbstractDataBrokerTestCustomizer implementation that uses a single-threaded executor for commits.

 * @author Michael Vorburger
 */
public class ConcurrentDataBrokerTestCustomizer extends AbstractDataBrokerTestCustomizer {

    private final ListeningExecutorService dataTreeChangeListenerExecutorSingleton;

    public ConcurrentDataBrokerTestCustomizer(boolean useMTDataTreeChangeListenerExecutor) {
        if (useMTDataTreeChangeListenerExecutor) {
            dataTreeChangeListenerExecutorSingleton = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        } else {
            dataTreeChangeListenerExecutorSingleton = MoreExecutors.newDirectExecutorService();
        }
    }

    @Override
    public ListeningExecutorService getCommitCoordinatorExecutor() {
        return MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
    }

    @Override
    public ListeningExecutorService getDataTreeChangeListenerExecutor() {
        return dataTreeChangeListenerExecutorSingleton;
    }
}
