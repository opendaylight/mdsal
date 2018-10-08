/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;

public interface DataTreeProducerFactory {
    /**
     * Create a producer, which is able to access to a set of trees.
     *
     * @param subtrees The collection of subtrees the resulting producer should have access to.
     * @return A {@link DataTreeProducer} instance.
     * @throws IllegalArgumentException if subtrees is empty.
     */
    @NonNull DataTreeProducer createProducer(@NonNull Collection<DataTreeIdentifier<?>> subtrees);
}