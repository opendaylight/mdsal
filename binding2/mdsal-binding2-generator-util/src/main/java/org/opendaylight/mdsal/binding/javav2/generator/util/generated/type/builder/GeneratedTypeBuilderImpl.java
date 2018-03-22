/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Optional;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTypeForBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.TypeComment;
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
    private boolean isWithBuilder = false;
    private String normalizedRootPackageName = null;

    public GeneratedTypeBuilderImpl(final String packageName, final String name, final ModuleContext context) {
        super(packageName, name, context);
        setAbstract(true);
    }

    public GeneratedTypeBuilderImpl(final String packageName, final String name, final boolean isPkNameNormalized,
            final boolean isTypeNormalized, final ModuleContext context) {
        super(packageName, name, isPkNameNormalized, isTypeNormalized, context);
        setAbstract(true);
    }

    @Override
    public GeneratedType toInstance() {
        if (this.isWithBuilder()) {
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
    protected GeneratedTypeBuilderImpl thisInstance() {
        return this;
    }

    public boolean isWithBuilder() {
        return isWithBuilder;
    }

    public void setWithBuilder(boolean withBuilder) {
        isWithBuilder = withBuilder;
    }

    public String getNormalizedRootPackageName() {
        return normalizedRootPackageName;
    }

    public void setNormalizedRootPackageName(final String normalizedRootPackageName) {
        this.normalizedRootPackageName = normalizedRootPackageName;
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

        private final String normalizedRootPackageName;
        private final String normalizedPkgNameForBuilder;

        public GeneratedTypeWithBuilderImpl(GeneratedTypeBuilderImpl builder) {
            super(builder);
            Preconditions.checkState(builder.getNormalizedRootPackageName() != null,
                    "Base package name can not be null for type with builder!");
            this.normalizedRootPackageName = builder.getNormalizedRootPackageName();
            this.normalizedPkgNameForBuilder = BindingGeneratorUtil.replacePackageTopNamespace(
                this.normalizedRootPackageName, this.getPackageName(),
                BindingNamespaceType.Data, BindingNamespaceType.Builder);
        }

        @Override
        public String getPackageNameForBuilder() {
            return this.normalizedPkgNameForBuilder;
        }
    }
}
