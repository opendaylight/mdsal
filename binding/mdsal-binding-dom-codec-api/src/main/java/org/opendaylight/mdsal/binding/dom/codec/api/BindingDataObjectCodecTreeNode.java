/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableCollection;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.BindingObject;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

@Beta
public interface BindingDataObjectCodecTreeNode<T extends DataObject>
        extends CommonDataObjectCodecTreeNode<T>, BindingNormalizedNodeCodec<T> {
    /**
     * Returns codec which uses caches serialization / deserialization results.
     *
     * <p>
     * Caching may introduce performance penalty to serialization / deserialization
     * but may decrease use of heap for repetitive objects.
     *
     * @param cacheSpecifier Set of objects, for which cache may be in place
     * @return Codec which uses cache for serialization / deserialization.
     */
    @NonNull BindingNormalizedNodeCachingCodec<T> createCachingCodec(
            @NonNull ImmutableCollection<Class<? extends BindingObject>> cacheSpecifier);

    /**
     * Returns full set of child path arguments.
     *
     * @return set of path arguments
     */
    default @NonNull Set<YangInstanceIdentifier.PathArgument> getChildPathArguments() {
        return Set.of();
    }

    /**
     * Returns full set of child binding classes.
     *
     * @return set of child classes
     */
    default @NonNull Set<Class<?>> getChildBindingClasses() {
        return Set.of();
    }
}
