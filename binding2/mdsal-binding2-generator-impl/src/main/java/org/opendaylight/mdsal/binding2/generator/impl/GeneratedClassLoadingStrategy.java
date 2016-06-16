/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.generator.impl;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding2.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding2.model.api.Type;

@Beta
public abstract class GeneratedClassLoadingStrategy implements ClassLoadingStrategy {

    @Override
    public Class<?> loadClass(Type type) throws ClassNotFoundException {
        return loadClass(type.getFullyQualifiedName());
    }

    @Override
    public abstract Class<?> loadClass(String fqcn) throws ClassNotFoundException;
}
