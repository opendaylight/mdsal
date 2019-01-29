/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring.schema;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

/**
 * {@link SchemaContext} which is composed of 2 other ones.
 *
 * @author Michael Vorburger.ch
 */
class HybridSchemaContext implements SchemaContext {
    // intentionally package local here, for now; may be later move to yangtools

    private final SchemaContext priorityContext;
    private final SchemaContext fallbackContext;

    HybridSchemaContext(
            @Nullable SchemaContext priorityContext, @NonNull SchemaContext fallbackContext) {
        this.priorityContext = priorityContext != null ? priorityContext : EmptySchemaContext.INSTANCE;
        this.fallbackContext = requireNonNull(fallbackContext, "fallbackContext");
    }

    @Override
    public Set<Module> getModules() {
        return Sets.union(priorityContext.getModules(), fallbackContext.getModules());
    }

    @Override
    public Set<DataSchemaNode> getDataDefinitions() {
        return Sets.union(priorityContext.getDataDefinitions(), fallbackContext.getDataDefinitions());
    }

    @Override
    public Set<ExtensionDefinition> getExtensions() {
        return Sets.union(priorityContext.getExtensions(), fallbackContext.getExtensions());
    }

    @Override
    public Set<RpcDefinition> getOperations() {
        return Sets.union(priorityContext.getOperations(), fallbackContext.getOperations());
    }

    @Override
    public Set<GroupingDefinition> getGroupings() {
        return Sets.union(priorityContext.getGroupings(), fallbackContext.getGroupings());
    }

    @Override
    public Set<TypeDefinition<?>> getTypeDefinitions() {
        return Sets.union(priorityContext.getTypeDefinitions(), fallbackContext.getTypeDefinitions());
    }

    @Override
    public Set<UsesNode> getUses() {
        return Sets.union(priorityContext.getUses(), fallbackContext.getUses());
    }

    @Override
    public Set<AugmentationSchemaNode> getAvailableAugmentations() {
        return Sets.union(priorityContext.getAvailableAugmentations(), fallbackContext.getAvailableAugmentations());
    }

    @Override
    public Set<NotificationDefinition> getNotifications() {
        return Sets.union(priorityContext.getNotifications(), fallbackContext.getNotifications());
    }

    @Override
    public Collection<DataSchemaNode> getChildNodes() {
        return new UnionCollection<>(priorityContext.getChildNodes(), fallbackContext.getChildNodes());
    }

    @Override
    public Optional<DataSchemaNode> findDataChildByName(QName qname) {
        return or(priorityContext.findDataChildByName(qname), () -> fallbackContext.findDataChildByName(qname));
    }

    @Override
    public Optional<Module> findModule(QNameModule qname) {
        return or(priorityContext.findModule(qname), () -> fallbackContext.findModule(qname));
    }

    @Override
    public SchemaPath getPath() {
        return fallbackContext.getPath();
    }

    @Override
    public QName getQName() {
        return fallbackContext.getQName();
    }

    @Override
    public Status getStatus() {
        return fallbackContext.getStatus();
    }

    @Override
    public boolean isPresenceContainer() {
        return fallbackContext.isPresenceContainer();
    }

    @Override
    public boolean isConfiguration() {
        return fallbackContext.isConfiguration();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isAugmenting() {
        return fallbackContext.isAugmenting();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isAddedByUses() {
        return fallbackContext.isAddedByUses();
    }

    // when we are on Java 9+ then replace this by the built-in Optional.or() (see https://docs.oracle.com/javase/9/docs/api/java/util/Optional.html#or-java.util.function.Supplier-)
    private static <T> Optional<T> or(Optional<T> first, Supplier<Optional<T>> supplierOfSecond) {
        if (first.isPresent()) {
            return first;
        }
        Optional<T> second = supplierOfSecond.get();
        if (second.isPresent()) {
            return second;
        }
        return Optional.empty();
    }
}
