/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.yang.types;

import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.encodeAngleBrackets;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.packageNameWithNamespacePrefix;
import static org.opendaylight.mdsal.binding.javav2.generator.util.Types.getOuterClassPackageName;
import static org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeGenHelper.addStringRegExAsConstant;
import static org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeGenHelper.baseTypeDefForExtendedType;
import static org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeGenHelper.getAllTypedefs;
import static org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeGenHelper.getParentModule;
import static org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeGenHelper.makeSerializable;
import static org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeGenHelper.provideGeneratedTOFromExtendedType;
import static org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeGenHelper.provideTypeForEnum;
import static org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeGenHelper.resolveRegExpressionsFromTypedef;
import static org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeGenHelper.sortTypeDefinitionAccordingDepth;
import static org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeGenHelper.wrapJavaTypeIntoTO;
import static org.opendaylight.mdsal.binding.javav2.util.BindingMapping.getRootPackageName;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findDataSchemaNode;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findDataSchemaNodeForRelativeXPath;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findParentModule;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.generator.spi.TypeProvider;
import org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifier;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedPropertyBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.javav2.model.api.Enumeration;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.javav2.model.api.Restrictions;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.EnumBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.ModuleDependencySort;
import org.opendaylight.yangtools.yang.model.util.RevisionAwareXPathImpl;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.util.YangValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
public final class TypeProviderImpl implements TypeProvider {

    private static final Logger LOG = LoggerFactory.getLogger(TypeProviderImpl.class);
    private static final Pattern NUMBERS_PATTERN = Pattern.compile("[0-9]+\\z");

    /**
     * Contains the schema data red from YANG files.
     */
    private final SchemaContext schemaContext;

    /**
     * Map<moduleName, Map<moduleDate, Map<typeName, type>>>
     */
    private final Map<String, Map<Date, Map<String, Type>>> genTypeDefsContextMap;

    /**
     * Map which maps schema paths to JAVA <code>Type</code>.
     */
    private final Map<SchemaPath, Type> referencedTypes;

    /**
     * Map for additional types e.g unions
     */
    private final Map<Module, Set<Type>> additionalTypes;

    /**
     * Creates new instance of class <code>TypeProviderImpl</code>.
     *
     * @param schemaContext
     *            contains the schema data red from YANG files
     * @throws IllegalArgumentException
     *             if <code>schemaContext</code> equal null.
     */
    public TypeProviderImpl(final SchemaContext schemaContext) {
        this.schemaContext = schemaContext;
        this.genTypeDefsContextMap = new HashMap<>();
        this.referencedTypes = new HashMap<>();
        this.additionalTypes = new HashMap<>();
        resolveTypeDefsFromContext(schemaContext, this.genTypeDefsContextMap, this.additionalTypes);
    }

    @Override
    public Type javaTypeForSchemaDefinitionType(final TypeDefinition<?> type, final SchemaNode parentNode,
            ModuleContext context) {
        return javaTypeForSchemaDefinitionType(type, parentNode, null, context);
    }

    /**
     * Converts schema definition type <code>typeDefinition</code> to JAVA
     * <code>Type</code>
     *
     * @param type
     *            type definition which is converted to JAVA type
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>typeDefinition</code> equal null</li>
     *             <li>if QName of <code>typeDefinition</code> equal null</li>
     *             <li>if name of <code>typeDefinition</code> equal null</li>
     *             </ul>
     */
    @Override
    public Type javaTypeForSchemaDefinitionType(final TypeDefinition<?> type, final SchemaNode parentNode, final
            Restrictions restrictions, ModuleContext context) {
        return javaTypeForSchemaDefType(type, parentNode, restrictions, this.schemaContext, this
                .genTypeDefsContextMap, context);
    }

    @Override
    public String getTypeDefaultConstruction(final LeafSchemaNode node) {
        return null;
    }

    @Override
    public String getConstructorPropertyName(final SchemaNode node) {
        return null;
    }

    @Override
    public String getParamNameFromType(final TypeDefinition<?> type) {
        return null;
    }

    public Map<String, Map<Date, Map<String, Type>>> getGenTypeDefsContextMap() {
        return this.genTypeDefsContextMap;
    }

    /**
     * Passes through all modules and through all its type definitions and
     * convert it to generated types.
     *
     * The modules are firstly sorted by mutual dependencies. The modules are
     * sequentially passed. All type definitions of a module are at the
     * beginning sorted so that type definition with less amount of references
     * to other type definition are processed first.<br />
     * For each module is created mapping record in the map
     * {@link TypeProviderImpl#genTypeDefsContextMap genTypeDefsContextMap}
     * which map current module name to the map which maps type names to
     * returned types (generated types).
     *
     */
    private void resolveTypeDefsFromContext(final SchemaContext schemaContext, final Map<String, Map<Date, Map<String,
            Type>>> genTypeDefsContextMap, final Map<Module, Set<Type>> additionalTypes) {

        final Set<Module> modules = schemaContext.getModules();
        Preconditions.checkArgument(modules != null, "Set of Modules cannot be NULL!");
        final List<Module> modulesSortedByDependency = ModuleDependencySort.sort(modules);

        for (final Module module : modulesSortedByDependency) {
            Map<Date, Map<String, Type>> dateTypeMap = genTypeDefsContextMap.get(module.getName());
            if (dateTypeMap == null) {
                dateTypeMap = new HashMap<>();
            }
            dateTypeMap.put(module.getRevision(), Collections.emptyMap());
            genTypeDefsContextMap.put(module.getName(), dateTypeMap);
        }

        modulesSortedByDependency.stream().filter(module -> module != null).forEach(module -> {
            ModuleContext context = new ModuleContext();
            final String basePackageName = packageNameWithNamespacePrefix(getRootPackageName(module),
                    BindingNamespaceType.Typedef);
            final List<TypeDefinition<?>> typeDefinitions = getAllTypedefs(module);
            final List<TypeDefinition<?>> listTypeDefinitions = sortTypeDefinitionAccordingDepth(typeDefinitions);
            if (listTypeDefinitions != null) {
                for (final TypeDefinition<?> typedef : listTypeDefinitions) {
                    typedefToGeneratedType(basePackageName, module, typedef, genTypeDefsContextMap,
                            additionalTypes, schemaContext, context);
                }
            }
        });
    }

