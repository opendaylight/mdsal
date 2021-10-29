/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * The result of BindingGenerator run. Contains mapping between Types and SchemaNodes.
 */
@Beta
public interface BindingRuntimeTypes extends EffectiveModelContextProvider, RuntimeTypeContainer, Immutable {

    Optional<AugmentRuntimeType> findAugmentation(Type type);

    Optional<Type> findIdentity(QName qname);

    Optional<WithStatus> findSchema(Type type);

    Optional<Type> findType(WithStatus schema);

    Optional<Type> findOriginalAugmentationType(AugmentationSchemaNode augment);

    Multimap<Type, Type> getChoiceToCases();

    Collection<Type> findCases(Type choiceType);

    default @Nullable RuntimeType schemaTreeChild(final Absolute path) {
        final var it = path.getNodeIdentifiers().iterator();
        var tmp = schemaTreeChild(it.next());
        while (it.hasNext() && tmp instanceof RuntimeTypeContainer) {
            tmp = ((RuntimeTypeContainer) tmp).schemaTreeChild(it.next());
        }
        return tmp;
    }
}
