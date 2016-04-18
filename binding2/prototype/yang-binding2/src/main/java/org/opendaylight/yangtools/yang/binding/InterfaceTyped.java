/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

/**
 * InterfaceTyped - object contains structured data
 * 
 * Marker interface which must be implemented by all interfaces
 * generated for YANG:
 * <ul>
 * <li>Rpc Input
 * <li>Output 
 * <li>Notification
 * <li>Container
 * <li>List
 * <li>Case
 * </ul>
 */
public interface InterfaceTyped {
    //FIXME: add generics

    Class<? extends InterfaceTyped> implementedInterface();
}
