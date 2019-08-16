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

public final class RuntimeGeneratedTOBuilder extends AbstractGeneratedTOBuilder {
    public RuntimeGeneratedTOBuilder(final JavaTypeName identifier) {
        super(identifier);
    }

    @Override
    public void setRestrictions(final Restrictions restrictions) {
        // No-op
    }

    @Override
    public void setSUID(final GeneratedPropertyBuilder suid) {
        // No-op
    }

    @Override
    public void setDescription(final String description) {
        // No-op
    }

    @Override
    public void setModuleName(final String moduleName) {
        // No-op
    }

    @Override
    public void setSchemaPath(final SchemaPath schemaPath) {
        // No-op
    }

    @Override
    public void setReference(final String reference) {
        // No-op
    }

    @Override
    public GeneratedTransferObject build() {
        return new GTO(this);
    }

    @Override
    AbstractEnumerationBuilder newEnumerationBuilder(final JavaTypeName identifier) {
        return new RuntimeEnumerationBuilder(identifier);
    }

    private static final class GTO extends AbstractGeneratedTransferObject {
        GTO(final RuntimeGeneratedTOBuilder builder) {
            super(builder);
        }

        @Override
        public Restrictions getRestrictions() {
            throw unsupported();
        }

        @Override
        public GeneratedProperty getSUID() {
            throw unsupported();
        }

        @Override
        public String getDescription() {
            throw unsupported();
        }

        @Override
        public String getReference() {
            throw unsupported();
        }

        @Override
        public Iterable<QName> getSchemaPath() {
            throw unsupported();
        }

        @Override
        public String getModuleName() {
            throw unsupported();
        }

        private static UnsupportedOperationException unsupported() {
            return new UnsupportedOperationException("Not available at runtime");
        }
    }
}
