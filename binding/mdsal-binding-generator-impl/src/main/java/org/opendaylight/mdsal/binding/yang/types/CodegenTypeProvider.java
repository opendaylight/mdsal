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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.mdsal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.AbstractEnumerationBuilder;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.CodegenEnumerationBuilder;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.CodegenGeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.CodegenGeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AbstractTypeProvider} which generates full metadata, suitable for codegen purposes. For runtime purposes,
 * considering using {@link RuntimeTypeProvider}.
 */
@Beta
// FIXME: make this class final after TypeProviderImpl is gone
public class CodegenTypeProvider extends AbstractTypeProvider {
    private static final Logger LOG = LoggerFactory.getLogger(CodegenTypeProvider.class);

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

    /**
     * Converts the pattern constraints to the list of
     * the strings which represents these constraints.
     *
     * @param patternConstraints
     *            list of pattern constraints
     * @return list of strings which represents the constraint patterns
     */
    @Override
    public Map<String, String> resolveRegExpressions(final List<PatternConstraint> patternConstraints) {
        if (patternConstraints.isEmpty()) {
            return ImmutableMap.of();
        }

        final Map<String, String> regExps = Maps.newHashMapWithExpectedSize(patternConstraints.size());
        for (PatternConstraint patternConstraint : patternConstraints) {
            String regEx = patternConstraint.getJavaPatternString();

            // The pattern can be inverted
            final Optional<ModifierKind> optModifier = patternConstraint.getModifier();
            if (optModifier.isPresent()) {
                regEx = applyModifier(optModifier.get(), regEx);
            }

            regExps.put(regEx, patternConstraint.getRegularExpressionString());
        }

        return regExps;
    }

    private static String applyModifier(final ModifierKind modifier, final String pattern) {
        switch (modifier) {
            case INVERT_MATCH:
                return BindingMapping.negatePatternString(pattern);
            default:
                LOG.warn("Ignoring unhandled modifier {}", modifier);
                return pattern;
        }
    }

    @Override
    public GeneratedTOBuilder newGeneratedTOBuilder(final String packageName, final String name) {
        return new CodegenGeneratedTOBuilder(packageName, name);
    }

    @Override
    public GeneratedTypeBuilder newGeneratedTypeBuilder(final String packageName, final String name) {
        return new CodegenGeneratedTypeBuilder(packageName, name);
    }

    @Override
    public AbstractEnumerationBuilder newEnumerationBuilder(final String packageName, final String name) {
        return new CodegenEnumerationBuilder(packageName, name);
    }
}
