/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.common.api.clustering;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * {@link ClusterSingletonService} interface represents a single cluster service instance. It has to implement
 * every service (RPCs or Applications) which would like to be instantiated on same Cluster Node. Grouping is
 * realized by ServiceGroupIdentifier. Servicies with same ServiceGroupIdentifier has to run on same Cluster
 * Node. ServiceGroupIdentifier must not change during whole {@link ClusterSingletonService} lifecycle.
 */
public interface ClusterSingletonService {

    /**
     * This method is invoked to instantiate an underlying service instance when
     * ownership has been granted for the service entity.
     */
    void instantiateServiceInstance();

    /**
     * This method is invoked to close the underlying service instance when ownership has been lost
     * for the service entity. If the act of closing the instance may perform blocking operations or
     * take some time, it should be done asynchronously to avoid blocking the current thread.
     *
     * @return a ListenableFuture that is completed when the underlying instance close operation is complete.
     */
    ListenableFuture<Void> closeServiceInstance();

    /**
     * Method returns identificator for {@link ClusterSingletonService} Group.
     *
     * @return defined Service Group Identifier
     */
    String getServiceGroupIdentifier();

}
