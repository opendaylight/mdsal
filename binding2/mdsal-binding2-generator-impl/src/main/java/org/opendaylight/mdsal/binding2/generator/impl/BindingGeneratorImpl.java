/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.generator.impl;

import java.util.List;
import java.util.Set;
import org.opendaylight.mdsal.binding2.generator.api.BindingGenerator;
import org.opendaylight.mdsal.binding2.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class BindingGeneratorImpl implements BindingGenerator {

    /**
     * When set to true, generated classes will include javadoc comments which
     * are useful for users.
     */
    private final boolean verboseClassComments;

    /**
     * Create a new binding generator.
     *
     * @param verboseClassComments generate verbose comments
     */
    public BindingGeneratorImpl(final boolean verboseClassComments) {
        this.verboseClassComments = verboseClassComments;
    }

    @Override
    public List<Type> generateTypes(SchemaContext context) {
        //TODO: implement
        return null;
    }

    @Override
    public List<Type> generateTypes(SchemaContext context, Set<Module> modules) {
        //TODO: implement
        return null;
    }
}
