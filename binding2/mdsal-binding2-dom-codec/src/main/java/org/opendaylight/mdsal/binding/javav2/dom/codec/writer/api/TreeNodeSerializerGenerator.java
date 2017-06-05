/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.writer.api;

import org.opendaylight.mdsal.binding.javav2.generator.impl.util.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializerImplementation;

/**
 * Public interface exposed from generator implementation.
 */
public interface TreeNodeSerializerGenerator {

    /**
     * Get a serializer for a particular type.
     *
     * @param type - class of type
     * @return serializer instance
     */
    TreeNodeSerializerImplementation getSerializer(Class<?> type);

    /**
     * Notify the generator that the runtime context has been updated.
     *
     * @param runtime - new runtime context
     */
    void onBindingRuntimeContextUpdated(BindingRuntimeContext runtime);
}