    /**
     * Converts <code>typeDefinition</code> to concrete JAVA <code>Type</code>.
     *
     * @param typeDefinition
     *            type definition which should be converted to JAVA
     *            <code>Type</code>
     * @return JAVA <code>Type</code> which represents
     *         <code>typeDefinition</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>typeDefinition</code> equal null</li>
     *             <li>if Q name of <code>typeDefinition</code></li>
     *             <li>if name of <code>typeDefinition</code></li>
     *             </ul>
     */
    public Type generatedTypeForExtendedDefinitionType(final TypeDefinition<?> typeDefinition, final SchemaNode parentNode) {
        Preconditions.checkArgument(typeDefinition != null, "Type Definition cannot be NULL!");
        Preconditions.checkArgument(typeDefinition.getQName().getLocalName() != null,
                "Type Definitions Local Name cannot be NULL!");

        final TypeDefinition<?> baseTypeDef = baseTypeDefForExtendedType(typeDefinition);
        if (!(baseTypeDef instanceof LeafrefTypeDefinition) && !(baseTypeDef instanceof IdentityrefTypeDefinition)) {
            final Module module = findParentModule(this.schemaContext, parentNode);

            if (module != null) {
                final Map<Date, Map<String, Type>> modulesByDate = this.genTypeDefsContextMap.get(module.getName());
                final Map<String, Type> genTOs = modulesByDate.get(module.getRevision());
                if (genTOs != null) {
                    return genTOs.get(typeDefinition.getQName().getLocalName());
                }
            }
        }
        return null;
    }

    /**
     * Puts <code>refType</code> to map with key <code>refTypePath</code>
     *
     * @param refTypePath
     *            schema path used as the map key
     * @param refType
     *            type which represents the map value
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>refTypePath</code> equal null</li>
     *             <li>if <code>refType</code> equal null</li>
     *             </ul>
     *
     */
    public void putReferencedType(final SchemaPath refTypePath, final Type refType) {
        Preconditions.checkArgument(refTypePath != null,
                "Path reference of Enumeration Type Definition cannot be NULL!");
        Preconditions.checkArgument(refType != null, "Reference to Enumeration Type cannot be NULL!");
        this.referencedTypes.put(refTypePath, refType);
    }

