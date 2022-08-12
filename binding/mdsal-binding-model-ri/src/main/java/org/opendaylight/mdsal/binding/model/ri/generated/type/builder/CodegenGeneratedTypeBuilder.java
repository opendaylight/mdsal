/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri.generated.type.builder;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaClassCompleteness;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public final class CodegenGeneratedTypeBuilder extends AbstractGeneratedTypeBuilder<GeneratedTypeBuilder> implements
        GeneratedTypeBuilder {

    private String description;
    private String reference;
    private String moduleName;
    private SchemaPath schemaPath;

    public CodegenGeneratedTypeBuilder(final JavaTypeName identifier) {
        super(identifier);
        setClassCompleteness(JavaClassCompleteness.ABSTRACT);
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
    public GeneratedType build() {
        return new GeneratedTypeImpl(this);
    }

    @Override
    protected CodegenGeneratedTypeBuilder thisInstance() {
        return this;
    }

    private static final class GeneratedTypeImpl extends AbstractGeneratedType {

        private final String description;
        private final String reference;
        private final String moduleName;
        private final SchemaPath schemaPath;

        GeneratedTypeImpl(final CodegenGeneratedTypeBuilder builder) {
            super(builder);

            description = builder.description;
            reference = builder.reference;
            moduleName = builder.moduleName;
            schemaPath = builder.schemaPath;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getReference() {
            return reference;
        }

        @Override
        public Iterable<QName> getSchemaPath() {
            return schemaPath.getPathFromRoot();
        }

        @Override
        public String getModuleName() {
            return moduleName;
        }
    }
}
