/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.model.api;

import com.google.common.annotations.Beta;

/**
 * Represents an instance of simple parametrized type such as List&lt;String&gt;.
 *
 * The parametrized Type is designed to be used to store information of Java
 * Generic Type. The array of {@link #getActualTypeArguments()} holds
 * information of all generic parameters defined for Parameterized Type.
 */
@Beta
public interface ParametrizedType extends Type {

    /**
     * Returns array of Types that are defined for Parameterized Type.
     * <br>
     * (for example if ParameterizedType encapsulates java generic Map that
     * specifies two parameters Map&lt;K,V&gt; and the K is java.lang.Integer and V
     * is defined as GeneratedType the array will contain two Types to store
     * the information of generic parameters.)
     *
     * @return array of Types that are defined for Parameterized Type.
     */
    Type[] getActualTypeArguments();

    /**
     * Returns the Raw Type definition of Parameterized Type.
     *
     * @return the Raw Type definition of Parameterized Type.
     */
    Type getRawType();
}
