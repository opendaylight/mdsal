/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util.generated.type.builder;

import java.util.Set;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.TypeComment;
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
        setAbstract(true);
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
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("GeneratedTransferObject [packageName=");
        builder.append(getPackageName());
        builder.append(", name=");
        builder.append(getName());

        final TypeComment comment = getComment();
        if (comment != null) {
            builder.append(", comment=");
            builder.append(comment.getJavadoc());
        }
        builder.append(", annotations=");
        builder.append(getAnnotations());
        builder.append(", implements=");
        builder.append(getImplementsTypes());
        builder.append(", enclosedTypes=");
        builder.append(getEnclosedTypes());
        builder.append(", constants=");
        builder.append(getConstants());
        builder.append(", enumerations=");
        builder.append(getEnumerations());
        builder.append(", properties=");
        builder.append(", methods=");
        builder.append(getMethodDefinitions());
        builder.append("]");
        return builder.toString();
    }

    @Override
    public GeneratedType build() {
        return new GeneratedTypeImpl(this);
    }

    @Override
    AbstractEnumerationBuilder newEnumerationBuilder(final JavaTypeName identifier) {
        return new CodegenEnumerationBuilder(identifier);
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

            this.description = builder.description;
            this.reference = builder.reference;
            this.moduleName = builder.moduleName;
            this.schemaPath = builder.schemaPath;
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
