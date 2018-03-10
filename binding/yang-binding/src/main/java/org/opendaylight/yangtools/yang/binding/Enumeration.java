/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

/**
 * Interface implemented by all enumerations generated by YANG Binding.
 *
 * @author Robert Varga
 */
public interface Enumeration {
    /**
     * Returns the assigned name of the enumeration item as it is specified in the input YANG.
     *
     * @return the assigned name of the enumeration item as it is specified in the input YANG.
     */
    String getName();

    /**
     * Returns the assigned value of the enumeration item as it is specified in the input YANG.
     *
     * @return the assigned value of the enumeration item as it is specified in the input YANG.
     */
    int getIntValue();
}
