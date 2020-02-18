/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import com.google.common.annotations.Beta;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.inject.Singleton;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.binding.generator.api.BindingRuntimeGenerator;
import org.opendaylight.mdsal.binding.generator.api.BindingRuntimeTypes;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

/**
 * Default implementation of {@link BindingRuntimeGenerator}.
 */
@Beta
@MetaInfServices
@Singleton
public final class DefaultBindingRuntimeGenerator extends AbstractRenamingGenerator implements BindingRuntimeGenerator {
    @Override
    public BindingRuntimeTypes generateTypeMapping(final SchemaContext context) {
        checkContext(context);

        final Map<SchemaNode, JavaTypeName> renames = new IdentityHashMap<>();
        for (;;) {
            try {
                return new RuntimeTypeGenerator(context, renames).toTypeMapping();
            } catch (RenameMappingException e) {
                rename(renames, e);
            }
        }
    }
}
