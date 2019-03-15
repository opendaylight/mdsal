/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static java.util.Objects.requireNonNull;

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
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModuleContext implements Mutable {
    private static final Logger LOG = LoggerFactory.getLogger(ModuleContext.class);

    private final BiMap<Type, AugmentationSchemaNode> typeToAugmentation = HashBiMap.create();
    private final Map<SchemaPath, GeneratedTypeBuilder> childNodes = new HashMap<>();
    private final Map<SchemaPath, GeneratedTypeBuilder> groupings = new HashMap<>();
    private final BiMap<Type, CaseSchemaNode> caseTypeToSchema = HashBiMap.create();
    private final Map<SchemaPath, GeneratedTypeBuilder> cases = new HashMap<>();
    private final Map<QName, GeneratedTypeBuilder> identities = new HashMap<>();
    private final List<GeneratedTypeBuilder> augmentations = new ArrayList<>();
    private final Multimap<Type, Type> choiceToCases = HashMultimap.create();
    private final Set<GeneratedTypeBuilder> topLevelNodes = new HashSet<>();
    private final Map<Type, WithStatus> typeToSchema = new HashMap<>();
    private final List<GeneratedTOBuilder> genTOs = new ArrayList<>();
    private final Map<SchemaPath, Type> innerTypes = new HashMap<>();
    private final Map<SchemaPath, Type> typedefs = new HashMap<>();
    private final Module module;

    // Conflict mapping
    private final Map<JavaTypeName, SchemaNode> nameMapping = new HashMap<>();

    private GeneratedTypeBuilder moduleNode;
    private String modulePackageName;

    ModuleContext(final Module module) {
        this.module = requireNonNull(module);
    }

    Module module() {
        return module;
    }

    String modulePackageName() {
        String ret = modulePackageName;
        if (ret == null) {
            modulePackageName = ret = BindingMapping.getRootPackageName(module.getQNameModule());
        }
        return ret;
    }

    List<Type> getGeneratedTypes() {
        List<Type> result = new ArrayList<>();

        if (moduleNode != null) {
            result.add(moduleNode.build());
        }

        for (GeneratedTOBuilder b : genTOs) {
            result.add(b.build());
        }
        for (Type b : typedefs.values()) {
            if (b != null) {
                result.add(b);
            }
        }
        for (GeneratedTypeBuilder b : childNodes.values()) {
            result.add(b.build());
        }
        for (GeneratedTypeBuilder b : groupings.values()) {
            result.add(b.build());
        }
        for (GeneratedTypeBuilder b : cases.values()) {
            result.add(b.build());
        }
        for (GeneratedTypeBuilder b : identities.values()) {
            result.add(b.build());
        }
        for (GeneratedTypeBuilder b : topLevelNodes) {
            result.add(b.build());
        }
        for (GeneratedTypeBuilder b : augmentations) {
            result.add(b.build());
        }
        return result;
    }

    public Multimap<Type, Type> getChoiceToCases() {
        return Multimaps.unmodifiableMultimap(choiceToCases);
    }

    public GeneratedTypeBuilder getModuleNode() {
        return moduleNode;
    }

    public GeneratedTypeBuilder getChildNode(final SchemaPath path) {
        return childNodes.get(path);
    }

    public GeneratedTypeBuilder getGrouping(final SchemaPath path) {
        return groupings.get(path);
    }

    public GeneratedTypeBuilder getCase(final SchemaPath path) {
        return cases.get(path);
    }

    public void addModuleNode(final GeneratedTypeBuilder newModuleNode) {
        this.moduleNode = newModuleNode;
    }

    public void addGeneratedTOBuilder(final GeneratedTOBuilder builder) {
        genTOs.add(builder);
    }

    public void addChildNodeType(final SchemaNode def, final GeneratedTypeBuilder builder) {
        checkNamingConflict(def, builder.getIdentifier());
        childNodes.put(def.getPath(), builder);
        typeToSchema.put(builder, def);
    }

    public void addGroupingType(final GroupingDefinition def, final GeneratedTypeBuilder builder) {
        checkNamingConflict(def, builder.getIdentifier());
        groupings.put(def.getPath(), builder);
    }

    public void addTypedefType(final TypeDefinition<?> def, final Type type) {
        final JavaTypeName name = type.getIdentifier();
        final SchemaNode existingDef = nameMapping.putIfAbsent(name, def);
        if (existingDef != null) {
            if (!(existingDef instanceof TypeDefinition)) {
                throw resolveNamingConfict(existingDef, def, name);
            }

            // This seems to be fine
            LOG.debug("GeneratedType conflict between {} and {} on {}", def, existingDef, name);
        }

        typedefs.put(def.getPath(), type);
    }

    public void addCaseType(final SchemaPath path, final GeneratedTypeBuilder builder) {
        cases.put(path, builder);
    }

    public void addIdentityType(final IdentitySchemaNode def, final GeneratedTypeBuilder builder) {
        checkNamingConflict(def, builder.getIdentifier());
        identities.put(def.getQName(), builder);
    }

    public void addTopLevelNodeType(final GeneratedTypeBuilder builder) {
        topLevelNodes.add(builder);
    }

    public void addAugmentType(final GeneratedTypeBuilder builder) {
        augmentations.add(builder);
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

    public Map<QName, GeneratedTypeBuilder> getIdentities() {
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
     * Returns mapping of type to its schema. Valid values are only instances of {@link DataSchemaNode}
     * or {@link AugmentationSchemaNode}.
     *
     * @return Mapping from type to corresponding schema
     */
    public Map<Type, WithStatus> getTypeToSchema() {
        return Collections.unmodifiableMap(typeToSchema);
    }

    protected void addTypeToSchema(final Type type, final TypeDefinition<?> typedef) {
        typeToSchema.put(type, typedef);
    }

    /**
     * Adds mapping between schema path and an inner type.
     */
    void addInnerTypedefType(final SchemaPath path, final Type type) {
        innerTypes.put(path, type);
    }

    public Type getInnerType(final SchemaPath path) {
        return innerTypes.get(path);
    }

    private void checkNamingConflict(final SchemaNode def, final JavaTypeName name) {
        final SchemaNode existingDef = nameMapping.putIfAbsent(name, def);
        if (existingDef != null) {
            if (def.equals(existingDef)) {
                if (LOG.isDebugEnabled()) {
                    // TODO: this should not really be happening
                    LOG.debug("Duplicate definition on {} as {}", name, def, new Throwable());
                }
            } else {
                throw resolveNamingConfict(existingDef, def, name);
            }
        }
    }

    private static IllegalStateException resolveNamingConfict(final SchemaNode existing, final SchemaNode incoming,
            final JavaTypeName name) {
        if (existing instanceof IdentitySchemaNode) {
            if (incoming instanceof GroupingDefinition || incoming instanceof TypeDefinition
                    || incoming instanceof DataSchemaNode || incoming instanceof NotificationDefinition
                    || incoming instanceof OperationDefinition) {
                return new RenameMappingException(name, existing);
            }
        } else if (existing instanceof GroupingDefinition) {
            if (incoming instanceof IdentitySchemaNode) {
                return new RenameMappingException(name, incoming);
            }
            if (incoming instanceof TypeDefinition || incoming instanceof DataSchemaNode
                    || incoming instanceof NotificationDefinition || incoming instanceof OperationDefinition) {
                return new RenameMappingException(name, existing);
            }
        } else if (existing instanceof TypeDefinition) {
            if (incoming instanceof GroupingDefinition || incoming instanceof IdentitySchemaNode) {
                return new RenameMappingException(name, incoming);
            }
            if (incoming instanceof DataSchemaNode || incoming instanceof NotificationDefinition
                    || incoming instanceof OperationDefinition) {
                return new RenameMappingException(name, existing);
            }
        } else {
            if (incoming instanceof GroupingDefinition || incoming instanceof IdentitySchemaNode
                    || incoming instanceof TypeDefinition) {
                return new RenameMappingException(name, incoming);
            }
        }

        return new IllegalStateException(String.format("Unhandled GeneratedType conflict between %s and %s on %s",
            incoming, existing, name));
    }
}
