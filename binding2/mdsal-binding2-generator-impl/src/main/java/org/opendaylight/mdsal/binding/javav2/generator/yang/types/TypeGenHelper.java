/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.generator.yang.types;

import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.encodeAngleBrackets;
import static org.opendaylight.mdsal.binding.javav2.generator.yang.types.TypeProviderImpl.addUnitsToGenTO;
import static org.opendaylight.yangtools.yang.model.util.SchemaContextUtil.findParentModule;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil;
import org.opendaylight.mdsal.binding.javav2.generator.util.Types;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.EnumerationBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedPropertyBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.mdsal.binding.javav2.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.javav2.model.api.Enumeration;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.javav2.model.api.Restrictions;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ActionNodeContainer;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Auxiliary util class for {@link TypeProviderImpl} class
 */
@Beta
public final class TypeGenHelper {
    private static final Logger LOG = LoggerFactory.getLogger(TypeGenHelper.class);

    private TypeGenHelper() {
        throw new UnsupportedOperationException("Util class");
    }

    /**
     * Gets base type definition for <code>extendTypeDef</code>. The method is
     * recursively called until non <code>ExtendedType</code> type is found.
     *
     * @param extendTypeDef
     *            type definition for which is the base type definition sought
     * @return type definition which is base type for <code>extendTypeDef</code>
     * @throws IllegalArgumentException
     *             if <code>extendTypeDef</code> equal null
     */
    static TypeDefinition<?> baseTypeDefForExtendedType(final TypeDefinition<?> extendTypeDef) {
        Preconditions.checkArgument(extendTypeDef != null, "Type Definition reference cannot be NULL!");

        TypeDefinition<?> ret = extendTypeDef;
        while (ret.getBaseType() != null) {
            ret = ret.getBaseType();
        }

        return ret;
    }

    /**
     * Creates generated TO with data about inner extended type
     * <code>innerExtendedType</code>, about the package name
     * <code>typedefName</code> and about the generated TO name
     * <code>typedefName</code>.
     *
     * It is supposed that <code>innerExtendedType</code> is already present in
     * {@link TypeProviderImpl#genTypeDefsContextMap genTypeDefsContextMap} to
     * be possible set it as extended type for the returning generated TO.
     *
     * @param typedef
     *            Type Definition
     * @param innerExtendedType
     *            extended type which is part of some other extended type
     * @param basePackageName
     *            string with the package name of the module
     * @param moduleName
     *            Module Name
     * @return generated TO which extends generated TO for
     *         <code>innerExtendedType</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>extendedType</code> equals null</li>
     *             <li>if <code>basePackageName</code> equals null</li>
     *             <li>if <code>typedefName</code> equals null</li>
     *             </ul>
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static GeneratedTransferObject provideGeneratedTOFromExtendedType(final TypeDefinition<?> typedef, final
            TypeDefinition<?> innerExtendedType, final String basePackageName, final String moduleName, final SchemaContext
            schemaContext, final Map<String, Map<Optional<Revision>, Map<String, Type>>> genTypeDefsContextMap,
            final ModuleContext context) {

        Preconditions.checkArgument(innerExtendedType != null, "Extended type cannot be NULL!");
        Preconditions.checkArgument(basePackageName != null, "String with base package name cannot be NULL!");

        final String typedefName = typedef.getQName().getLocalName();
        final String innerTypeDef = innerExtendedType.getQName().getLocalName();
        final GeneratedTOBuilderImpl genTOBuilder = new GeneratedTOBuilderImpl(basePackageName, typedefName, context);
        final String typedefDescription = encodeAngleBrackets(typedef.getDescription().orElse(null));

        genTOBuilder.setDescription(typedefDescription);
        typedef.getReference().ifPresent(genTOBuilder::setReference);
        genTOBuilder.setSchemaPath((List) typedef.getPath().getPathFromRoot());
        genTOBuilder.setModuleName(moduleName);
        genTOBuilder.setTypedef(true);
        final Restrictions r = BindingGeneratorUtil.getRestrictions(typedef);
        genTOBuilder.setRestrictions(r);
        if (typedef.getStatus() == Status.DEPRECATED) {
            genTOBuilder.addAnnotation("", "Deprecated");
        }

        if (baseTypeDefForExtendedType(innerExtendedType) instanceof UnionTypeDefinition) {
            genTOBuilder.setIsUnion(true);
        }

        Map<Optional<Revision>, Map<String, Type>> modulesByDate = null;
        Map<String, Type> typeMap = null;
        final Module parentModule = findParentModule(schemaContext, innerExtendedType);
        if (parentModule != null) {
            modulesByDate = genTypeDefsContextMap.get(parentModule.getName());
            typeMap = modulesByDate.get(parentModule.getRevision());
        }

        if (typeMap != null) {
            final Type type = typeMap.get(innerTypeDef);
            if (type instanceof GeneratedTransferObject) {
                genTOBuilder.setExtendsType((GeneratedTransferObject) type);
            }
        }
        addUnitsToGenTO(genTOBuilder, typedef.getUnits().orElse(null));
        makeSerializable(genTOBuilder);

        return genTOBuilder.toInstance();
    }

    /**
     * Wraps base YANG type to generated TO.
     *
     * @param basePackageName
     *            string with name of package to which the module belongs
     * @param typedef
     *            type definition which is converted to the TO
     * @param javaType
     *            JAVA <code>Type</code> to which is <code>typedef</code> mapped
     * @return generated transfer object which represent<code>javaType</code>
     */
    static GeneratedTransferObject wrapJavaTypeIntoTO(final String basePackageName, final TypeDefinition<?> typedef,
           final Type javaType, final String moduleName, final ModuleContext context) {
        Preconditions.checkNotNull(javaType, "javaType cannot be null");
        final String propertyName = "value";

        final GeneratedTOBuilder genTOBuilder = typedefToTransferObject(basePackageName, typedef, moduleName, context);
        genTOBuilder.setRestrictions(BindingGeneratorUtil.getRestrictions(typedef));
        final GeneratedPropertyBuilder genPropBuilder = genTOBuilder.addProperty(propertyName);
        genPropBuilder.setReturnType(javaType);
        genTOBuilder.addEqualsIdentity(genPropBuilder);
        genTOBuilder.addHashIdentity(genPropBuilder);
        genTOBuilder.addToStringProperty(genPropBuilder);
        if (typedef.getStatus() == Status.DEPRECATED) {
            genTOBuilder.addAnnotation("", "Deprecated");
        }
        if (javaType instanceof ConcreteType && "String".equals(javaType.getName()) && typedef.getBaseType() != null) {
            addStringRegExAsConstant(genTOBuilder, resolveRegExpressionsFromTypedef(typedef));
        }
        addUnitsToGenTO(genTOBuilder, typedef.getUnits().orElse(null));
        genTOBuilder.setTypedef(true);
        makeSerializable((GeneratedTOBuilderImpl) genTOBuilder);
        return genTOBuilder.toInstance();
    }

