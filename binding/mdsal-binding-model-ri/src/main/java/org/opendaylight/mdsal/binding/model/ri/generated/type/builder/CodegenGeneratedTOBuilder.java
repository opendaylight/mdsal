/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.YangStatementPath;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;

public class CodegenGeneratedTOBuilder extends AbstractGeneratedTOBuilder {
    private Restrictions restrictions;
    private GeneratedPropertyBuilder suid;
    private String reference;
    private String description;
    private String moduleName;
    private YangStatementPath statementPath;

    public CodegenGeneratedTOBuilder(final JavaTypeName identifier) {
        super(identifier);
    }

    @Override
    public final void setRestrictions(final Restrictions restrictions) {
        this.restrictions = restrictions;
    }

    @Override
    public final void setSUID(final GeneratedPropertyBuilder newSuid) {
        this.suid = newSuid;
    }

    @Override
    public final void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public final void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public final void setStatementPath(final YangStatementPath statementPath) {
        this.statementPath = requireNonNull(statementPath);
    }

    @Override
    public final void setReference(final String reference) {
        this.reference = reference;
    }

    @Override
    public final GeneratedTransferObject build() {
        return new GTO(this);
    }

    private static final class GTO extends AbstractGeneratedTransferObject {
        private final Restrictions restrictions;
        private final GeneratedProperty suid;
        private final String reference;
        private final String description;
        private final String moduleName;
        private final YangStatementPath statementPath;

        GTO(final CodegenGeneratedTOBuilder builder) {
            super(builder);
            this.restrictions = builder.restrictions;
            this.reference = builder.reference;
            this.description = builder.description;
            this.moduleName = builder.moduleName;
            this.statementPath = builder.statementPath;

            if (builder.suid == null) {
                this.suid = null;
            } else {
                this.suid = builder.suid.toInstance();
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
        public YangStatementPath statementPath() {
            return this.statementPath;
        }

        @Override
        public String getModuleName() {
            return this.moduleName;
        }
    }
}
