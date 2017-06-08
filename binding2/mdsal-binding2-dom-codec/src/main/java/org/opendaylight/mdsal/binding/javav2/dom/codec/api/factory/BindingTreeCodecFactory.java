/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.api.factory;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.dom.codec.api.BindingTreeCodec;
import org.opendaylight.mdsal.binding.javav2.runtime.context.BindingRuntimeContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@Beta
public interface BindingTreeCodecFactory {

    /**
     * Creates Binding Codec Tree for specified Binding runtime context.
     *
     * @param context
     *            - Binding Runtime Context for which Binding codecs should be
     *            instantiated
     * @return Binding Codec Tree for specified Binding runtime context
     */
    BindingTreeCodec create(BindingRuntimeContext context);

    /**
     * Creates Binding Codec Tree for schema context according to binding
     * classes.
     *
     * @param schemaContext
     *            - schema context for which Binding codecs should be
     *            instantiated
     * @param bindingClasses
     *            - Binding Runtime Context will be constructed using bindings
     *            which contains specified classes, in order to support
     *            deserialization in multi-classloader environment
     * @return Binding Codec Tree for specified Binding runtime context
     */
    BindingTreeCodec create(SchemaContext schemaContext, Class<?>... bindingClasses);
}
