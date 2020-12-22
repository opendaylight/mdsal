/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.Enumeration;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.util.BaseYangTypes;
import org.opendaylight.mdsal.binding.model.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.model.util.BindingTypes;
import org.opendaylight.mdsal.binding.model.util.TypeConstants;
import org.opendaylight.mdsal.binding.model.util.Types;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.AbstractEnumerationBuilder;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.binding.RegexPatterns;
import org.opendaylight.yangtools.yang.binding.TypeObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
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

    /**
     * The generator corresponding to our YANG base type. It produces the superclass of our encapsulated type. If it is
     * {@code null}, this generator is the root of the hierarchy.
     */
    private TypedefGenerator baseGen;
    private TypeReference refType;
    private Map<QName, TypedefGenerator> unionBaseTypes;
    private List<GeneratedType> auxiliaryGeneratedTypes = List.of();

    AbstractTypeObjectGenerator(final T statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
        type = statement().findFirstEffectiveSubstatement(TypeEffectiveStatement.class).orElseThrow();
    }

    @Override
    public final List<GeneratedType> auxiliaryGeneratedTypes() {
        return auxiliaryGeneratedTypes;
    }

    @Override
    final void linkDependencies(final GeneratorContext context) {
        final QName typeName = type.argument();
        if (!isBuiltinName(typeName)) {
            baseGen = context.resolveTypedef(typeName);
            baseGen.addDerivedGenerator(this);
        }
    }

    void bindTypeDefinition(final GeneratorContext context) {
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
        } else if (TypeDefinitions.UNION.equals(arg)) {
            final Map<QName, TypedefGenerator> tmp = new HashMap<>();
            resolveUnionDependencies(tmp, context, type);
            unionBaseTypes = Map.copyOf(tmp);
            LOG.info("Resolved union {} to dependencies {}", type, unionBaseTypes);
        }

        LOG.debug("Resolved base {} to generator {}", type, refType);
        bindDerivedGenerators(refType);
    }

    final void bindTypeDefinition(final @Nullable TypeReference reference) {
        refType = reference;
        LOG.trace("Resolved derived {} to generator {}", type, refType);
    }

    private static boolean isBuiltinName(final QName typeName) {
        return YangConstants.RFC6020_YANG_MODULE.equals(typeName.getModule());
    }

    private static void resolveUnionDependencies(final Map<QName, TypedefGenerator> deps,
            final GeneratorContext context, final TypeEffectiveStatement<?> union) {
        for (EffectiveStatement<?, ?> stmt : union.effectiveSubstatements()) {
            if (stmt instanceof TypeEffectiveStatement) {
                final TypeEffectiveStatement<?> type = (TypeEffectiveStatement<?>) stmt;
                final QName typeName = type.argument();
                if (TypeDefinitions.UNION.equals(typeName)) {
                    resolveUnionDependencies(deps, context, type);
                } else if (!isBuiltinName(typeName) && !deps.containsKey(typeName)) {
                    deps.put(typeName, context.resolveTypedef(typeName));
                }
            }
        }
    }

    abstract void bindDerivedGenerators(@Nullable TypeReference reference);

    @Override
    final ClassPlacement classPlacement() {
        if (refType != null) {
            // Reference types never create a new type
            return ClassPlacement.NONE;
        }
        if (baseGen != null && baseGen.isEnumeration()) {
            // Types derived from an enumeration never create a new type, as that would have to be a subclass of an enum
            // and since enums are final, that can never happen.
            return ClassPlacement.NONE;
        }
        return classPlacementImpl(baseGen);
    }

    abstract @NonNull ClassPlacement classPlacementImpl(@Nullable TypedefGenerator baseGen);

    final boolean isEnumeration() {
        return baseGen != null ? baseGen.isEnumeration() : TypeDefinitions.ENUMERATION.equals(type.argument());
    }

    @Override
    final Type methodReturnType(final TypeBuilderFactory builderFactory) {
        // Override super default and dispatch to subclass, passing it internal state.
        return methodReturnType(builderFactory, baseGen, refType);
    }

    abstract @NonNull Type methodReturnType(@NonNull TypeBuilderFactory builderFactory,
        @Nullable TypedefGenerator baseGen, @Nullable TypeReference refType);

    @Override
    final GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        if (baseGen != null) {
            final GeneratedType baseType = baseGen.getGeneratedType(builderFactory);
            verify(baseType instanceof GeneratedTransferObject, "Unexpected base type %s", baseType);
            return createDerivedType(builderFactory, (GeneratedTransferObject) baseType);
        }

        // FIXME: why do we need this boolean?
        final boolean isTypedef = this instanceof TypedefGenerator;
        final QName arg = type.argument();
        if (TypeDefinitions.BITS.equals(arg)) {
            return createBits(builderFactory, typeName(), currentModule(), extractTypeDefinition(), isTypedef);
        } else if (TypeDefinitions.ENUMERATION.equals(arg)) {
            return createEnumeration(builderFactory, typeName(), currentModule(),
                (EnumTypeDefinition) extractTypeDefinition());
        } else if (TypeDefinitions.UNION.equals(arg)) {
            final List<GeneratedType> tmp = new ArrayList<>(1);
            final GeneratedTransferObject ret = createUnion(tmp, builderFactory, statement(), unionBaseTypes,
                typeName(), currentModule(), type, isTypedef);
            auxiliaryGeneratedTypes = List.copyOf(tmp);
            return ret;
        } else {
            return createSimple(builderFactory, typeName(), currentModule(),
                verifyNotNull(SIMPLE_TYPES.get(arg), "Unhandled type %s", arg), extractTypeDefinition());
        }
    }

    private static @NonNull GeneratedTransferObject createBits(final TypeBuilderFactory builderFactory,
            final JavaTypeName typeName, final ModuleGenerator module, final TypeDefinition<?> typedef,
            final boolean isTypedef) {
        final GeneratedTOBuilder builder = builderFactory.newGeneratedTOBuilder(typeName);
        builder.setTypedef(isTypedef);
        builder.addImplementsType(BindingTypes.TYPE_OBJECT);
        builder.setBaseType(typedef);

        for (Bit bit : ((BitsTypeDefinition) typedef).getBits()) {
            final String name = bit.getName();
            GeneratedPropertyBuilder genPropertyBuilder = builder.addProperty(BindingMapping.getPropertyName(name));
            genPropertyBuilder.setReadOnly(true);
            genPropertyBuilder.setReturnType(BaseYangTypes.BOOLEAN_TYPE);

            builder.addEqualsIdentity(genPropertyBuilder);
            builder.addHashIdentity(genPropertyBuilder);
            builder.addToStringProperty(genPropertyBuilder);
        }

        // builder.setSchemaPath(typedef.getPath());
        builder.setModuleName(module.statement().argument().getLocalName());
        addCodegenInformation(typedef, builder);
        annotateDeprecatedIfNecessary(typedef, builder);
        makeSerializable(builder);
        return builder.build();
    }

    private static @NonNull Enumeration createEnumeration(final TypeBuilderFactory builderFactory,
            final JavaTypeName typeName, final ModuleGenerator module, final EnumTypeDefinition typedef) {
        // TODO units for typedef enum
        final AbstractEnumerationBuilder builder = builderFactory.newEnumerationBuilder(typeName);

        typedef.getDescription().map(BindingGeneratorUtil::encodeAngleBrackets)
            .ifPresent(builder::setDescription);
        typedef.getReference().ifPresent(builder::setReference);

        builder.setModuleName(module.statement().argument().getLocalName());
        builder.updateEnumPairsFromEnumTypeDef(typedef);
        return builder.toInstance();
    }

    private static @NonNull GeneratedType createSimple(final TypeBuilderFactory builderFactory,
            final JavaTypeName typeName, final ModuleGenerator module, final Type javaType,
            final TypeDefinition<?> typedef) {
        final String moduleName = module.statement().argument().getLocalName();
        final GeneratedTOBuilder builder = builderFactory.newGeneratedTOBuilder(typeName);
        builder.setTypedef(true);
        builder.addImplementsType(BindingTypes.scalarTypeObject(javaType));

        final GeneratedPropertyBuilder genPropBuilder = builder.addProperty(TypeConstants.VALUE_PROP);
        genPropBuilder.setReturnType(javaType);
        builder.addEqualsIdentity(genPropBuilder);
        builder.addHashIdentity(genPropBuilder);
        builder.addToStringProperty(genPropBuilder);

        builder.setRestrictions(BindingGeneratorUtil.getRestrictions(typedef));

//        builder.setSchemaPath(typedef.getPath());
        builder.setModuleName(moduleName);
        addCodegenInformation(typedef, builder);

        annotateDeprecatedIfNecessary(typedef, builder);

        if (javaType instanceof ConcreteType && "String".equals(javaType.getName()) && typedef.getBaseType() != null) {
            addStringRegExAsConstant(builder, resolveRegExpressions(typedef));
        }
//        addUnitsToGenTO(genTOBuilder, typedef.getUnits().orElse(null));

        makeSerializable(builder);
        return builder.build();
    }

    private static @NonNull GeneratedTransferObject createUnion(final List<GeneratedType> auxiliaryGeneratedTypes,
            final TypeBuilderFactory builderFactory, final EffectiveStatement<?, ?> definingStatement,
            final Map<QName, TypedefGenerator> unionBaseTypes, final JavaTypeName typeName,
            final ModuleGenerator module, final TypeEffectiveStatement<?> type, final boolean isTypedef) {
        final GeneratedTOBuilder builder = builderFactory.newGeneratedTOBuilder(typeName);
        builder.addImplementsType(BindingTypes.TYPE_OBJECT);
        builder.setIsUnion(true);

//        builder.setSchemaPath(typedef.getPath());
        builder.setModuleName(module.statement().argument().getLocalName());
        addCodegenInformation(definingStatement, builder);

        annotateDeprecatedIfNecessary(definingStatement, builder);

        // Pattern string is the key, XSD regex is the value. The reason for this choice is that the pattern carries
        // also negation information and hence guarantees uniqueness.
        final Map<String, String> expressions = new HashMap<>();
        for (EffectiveStatement<?, ?> stmt : type.effectiveSubstatements()) {
            if (stmt instanceof TypeEffectiveStatement) {
                final TypeEffectiveStatement<?> subType = (TypeEffectiveStatement<?>) stmt;
                final QName subName = subType.argument();
                final String localName = subName.getLocalName();
                final String propName = BindingMapping.getPropertyName(localName);
                if (builder.containsProperty(propName)) {
                    /*
                     *  FIXME: this is not okay, as we are ignoring multiple base types. For example in the case of:
                     *
                     *    type union {
                     *      type string {
                     *        length 1..5;
                     *      }
                     *      type string {
                     *        length 8..10;
                     *      }
                     *    }
                     *
                     *  We should be treating the construct the same as:
                     *
                     *    type union {
                     *      type string {
                     *        length 1..5|8..10;
                     *      }
                     *    }
                     *
                     *  i.e. while we have the same property, that property has multiple alternative restrictions. Our
                     *  type system is probably not up to the snuff, though.
                     */
                    continue;
                }

                final Type generatedType;
                if (TypeDefinitions.UNION.equals(subName)) {
                    final JavaTypeName subUnionName = typeName.createSibling(
                        provideAvailableNameForGenTOBuilder(typeName.simpleName()));
                    final GeneratedTransferObject subUnion = createUnion(auxiliaryGeneratedTypes, builderFactory,
                        definingStatement, unionBaseTypes, subUnionName, module, subType, isTypedef);
                    builder.addEnclosingTransferObject(subUnion);
                    generatedType = subUnion;
                } else if (TypeDefinitions.ENUMERATION.equals(subName)) {
                    generatedType = createEnumeration(builderFactory,
                        typeName.createEnclosed(BindingMapping.getClassName(localName), "$"), module,
                        (EnumTypeDefinition) subType.getTypeDefinition());
                } else if (TypeDefinitions.BITS.equals(subName)) {
                    generatedType = createBits(builderFactory,
                        typeName.createEnclosed(BindingMapping.getClassName(localName), "$"), module,
                        subType.getTypeDefinition(), isTypedef);
                } else {
                    Type baseType = SIMPLE_TYPES.get(subName);
                    if (baseType == null) {

//                      // If we have a base type we should follow the type definition backwards, except for
//                      // identityrefs, as  those do not follow type encapsulation -- we use the general case for that.
//                      if (unionType.getBaseType() != null  && !(unionType instanceof IdentityrefTypeDefinition)) {
//                          resolveExtendedSubtypeAsUnion(builder, unionType, expressions, parentNode);
//                      } else if (unionType instanceof UnionTypeDefinition) {
//                          generatedTOBuilders.addAll(resolveUnionSubtypeAsUnion(builder,
//                              (UnionTypeDefinition) unionType, parentNode));
//                      } else if (unionType instanceof EnumTypeDefinition) {
//                          final Enumeration enumeration = addInnerEnumerationToTypeBuilder(
//                              (EnumTypeDefinition) unionType, unionTypeName, builder);
//                          updateUnionTypeAsProperty(builder, enumeration, unionTypeName);
//                      } else {
//                          final Type javaType = javaTypeForSchemaDefinitionType(unionType, parentNode);
//                          updateUnionTypeAsProperty(builder, javaType, unionTypeName);
//                      }

                        final TypedefGenerator baseGen = verifyNotNull(unionBaseTypes.get(subName), "Cannot resolve %s",
                            subName);
                        baseType = baseGen.getGeneratedType(builderFactory);
                    }

                    final TypeDefinition<?> typedef = type.getTypeDefinition();
                    final Restrictions restrictions = BindingGeneratorUtil.getRestrictions(typedef);
                    generatedType = restrictions.isEmpty() ? baseType : Types.restrictedType(baseType, restrictions);
                }

                final GeneratedPropertyBuilder propBuilder = builder
                    .addProperty(propName)
                    .setReturnType(generatedType);

                builder.addEqualsIdentity(propBuilder);
                builder.addHashIdentity(propBuilder);
                builder.addToStringProperty(propBuilder);
            }
        }
        addStringRegExAsConstant(builder, expressions);

        makeSerializable(builder);
        final GeneratedTransferObject ret = builder.build();

        // Define a corresponding union builder. Typedefs are always anchored at a Java package root,
        // so we are placing the builder alongside the union.
        final GeneratedTOBuilder unionBuilder = builderFactory.newGeneratedTOBuilder(unionBuilderName(typeName));
        unionBuilder.setIsUnionBuilder(true);
        unionBuilder.addMethod("getDefaultInstance")
            .setAccessModifier(AccessModifier.PUBLIC)
            .setStatic(true)
            .setReturnType(ret)
            .addParameter(Types.STRING, "defaultValue");
        auxiliaryGeneratedTypes.add(unionBuilder.build());

        return ret;
    }

    // FIXME: this can be a source of conflicts as we are not guarding against nesting
    private static @NonNull JavaTypeName unionBuilderName(final JavaTypeName unionName) {
        final StringBuilder sb = new StringBuilder();
        for (String part : unionName.localNameComponents()) {
            sb.append(part);
        }
        return JavaTypeName.create(unionName.packageName(), sb.append("Builder").toString());
    }

    // FIXME: we should not rely on TypeDefinition
    abstract @NonNull TypeDefinition<?> extractTypeDefinition();

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
    private static Map<String, String> resolveRegExpressions(final List<PatternConstraint> patternConstraints) {
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

    /**
     * Returns string which contains the same value as <code>name</code> but integer suffix is incremented by one. If
     * <code>name</code> contains no number suffix, a new suffix initialized at 1 is added. A suffix is actually
     * composed of a '$' marker, which is safe, as no YANG identifier can contain '$', and a unsigned decimal integer.
     *
     * @param name string with name of augmented node
     * @return string with the number suffix incremented by one (or 1 is added)
     */
    private static String provideAvailableNameForGenTOBuilder(final String name) {
        final int dollar = name.indexOf('$');
        if (dollar == -1) {
            return name + "$1";
        }

        final int newSuffix = Integer.parseUnsignedInt(name.substring(dollar + 1)) + 1;
        verify(newSuffix > 0, "Suffix counter overflow");
        return name.substring(0, dollar + 1) + newSuffix;
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
