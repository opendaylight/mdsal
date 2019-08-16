/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.util.generated.type.builder;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.TypeComment;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public final class RuntimeGeneratedTypeBuilder extends AbstractGeneratedTypeBuilder<GeneratedTypeBuilder> implements
        GeneratedTypeBuilder {

    public RuntimeGeneratedTypeBuilder(final JavaTypeName identifier) {
        super(identifier);
        setAbstract(true);
    }

    @Override
    public GeneratedType build() {
        return new GeneratedTypeImpl(this);
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
    AbstractEnumerationBuilder newEnumerationBuilder(final JavaTypeName identifier) {
        return new RuntimeEnumerationBuilder(identifier);
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
    protected RuntimeGeneratedTypeBuilder thisInstance() {
        return this;
    }

    private static final class GeneratedTypeImpl extends AbstractGeneratedType {
        GeneratedTypeImpl(final RuntimeGeneratedTypeBuilder builder) {
            super(builder);
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
