/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.config.api;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Marker interface for fields holding initial configuration. Annotated field has to be a constant and its type must
 * be that of a generated top-level {@link DataObject}. For example:
 * <pre>
 *   {@code
 *     public class Example {
 *       @InitialConfiguration
 *       public static final Cont INITIAL_CONT = new ContBuilder().build();
 *   }
 * </pre>
 */
@Documented
@Retention(SOURCE)
@Target(FIELD)
public @interface InitialConfiguration {

}
