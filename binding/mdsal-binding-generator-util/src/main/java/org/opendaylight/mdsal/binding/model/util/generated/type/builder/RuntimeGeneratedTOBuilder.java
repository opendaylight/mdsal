/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util.generated.type.builder;

import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class RuntimeGeneratedTOBuilder extends AbstractGeneratedTOBuilder {
    public RuntimeGeneratedTOBuilder(final JavaTypeName identifier) {
        super(identifier);
    }

    @Override
    public final void setRestrictions(final Restrictions restrictions) {
        // No-op
    }

    @Override
    public final void setSUID(final GeneratedPropertyBuilder suid) {
        // No-op
    }

    @Override
    public final void setDescription(final String description) {
        // No-op
    }

    @Override
    public final void setModuleName(final String moduleName) {
        // No-op
    }

    @Override
    public final void setSchemaPath(final SchemaPath schemaPath) {
        // No-op
    }

    @Override
    public final void setReference(final String reference) {
        // No-op
    }

    @Override
    public GeneratedTransferObject build() {
        return new GTO(this);
    }

    protected static class GTO extends AbstractGeneratedTransferObject {
        protected GTO(final RuntimeGeneratedTOBuilder builder) {
            super(builder);
        }

        @Override
        public final Restrictions getRestrictions() {
            throw unsupported();
        }

        @Override
        public final GeneratedProperty getSUID() {
            throw unsupported();
        }

        @Override
        public final String getDescription() {
            throw unsupported();
        }

        @Override
        public final String getReference() {
            throw unsupported();
        }

        @Override
        public final Iterable<QName> getSchemaPath() {
            throw unsupported();
        }

        @Override
        public final String getModuleName() {
            throw unsupported();
        }

        private static UnsupportedOperationException unsupported() {
            return new UnsupportedOperationException("Not available at runtime");
        }
    }
}
