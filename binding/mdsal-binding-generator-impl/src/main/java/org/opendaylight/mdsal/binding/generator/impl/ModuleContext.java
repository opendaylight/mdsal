/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
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
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

public final class ModuleContext {
    private GeneratedTypeBuilder moduleNode;
    private final List<GeneratedTOBuilder> genTOs = new ArrayList<>();
    private final Map<SchemaPath, Type> typedefs = new HashMap<>();
    private final Map<SchemaPath, GeneratedTypeBuilder> childNodes = new HashMap<>();
    private final Map<SchemaPath, GeneratedTypeBuilder> groupings = new HashMap<>();
    private final Map<SchemaPath, GeneratedTypeBuilder> cases = new HashMap<>();
    private final Map<QName,GeneratedTOBuilder> identities = new HashMap<>();
    private final Set<GeneratedTypeBuilder> topLevelNodes = new HashSet<>();
    private final List<GeneratedTypeBuilder> augmentations = new ArrayList<>();
    private final BiMap<Type, AugmentationSchemaNode> typeToAugmentation = HashBiMap.create();

    private final Map<Type,Object> typeToSchema = new HashMap<>();


    private final Multimap<Type, Type> choiceToCases = HashMultimap.create();
    private final BiMap<Type, CaseSchemaNode> caseTypeToSchema = HashBiMap.create();

    private final Map<SchemaPath, Type> innerTypes = new HashMap<>();

    List<Type> getGeneratedTypes() {
        List<Type> result = new ArrayList<>();

        if (moduleNode != null) {
            result.add(moduleNode.toInstance());
        }

        for (GeneratedTOBuilder b : genTOs) {
            result.add(b.toInstance());
        }
        for (Type b : typedefs.values()) {
            if (b != null) {
                result.add(b);
            }
        }
        for (GeneratedTypeBuilder b : childNodes.values()) {
            result.add(b.toInstance());
        }
        for (GeneratedTypeBuilder b : groupings.values()) {
            result.add(b.toInstance());
        }
        for (GeneratedTypeBuilder b : cases.values()) {
            result.add(b.toInstance());
        }
        for (GeneratedTOBuilder b : identities.values()) {
            result.add(b.toInstance());
        }
        for (GeneratedTypeBuilder b : topLevelNodes) {
            result.add(b.toInstance());
        }
        for (GeneratedTypeBuilder b : augmentations) {
            result.add(b.toInstance());
        }
        return result;
    }

    public Multimap<Type, Type> getChoiceToCases() {
        return Multimaps.unmodifiableMultimap(choiceToCases);
    }

    public GeneratedTypeBuilder getModuleNode() {
        return moduleNode;
    }

    public GeneratedTypeBuilder getChildNode(final SchemaPath p) {
        return childNodes.get(p);
    }

    public GeneratedTypeBuilder getGrouping(final SchemaPath p) {
        return groupings.get(p);
    }

    public GeneratedTypeBuilder getCase(final SchemaPath p) {
        return cases.get(p);
    }

    public void addModuleNode(final GeneratedTypeBuilder moduleNode) {
        this.moduleNode = moduleNode;
    }

    public void addGeneratedTOBuilder(final GeneratedTOBuilder b) {
        genTOs.add(b);
    }

    public void addChildNodeType(final SchemaNode p, final GeneratedTypeBuilder b) {
        childNodes.put(p.getPath(), b);
        typeToSchema.put(b,p);
    }

    public void addGroupingType(final SchemaPath p, final GeneratedTypeBuilder b) {
        groupings.put(p, b);
    }

    public void addTypedefType(final SchemaPath p, final Type t) {
        typedefs.put(p, t);
    }

    public void addCaseType(final SchemaPath p, final GeneratedTypeBuilder b) {
        cases.put(p, b);
    }

    public void addIdentityType(final QName name,final GeneratedTOBuilder b) {
        identities.put(name,b);
    }

    public void addTopLevelNodeType(final GeneratedTypeBuilder b) {
        topLevelNodes.add(b);
    }

    public void addAugmentType(final GeneratedTypeBuilder b) {
        augmentations.add(b);
    }

    public Map<SchemaPath, Type> getTypedefs() {
        return typedefs;
    }

    public Map<SchemaPath, GeneratedTypeBuilder> getChildNodes() {
        return Collections.unmodifiableMap(childNodes);
    }

    public Map<SchemaPath, GeneratedTypeBuilder> getGroupings() {
        return Collections.unmodifiableMap(groupings);
    }

    public Map<SchemaPath, GeneratedTypeBuilder> getCases() {
        return Collections.unmodifiableMap(cases);
    }

    public Map<QName,GeneratedTOBuilder> getIdentities() {
        return Collections.unmodifiableMap(identities);
    }

    public Set<GeneratedTypeBuilder> getTopLevelNodes() {
        return Collections.unmodifiableSet(topLevelNodes);
    }

    public List<GeneratedTypeBuilder> getAugmentations() {
        return Collections.unmodifiableList(augmentations);
    }

    public BiMap<Type, AugmentationSchemaNode> getTypeToAugmentation() {
        return Maps.unmodifiableBiMap(typeToAugmentation);
    }

    public void addTypeToAugmentation(final GeneratedTypeBuilder builder, final AugmentationSchemaNode schema) {
        typeToAugmentation.put(builder, schema);
        typeToSchema.put(builder, schema);
    }

    public void addChoiceToCaseMapping(final Type choiceType, final Type caseType, final CaseSchemaNode schema) {
        choiceToCases.put(choiceType, caseType);
        caseTypeToSchema.put(caseType, schema);
        typeToSchema.put(caseType, schema);
    }

    public BiMap<Type, CaseSchemaNode> getCaseTypeToSchemas() {
        return Maps.unmodifiableBiMap(caseTypeToSchema);
    }

    /**
     *
     * Returns mapping of type to its schema.
     *
     * Valid values are only instances of {@link DataSchemaNode} or {@link AugmentationSchemaNode}
     *
     * @return Mapping from type to corresponding schema
     */
    public Map<Type, Object> getTypeToSchema() {
        return Collections.unmodifiableMap(typeToSchema);
    }

    protected void addTypeToSchema(final Type type, final TypeDefinition<?> typedef) {
        typeToSchema.put(type, typedef);
    }

    /**
     * Adds mapping between schema path and an inner type.
     *
     * @param path
     * @param type
     */
    void addInnerTypedefType(final SchemaPath path, final Type type) {
        innerTypes.put(path, type);
    }

    public Type getInnerType(final SchemaPath path) {
        return innerTypes.get(path);
    }

}
