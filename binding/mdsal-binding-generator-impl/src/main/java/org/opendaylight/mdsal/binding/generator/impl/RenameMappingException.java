/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

@NonNullByDefault
final class RenameMappingException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    private final JavaTypeName name;
    private final SchemaNode definition;

    RenameMappingException(final JavaTypeName name, final SchemaNode definition) {
        super("Remap " + name + " occupant " + definition);
        this.name = requireNonNull(name);
        this.definition = requireNonNull(definition);
    }

    JavaTypeName getName() {
        return name;
    }

    SchemaNode getDefinition() {
        return definition;
    }
}
