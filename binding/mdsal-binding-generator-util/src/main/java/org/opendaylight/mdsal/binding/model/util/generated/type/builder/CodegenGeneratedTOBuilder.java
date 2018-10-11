/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
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

public final class CodegenGeneratedTOBuilder extends AbstractGeneratedTOBuilder {

    private Restrictions restrictions;
    private GeneratedPropertyBuilder suid;
    private String reference;
    private String description;
    private String moduleName;
    private SchemaPath schemaPath;

    public CodegenGeneratedTOBuilder(final JavaTypeName identifier) {
        super(identifier);
    }

    @Override
    public void setRestrictions(final Restrictions restrictions) {
        this.restrictions = restrictions;
    }

    @Override
    public void setSUID(final GeneratedPropertyBuilder newSuid) {
        this.suid = newSuid;
    }

    @Override
    public GeneratedTransferObject build() {
        return new GTO(this);
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public void setSchemaPath(final SchemaPath schemaPath) {
        this.schemaPath = schemaPath;
    }

    @Override
    public void setReference(final String reference) {
        this.reference = reference;
    }

    @Override
    AbstractEnumerationBuilder newEnumerationBuilder(final JavaTypeName identifier) {
        return new CodegenEnumerationBuilder(identifier);
    }

    private static final class GTO extends AbstractGeneratedTransferObject {
        private final Restrictions restrictions;
        private final GeneratedProperty suid;
        private final String reference;
        private final String description;
        private final String moduleName;
        private final SchemaPath schemaPath;

        GTO(final CodegenGeneratedTOBuilder builder) {
            super(builder);
            this.restrictions = builder.restrictions;
            this.reference = builder.reference;
            this.description = builder.description;
            this.moduleName = builder.moduleName;
            this.schemaPath = builder.schemaPath;

            if (builder.suid == null) {
                this.suid = null;
            } else {
                this.suid = builder.suid.toInstance(GTO.this);
            }
        }

        @Override
        public Restrictions getRestrictions() {
            return this.restrictions;
        }

        @Override
        public GeneratedProperty getSUID() {
            return this.suid;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public String getReference() {
            return this.reference;
        }

        @Override
        public Iterable<QName> getSchemaPath() {
            return this.schemaPath.getPathFromRoot();
        }

        @Override
        public String getModuleName() {
            return this.moduleName;
        }
    }
}
