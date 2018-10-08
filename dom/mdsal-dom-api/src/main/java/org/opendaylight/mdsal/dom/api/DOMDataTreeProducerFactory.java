/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Base source of {@link DOMDataTreeProducer}s. This interface is usually not used directly,
 * but rather through one of its sub-interfaces.
 */
public interface DOMDataTreeProducerFactory {
    /**
     * Create a producer, which is able to access to a set of trees.
     *
     * @param subtrees The collection of subtrees the resulting producer should have access to.
     * @return A {@link DOMDataTreeProducer} instance.
     * @throws IllegalArgumentException if subtrees is empty.
     */
    @NonNull DOMDataTreeProducer createProducer(@NonNull Collection<DOMDataTreeIdentifier> subtrees);
}
