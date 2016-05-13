/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.generator.api;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding2.model.api.Type;

/**
 *
 * Part of the Binding runtime context that dynamically loads Java classes into the Java
 * Virtual Machine. Usually classes are only loaded on demand.
 *
 */
@Beta
public interface ClassLoadingStrategy {

    /**
     *
     * @param type Generated Type
     * @return instance of class
     * @throws ClassNotFoundException
     */
    Class<?> loadClass(Type type) throws ClassNotFoundException;

    /**
     *
     * @param fqcn fully qualified Java class name
     * @return instance of class
     * @throws ClassNotFoundException
     */
    Class<?> loadClass(String fqcn) throws ClassNotFoundException;
}
