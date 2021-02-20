/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.util.BaseYangTypes;
import org.opendaylight.mdsal.binding.model.util.TypeConstants;
import org.opendaylight.mdsal.binding.model.util.Types;
import org.opendaylight.yangtools.yang.binding.RegexPatterns;
import org.opendaylight.yangtools.yang.binding.TypeObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.TypeDefinitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common base class for {@link TypedefGenerator} and {@link AbstractTypeAwareGenerator}. This encompasses three
 * different statements with two different semantics:
 * <ul>
 *   <li>{@link TypedefGenerator}s always result in a generated {@link TypeObject}, even if the semantics is exactly
 *       the same as its base type. This aligns with {@code typedef} defining a new type.<li>
 *   <li>{@link LeafGenerator}s and {@link LeafListGenerator}s, on the other hand, do not generate a {@link TypeObject}
 *       unless absolutely necassary. This aligns with {@code leaf} and {@code leaf-list} being mapped onto a property
 *       of its parent type.<li>
 * </ul>
 *
 * <p>
 * To throw a bit of confusion into the mix, there are three exceptions to those rules:
 * <ul>
 *   <li>
 *     {@code identityref} definitions never result in a type definition being emitted. The reason for this has to do
 *     with identity type mapping as well as history of our codebase.
 *
 *     <p>
 *     The problem at hand is inconsistency between the fact that identity is mapped to a {@link Class}, which is also
 *     returned from leaves which specify it like this:
 *     <pre>
 *       <code>
 *         identity iden;
 *
 *         container foo {
 *           leaf foo {
 *             type identityref {
 *               base iden;
 *             }
 *           }
 *         }
 *       </code>
 *     </pre>
 *     which results in fine-looking
 *     <pre>
 *       <code>
 *         interface Foo {
 *           Class&lt;? extends Iden&gt; getFoo();
 *         }
 *       </code>
 *     </pre>
 *
 *     <p>
 *     This gets more dicey if we decide to extend the previous snippet to also include:
 *     <pre>
 *       <code>
 *         typedef bar-ref {
 *           type identityref {
 *             base iden;
 *           }
 *         }
 *
 *         container bar {
 *           leaf bar {
 *             type bar-ref;
 *           }
 *         }
 *       </code>
 *     </pre>
 *
 *     <p>
 *     Now we have competing requirements: {@code typedef} would like us to use encapsulation to capture the defined
 *     type, while {@code getBar()} wants us to retain shape with getFoo(), as it should not matter how the
 *     {@code identityref} was formed. We need to pick between:
 *     <ol>
 *       <li>
 *         <pre>
 *           <code>
 *             public class BarRef extends ScalarTypeObject&lt;Class&lt;? extends Iden&gt;&gt; {
 *               Class&lt;? extends Iden&gt; getValue() {
 *                 // ...
 *               }
 *             }
 *
 *             interface Bar {
 *               BarRef getBar();
 *             }
 *           </code>
 *         </pre>
 *       </li>
 *       <li>
 *         <pre>
 *           <code>
 *             interface Bar {
 *               Class&lt;? extends Iden&gt; getBar();
 *             }
 *           </code>
 *         </pre>
 *       </li>
 *     </ol>
 *
 *     <p>
 *     Here the second option is more user-friendly, as the type system works along the lines of <b>reference</b>
 *     semantics, treating and {@code Bar.getBar()} and {@code Foo.getFoo()} as equivalent. The first option would
 *     force users to go through explicit encapsulation, for no added benefit as the {@code typedef} cannot possibly add
 *     anything useful to the actual type semantics.
 *   </li>
 *   <li>
 *     {@code leafref} definitions never result in a type definition being emitted. The reasons for this are similar to
 *     {@code identityref}, but have an additional twist: a {@leafref} can target a relative path, which may only be
 *     resolved at a particular instantiation.
 *
 *     Take the example of the following model:
 *     <pre>
 *       <code>
 *         grouping grp {
 *           typedef ref {
 *             type leafref {
 *               path ../xyzzy;
 *             }
 *           }
 *
 *           leaf foo {
 *             type ref;
 *           }
 *         }
 *
 *         container bar {
             uses grp;
 *
 *           leaf xyzzy {
 *             type string;
 *           }
 *         }
 *
 *         container baz {
 *           uses grp;
 *
 *           leaf xyzzy {
 *             type int32;
 *           }
 *         }
 *       </code>
 *     </pre>
 *
 *     The {@code typedef ref} points to outside of the grouping, and hence the type of {@code leaf foo} is polymorphic:
 *     the definition in {@code grouping grp} needs to use {@code Object}, whereas the instantiations in
 *     {@code container bar} and {@code container baz} need to use {@code String} and {@link Integer} respectively.
 *     Expressing the resulting interface contracts requires return type specialization and run-time checks. An
 *     intermediate class generated for the typedef would end up being a hindrance without any benefit.
 *   <li>
 *   <li>
 *     {@code enumeration} definitions never result in a derived type. This is because these are mapped to Java
 *     {@code enum}, which does not allow subclassing.
 *   <li>
 * </ul>
 *
 * <p>
 * At the end of the day, the mechanic translation rules are giving way to correctly mapping the semantics -- which in
 * both of the exception cases boil down to tracking type indirection. Intermediate constructs involved in tracking
 * type indirection in YANG constructs is therefore explicitly excluded from the generated Java code, but the Binding
 * Specification still takes them into account when determining types as outlined above.
 */
