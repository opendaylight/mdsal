/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.generator.impl;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.Set;
import org.opendaylight.mdsal.binding2.generator.api.BindingGenerator;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Main class for Binding generator v2. Provides transformation of Schema Context to
 * generated transfer objects. Process is accompanied with Twirl templates to generate
 * particular Javadoc for related YANG elements.
 */
@Beta
public class BindingGeneratorImpl implements BindingGenerator {

    /**
     * When set to true, generated classes will include Javadoc comments
     * which are useful for users.
     */
    private final boolean verboseClassComments;

    /**
     * Creates a new binding generator v2.
     *
     * @param verboseClassComments generate verbose comments
     */
    public BindingGeneratorImpl(final boolean verboseClassComments) {
        this.verboseClassComments = verboseClassComments;
    }

    @Override
    public List<Type> generateTypes(SchemaContext context) {
        return null;
    }

    @Override
    public List<Type> generateTypes(SchemaContext context, Set<Module> modules) {
        return null;
    }
}
