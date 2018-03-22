/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.context;

import com.google.common.annotations.Beta;
import com.google.common.collect.BiMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * This interface holds information about generated entities in context of YANG module
 */
@Beta
public interface ModuleContext {

    Module module();

    /**
     * Return normalized root package name string of the module.
     *
     * @return root package name
     */
    @Nonnull
    String normalizedRootPackageName();

    /**
     * Return normalized namespace package name string of the module.
     *
     * @return namespace package name
     */
    @Nonnull
    String normalizedNSPackageName(@Nonnull final BindingNamespaceType namespaceType);

    List<Type> getGeneratedTypes();

    Multimap<Type, Type> getChoiceToCases();

    GeneratedTypeBuilder getModuleNode();

    GeneratedTypeBuilder getChildNode(final SchemaPath p);

    GeneratedTypeBuilder getGrouping(final SchemaPath p);

    GeneratedTypeBuilder getCase(final SchemaPath p);

    void addModuleNode(final GeneratedTypeBuilder moduleNode);

    void addGeneratedTOBuilder(final SchemaPath schemaPath, final GeneratedTOBuilder b);

    void addChildNodeType(final SchemaNode p, final GeneratedTypeBuilder b);

    void addGroupingType(final GroupingDefinition p, final GeneratedTypeBuilder b);

    void addTypedefType(final SchemaPath p, final Type t);

    void addCaseType(final SchemaPath p, final GeneratedTypeBuilder b);

    void addIdentityType(final QName name,final GeneratedTypeBuilder b);

    void addTopLevelNodeType(final GeneratedTypeBuilder b);

    void addAugmentType(final GeneratedTypeBuilder b);

    Map<SchemaPath, Type> getTypedefs();

    Map<SchemaPath, GeneratedTypeBuilder> getChildNodes();

    Map<SchemaPath, GeneratedTypeBuilder> getGroupings();

    Map<SchemaPath, GeneratedTypeBuilder> getCases();

    Map<QName,GeneratedTypeBuilder> getIdentities();

    Set<GeneratedTypeBuilder> getTopLevelNodes();

    List<GeneratedTypeBuilder> getAugmentations();

    Multimap<Type, AugmentationSchemaNode> getTypeToAugmentations();

    BiMap<SchemaPath, Type> getTargetToAugmentation();

    void addTypeToAugmentations(final GeneratedTypeBuilder builder, final List<AugmentationSchemaNode> schemaList);

    void addTargetToAugmentation(final GeneratedTypeBuilder builder, final SchemaPath augmentTarget);

    void addChoiceToCaseMapping(final Type choiceType, final Type caseType, final CaseSchemaNode schema);

    BiMap<Type, CaseSchemaNode> getCaseTypeToSchemas();

    /**
     *
     * Returns mapping of type to its schema.
     *
     * Valid values are only instances of {@link DataSchemaNode} or {@link AugmentationSchemaNode}
     *
     * @return Mapping from type to corresponding schema
     */
    Map<Type, WithStatus> getTypeToSchema();

    void addTypeToSchema(final Type type, final WithStatus schema);

    /**
     * Adds mapping between schema path and inner enum, inner union, inner bits.
     *
     * @param path
     * @param builder
     */
    void addInnerTypedefType(final SchemaPath path, final Type builder);

    Type getInnerType(final SchemaPath path);

    void addKeyType(final SchemaPath path, final GeneratedTypeBuilder genType);

    GeneratedTypeBuilder getKeyType(final SchemaPath path);

    GeneratedTOBuilder getKeyGenTO(final SchemaPath path);

    ListMultimap<String, String> getPackagesMap();

    void putToPackagesMap(final String packageName, final String actualClassName);

    void cleanPackagesMap();
}
