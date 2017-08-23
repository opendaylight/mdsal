/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.context;

import com.google.common.annotations.Beta;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * This class holds information about generated entities in context of YANG module
 */
@Beta
public final class ModuleContext {
    private GeneratedTypeBuilder moduleNode;
    private final Map<SchemaPath,GeneratedTOBuilder> genTOs = new HashMap<>();
    private final Map<SchemaPath, Type> typedefs = new HashMap<>();
    private final Map<SchemaPath, GeneratedTypeBuilder> childNodes = new HashMap<>();
    private final BiMap<String, GeneratedTypeBuilder> dataTypes = HashBiMap.create();
    private final Map<SchemaPath, GeneratedTypeBuilder> groupings = new HashMap<>();
    private final Map<SchemaPath, GeneratedTypeBuilder> cases = new HashMap<>();
    private final Map<QName,GeneratedTOBuilder> identities = new HashMap<>();
    private final Set<GeneratedTypeBuilder> topLevelNodes = new HashSet<>();
    private final List<GeneratedTypeBuilder> augmentations = new ArrayList<>();
    private final Map<Type,List<AugmentationSchema>> typeToAugmentations = new HashMap<>();
    private final BiMap<SchemaPath,Type> targetToAugmentation = HashBiMap.create();
    private final Map<Type,Object> typeToSchema = new HashMap<>();
    private final Multimap<Type, Type> choiceToCases = HashMultimap.create();
    private final BiMap<Type,ChoiceCaseNode> caseTypeToSchema = HashBiMap.create();
    private final Map<SchemaPath, Type> innerTypes = new HashMap<>();
    private final Map<SchemaPath,GeneratedTypeBuilder> keyTypes = new HashMap<>();
    //map is getting manipulated based on unique YANG module namespace rule
    private final ListMultimap<String, String> packagesMap = Multimaps.synchronizedListMultimap
            (ArrayListMultimap.create());

    public List<Type> getGeneratedTypes() {
        final List<Type> result = new ArrayList<>();

        if (this.moduleNode != null) {
            result.add(this.moduleNode.toInstance());
        }

        result.addAll(this.genTOs.values().stream().map(GeneratedTOBuilder::toInstance).collect(Collectors.toList()));
        result.addAll(this.typedefs.values().stream().filter(b -> b != null).collect(Collectors.toList()));
        result.addAll(this.dataTypes.values().stream().map(GeneratedTypeBuilder::toInstance).collect(Collectors.toList()));
        result.addAll(this.groupings.values().stream().map(GeneratedTypeBuilder::toInstance).collect(Collectors.toList()));
        result.addAll(this.cases.values().stream().map(GeneratedTypeBuilder::toInstance).collect(Collectors.toList()));
        result.addAll(this.identities.values().stream().map(GeneratedTOBuilder::toInstance).collect(Collectors.toList()));
        result.addAll(this.topLevelNodes.stream().map(GeneratedTypeBuilder::toInstance).collect(Collectors.toList()));
        result.addAll(this.augmentations.stream().map(GeneratedTypeBuilder::toInstance).collect(Collectors.toList()));
        result.addAll(this.keyTypes.values().stream().map(GeneratedTypeBuilder::toInstance).collect(Collectors.toList()));
        return ImmutableList.copyOf(result);
    }

    public Multimap<Type, Type> getChoiceToCases() {
        return Multimaps.unmodifiableMultimap(this.choiceToCases);
    }

    public GeneratedTypeBuilder getModuleNode() {
        return this.moduleNode;
    }

    public GeneratedTypeBuilder getChildNode(final SchemaPath p) {
        return this.childNodes.get(p);
    }

    public GeneratedTypeBuilder getGrouping(final SchemaPath p) {
        return this.groupings.get(p);
    }

    public GeneratedTypeBuilder getCase(final SchemaPath p) {
        return this.cases.get(p);
    }

    public void addModuleNode(final GeneratedTypeBuilder moduleNode) {
        this.moduleNode = moduleNode;
    }

    public void addGeneratedTOBuilder(final SchemaPath schemaPath, final GeneratedTOBuilder b) {
        this.genTOs.put(schemaPath, b);
    }

    public void addChildNodeType(final SchemaNode p, final GeneratedTypeBuilder b) {
        this.childNodes.put(p.getPath(), b);
        this.typeToSchema.put(b,p);
        this.dataTypes.put(b.getFullyQualifiedName(), b);
    }

    public void addGroupingType(final GroupingDefinition p, final GeneratedTypeBuilder b) {
        this.groupings.put(p.getPath(), b);
        this.typeToSchema.put(b, p);
    }