    /**
     * Converts <code>typeDef</code> which should be of the type
     * <code>BitsTypeDefinition</code> to <code>GeneratedTOBuilder</code>.
     *
     * All the bits of the typeDef are added to returning generated TO as
     * properties.
     *
     * @param basePackageName
     *            string with name of package to which the module belongs
     * @param typeDef
     *            type definition from which is the generated TO builder created
     * @param typeDefName
     *            string with the name for generated TO builder
     * @return generated TO builder which represents <code>typeDef</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>typeDef</code> equals null</li>
     *             <li>if <code>basePackageName</code> equals null</li>
     *             </ul>
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public GeneratedTOBuilder provideGeneratedTOBuilderForBitsTypeDefinition(final String basePackageName,
            final TypeDefinition<?> typeDef, final String typeDefName, final String moduleName, ModuleContext context) {

        Preconditions.checkArgument(typeDef != null, "typeDef cannot be NULL!");
        Preconditions.checkArgument(basePackageName != null, "Base Package Name cannot be NULL!");

        if (typeDef instanceof BitsTypeDefinition) {
            final BitsTypeDefinition bitsTypeDefinition = (BitsTypeDefinition) typeDef;

            final GeneratedTOBuilderImpl genTOBuilder = new GeneratedTOBuilderImpl(basePackageName, typeDefName,
                    true, false, context);
            final String typedefDescription = encodeAngleBrackets(typeDef.getDescription());

            genTOBuilder.setDescription(typedefDescription);
            genTOBuilder.setReference(typeDef.getReference());
            genTOBuilder.setSchemaPath((List) typeDef.getPath().getPathFromRoot());
            genTOBuilder.setModuleName(moduleName);
            genTOBuilder.setBaseType(typeDef);

            final List<Bit> bitList = bitsTypeDefinition.getBits();
            GeneratedPropertyBuilder genPropertyBuilder;
            for (final Bit bit : bitList) {
                final String name = bit.getName();
                genPropertyBuilder =
                        genTOBuilder.addProperty(JavaIdentifierNormalizer.normalizeSpecificIdentifier(name, JavaIdentifier.METHOD));
                genPropertyBuilder.setReadOnly(true);
                genPropertyBuilder.setReturnType(BaseYangTypes.BOOLEAN_TYPE);

                genTOBuilder.addEqualsIdentity(genPropertyBuilder);
                genTOBuilder.addHashIdentity(genPropertyBuilder);
                genTOBuilder.addToStringProperty(genPropertyBuilder);
            }

            return genTOBuilder;
        }
        return null;
    }

    /**
     * Converts <code>typedef</code> to generated TO with
     * <code>typeDefName</code>. Every union type from <code>typedef</code> is
     * added to generated TO builder as property.
     *
     * @param basePackageName
     *            string with name of package to which the module belongs
     * @param typedef
     *            type definition which should be of type
     *            <code>UnionTypeDefinition</code>
     * @param typeDefName
     *            string with name for generated TO
     * @return generated TO builder which represents <code>typedef</code>
     * @throws NullPointerException
     *             <ul>
     *             <li>if <code>basePackageName</code> is null</li>
     *             <li>if <code>typedef</code> is null</li>
     *             <li>if QName of <code>typedef</code> is null</li>
     *             </ul>
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<GeneratedTOBuilder> provideGeneratedTOBuildersForUnionTypeDef(final String basePackageName,
            final UnionTypeDefinition typedef, final String typeDefName, final SchemaNode parentNode,
            final SchemaContext schemaContext, final Map<String, Map<Date, Map<String, Type>>> genTypeDefsContextMap, ModuleContext context) {
        Preconditions.checkNotNull(basePackageName, "Base Package Name cannot be NULL!");
        Preconditions.checkNotNull(typedef, "Type Definition cannot be NULL!");
        Preconditions.checkNotNull(typedef.getQName(), "Type definition QName cannot be NULL!");

        final List<GeneratedTOBuilder> generatedTOBuilders = new ArrayList<>();
        final List<TypeDefinition<?>> unionTypes = typedef.getTypes();
        final Module module = findParentModule(schemaContext, parentNode);

        final GeneratedTOBuilderImpl unionGenTOBuilder;
        unionGenTOBuilder = new GeneratedTOBuilderImpl(basePackageName, typeDefName, true, false,
                context);
        final String typedefDescription = encodeAngleBrackets(typedef.getDescription());
        unionGenTOBuilder.setDescription(typedefDescription);
        unionGenTOBuilder.setReference(typedef.getReference());
        unionGenTOBuilder.setSchemaPath((List) typedef.getPath().getPathFromRoot());
        unionGenTOBuilder.setModuleName(module.getName());

        generatedTOBuilders.add(unionGenTOBuilder);
        unionGenTOBuilder.setIsUnion(true);
        final List<String> regularExpressions = new ArrayList<>();
        for (final TypeDefinition<?> unionType : unionTypes) {
            final String unionTypeName = unionType.getQName().getLocalName();
            if (unionType.getBaseType() != null) {
                resolveExtendedSubtypeAsUnion(unionGenTOBuilder, unionType, regularExpressions,
                        parentNode, schemaContext, genTypeDefsContextMap);
            } else if (unionType instanceof UnionTypeDefinition) {
                generatedTOBuilders.add(resolveUnionSubtypeAsUnion(unionGenTOBuilder, (UnionTypeDefinition) unionType,
                        unionGenTOBuilder.getFullyQualifiedName(), parentNode, schemaContext, genTypeDefsContextMap,
                        context));
            } else if (unionType instanceof EnumTypeDefinition) {
                final Enumeration enumeration = addInnerEnumerationToTypeBuilder((EnumTypeDefinition) unionType,
                        unionTypeName, unionGenTOBuilder, context);
                updateUnionTypeAsProperty(unionGenTOBuilder, enumeration, unionTypeName);
            } else {
                final Type javaType = javaTypeForSchemaDefType(unionType, parentNode, null, schemaContext,
                        genTypeDefsContextMap, context);
                updateUnionTypeAsProperty(unionGenTOBuilder, javaType, unionTypeName);
            }
        }
        if (!regularExpressions.isEmpty()) {
            addStringRegExAsConstant(unionGenTOBuilder, regularExpressions);
        }

        //storeGenTO(typedef, unionGenTOBuilder, parentNode);

        return generatedTOBuilders;
    }

    public Map<Module, Set<Type>> getAdditionalTypes() {
        return this.additionalTypes;
    }

    public static void addUnitsToGenTO(final GeneratedTOBuilder to, final String units) {
        if (!Strings.isNullOrEmpty(units)) {
            to.addConstant(Types.STRING, "Units", "\"" + units + "\"");
            final GeneratedPropertyBuilder prop = new GeneratedPropertyBuilderImpl("UNITS");
            prop.setReturnType(Types.STRING);
            to.addToStringProperty(prop);
        }
    }

    private Type javaTypeForSchemaDefType(final TypeDefinition<?> typeDefinition, final SchemaNode parentNode,
            final Restrictions r, final SchemaContext schemaContext,
            final Map<String, Map<Date, Map<String, Type>>> genTypeDefsContextMap, ModuleContext context) {
        Preconditions.checkArgument(typeDefinition != null, "Type Definition cannot be NULL!");
        final String typedefName = typeDefinition.getQName().getLocalName();
        Preconditions.checkArgument(typedefName != null, "Type Definitions Local Name cannot be NULL!");

        // Deal with base types
        if (typeDefinition.getBaseType() == null) {
            // We have to deal with differing handling of decimal64. The old parser used a fixed Decimal64 type
            // and generated an enclosing ExtendedType to hold any range constraints. The new parser instantiates
            // a base type which holds these constraints.
            if (typeDefinition instanceof DecimalTypeDefinition) {
                final Type ret = BaseYangTypes.BASE_YANG_TYPES_PROVIDER.javaTypeForSchemaDefinitionType
                        (typeDefinition, parentNode, r, null);
                if (ret != null) {
                    return ret;
                }
            }

            // Deal with leafrefs/identityrefs
            Type ret = javaTypeForLeafrefOrIdentityRef(typeDefinition, parentNode, schemaContext,
                    genTypeDefsContextMap, context);
            if (ret != null) {
                return ret;
            }

            ret = BaseYangTypes.BASE_YANG_TYPES_PROVIDER.javaTypeForSchemaDefinitionType(typeDefinition, parentNode,
                    null);
            if (ret == null) {
                LOG.debug("Failed to resolve Java type for {}", typeDefinition);
            }

            return ret;
        }

        Type returnType = javaTypeForExtendedType(typeDefinition, schemaContext, genTypeDefsContextMap, context);
        if (r != null && !r.isEmpty() && returnType instanceof GeneratedTransferObject) {
            final GeneratedTransferObject gto = (GeneratedTransferObject) returnType;
            final Module module = findParentModule(schemaContext, parentNode);
            final Module module1 = findParentModule(schemaContext, typeDefinition);
            final String basePackageName = BindingMapping.getRootPackageName(module);
            final String packageName = BindingGeneratorUtil.packageNameForGeneratedType(basePackageName, typeDefinition
                    .getPath(), BindingNamespaceType.Typedef);
            final String genTOName =
                    JavaIdentifierNormalizer.normalizeSpecificIdentifier(typedefName, JavaIdentifier.CLASS);
            final String name = packageName + "." + genTOName;
            if (module.equals(module1) && !(returnType.getFullyQualifiedName().equals(name))) {
                returnType = shadedTOWithRestrictions(gto, r, context);
            }
        }
        return returnType;
    }

    /**
     *
     * @param basePackageName
     *            string with name of package to which the module belongs
     * @param module
     *            string with the name of the module for to which the
     *            <code>typedef</code> belongs
     * @param typedef
     *            type definition of the node for which should be creted JAVA
     *            <code>Type</code> (usually generated TO)
     * @return JAVA <code>Type</code> representation of <code>typedef</code> or
     *         <code>null</code> value if <code>basePackageName</code> or
     *         <code>modulName</code> or <code>typedef</code> or Q name of
     *         <code>typedef</code> equals <code>null</code>
     */
    private Type typedefToGeneratedType(final String basePackageName, final Module module,
            final TypeDefinition<?> typedef, final Map<String, Map<Date, Map<String, Type>>> genTypeDefsContextMap,
            final Map<Module, Set<Type>> additionalTypes, final SchemaContext schemaContext, ModuleContext context) {
        final String moduleName = module.getName();
        final Date moduleRevision = module.getRevision();
        if ((basePackageName != null) && (moduleName != null) && (typedef != null)) {
            final String typedefName = typedef.getQName().getLocalName();
            final TypeDefinition<?> innerTypeDefinition = typedef.getBaseType();
            if (!(innerTypeDefinition instanceof LeafrefTypeDefinition)
                    && !(innerTypeDefinition instanceof IdentityrefTypeDefinition)) {
                Type returnType;
                if (innerTypeDefinition.getBaseType() != null) {
                    returnType = provideGeneratedTOFromExtendedType(typedef, innerTypeDefinition, basePackageName,
                            module.getName(), schemaContext, genTypeDefsContextMap, context);
                } else if (innerTypeDefinition instanceof UnionTypeDefinition) {
                    final GeneratedTOBuilder genTOBuilder = provideGeneratedTOBuilderForUnionTypeDef(basePackageName,
                            (UnionTypeDefinition) innerTypeDefinition, typedefName, typedef, schemaContext,
                            genTypeDefsContextMap, context);
                    genTOBuilder.setTypedef(true);
                    genTOBuilder.setIsUnion(true);
                    addUnitsToGenTO(genTOBuilder, typedef.getUnits());
                    makeSerializable((GeneratedTOBuilderImpl) genTOBuilder);
                    returnType = genTOBuilder.toInstance();
                } else if (innerTypeDefinition instanceof EnumTypeDefinition) {
                    // enums are automatically Serializable
                    final EnumTypeDefinition enumTypeDef = (EnumTypeDefinition) innerTypeDefinition;
                    // TODO units for typedef enum
                    returnType = provideTypeForEnum(enumTypeDef, typedefName, typedef, schemaContext, context);
                } else if (innerTypeDefinition instanceof BitsTypeDefinition) {
                    final BitsTypeDefinition bitsTypeDefinition = (BitsTypeDefinition) innerTypeDefinition;
                    final GeneratedTOBuilder genTOBuilder =
                            provideGeneratedTOBuilderForBitsTypeDefinition(
                                    basePackageName, bitsTypeDefinition, typedefName, module.getName(), context);
                    genTOBuilder.setTypedef(true);
                    addUnitsToGenTO(genTOBuilder, typedef.getUnits());
                    makeSerializable((GeneratedTOBuilderImpl) genTOBuilder);
                    returnType = genTOBuilder.toInstance();
                } else {
                    final Type javaType = javaTypeForSchemaDefType(innerTypeDefinition, typedef, null,
                            schemaContext, genTypeDefsContextMap, context);
                    returnType = wrapJavaTypeIntoTO(basePackageName, typedef, javaType, module.getName(), context);
                }
                if (returnType != null) {
                    final Map<Date, Map<String, Type>> modulesByDate = genTypeDefsContextMap.get(moduleName);
                    Map<String, Type> typeMap = modulesByDate.get(moduleRevision);
                    if (typeMap != null) {
                        if (typeMap.isEmpty()) {
                            typeMap = new HashMap<>(4);
                            modulesByDate.put(moduleRevision, typeMap);
                        }
                        typeMap.put(typedefName, returnType);
                    }
                    return returnType;
                }
            }
        }
        return null;
    }

