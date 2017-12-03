/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.common.api;

/**
 * Datastore instantiation handle. Note this is a hybrid between RFC7950/RFC6241 and
 * draft-ietf-netmod-revised-datastores and may need to be revised if we decide to formalize NMDA at MD-SAL layer
 * as opposed to application layer.
 */
public enum LogicalDatastoreType {
    /**
     * Logical Datastore representing operational state of the system and it's components.
     * This datastore is used to describe operational state of the system and it's operation related data.
     *
     * <p>
     * This datastore allows storage of both config=true and config=false YANG data.
     */
    OPERATIONAL,
    /**
     * Logical Datastore representing configuration state of the system and it's components.
     * This datastore is used to describe intended state of the system and intended operation mode.
     * <p>
     * This datastore allows storage of only config=true YANG data.
     */
    CONFIGURATION
}
