/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc8525;

import org.opendaylight.binding.runtime.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.model.api.Type;

final class SimpleStrategy implements ClassLoadingStrategy {
    static final SimpleStrategy INSTANCE = new SimpleStrategy();

    @Override
    public Class<?> loadClass(final Type type) throws ClassNotFoundException {
        return loadClass(type.getFullyQualifiedName());
    }

    @Override
    public Class<?> loadClass(final String fullyQualifiedName) throws ClassNotFoundException {
        return SimpleStrategy.class.getClassLoader().loadClass(fullyQualifiedName);
    }
}
