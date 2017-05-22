/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.api;

import com.google.common.annotations.Beta;
import java.util.Collection;

/**
 * Factory which allows a creation of producer.
 */
@Beta
public interface DataTreeProducerFactory {

    /**
     * Create a producer, which is able to access to a set of trees.
     *
     * @param subtrees The collection of subtrees the resulting producer should have access to.
     * @return A {@link DataTreeProducer} instance.
     * @throws IllegalArgumentException if subtrees is empty.
     */
    DataTreeProducer createProducer(Collection<DataTreeIdentifier<?>> subtrees);
}