    /**
     * Returns JAVA <code>Type</code> for instances of the type
     * <code>ExtendedType</code>.
     *
     * @param typeDefinition
     *            type definition which is converted to JAVA <code>Type</code>
     * @return JAVA <code>Type</code> instance for <code>typeDefinition</code>
     */
    private Type javaTypeForExtendedType(final TypeDefinition<?> typeDefinition, final SchemaContext schemaContext,
            final Map<String, Map<Date, Map<String, Type>>> genTypeDefsContextMap, ModuleContext context) {

        final String typedefName = typeDefinition.getQName().getLocalName();
        final TypeDefinition<?> baseTypeDef = baseTypeDefForExtendedType(typeDefinition);
        Type returnType = javaTypeForLeafrefOrIdentityRef(baseTypeDef, typeDefinition, schemaContext,
                genTypeDefsContextMap, context);
        if (returnType == null) {
            final Module module = findParentModule(schemaContext, typeDefinition);
            final Restrictions r = BindingGeneratorUtil.getRestrictions(typeDefinition);
            if (module != null) {
                final Map<Date, Map<String, Type>> modulesByDate = genTypeDefsContextMap.get(module.getName());
                final Map<String, Type> genTOs = modulesByDate.get(module.getRevision());
                if (genTOs != null) {
                    returnType = genTOs.get(typedefName);
                }
                if (returnType == null) {
                    returnType = BaseYangTypes.BASE_YANG_TYPES_PROVIDER.javaTypeForSchemaDefinitionType(
                            baseTypeDef, typeDefinition, r, null);
                }
            }
        }
        return returnType;
    }

