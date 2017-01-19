/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.api;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.Set;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 *
 * Transforms Schema Context to Generated types.
 *
 */
@Beta
public interface BindingGenerator {

    /**
     * Generate Types from whole Schema Context. <br>
     * The method will return List of All Generated Types that could be
     * Generated from Schema Context.
     *
     *
     * @param context
     *            Schema Context
     * @return List of Generated Types
     *
     * @see SchemaContext
     */
    List<Type> generateTypes(SchemaContext context);

    /**
     * Generate Types from Schema Context restricted by sub set of specified
     * Modules. The Schema Context MUST contain all of the sub modules otherwise
     * there is no guarantee that result List of Generated Types will
     * contain correct Generated Types.
     *
     * @param context
     *            Schema Context
     * @param modules
     *            Sub Set of Modules
     * @return List of Generated Types restricted by sub set of Modules
     *
     * @see Module
     * @see SchemaContext
     */
    List<Type> generateTypes(SchemaContext context, Set<Module> modules);
}
