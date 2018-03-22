/*
 * Copyright (c) 2018 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.generator.impl;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.packageNameWithNamespacePrefix;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Streams;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.mdsal.binding.javav2.generator.context.ModuleContext;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.javav2.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * This class holds information about generated entities in context of YANG module
 */
@Beta
@NotThreadSafe
public final class ModuleContextImpl implements ModuleContext {
    private GeneratedTypeBuilder moduleNode;
    private final Map<SchemaPath, GeneratedTOBuilder> genTOs = new HashMap<>();
    private final Map<SchemaPath, Type> typedefs = new HashMap<>();
    private final Map<SchemaPath, GeneratedTypeBuilder> childNodes = new HashMap<>();
    private final BiMap<String, GeneratedTypeBuilder> dataTypes = HashBiMap.create();
    private final Map<SchemaPath, GeneratedTypeBuilder> groupings = new HashMap<>();
    private final Map<SchemaPath, GeneratedTypeBuilder> cases = new HashMap<>();
    private final Map<QName,GeneratedTypeBuilder> identities = new HashMap<>();
    private final Set<GeneratedTypeBuilder> topLevelNodes = new HashSet<>();
    private final List<GeneratedTypeBuilder> augmentations = new ArrayList<>();
    private final Multimap<Type,AugmentationSchemaNode> typeToAugmentations = HashMultimap.create();
    private final BiMap<SchemaPath,Type> targetToAugmentation = HashBiMap.create();
    private final Map<Type,Object> typeToSchema = new HashMap<>();
    private final Multimap<Type, Type> choiceToCases = HashMultimap.create();
    private final BiMap<Type, CaseSchemaNode> caseTypeToSchema = HashBiMap.create();
    private final Map<SchemaPath, Type> innerTypes = new HashMap<>();
    private final Map<SchemaPath, GeneratedTypeBuilder> keyTypes = new HashMap<>();
    //map is getting manipulated based on unique YANG module namespace rule
    private final ListMultimap<String, String> packagesMap = Multimaps.synchronizedListMultimap
            (ArrayListMultimap.create());
    private final LoadingCache<BindingNamespaceType, String> nsPackageNames = CacheBuilder.newBuilder()
        .weakKeys().build(new CacheLoader<BindingNamespaceType, String>() {

            @Override
            public String load(@Nonnull final BindingNamespaceType key) {
                return packageNameWithNamespacePrefix(normalizedRootPackageName(), key);
            }
        });

    private final Module module;
    private String modulePackageName;

    public ModuleContextImpl(final Module module) {
        this.module = requireNonNull(module);
    }

    public Module module() {
        return module;
    }

    /**
     * Return normalized root package name string of the module.
     *
     * @return root package name
     */
    @Nonnull
    @Override
    public String normalizedRootPackageName() {
        if (modulePackageName == null) {
            modulePackageName = JavaIdentifierNormalizer.normalizeFullPackageName(
                BindingMapping.getRootPackageName(module));
        }
        return modulePackageName;
    }

    /**
     * Return normalized namespace package name string of the module.
     *
     * @return namespace package name
     */
    @Nonnull
    @Override
    public String normalizedNSPackageName(@Nonnull final BindingNamespaceType namespaceType) {
        return nsPackageNames.getUnchecked(namespaceType);
    }

    @Override
    public List<Type> getGeneratedTypes() {
        final List<Type> result = new ArrayList<>();

        if (this.moduleNode != null) {
            result.add(this.moduleNode.toInstance());
        }

        Streams.concat(
            this.genTOs.values().stream().map(GeneratedTOBuilder::toInstance),
            this.typedefs.values().stream().filter(Objects::nonNull),
            this.dataTypes.values().stream().map(GeneratedTypeBuilder::toInstance),
            this.groupings.values().stream().map(GeneratedTypeBuilder::toInstance),
            this.cases.values().stream().map(GeneratedTypeBuilder::toInstance),
            this.identities.values().stream().map(GeneratedTypeBuilder::toInstance),
            this.topLevelNodes.stream().map(GeneratedTypeBuilder::toInstance),
            this.augmentations.stream().map(GeneratedTypeBuilder::toInstance),
            this.keyTypes.values().stream().map(GeneratedTypeBuilder::toInstance))
        .forEach(result::add);
        return ImmutableList.copyOf(result);
    }

    @Override
    public Multimap<Type, Type> getChoiceToCases() {
        return Multimaps.unmodifiableMultimap(this.choiceToCases);
    }

    @Override
    public GeneratedTypeBuilder getModuleNode() {
        return this.moduleNode;
    }

    @Override
    public GeneratedTypeBuilder getChildNode(final SchemaPath p) {
        return this.childNodes.get(p);
    }

    @Override
    public GeneratedTypeBuilder getGrouping(final SchemaPath p) {
        return this.groupings.get(p);
    }

    @Override
    public GeneratedTypeBuilder getCase(final SchemaPath p) {
        return this.cases.get(p);
    }

    @Override
    public void addModuleNode(final GeneratedTypeBuilder moduleNode) {
        this.moduleNode = moduleNode;
    }

    @Override
    public void addGeneratedTOBuilder(final SchemaPath schemaPath, final GeneratedTOBuilder b) {
        this.genTOs.put(schemaPath, b);
    }