    /**
     * Returns JAVA <code>Type</code> for instances of the type
     * <code>LeafrefTypeDefinition</code> or
     * <code>IdentityrefTypeDefinition</code>.
     *
     * @param typeDefinition
     *            type definition which is converted to JAVA <code>Type</code>
     * @return JAVA <code>Type</code> instance for <code>typeDefinition</code>
     */
    private Type javaTypeForLeafrefOrIdentityRef(final TypeDefinition<?> typeDefinition, final SchemaNode parentNode,
            final SchemaContext schemaContext, final Map<String, Map<Date, Map<String, Type>>> genTypeDefsContextMap,
            ModuleContext context) {
        if (typeDefinition instanceof LeafrefTypeDefinition) {
            final LeafrefTypeDefinition leafref = (LeafrefTypeDefinition) typeDefinition;
            if (isLeafRefSelfReference(leafref, parentNode, schemaContext)) {
                throw new YangValidationException("Leafref " + leafref.toString() + " is referencing itself, incoming" +
                        " StackOverFlowError detected.");
            }
            return provideTypeForLeafref(leafref, parentNode, schemaContext, genTypeDefsContextMap, context);
        } else if (typeDefinition instanceof IdentityrefTypeDefinition) {
            final IdentityrefTypeDefinition idref = (IdentityrefTypeDefinition) typeDefinition;
            return provideTypeForIdentityref(idref, schemaContext);
        } else {
            return null;
        }
    }

    /**
     * Converts <code>leafrefType</code> to JAVA <code>Type</code>.
     *
     * The path of <code>leafrefType</code> is followed to find referenced node
     * and its <code>Type</code> is returned.
     *
     * @param leafrefType
     *            leafref type definition for which is the type sought
     * @return JAVA <code>Type</code> of data schema node which is referenced in
     *         <code>leafrefType</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>leafrefType</code> equal null</li>
     *             <li>if path statement of <code>leafrefType</code> equal null</li>
     *             </ul>
     *
     */
    public Type provideTypeForLeafref(final LeafrefTypeDefinition leafrefType, final SchemaNode parentNode,
            final SchemaContext schemaContext, final Map<String, Map<Date, Map<String, Type>>> genTypeDefsContextMap,
            ModuleContext context) {

        Type returnType = null;
        Preconditions.checkArgument(leafrefType != null, "Leafref Type Definition reference cannot be NULL!");

        Preconditions.checkArgument(leafrefType.getPathStatement() != null,
                "The Path Statement for Leafref Type Definition cannot be NULL!");

        final RevisionAwareXPath xpath = leafrefType.getPathStatement();
        final String strXPath = xpath.toString();
        if (strXPath != null) {
            if (strXPath.indexOf('[') == -1) {
                Module module;
                SchemaNode actualSchemaNode;
                if ((parentNode instanceof DerivableSchemaNode) && ((DerivableSchemaNode) parentNode).isAddedByUses()) {
                    final SchemaNode originalNode = ((DerivableSchemaNode) parentNode).getOriginal().orNull();
                    Preconditions.checkNotNull(originalNode,"originalNode can not be null.");
                    actualSchemaNode = originalNode;
                    module = findParentModule(schemaContext, originalNode);
                } else {
                    actualSchemaNode = parentNode;
                    module = findParentModule(schemaContext, parentNode);
                }
                Preconditions.checkArgument(module != null, "Failed to find module for parent %s", parentNode);

                final SchemaNode dataNode;
                if (xpath.isAbsolute()) {
                    dataNode = findDataSchemaNode(schemaContext, module, xpath);
                } else {
                    dataNode = findDataSchemaNodeForRelativeXPath(schemaContext, module, actualSchemaNode, xpath);
                }
                Preconditions.checkArgument(dataNode != null, "Failed to find leafref target: %s in module %s (%s)",
                        strXPath, getParentModule(parentNode, schemaContext).getName(), parentNode.getQName().getModule());

                if (leafContainsEnumDefinition(dataNode)) {
                    returnType = this.referencedTypes.get(dataNode.getPath());
                } else if (leafListContainsEnumDefinition(dataNode)) {
                    returnType = Types.listTypeFor(this.referencedTypes.get(dataNode.getPath()));
                } else {
                    returnType = resolveTypeFromDataSchemaNode(dataNode, schemaContext, genTypeDefsContextMap, context);
                }
            } else {
                returnType = Types.typeForClass(Object.class);
            }
        }
        Preconditions.checkArgument(returnType != null, "Failed to find leafref target: %s in module %s (%s)",
                strXPath, getParentModule(parentNode, schemaContext).getName(), parentNode.getQName().getModule());
        return returnType;
    }

