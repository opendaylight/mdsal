/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.Optional;
import com.google.common.base.Preconditions;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTypeForBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.yangtools.yang.common.QName;

@Beta
public final class GeneratedTypeBuilderImpl extends AbstractGeneratedTypeBuilder<GeneratedTypeBuilder> implements
        GeneratedTypeBuilder {

    private String description;
    private String reference;
    private String moduleName;
    private List<QName> schemaPath;

    public GeneratedTypeBuilderImpl(final String packageName, final String name) {
        super(packageName, name);
        setAbstract(true);
    }

    public GeneratedTypeBuilderImpl(final String packageName, final String name,
                                    final boolean isPkNameNormalized,
                                    final boolean isTypeNormalized) {
        super(packageName, name, isPkNameNormalized, isTypeNormalized);
        setAbstract(true);
    }

    @Override
    public GeneratedType toInstance() {
        if (this.isDataObjectType()) {
            return new GeneratedTypeWithBuilderImpl(this);
        } else {
            return new GeneratedTypeImpl(this);
        }
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public void setSchemaPath(List<QName> schemaPath) {
        this.schemaPath = schemaPath;
    }

    @Override
    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GeneratedTransferObject [packageName=");
        builder.append(getPackageName());
        builder.append(", name=");
        builder.append(getName());
        builder.append(", comment=");
        builder.append(getComment());
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
    protected GeneratedTypeBuilderImpl thisInstance() {
        return this;
    }

    private static class GeneratedTypeImpl extends AbstractGeneratedType {

        private final String description;
        private final String reference;
        private final String moduleName;
        private final List<QName> schemaPath;

        public GeneratedTypeImpl(final GeneratedTypeBuilderImpl builder) {
            super(builder);

            this.description = builder.description;
            this.reference = builder.reference;
            this.moduleName = builder.moduleName;
            this.schemaPath = builder.schemaPath;
        }

        @Override
        public Optional<String> getDescription() {
            return Optional.ofNullable(this.description);
        }

        @Override
        public Optional<String> getReference() {
            return Optional.ofNullable(this.reference);
        }

        @Override
        public List<QName> getSchemaPath() {
            return schemaPath;
        }

        @Override
        public String getModuleName() {
            return moduleName;
        }
    }

    private static final class GeneratedTypeWithBuilderImpl extends GeneratedTypeImpl
            implements GeneratedTypeForBuilder {

        private final String basePackageName;
        private final String builderPackageName;

        public GeneratedTypeWithBuilderImpl(GeneratedTypeBuilderImpl builder) {
            super(builder);
            this.basePackageName = builder.getBasePackageName();
            this.builderPackageName = generatePackageNameForBuilder();
        }

        private String generatePackageNameForBuilder() {
            Preconditions.checkArgument(this.basePackageName != null);
            String normalizeBasePackageName = JavaIdentifierNormalizer.normalizeFullPackageName(this.basePackageName);

            if (!normalizeBasePackageName.equals(this.getPackageName())) {
                final String baseName = new StringBuilder(normalizeBasePackageName)
                        .append(".").append(BindingNamespaceType.Data.getPackagePrefix()).toString();

                Preconditions.checkState(this.getPackageName().equals(baseName)
                                || this.getPackageName().contains(baseName),
                        "Package name does not contain base name!");

                return new StringBuilder(normalizeBasePackageName)
                        .append(".")
                        .append(BindingNamespaceType.Builder.getPackagePrefix())
                        .append(this.getPackageName().substring(baseName.length()))
                        .toString();
            } else {
                return new StringBuilder(normalizeBasePackageName)
                        .append(".").append(BindingNamespaceType.Builder.getPackagePrefix()).toString();
            }
        }

        @Override
        public String getPackageNameForBuilder() {
            return this.builderPackageName;
        }
    }
}
