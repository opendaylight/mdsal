/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.binding.model.api.BoxableGeneratedType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;

public final class CodegenGeneratedTypeBuilder extends AbstractGeneratedTypeBuilder<GeneratedTypeBuilder> implements
        GeneratedTypeBuilder {

    private String description;
    private String reference;
    private String moduleName;
    private JavaTypeName parentType;

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
    public void setReference(final String reference) {
        this.reference = reference;
    }

    @Override
    public void setBoxableParent(final JavaTypeName parent) {
        this.parentType = requireNonNull(parent);
    }

    @Override
    public GeneratedType build() {
        return parentType == null ? new GeneratedTypeImpl(this) : new BoxableGeneratedTypeImpl(this);
    }

    @Override
    protected CodegenGeneratedTypeBuilder thisInstance() {
        return this;
    }

    private static class GeneratedTypeImpl extends AbstractGeneratedType {
        private final String description;
        private final String reference;
        private final String moduleName;

        GeneratedTypeImpl(final CodegenGeneratedTypeBuilder builder) {
            super(builder);

            description = builder.description;
            reference = builder.reference;
            moduleName = builder.moduleName;
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
        public String getModuleName() {
            return moduleName;
        }
    }

    private static final class BoxableGeneratedTypeImpl extends GeneratedTypeImpl implements BoxableGeneratedType {
        private final JavaTypeName parentType;

        BoxableGeneratedTypeImpl(final CodegenGeneratedTypeBuilder builder) {
            super(builder);

            parentType = builder.parentType;
        }

        @Override
        public JavaTypeName getParentType() {
            return parentType;
        }
    }
}
