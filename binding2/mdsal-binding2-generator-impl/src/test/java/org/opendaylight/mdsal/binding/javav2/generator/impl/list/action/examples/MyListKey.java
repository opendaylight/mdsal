/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.impl.list.action.examples;

import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentable;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.concepts.Identifier;

/**
 * key of list my-list
 */
public class MyListKey implements Identifier {

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return null;
    }
}