abstract class AbstractTypeObjectGenerator<T extends EffectiveStatement<?, ?>> extends AbstractDependentGenerator<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTypeObjectGenerator.class);
    static final ImmutableMap<QName, Type> SIMPLE_TYPES = ImmutableMap.<QName, Type>builder()
        .put(TypeDefinitions.BINARY, BaseYangTypes.BINARY_TYPE)
        .put(TypeDefinitions.BOOLEAN, BaseYangTypes.BOOLEAN_TYPE)
        .put(TypeDefinitions.DECIMAL64, BaseYangTypes.DECIMAL64_TYPE)
        .put(TypeDefinitions.EMPTY, BaseYangTypes.EMPTY_TYPE)
        .put(TypeDefinitions.INSTANCE_IDENTIFIER, BaseYangTypes.INSTANCE_IDENTIFIER)
        .put(TypeDefinitions.INT8, BaseYangTypes.INT8_TYPE)
        .put(TypeDefinitions.INT16, BaseYangTypes.INT16_TYPE)
        .put(TypeDefinitions.INT32, BaseYangTypes.INT32_TYPE)
        .put(TypeDefinitions.INT64, BaseYangTypes.INT64_TYPE)
        .put(TypeDefinitions.STRING, BaseYangTypes.STRING_TYPE)
        .put(TypeDefinitions.UINT8, BaseYangTypes.UINT8_TYPE)
        .put(TypeDefinitions.UINT16, BaseYangTypes.UINT16_TYPE)
        .put(TypeDefinitions.UINT32, BaseYangTypes.UINT32_TYPE)
        .put(TypeDefinitions.UINT64, BaseYangTypes.UINT64_TYPE)
        .build();

    final TypeEffectiveStatement<?> type;

    private TypedefGenerator baseGen;
    private TypeReference refType;

    AbstractTypeObjectGenerator(final T statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
        type = statement().findFirstEffectiveSubstatement(TypeEffectiveStatement.class).orElseThrow();
    }

    @Override
    final void linkDependencies(final GeneratorContext context) {
        final QName typeName = type.argument();
        if (!YangConstants.RFC6020_YANG_MODULE.equals(typeName.getModule())) {
            baseGen = context.resolveTypedef(typeName);
            baseGen.addDerivedGenerator(this);
        }
    }

    final void bindTypeDefinition(final GeneratorContext context) {
        if (baseGen != null) {
            // We have registered with baseGen, it will push the type to us
            return;
        }

        final QName arg = type.argument();
        if (TypeDefinitions.IDENTITYREF.equals(arg)) {
            refType = TypeReference.identityRef(type.streamEffectiveSubstatements(BaseEffectiveStatement.class)
                .map(BaseEffectiveStatement::argument)
                .map(context::resolveIdentity)
                .collect(Collectors.toUnmodifiableList()));
        } else if (TypeDefinitions.LEAFREF.equals(arg)) {
            refType = TypeReference.leafRef(context.resolveLeafref(
                type.findFirstEffectiveSubstatementArgument(PathEffectiveStatement.class).orElseThrow()));
        }

        LOG.info("Resolved base {} to generator {}", type, refType);
        bindDerivedGenerators(refType);
    }

    final void bindTypeDefinition(final @Nullable TypeReference reference) {
        refType = reference;
        LOG.trace("Resolved derived {} to generator {}", type, refType);
    }

    abstract void bindDerivedGenerators(@Nullable TypeReference reference);

    @Override
    final ClassPlacement classPlacement() {
        // We exclude references (as there is nothing to do) and enumerations (as that would require subclassing enum)
        return refType != null || baseGen != null && baseGen.isEnumeration() ? ClassPlacement.NONE
            : classPlacementImpl();
    }

    abstract @NonNull ClassPlacement classPlacementImpl();

    final boolean isEnumeration() {
        return baseGen != null ? baseGen.isEnumeration() : TypeDefinitions.ENUMERATION.equals(type.argument());
    }

    @Override
    final GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        if (baseGen == null) {
            return createRootType(builderFactory);
        }

        final GeneratedType baseType = baseGen.getType(builderFactory);
        verify(baseType instanceof GeneratedTransferObject, "Unexpected base type %s", baseType);
        return createDerivedType(builderFactory, (GeneratedTransferObject) baseType);
    }

    abstract @NonNull GeneratedType createRootType(@NonNull TypeBuilderFactory builderFactory);

    abstract @NonNull GeneratedTransferObject createDerivedType(@NonNull TypeBuilderFactory builderFactory,
        @NonNull GeneratedTransferObject baseType);

    /**
     * Adds to the {@code genTOBuilder} the constant which contains regular expressions from the {@code expressions}.
     *
     * @param genTOBuilder generated TO builder to which are {@code regular expressions} added
     * @param expressions list of string which represent regular expressions
     */
    static void addStringRegExAsConstant(final GeneratedTOBuilder genTOBuilder, final Map<String, String> expressions) {
        if (!expressions.isEmpty()) {
            genTOBuilder.addConstant(Types.listTypeFor(BaseYangTypes.STRING_TYPE), TypeConstants.PATTERN_CONSTANT_NAME,
                ImmutableMap.copyOf(expressions));
        }
    }

    /**
     * Converts the pattern constraints from {@code typedef} to the list of the strings which represents these
     * constraints.
     *
     * @param typedef extended type in which are the pattern constraints sought
     * @return list of strings which represents the constraint patterns
     * @throws IllegalArgumentException if <code>typedef</code> equals null
     */
    static Map<String, String> resolveRegExpressions(final TypeDefinition<?> typedef) {
        return typedef instanceof StringTypeDefinition
            // TODO: run diff against base ?
            ? resolveRegExpressions(((StringTypeDefinition) typedef).getPatternConstraints())
                : ImmutableMap.of();
    }

    /**
     * Converts the pattern constraints to the list of the strings which represents these constraints.
     *
     * @param patternConstraints list of pattern constraints
     * @return list of strings which represents the constraint patterns
     */
    static Map<String, String> resolveRegExpressions(final List<PatternConstraint> patternConstraints) {
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
                return RegexPatterns.negatePatternString(pattern);
            default:
                LOG.warn("Ignoring unhandled modifier {}", modifier);
                return pattern;
        }
    }
}
