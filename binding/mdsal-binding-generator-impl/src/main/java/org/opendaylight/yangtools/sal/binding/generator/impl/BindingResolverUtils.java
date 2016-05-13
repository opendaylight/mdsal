/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import java.util.Collection;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public final class BindingResolverUtils {
    private BindingResolverUtils() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static GeneratedTypeBuilder findChildNodeByPath(final SchemaPath path, Collection<ModuleContext> values) {
        for (final ModuleContext ctx : values) {
            final GeneratedTypeBuilder result = ctx.getChildNode(path);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static GeneratedTypeBuilder findGroupingByPath(final SchemaPath path, Collection<ModuleContext> values) {
        for (final ModuleContext ctx : values) {
            final GeneratedTypeBuilder result = ctx.getGrouping(path);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static GeneratedTypeBuilder findCaseByPath(final SchemaPath path, Collection<ModuleContext> values) {
        for (final ModuleContext ctx : values) {
            final GeneratedTypeBuilder result = ctx.getCase(path);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}