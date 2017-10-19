/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.generator.spi.generator;

import com.google.common.annotations.Beta;

/**
 * Base class for sharing the loading capability.
 */
@Beta
public abstract class AbstractGenerator {

    /**
     * Ensure that the serializer class for specified class is loaded and return
     * its name.
     *
     * @param cls
     *            - data tree class
     * @return serializer class name
     */
    public abstract String loadSerializerFor(Class<?> cls);
}
