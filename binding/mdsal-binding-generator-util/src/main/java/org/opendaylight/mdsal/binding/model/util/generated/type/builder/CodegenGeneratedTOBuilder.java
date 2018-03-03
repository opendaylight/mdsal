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
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public final class CodegenGeneratedTOBuilder extends AbstractGeneratedTOBuilder {

    private Restrictions restrictions;
    private GeneratedPropertyBuilder SUID;
    private String reference;
    private String description;
    private String moduleName;
    private SchemaPath schemaPath;

    public CodegenGeneratedTOBuilder(final String packageName, final String name) {
        super(packageName, name);
    }

    @Override
    public void setRestrictions(final Restrictions restrictions) {
        this.restrictions = restrictions;
    }

    @Override
    public void setSUID(final GeneratedPropertyBuilder suid) {
        this.SUID = suid;
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
    AbstractEnumerationBuilder newEnumerationBuilder(final String packageName, final String name) {
        return new CodegenEnumerationBuilder(packageName, name);
    }

    private static final class GTO extends AbstractGeneratedTransferObject {
        private final Restrictions restrictions;
        private final GeneratedProperty SUID;
        private final String reference;
        private final String description;
        private final String moduleName;
        private final SchemaPath schemaPath;

        public GTO(final CodegenGeneratedTOBuilder builder) {
            super(builder);
            this.restrictions = builder.restrictions;
            this.reference = builder.reference;
            this.description = builder.description;
            this.moduleName = builder.moduleName;
            this.schemaPath = builder.schemaPath;

            if (builder.SUID == null) {
                this.SUID = null;
            } else {
                this.SUID = builder.SUID.toInstance(GTO.this);
            }
        }

        @Override
        public Restrictions getRestrictions() {
            return this.restrictions;
        }

        @Override
        public GeneratedProperty getSUID() {
            return this.SUID;
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
