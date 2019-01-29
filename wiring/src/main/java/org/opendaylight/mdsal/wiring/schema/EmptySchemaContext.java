/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring.schema;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
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
 * {@link SchemaContext} which is completely empty.
 *
 * @author Michael Vorburger.ch
 */
final class EmptySchemaContext implements SchemaContext {
    // intentionally package local here, for now; may be later move to yangtools

    public static final @Nullable SchemaContext INSTANCE = new EmptySchemaContext();

    private EmptySchemaContext() { }

    @Override
    public Set<Module> getModules() {
        return Collections.emptySet();
    }

    @Override
    public Set<DataSchemaNode> getDataDefinitions() {
        return Collections.emptySet();
    }

    @Override
    public Set<ExtensionDefinition> getExtensions() {
        return Collections.emptySet();
    }

    @Override
    public Set<RpcDefinition> getOperations() {
        return Collections.emptySet();
    }

    @Override
    public Set<GroupingDefinition> getGroupings() {
        return Collections.emptySet();
    }

    @Override
    public Set<TypeDefinition<?>> getTypeDefinitions() {
        return Collections.emptySet();
    }

    @Override
    public Set<UsesNode> getUses() {
        return Collections.emptySet();
    }

    @Override
    public Set<AugmentationSchemaNode> getAvailableAugmentations() {
        return Collections.emptySet();
    }

    @Override
    public Set<NotificationDefinition> getNotifications() {
        return Collections.emptySet();
    }

    @Override
    public Collection<DataSchemaNode> getChildNodes() {
        return Collections.emptySet();
    }

    @Override
    public Optional<DataSchemaNode> findDataChildByName(QName qname) {
        return Optional.empty();
    }

    @Override
    public Optional<Module> findModule(QNameModule qname) {
        return Optional.empty();
    }

    @Override
    public SchemaPath getPath() {
        // This will actually never be called (because HybridSchemaContext delegates to the fallbackContext for this op)
        throw new UnsupportedOperationException();
    }

    @Override
    public QName getQName() {
        // This will actually never be called (because HybridSchemaContext delegates to the fallbackContext for this op)
        throw new UnsupportedOperationException();
    }

    @Override
    public Status getStatus() {
        // This will actually never be called (because HybridSchemaContext delegates to the fallbackContext for this op)
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPresenceContainer() {
        // This will actually never be called (because HybridSchemaContext delegates to the fallbackContext for this op)
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isConfiguration() {
        // This will actually never be called (because HybridSchemaContext delegates to the fallbackContext for this op)
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAugmenting() {
        // This will actually never be called (because HybridSchemaContext delegates to the fallbackContext for this op)
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAddedByUses() {
        // This will actually never be called (because HybridSchemaContext delegates to the fallbackContext for this op)
        throw new UnsupportedOperationException();
    }
}
