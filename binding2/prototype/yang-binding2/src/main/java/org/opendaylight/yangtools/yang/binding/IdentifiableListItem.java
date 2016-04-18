/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;


/**
 * IdentifiableListItem object, which could be identified by it's key
 *
 * @param <T> Identifier class for this object
 *
 */
public interface IdentifiableListItem<T extends Identifier<? extends IdentifiableListItem<T>>> {
    
    /**
     * Returns an unique identifier for the object
     * 
     * @return Identifier for the object
     */
    T identifier();
}
