/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

/**
 * Data Container - object contains structured data
 * 
 * Marker interface which must be implmeneted by all interfaces
 * generated for YANG:
 * &lt;ul&gt;
 * &lt;li&gt;Rpc Input
 * &lt;li&gt;Output 
 * &lt;li&gt;Notification
 * &lt;li&gt;Container
 * &lt;li&gt;List
 * &lt;li&gt;Case
 * &lt;/ul&gt;
 */
public interface DataContainer {

    Class<? extends DataContainer> getImplementedInterface();
}