    public void addTypedefType(final SchemaPath p, final Type t) {
        this.typedefs.put(p, t);
    }

    public void addCaseType(final SchemaPath p, final GeneratedTypeBuilder b) {
        this.cases.put(p, b);
    }

    public void addIdentityType(final QName name,final GeneratedTOBuilder b) {
        this.identities.put(name,b);
    }

    public void addTopLevelNodeType(final GeneratedTypeBuilder b) {
        this.topLevelNodes.add(b);
    }

    public void addAugmentType(final GeneratedTypeBuilder b) {
        this.augmentations.add(b);
    }

    public Map<SchemaPath, Type> getTypedefs() {
        return this.typedefs;
    }

    public Map<SchemaPath, GeneratedTypeBuilder> getChildNodes() {
        return Collections.unmodifiableMap(this.childNodes);
    }

    public Map<SchemaPath, GeneratedTypeBuilder> getGroupings() {
        return Collections.unmodifiableMap(this.groupings);
    }

    public Map<SchemaPath, GeneratedTypeBuilder> getCases() {
        return Collections.unmodifiableMap(this.cases);
    }

    public Map<QName,GeneratedTOBuilder> getIdentities() {
        return Collections.unmodifiableMap(this.identities);
    }

    public Set<GeneratedTypeBuilder> getTopLevelNodes() {
        return Collections.unmodifiableSet(this.topLevelNodes);
    }

    public List<GeneratedTypeBuilder> getAugmentations() {
        return Collections.unmodifiableList(this.augmentations);
    }

    public Map<Type, List<AugmentationSchema>> getTypeToAugmentations() {
        return Collections.unmodifiableMap(this.typeToAugmentations);
    }

    public BiMap<SchemaPath, Type> getTargetToAugmentation() {
        return Maps.unmodifiableBiMap(this.targetToAugmentation);
    }

    public void addTypeToAugmentation(final GeneratedTypeBuilder builder, final AugmentationSchema schema) {
        this.typeToSchema.put(builder, schema);
    }

    public void addTypeToAugmentations(final GeneratedTypeBuilder builder, final List<AugmentationSchema> schemaList) {
        this.typeToAugmentations.put(builder, schemaList);
    }

    public void addTargetToAugmentation(final GeneratedTypeBuilder builder, final SchemaPath augmentTarget) {
        this.targetToAugmentation.put(augmentTarget, builder);
    }

    public void addChoiceToCaseMapping(final Type choiceType, final Type caseType, final ChoiceCaseNode schema) {
        this.choiceToCases.put(choiceType, caseType);
        this.caseTypeToSchema.put(caseType, schema);
        this.typeToSchema.put(caseType, schema);
    }

    public BiMap<Type, ChoiceCaseNode> getCaseTypeToSchemas() {
        return Maps.unmodifiableBiMap(this.caseTypeToSchema);
    }

    /**
     *
     * Returns mapping of type to its schema.
     *
     * Valid values are only instances of {@link DataSchemaNode} or {@link AugmentationSchema}
     *
     * @return Mapping from type to corresponding schema
     */
    public Map<Type, Object> getTypeToSchema() {
        return Collections.unmodifiableMap(this.typeToSchema);
    }

    public void addTypeToSchema(final Type type, final TypeDefinition<?> typedef) {
        this.typeToSchema.put(type, typedef);
    }

    /**
     * Adds mapping between schema path and inner enum, inner union, inner bits.
     *
     * @param path
     * @param builder
     */
    public void addInnerTypedefType(final SchemaPath path, final Type builder) {
        this.innerTypes.put(path, builder);
    }

    public Type getInnerType(final SchemaPath path) {
        return this.innerTypes.get(path);
    }


    public void addKeyType(final SchemaPath path, final GeneratedTypeBuilder genType) {
        this.keyTypes.put(path, genType);
    }

    public GeneratedTypeBuilder getKeyType(final SchemaPath path) {
        return this.keyTypes.get(path);
    }

    public GeneratedTOBuilder getKeyGenTO(final SchemaPath path) {
        return this.genTOs.get(path);
    }

    public ListMultimap<String, String> getPackagesMap() {
        return Multimaps.unmodifiableListMultimap(packagesMap);
    }

    public void putToPackagesMap(final String packageName, final String actualClassName) {
        this.packagesMap.put(packageName, actualClassName);
    }

    public void cleanPackagesMap() {
        this.packagesMap.clear();
    }
}