    @Override
    public void addChildNodeType(final SchemaNode p, final GeneratedTypeBuilder b) {
        this.childNodes.put(p.getPath(), b);
        this.typeToSchema.put(b,p);
        this.dataTypes.put(b.getFullyQualifiedName(), b);
    }

    @Override
    public void addGroupingType(final GroupingDefinition p, final GeneratedTypeBuilder b) {
        this.groupings.put(p.getPath(), b);
        this.typeToSchema.put(b, p);
    }

    @Override
    public void addTypedefType(final SchemaPath p, final Type t) {
        this.typedefs.put(p, t);
    }

    @Override
    public void addCaseType(final SchemaPath p, final GeneratedTypeBuilder b) {
        this.cases.put(p, b);
    }

    @Override
    public void addIdentityType(final QName name,final GeneratedTypeBuilder b) {
        this.identities.put(name,b);
    }

    @Override
    public void addTopLevelNodeType(final GeneratedTypeBuilder b) {
        this.topLevelNodes.add(b);
    }

    @Override
    public void addAugmentType(final GeneratedTypeBuilder b) {
        this.augmentations.add(b);
    }

    @Override
    public Map<SchemaPath, Type> getTypedefs() {
        return this.typedefs;
    }

    @Override
    public Map<SchemaPath, GeneratedTypeBuilder> getChildNodes() {
        return Collections.unmodifiableMap(this.childNodes);
    }

    @Override
    public Map<SchemaPath, GeneratedTypeBuilder> getGroupings() {
        return Collections.unmodifiableMap(this.groupings);
    }

    @Override
    public Map<SchemaPath, GeneratedTypeBuilder> getCases() {
        return Collections.unmodifiableMap(this.cases);
    }

    @Override
    public Map<QName,GeneratedTypeBuilder> getIdentities() {
        return Collections.unmodifiableMap(this.identities);
    }

    @Override
    public Set<GeneratedTypeBuilder> getTopLevelNodes() {
        return Collections.unmodifiableSet(this.topLevelNodes);
    }

    @Override
    public List<GeneratedTypeBuilder> getAugmentations() {
        return Collections.unmodifiableList(this.augmentations);
    }

    @Override
    public Multimap<Type, AugmentationSchemaNode> getTypeToAugmentations() {
        return Multimaps.unmodifiableMultimap(this.typeToAugmentations);
    }

    @Override
    public BiMap<SchemaPath, Type> getTargetToAugmentation() {
        return Maps.unmodifiableBiMap(this.targetToAugmentation);
    }

    @Override
    public void addTypeToAugmentations(final GeneratedTypeBuilder builder,
            final List<AugmentationSchemaNode> schemaList) {
        schemaList.forEach(augmentNode -> this.typeToAugmentations.put(builder, augmentNode));
    }

    @Override
    public void addTargetToAugmentation(final GeneratedTypeBuilder builder, final SchemaPath augmentTarget) {
        this.targetToAugmentation.put(augmentTarget, builder);
    }

    @Override
    public void addChoiceToCaseMapping(final Type choiceType, final Type caseType, final CaseSchemaNode schema) {
        this.choiceToCases.put(choiceType, caseType);
        this.caseTypeToSchema.put(caseType, schema);
        this.typeToSchema.put(caseType, schema);
    }

    @Override
    public BiMap<Type, CaseSchemaNode> getCaseTypeToSchemas() {
        return Maps.unmodifiableBiMap(this.caseTypeToSchema);
    }

    /**
     *
     * Returns mapping of type to its schema.
     *
     * Valid values are only instances of {@link DataSchemaNode} or {@link AugmentationSchemaNode}
     *
     * @return Mapping from type to corresponding schema
     */
    @Override
    public Map<Type, Object> getTypeToSchema() {
        return Collections.unmodifiableMap(this.typeToSchema);
    }

    @Override
    public void addTypeToSchema(final Type type, final TypeDefinition<?> typedef) {
        this.typeToSchema.put(type, typedef);
    }

    /**
     * Adds mapping between schema path and inner enum, inner union, inner bits.
     *
     * @param path
     * @param builder
     */
    @Override
    public void addInnerTypedefType(final SchemaPath path, final Type builder) {
        this.innerTypes.put(path, builder);
    }

    @Override
    public Type getInnerType(final SchemaPath path) {
        return this.innerTypes.get(path);
    }

    @Override
    public void addKeyType(final SchemaPath path, final GeneratedTypeBuilder genType) {
        this.keyTypes.put(path, genType);
    }

    @Override
    public GeneratedTypeBuilder getKeyType(final SchemaPath path) {
        return this.keyTypes.get(path);
    }

    @Override
    public GeneratedTOBuilder getKeyGenTO(final SchemaPath path) {
        return this.genTOs.get(path);
    }

    @Override
    public ListMultimap<String, String> getPackagesMap() {
        return Multimaps.unmodifiableListMultimap(packagesMap);
    }

    @Override
    public void putToPackagesMap(final String packageName, final String actualClassName) {
        this.packagesMap.put(packageName, actualClassName);
    }

    @Override
    public void cleanPackagesMap() {
        this.packagesMap.clear();
    }
}
