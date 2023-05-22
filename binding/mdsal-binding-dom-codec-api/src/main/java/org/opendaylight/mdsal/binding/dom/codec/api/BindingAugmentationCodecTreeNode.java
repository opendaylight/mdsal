/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;

@Beta
public interface BindingAugmentationCodecTreeNode<T extends Augmentation<?>>
        extends CommonDataObjectCodecTreeNode<T> {
    /**
     * Return an {@link Augmentation} instance backed by specified parent data. Implementations are free to return
     * either an eager transformation (not retaining {@code parentData}), or a lazy proxy. In both cases they must
     * ensure the result has at least one property populated (i.e. is not empty).
     *
     * @return An Augmentation, or {@code null} if the augmentation would be empty
     * @throws NullPointerException if {@code parentData} is {@code null}
     * @throws IllegalArgumentException if {@code parentData} is not a compatible parent
     */
    @Nullable T filterFrom(@NonNull DataContainerNode parentData);

    /**
     * Return an {@link Augmentation} instance backed by specified parent data. Implementations are free to return
     * either an eager transformation (not retaining {@code parentData}), or a lazy proxy. In both cases they must
     * ensure the result has at least one property populated (i.e. is not empty).
     *
     * @return An Augmentation, or {@code null} if the augmentation would be empty
     * @throws NullPointerException if {@code parentData} is {@code null}
     * @throws IllegalArgumentException if {@code parentData} is not a compatible {@link DataContainerNode}
     */
    default @Nullable T filterFrom(final @NonNull NormalizedNode parentData) {
        if (requireNonNull(parentData) instanceof DataContainerNode parentContainer) {
            return filterFrom(parentContainer);
        }
        throw new IllegalArgumentException("Unsupported parent " + parentData.contract());
    }

    /**
     * Write the contents of an {@link Augmentation} into a writer. The writer must beinitialized at the
     * augmentations's {@link Augmentable} parent's equivalent {@link DataContainerNode}.
     *
     * @param writer Writer to stream to
     * @param data Data to stream
     * @throws NullPointerException if any argument is {@code null}
     * @throws IOException if a streaming error occurs
     */
    void streamTo(@NonNull NormalizedNodeStreamWriter writer, @NonNull T data) throws IOException;

    /**
     * Returns the {@link PathArgument}s of items contained in this {@link Augmentation}.
     *
     * @return A non-empty set of path arguments
     */
    @NonNull ImmutableSet<PathArgument> childPathArguments();

    /**
     * Returns the {@link Class}es of items contain in this {@link Augmentation}.
     *
     * @return A non-empty set of classes
     */
    @NonNull ImmutableSet<Class<?>> childBindingClasses();
}
