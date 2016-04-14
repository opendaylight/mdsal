/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.singleton.common.api;

/**
 * {@link ClusterSingletonServiceRegistration} interface contains a marker for
 * holding ClusterSingletonService registration and posibility to close it
 * via {@link AutoCloseable} interface.
 */
public interface ClusterSingletonServiceRegistration extends AutoCloseable {

    /**
     * Empty body for mark a ClusterSingletonService Registration instance
     */
}
