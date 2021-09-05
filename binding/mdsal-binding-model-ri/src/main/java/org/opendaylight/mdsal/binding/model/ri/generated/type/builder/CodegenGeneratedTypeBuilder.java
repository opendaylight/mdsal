/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri.generated.type.builder;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.YangStatementPath;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;

public final class CodegenGeneratedTypeBuilder extends AbstractGeneratedTypeBuilder<GeneratedTypeBuilder> implements
        GeneratedTypeBuilder {

    private String description;
    private String reference;
    private String moduleName;
    private YangStatementPath statementPath;

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
    public void setStatementPath(final YangStatementPath statementPath) {
        this.statementPath = statementPath;
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
        private final YangStatementPath statementPath;

        GeneratedTypeImpl(final CodegenGeneratedTypeBuilder builder) {
            super(builder);

            this.description = builder.description;
            this.reference = builder.reference;
            this.moduleName = builder.moduleName;
            this.statementPath = builder.statementPath;
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
        public YangStatementPath statementPath() {
            return this.statementPath;
        }

        @Override
        public String getModuleName() {
            return this.moduleName;
        }
    }
}