    /**
     * Checks if <code>dataNode</code> is <code>LeafSchemaNode</code> and if it
     * so then checks if it is of type <code>EnumTypeDefinition</code>.
     *
     * @param dataNode
     *            data schema node for which is checked if it is leaf and if it
     *            is of enum type
     * @return boolean value
     *         <ul>
     *         <li>true - if <code>dataNode</code> is leaf of type enumeration</li>
     *         <li>false - other cases</li>
     *         </ul>
     */
    private static boolean leafContainsEnumDefinition(final SchemaNode dataNode) {
        if (dataNode instanceof LeafSchemaNode) {
            final LeafSchemaNode leaf = (LeafSchemaNode) dataNode;
            //CompatUtils is not used here anymore
            if (leaf.getType() instanceof EnumTypeDefinition) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if <code>dataNode</code> is <code>LeafListSchemaNode</code> and if
     * it so then checks if it is of type <code>EnumTypeDefinition</code>.
     *
     * @param dataNode
     *            data schema node for which is checked if it is leaflist and if
     *            it is of enum type
     * @return boolean value
     *         <ul>
     *         <li>true - if <code>dataNode</code> is leaflist of type
     *         enumeration</li>
     *         <li>false - other cases</li>
     *         </ul>
     */
    private static boolean leafListContainsEnumDefinition(final SchemaNode dataNode) {
        if (dataNode instanceof LeafListSchemaNode) {
            final LeafListSchemaNode leafList = (LeafListSchemaNode) dataNode;
            if (leafList.getType() instanceof EnumTypeDefinition) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts <code>dataNode</code> to JAVA <code>Type</code>.
     *
     * @param dataNode
     *            contains information about YANG type
     * @return JAVA <code>Type</code> representation of <code>dataNode</code>
     */
    private Type resolveTypeFromDataSchemaNode(final SchemaNode dataNode, final SchemaContext schemaContext,
            final Map<String, Map<Date, Map<String, Type>>> genTypeDefsContextMap, ModuleContext context) {
        Type returnType = null;
        if (dataNode != null) {
            if (dataNode instanceof LeafSchemaNode) {
                final LeafSchemaNode leaf = (LeafSchemaNode) dataNode;
                //not using CompatUtils here anymore
                final TypeDefinition<?> type = leaf.getType();
                returnType = javaTypeForSchemaDefType(type, leaf, null, schemaContext, genTypeDefsContextMap, context);
            } else if (dataNode instanceof LeafListSchemaNode) {
                final LeafListSchemaNode leafList = (LeafListSchemaNode) dataNode;
                returnType = javaTypeForSchemaDefType(leafList.getType(), leafList, null, schemaContext,
                        genTypeDefsContextMap, context);
            }
        }
        return returnType;
    }

    /**
     * Seeks for identity reference <code>idref</code> the JAVA
     * <code>type</code>.<br />
     * <br />
     *
     * <i>Example:<br />
     * If identy which is referenced via <code>idref</code> has name <b>Idn</b>
     * then returning type is <b>{@code Class<? extends Idn>}</b></i>
     *
     * @param idref
     *            identityref type definition for which JAVA <code>Type</code>
     *            is sought
     * @return JAVA <code>Type</code> of the identity which is refrenced through
     *         <code>idref</code>
     */
    private static Type provideTypeForIdentityref(final IdentityrefTypeDefinition idref, final SchemaContext schemaContext) {
        //TODO: incompatibility with Binding spec v2, get first or only one
        final QName baseIdQName = idref.getIdentities().iterator().next().getQName();
        final Module module = schemaContext.findModuleByNamespaceAndRevision(baseIdQName.getNamespace(),
                baseIdQName.getRevision());
        IdentitySchemaNode identity = null;
        for (final IdentitySchemaNode id : module.getIdentities()) {
            if (id.getQName().equals(baseIdQName)) {
                identity = id;
            }
        }
        Preconditions.checkArgument(identity != null, "Target identity '" + baseIdQName + "' do not exists");

        final String basePackageName = BindingMapping.getRootPackageName(module);
        final String packageName = BindingGeneratorUtil.packageNameForGeneratedType(basePackageName, identity.getPath
                (), BindingNamespaceType.Identity);

        final String genTypeName = JavaIdentifierNormalizer.normalizeSpecificIdentifier(identity.getQName().getLocalName(),
                JavaIdentifier.CLASS);

        final Type baseType = Types.typeForClass(Class.class);
        final Type paramType = Types.wildcardTypeFor(packageName, genTypeName, true, true, null);
        return Types.parameterizedTypeFor(baseType, paramType);
    }

    private static GeneratedTransferObject shadedTOWithRestrictions(final GeneratedTransferObject gto,
            final Restrictions r, ModuleContext context) {
        final GeneratedTOBuilder gtob = new GeneratedTOBuilderImpl(gto.getPackageName(), gto.getName(), context);
        final GeneratedTransferObject parent = gto.getSuperType();
        if (parent != null) {
            gtob.setExtendsType(parent);
        }
        gtob.setRestrictions(r);
        for (final GeneratedProperty gp : gto.getProperties()) {
            final GeneratedPropertyBuilder gpb = gtob.addProperty(gp.getName());
            gpb.setValue(gp.getValue());
            gpb.setReadOnly(gp.isReadOnly());
            gpb.setAccessModifier(gp.getAccessModifier());
            gpb.setReturnType(gp.getReturnType());
            gpb.setFinal(gp.isFinal());
            gpb.setStatic(gp.isStatic());
        }
        return gtob.toInstance();
    }

    /**
     * Adds a new property with the name <code>propertyName</code> and with type
     * <code>type</code> to <code>unonGenTransObject</code>.
     *
     * @param unionGenTransObject
     *            generated TO to which should be property added
     * @param type
     *            JAVA <code>type</code> of the property which should be added
     *            to <code>unionGentransObject</code>
     * @param propertyName
     *            string with name of property which should be added to
     *            <code>unionGentransObject</code>
     */
    private static void updateUnionTypeAsProperty(final GeneratedTOBuilder unionGenTransObject, final Type type, final String propertyName) {
        if (unionGenTransObject != null && type != null && !unionGenTransObject.containsProperty(propertyName)) {
            final GeneratedPropertyBuilder propBuilder = unionGenTransObject
                    .addProperty(JavaIdentifierNormalizer.normalizeSpecificIdentifier(propertyName, JavaIdentifier.METHOD));
            propBuilder.setReturnType(type);

            unionGenTransObject.addEqualsIdentity(propBuilder);
            unionGenTransObject.addHashIdentity(propBuilder);
            unionGenTransObject.addToStringProperty(propBuilder);
        }
    }

    /**
     * Wraps code which handle case when union subtype is also of the type
     * <code>UnionType</code>.
     *
     * In this case the new generated TO is created for union subtype (recursive
     * call of method
     * {@link #provideGeneratedTOBuilderForUnionTypeDef(String, UnionTypeDefinition, String, SchemaNode, SchemaContext, Map, ModuleContext)}
     * provideGeneratedTOBuilderForUnionTypeDef} and in parent TO builder
     * <code>parentUnionGenTOBuilder</code> is created property which type is
     * equal to new generated TO.
     *
     * @param parentUnionGenTOBuilder
     *            generated TO builder to which is the property with the child
     *            union subtype added
     * @param basePackageName
     *            string with the name of the module package
     * @param unionSubtype
     *            type definition which represents union subtype
     * @return list of generated TO builders. The number of the builders can be
     *         bigger one due to recursive call of
     *         <code>provideGeneratedTOBuildersForUnionTypeDef</code> method.
     */
    private GeneratedTOBuilder resolveUnionSubtypeAsUnion(final GeneratedTOBuilder parentUnionGenTOBuilder,
            final UnionTypeDefinition unionSubtype, final String basePackageName, final SchemaNode parentNode,
            final SchemaContext schemaContext, final Map<String, Map<Date, Map<String, Type>>> genTypeDefsContextMap,
            ModuleContext context) {

        final String newTOBuilderName = provideAvailableNameForGenTOBuilder(parentUnionGenTOBuilder.getName());
        final GeneratedTOBuilder subUnionGenTOBUilder = provideGeneratedTOBuilderForUnionTypeDef(
                basePackageName, unionSubtype, newTOBuilderName, parentNode, schemaContext, genTypeDefsContextMap,
                context);

        final GeneratedPropertyBuilder propertyBuilder;
        propertyBuilder = parentUnionGenTOBuilder
                .addProperty(JavaIdentifierNormalizer.normalizeSpecificIdentifier(newTOBuilderName, JavaIdentifier.METHOD));
        propertyBuilder.setReturnType(subUnionGenTOBUilder);
        parentUnionGenTOBuilder.addEqualsIdentity(propertyBuilder);
        parentUnionGenTOBuilder.addToStringProperty(propertyBuilder);

        return subUnionGenTOBUilder;
    }

    /**
     * Converts output list of generated TO builders to one TO builder (first
     * from list) which contains the remaining builders as its enclosing TO.
     *
     * @param basePackageName
     *            string with name of package to which the module belongs
     * @param typedef
     *            type definition which should be of type
     *            <code>UnionTypeDefinition</code>
     * @param typeDefName
     *            string with name for generated TO
     * @return generated TO builder with the list of enclosed generated TO
     *         builders
     */
    public GeneratedTOBuilder provideGeneratedTOBuilderForUnionTypeDef(final String basePackageName,
            final UnionTypeDefinition typedef, final String typeDefName, final SchemaNode parentNode,
            final SchemaContext schemaContext, final Map<String, Map<Date, Map<String, Type>>> genTypeDefsContextMap,
            ModuleContext context) {

        final List<GeneratedTOBuilder> builders = provideGeneratedTOBuildersForUnionTypeDef(basePackageName,
                typedef, typeDefName, parentNode, schemaContext, genTypeDefsContextMap, context);
        Preconditions.checkState(!builders.isEmpty(), "No GeneratedTOBuilder objects generated from union %s", typedef);

        final GeneratedTOBuilder resultTOBuilder = builders.remove(0);
        for (final GeneratedTOBuilder genTOBuilder : builders) {
            resultTOBuilder.addEnclosingTransferObject(genTOBuilder);
        }

        final GeneratedPropertyBuilder genPropBuilder;

        genPropBuilder = resultTOBuilder.addProperty("value").setReturnType(Types.CHAR_ARRAY).setReadOnly(false);
        resultTOBuilder.addEqualsIdentity(genPropBuilder);
        resultTOBuilder.addHashIdentity(genPropBuilder);
        resultTOBuilder.addToStringProperty(genPropBuilder);

        provideGeneratedTOBuilderForUnionBuilder(findParentModule(schemaContext, parentNode), resultTOBuilder);

        return resultTOBuilder;
    }


    private GeneratedTOBuilder provideGeneratedTOBuilderForUnionBuilder(final Module parentModule,
            final GeneratedTOBuilder genTOBuilder) {
        final String outerCls = Types.getOuterClassName(genTOBuilder);
        final StringBuilder name;
        if (outerCls != null) {
            name = new StringBuilder(outerCls);
        } else {
            name = new StringBuilder();
        }
        name.append(genTOBuilder.getName());
        name.append("Builder");
        final GeneratedTOBuilderImpl unionBuilder = new GeneratedTOBuilderImpl(getOuterClassPackageName(genTOBuilder),
                name.toString(), true);
        unionBuilder.setIsUnionBuilder(true);

        final MethodSignatureBuilder method = unionBuilder.addMethod("getDefaultInstance");
        method.setReturnType(genTOBuilder);
        method.addParameter(Types.STRING, "defaultValue");
        method.setAccessModifier(AccessModifier.PUBLIC);
        method.setStatic(true);

        final Set<Type> types = this.getAdditionalTypes().get(parentModule);
        if (types == null) {
            this.getAdditionalTypes().put(parentModule,
                    Sets.newHashSet(unionBuilder.toInstance()));
        } else {
            types.add(unionBuilder.toInstance());
        }

        return unionBuilder;
    }

    /**
     * Wraps code which handle case when union subtype is of the type
     * <code>ExtendedType</code>.
     *
     * If TO for this type already exists it is used for the creation of the
     * property in <code>parentUnionGenTOBuilder</code>. In other case the base
     * type is used for the property creation.
     *
     * @param parentUnionGenTOBuilder
     *            generated TO builder in which new property is created
     * @param unionSubtype
     *            type definition of the <code>ExtendedType</code> type which
     *            represents union subtype
     * @param regularExpressions
     *            list of strings with the regular expressions
     * @param parentNode
     *            parent Schema Node for Extended Subtype
     *
     */
    private static void resolveExtendedSubtypeAsUnion(final GeneratedTOBuilder parentUnionGenTOBuilder,
            final TypeDefinition<?> unionSubtype, final List<String> regularExpressions, final SchemaNode parentNode,
            final SchemaContext schemaContext, final Map<String, Map<Date, Map<String, Type>>> genTypeDefsContextMap) {

        final String unionTypeName = unionSubtype.getQName().getLocalName();
        final Type genTO = findGenTO(unionTypeName, unionSubtype, schemaContext, genTypeDefsContextMap);
        if (genTO != null) {
            updateUnionTypeAsProperty(parentUnionGenTOBuilder, genTO, unionTypeName);
        } else {
            final TypeDefinition<?> baseType = baseTypeDefForExtendedType(unionSubtype);
            if (unionTypeName.equals(baseType.getQName().getLocalName())) {
                final Type javaType = BaseYangTypes.BASE_YANG_TYPES_PROVIDER.javaTypeForSchemaDefinitionType(baseType,
                        parentNode, null);
                if (javaType != null) {
                    updateUnionTypeAsProperty(parentUnionGenTOBuilder, javaType, unionTypeName);
                }
            }
            if (baseType instanceof StringTypeDefinition) {
                regularExpressions.addAll(resolveRegExpressionsFromTypedef(unionSubtype));
            }
        }
    }

    /**
     * Returns string which contains the same value as <code>name</code> but
     * integer suffix is incremented by one. If <code>name</code> contains no
     * number suffix then number 1 is added.
     *
     * @param name
     *            string with name of augmented node
     * @return string with the number suffix incremented by one (or 1 is added)
     */
    private static String provideAvailableNameForGenTOBuilder(final String name) {
        final Matcher mtch = NUMBERS_PATTERN.matcher(name);
        if (mtch.find()) {
            final int newSuffix = Integer.valueOf(name.substring(mtch.start())) + 1;
            return name.substring(0, mtch.start()) + newSuffix;
        } else {
            return name + 1;
        }
    }

    /**
     * Searches for generated TO for <code>searchedTypeDef</code> type
     * definition in {@link #genTypeDefsContextMap genTypeDefsContextMap}
     *
     * @param searchedTypeName
     *            string with name of <code>searchedTypeDef</code>
     * @return generated TO for <code>searchedTypeDef</code> or
     *         <code>null</code> it it doesn't exist
     */
    private static Type findGenTO(final String searchedTypeName, final SchemaNode parentNode,
            final SchemaContext schemaContext, final Map<String, Map<Date, Map<String, Type>>> genTypeDefsContextMap) {

        final Module typeModule = findParentModule(schemaContext, parentNode);
        if (typeModule != null && typeModule.getName() != null) {
            final Map<Date, Map<String, Type>> modulesByDate = genTypeDefsContextMap.get(typeModule.getName());
            final Map<String, Type> genTOs = modulesByDate.get(typeModule.getRevision());
            if (genTOs != null) {
                return genTOs.get(searchedTypeName);
            }
        }
        return null;
    }

    /**
     * Adds enumeration to <code>typeBuilder</code>. The enumeration data are
     * taken from <code>enumTypeDef</code>.
     *
     * @param enumTypeDef
     *            enumeration type definition is source of enumeration data for
     *            <code>typeBuilder</code>
     * @param enumName
     *            string with the name of enumeration
     * @param typeBuilder
     *            generated type builder to which is enumeration added
     * @return enumeration type which contains enumeration data form
     *         <code>enumTypeDef</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>enumTypeDef</code> equals null</li>
     *             <li>if enum values of <code>enumTypeDef</code> equal null</li>
     *             <li>if Q name of <code>enumTypeDef</code> equal null</li>
     *             <li>if name of <code>enumTypeDef</code> equal null</li>
     *             <li>if name of <code>typeBuilder</code> equal null</li>
     *             </ul>
     *
     */
    private static Enumeration addInnerEnumerationToTypeBuilder(final EnumTypeDefinition enumTypeDef,
            final String enumName, final GeneratedTypeBuilderBase<?> typeBuilder, ModuleContext context) {
        Preconditions.checkArgument(enumTypeDef != null, "EnumTypeDefinition reference cannot be NULL!");
        Preconditions.checkArgument(enumTypeDef.getQName().getLocalName() != null,
                "Local Name in EnumTypeDefinition QName cannot be NULL!");
        Preconditions.checkArgument(typeBuilder != null, "Generated Type Builder reference cannot be NULL!");

        final EnumBuilder enumBuilder = typeBuilder.addEnumeration(enumName, context);
        final String enumTypedefDescription = encodeAngleBrackets(enumTypeDef.getDescription());
        enumBuilder.setDescription(enumTypedefDescription);
        enumBuilder.updateEnumPairsFromEnumTypeDef(enumTypeDef);
        return enumBuilder.toInstance(enumBuilder);
    }

    private static boolean isLeafRefSelfReference(final LeafrefTypeDefinition leafref, final SchemaNode parentNode,
            final SchemaContext schemaContext) {
        final SchemaNode leafRefValueNode;
        final RevisionAwareXPath leafRefXPath = leafref.getPathStatement();
        final RevisionAwareXPath leafRefStrippedXPath = new RevisionAwareXPathImpl(leafRefXPath.toString()
                .replaceAll("\\[(.*?)\\]", ""), leafRefXPath.isAbsolute());

        ///// skip leafrefs in augments - they're checked once augments are resolved
        final Iterator<QName> iterator = parentNode.getPath().getPathFromRoot().iterator();
        boolean isAugmenting = false;
        DataNodeContainer current = null;
        DataSchemaNode dataChildByName;

        while (iterator.hasNext() && !isAugmenting) {
            final QName next = iterator.next();
            if (current == null) {
                dataChildByName = schemaContext.getDataChildByName(next);
            } else {
                dataChildByName = current.getDataChildByName(next);
            }
            if (dataChildByName != null) {
                isAugmenting = dataChildByName.isAugmenting();
            } else {
                return false;
            }
            if (dataChildByName instanceof DataNodeContainer) {
                current = (DataNodeContainer) dataChildByName;
            }
        }
        if (isAugmenting) {
            return false;
        }
        /////

        final Module parentModule = getParentModule(parentNode, schemaContext);
        if (!leafRefStrippedXPath.isAbsolute()) {
            leafRefValueNode = SchemaContextUtil.findDataSchemaNodeForRelativeXPath(schemaContext, parentModule,
                    parentNode, leafRefStrippedXPath);
        } else {
            leafRefValueNode = SchemaContextUtil.findDataSchemaNode(schemaContext, parentModule, leafRefStrippedXPath);
        }
        return (leafRefValueNode != null) && leafRefValueNode.equals(parentNode);
    }


}
