/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.annotations;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @deprecated Use {@link org.opendaylight.mdsal.yang.binding.annotations.QName} instead
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
public @interface QName {

    String namespace();
    String revision();
    String name();

}
