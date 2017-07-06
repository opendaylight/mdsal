/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.util;

import com.google.common.annotations.Beta;

/**
 * Types of java identifier:
 * <ul>
 * <li>class</li>
 * <li>interface</li>
 * <li>enum</li>
 * <li>enum value</li>
 * <li>method</li>
 * <li>variable</li>
 * <li>constant</li>
 * <li>package</li>
 * </ul>
 */
@Beta
public enum JavaIdentifier {

    CLASS, INTERFACE, ENUM, ENUM_VALUE, METHOD, VARIABLE, CONSTANT, PACKAGE
}
