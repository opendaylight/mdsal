/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.ext;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;

/**
 * The binding manifestation of {@code augment-identifier}.
 */
@Documented
@Target(TYPE)
public @interface AugmentIdentifier {
    /**
     * Value of the {@code augment-identifier} statement argument. Required to comply with the YANG {@code identifier}
     * parser production.
     *
     * @return Argument value
     */
    String value();
}
