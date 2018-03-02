/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import static org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil.encodeAngleBrackets;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.opendaylight.mdsal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

/**
 * {@link AbstractTypeProvider} which generates full metadata, suitable for codegen purposes. For runtime purposes,
 * considering using {@link RuntimeTypeProvider}.
 */
@Beta
// FIXME: make this class final after TypeProviderImpl is gone
public class CodegenTypeProvider extends AbstractTypeProvider {
    /**
     * Creates new instance of class <code>TypeProviderImpl</code>.
     *
     * @param schemaContext contains the schema data read from YANG files
     * @throws IllegalArgumentException if <code>schemaContext</code> is null.
     */
    public CodegenTypeProvider(final SchemaContext schemaContext) {
        super(schemaContext);
    }

    @Override
    public void addEnumDescription(final EnumBuilder enumBuilder, final EnumTypeDefinition enumTypeDef) {
        final Optional<String> optDesc = enumTypeDef.getDescription();
        if (optDesc.isPresent()) {
            enumBuilder.setDescription(encodeAngleBrackets(optDesc.get()));
        }
    }

    @Override
    void addCodegenInformation(final GeneratedTypeBuilderBase<?> genTOBuilder, final TypeDefinition<?> typeDef) {
        final Optional<String> optDesc = typeDef.getDescription();
        if (optDesc.isPresent()) {
            genTOBuilder.setDescription(encodeAngleBrackets(optDesc.get()));
        }
        typeDef.getReference().ifPresent(genTOBuilder::setReference);
    }
}
