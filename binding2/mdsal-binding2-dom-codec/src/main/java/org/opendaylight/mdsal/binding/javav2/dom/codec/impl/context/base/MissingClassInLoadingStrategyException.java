/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.context.base;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.MissingSchemaException;
import org.opendaylight.mdsal.binding.javav2.generator.api.ClassLoadingStrategy;

/**
 * Thrown when user schema for supplied binding class is available in present schema context, but
 * binding class itself is not known to codecs because backing class loading strategy did not
 * provided it.
 */
@Beta
public class MissingClassInLoadingStrategyException extends MissingSchemaException {

    private static final long serialVersionUID = 1L;

    protected MissingClassInLoadingStrategyException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public static void check(final ClassLoadingStrategy strategy, final Class<?> childClass) {
        try {
            strategy.loadClass(childClass.getName());
        } catch (final ClassNotFoundException e) {
            final String message =
                    String.format("User supplied class %s is not available in %s.", childClass.getName(), strategy);
            throw new MissingClassInLoadingStrategyException(message, e);
        }
    }

}