    /**
     * Converts the pattern constraints from <code>typedef</code> to the list of
     * the strings which represents these constraints.
     *
     * @param typedef
     *            extended type in which are the pattern constraints sought
     * @return map of strings which represents the constraint patterns
     * @throws IllegalArgumentException
     *             if <code>typedef</code> equals null
     *
     */
    static Map<String, String> resolveRegExpressionsFromTypedef(final TypeDefinition<?> typedef) {
        if (!(typedef instanceof StringTypeDefinition)) {
            return ImmutableMap.of();
        }

        // TODO: run diff against base ?
        return resolveRegExpressions(((StringTypeDefinition) typedef).getPatternConstraints());
    }

    /**
     * Converts the pattern constraints to the list of
     * the strings which represents these constraints.
     *
     * @param patternConstraints
     *            list of pattern constraints
     * @return list of strings which represents the constraint patterns
     */
    public static Map<String, String> resolveRegExpressions(final List<PatternConstraint> patternConstraints) {
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

    /**
     * Finds out for each type definition how many immersion (depth) is
     * necessary to get to the base type. Every type definition is inserted to
     * the map which key is depth and value is list of type definitions with
     * equal depth. In next step are lists from this map concatenated to one
     * list in ascending order according to their depth. All type definitions
     * are in the list behind all type definitions on which depends.
     *
     * @param unsortedTypeDefinitions
     *            list of type definitions which should be sorted by depth
     * @return list of type definitions sorted according their each other
     *         dependencies (type definitions which are depend on other type
     *         definitions are in list behind them).
     */
    static List<TypeDefinition<?>> sortTypeDefinitionAccordingDepth(
            final Collection<TypeDefinition<?>> unsortedTypeDefinitions) {
        final List<TypeDefinition<?>> sortedTypeDefinition = new ArrayList<>();

        final Map<Integer, List<TypeDefinition<?>>> typeDefinitionsDepths = new TreeMap<>();
        for (TypeDefinition<?> unsortedTypeDefinition : unsortedTypeDefinitions) {
            final int depth = getTypeDefinitionDepth(unsortedTypeDefinition);
            final List<TypeDefinition<?>> typeDefinitionsConcreteDepth = typeDefinitionsDepths.computeIfAbsent(depth, k -> new ArrayList<>());
            typeDefinitionsConcreteDepth.add(unsortedTypeDefinition);
        }

        // SortedMap guarantees order corresponding to keys in ascending order
        typeDefinitionsDepths.values().forEach(sortedTypeDefinition::addAll);

        return sortedTypeDefinition;
    }

    /**
     *
     * Adds to the <code>genTOBuilder</code> the constant which contains regular
     * expressions from the <code>regularExpressions</code>
     *
     * @param genTOBuilder
     *            generated TO builder to which are
     *            <code>regular expressions</code> added
     * @param expressions
     *            list of string which represent regular expressions
     */
    static void addStringRegExAsConstant(final GeneratedTOBuilder genTOBuilder, final Map<String, String> expressions) {
        if (!expressions.isEmpty()) {
            genTOBuilder.addConstant(Types.listTypeFor(BaseYangTypes.STRING_TYPE), BindingMapping.PATTERN_CONSTANT_NAME,
                ImmutableMap.copyOf(expressions));
        }
    }

    /**
     * Returns how many immersion is necessary to get from the type definition
     * to the base type.
     *
     * @param typeDefinition
     *            type definition for which is depth sought.
     * @return number of immersions which are necessary to get from the type
     *         definition to the base type
     */
    private static int getTypeDefinitionDepth(final TypeDefinition<?> typeDefinition) {
        if (typeDefinition == null) {
            return 1;
        }
        final TypeDefinition<?> baseType = typeDefinition.getBaseType();
        if (baseType == null) {
            return 1;
        }

        int depth = 1;
        if (baseType.getBaseType() != null) {
            depth = depth + getTypeDefinitionDepth(baseType);
        } else if (baseType instanceof UnionTypeDefinition) {
            final List<TypeDefinition<?>> childTypeDefinitions = ((UnionTypeDefinition) baseType).getTypes();
            int maxChildDepth = 0;
            int childDepth = 1;
            for (TypeDefinition<?> childTypeDefinition : childTypeDefinitions) {
                childDepth = childDepth + getTypeDefinitionDepth(childTypeDefinition);
                if (childDepth > maxChildDepth) {
                    maxChildDepth = childDepth;
                }
            }
            return maxChildDepth;
        }
        return depth;
    }

    static List<TypeDefinition<?>> getAllTypedefs(final Module module) {
        final List<TypeDefinition<?>> ret = new ArrayList<>();

        fillRecursively(ret, module);

        final Set<NotificationDefinition> notifications = module.getNotifications();
        for (NotificationDefinition notificationDefinition : notifications) {
            fillRecursively(ret, notificationDefinition);
        }

        final Set<RpcDefinition> rpcs = module.getRpcs();
        for (RpcDefinition rpcDefinition : rpcs) {
            ret.addAll(rpcDefinition.getTypeDefinitions());
            final ContainerSchemaNode input = rpcDefinition.getInput();
            if (input != null) {
                fillRecursively(ret, input);
            }
            final ContainerSchemaNode output = rpcDefinition.getOutput();
            if (output != null) {
                fillRecursively(ret, output);
            }
        }

        final Collection<DataSchemaNode> potentials = module.getChildNodes();

        for (DataSchemaNode potential : potentials) {
            if (potential instanceof ActionNodeContainer) {
                final Set<ActionDefinition> actions = ((ActionNodeContainer) potential).getActions();
                for (ActionDefinition action: actions) {
                    final ContainerSchemaNode input = action.getInput();
                    if (input != null) {
                        fillRecursively(ret, input);
                    }
                    final ContainerSchemaNode output = action.getOutput();
                    if (output != null) {
                        fillRecursively(ret, output);
                    }
                }
            }
        }

        return ret;
    }

    private static void fillRecursively(final List<TypeDefinition<?>> list, final DataNodeContainer container) {
        final Collection<DataSchemaNode> childNodes = container.getChildNodes();
        if (childNodes != null) {
            childNodes.stream().filter(childNode -> !childNode.isAugmenting()).forEach(childNode -> {
                if (childNode instanceof ContainerSchemaNode) {
                    fillRecursively(list, (ContainerSchemaNode) childNode);
                } else if (childNode instanceof ListSchemaNode) {
                    fillRecursively(list, (ListSchemaNode) childNode);
                } else if (childNode instanceof ChoiceSchemaNode) {
                    for (CaseSchemaNode caseNode : ((ChoiceSchemaNode) childNode).getCases().values()) {
                        fillRecursively(list, caseNode);
                    }
                }
            });
        }

        list.addAll(container.getTypeDefinitions());

        final Set<GroupingDefinition> groupings = container.getGroupings();
        if (groupings != null) {
            for (GroupingDefinition grouping : groupings) {
                fillRecursively(list, grouping);
            }
        }
    }

    /**
     * Add {@link Serializable} to implemented interfaces of this TO. Also
     * compute and add serialVersionUID property.
     *
     * @param gto
     *            transfer object which needs to be serializable
     */
    static void makeSerializable(final GeneratedTOBuilderImpl gto) {
        gto.addImplementsType(Types.typeForClass(Serializable.class));
        final GeneratedPropertyBuilder prop = new GeneratedPropertyBuilderImpl("serialVersionUID");
        prop.setValue(Long.toString(BindingGeneratorUtil.computeDefaultSUID(gto)));
        gto.setSUID(prop);
    }

    /**
     * Converts <code>enumTypeDef</code> to
     * {@link Enumeration
     * enumeration}.
     *
     * @param enumTypeDef
     *            enumeration type definition which is converted to enumeration
     * @param enumName
     *            string with name which is used as the enumeration name
     * @return enumeration type which is built with data (name, enum values)
     *         from <code>enumTypeDef</code>
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>enumTypeDef</code> equals null</li>
     *             <li>if enum values of <code>enumTypeDef</code> equal null</li>
     *             <li>if Q name of <code>enumTypeDef</code> equal null</li>
     *             <li>if name of <code>enumTypeDef</code> equal null</li>
     *             </ul>
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static Enumeration provideTypeForEnum(final EnumTypeDefinition enumTypeDef, final String enumName,
           final SchemaNode parentNode, final SchemaContext schemaContext, final ModuleContext context) {
        Preconditions.checkArgument(enumTypeDef != null, "EnumTypeDefinition reference cannot be NULL!");
        Preconditions.checkArgument(enumTypeDef.getQName().getLocalName() != null,
                "Local Name in EnumTypeDefinition QName cannot be NULL!");
        final Module module = findParentModule(schemaContext, parentNode);
        final String basePackageName = BindingMapping.getRootPackageName(module);
        final String packageName;

        if (parentNode instanceof TypeDefinition) {
            packageName = BindingGeneratorUtil.packageNameWithNamespacePrefix(
                    BindingMapping.getRootPackageName(module),
                    BindingNamespaceType.Typedef);
        } else {
            packageName = basePackageName;
        }

        final EnumerationBuilderImpl enumBuilder = new EnumerationBuilderImpl(packageName, enumName, context);
        final String enumTypedefDescription = encodeAngleBrackets(enumTypeDef.getDescription().orElse(null));
        enumBuilder.setDescription(enumTypedefDescription);
        enumBuilder.setReference(enumTypeDef.getReference().orElse(null));
        enumBuilder.setModuleName(module.getName());
        enumBuilder.setSchemaPath((List) enumTypeDef.getPath().getPathFromRoot());
        enumBuilder.updateEnumPairsFromEnumTypeDef(enumTypeDef);
        return enumBuilder.toInstance(null);
    }

    /**
     * Converts <code>typedef</code> to the generated TO builder.
     *
     * @param basePackageName
     *            string with name of package to which the module belongs
     * @param typedef
     *            type definition from which is the generated TO builder created
     * @return generated TO builder which contains data from
     *         <code>typedef</code> and <code>basePackageName</code>
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static GeneratedTOBuilderImpl typedefToTransferObject(final String basePackageName,
            final TypeDefinition<?> typedef, final String moduleName, final ModuleContext context) {
        final String typeDefTOName = typedef.getQName().getLocalName();

        if (basePackageName != null && typeDefTOName != null) {
            final GeneratedTOBuilderImpl newType = new GeneratedTOBuilderImpl(basePackageName, typeDefTOName, context);
            final String typedefDescription = encodeAngleBrackets(typedef.getDescription().orElse(null));

            newType.setDescription(typedefDescription);
            typedef.getReference().ifPresent(newType::setReference);
            newType.setSchemaPath((List) typedef.getPath().getPathFromRoot());
            newType.setModuleName(moduleName);

            return newType;
        }
        return null;
    }

    static Module getParentModule(final SchemaNode node, final SchemaContext schemaContext) {
        return schemaContext.findModule(node.getPath().getPathFromRoot().iterator().next().getModule()).orElse(null);
    }
}
