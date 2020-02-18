/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.Map;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractRenamingGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractRenamingGenerator.class);

    static void checkContext(final SchemaContext context) {
        checkArgument(context != null, "Schema Context reference cannot be NULL.");
        checkState(context.getModules() != null, "Schema Context does not contain defined modules.");
    }

    static void rename(final Map<SchemaNode, JavaTypeName> renames, final RenameMappingException ex) {
        final JavaTypeName name = ex.getName();
        final SchemaNode def = ex.getDefinition();
        final JavaTypeName existing = renames.get(def);
        if (existing != null) {
            throw new IllegalStateException("Attempted to relocate " + def + " to " + name + ", already remapped to "
                    + existing, ex);
        }

        final String suffix;
        if (def instanceof IdentitySchemaNode) {
            suffix = "$I";
        } else if (def instanceof GroupingDefinition) {
            suffix = "$G";
        } else if (def instanceof TypeDefinition) {
            suffix = "$T";
        } else {
            throw new IllegalStateException("Unhandled remapping of " + def + " at " + name, ex);
        }

        final JavaTypeName newName = name.createSibling(name.simpleName() + suffix);
        renames.put(def, newName);
        LOG.debug("Restarting code generation after remapping {} to {}", name, newName);
    }

}
