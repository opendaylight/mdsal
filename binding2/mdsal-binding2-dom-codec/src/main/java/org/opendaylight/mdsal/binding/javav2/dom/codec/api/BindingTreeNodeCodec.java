/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableCollection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeArgument;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;

/**
 * Specific subtree codec to model subtree between Java Binding and DOM.
 *
 * @param <T>
 *            - Binding representation of data
 */
@Beta
public interface BindingTreeNodeCodec<T extends TreeNode> extends BindingNormalizedNodeCodec<T> {

    /**
     * Returns binding class of interface which represents API of current schema
     * node.
     *
     * @return interface which defines API of binding representation of data.
     */
    @Nonnull
    Class<T> getBindingClass();

    /**
     * Returns child context as if it was walked by
     * {@link BindingStreamEventWriter}. This means that to enter case, one must
     * issue getChild(ChoiceClass).getChild(CaseClass).
     *
     * @param childClass
     *            - child class by Biding Stream navigation
     * @param <E> data type
     * @return context of child
     * @throws IllegalArgumentException
     *             - if supplied child class is not valid in specified context
     */
    @Nonnull
    <E extends TreeNode> BindingTreeNodeCodec<E> streamChild(@Nonnull Class<E> childClass);

    /**
     * Returns child context as if it was walked by
     * {@link BindingStreamEventWriter}. This means that to enter case, one must
     * issue getChild(ChoiceClass).getChild(CaseClass).
     *
     * <p>
     * This method differs from {@link #streamChild(Class)}, that is less
     * stricter for interfaces representing augmentation and cases, that may
     * return {@link BindingTreeNodeCodec} even if augmentation interface
     * containing same data was supplied and does not represent augmentation of
     * this node.
     *
     * @param childClass
     *            - child class by Binding Stream navigation
     * @param <E> data type
     * @return context of child or Optional.empty if supplied is not applicable
     *         in context
     */
    <E extends TreeNode> Optional<? extends BindingTreeNodeCodec<E>> possibleStreamChild(@Nonnull Class<E> childClass);

    /**
     * Returns nested node context using supplied YANG Instance Identifier.
     *
     * @param child
     *            - Yang Instance Identifier Argument
     * @return context of child
     * @throws IllegalArgumentException
     *             - if supplied argument does not represent valid child
     */
    @Nonnull
    BindingTreeNodeCodec<?> yangPathArgumentChild(@Nonnull YangInstanceIdentifier.PathArgument child);

    /**
     * Returns nested node context using supplied Binding Instance Identifier
     * and adds YANG instance identifiers to supplied list.
     *
     * @param arg
     *            - Binding Instance Identifier Argument
     * @param builder
     *            - mutable instance of list, which is appended by
     *            YangInstanceIdentifiers as tree is walked, use null if such
     *            side-product is not needed
     * @return context of child
     * @throws IllegalArgumentException
     *             - if supplied argument does not represent valid child.
     */
    @Nonnull
    BindingTreeNodeCodec<?> bindingPathArgumentChild(@Nonnull TreeArgument<?> arg,
            @Nullable List<YangInstanceIdentifier.PathArgument> builder);

    /**
     * Returns codec which uses caches serialization / deserialization results.
     *
     * <p>
     * Caching may introduce performance penalty to serialization /
     * deserialization but may decrease use of heap for repetitive objects.
     *
     * @param cacheSpecifier
     *            - set of objects, for which cache may be in place
     * @return codec which uses cache for serialization / deserialization
     */
    @Nonnull
    BindingNormalizedNodeCachingCodec<T>
            createCachingCodec(@Nonnull ImmutableCollection<Class<? extends TreeNode>> cacheSpecifier);

    /**
     * Writes data representing object to supplied stream.
     *
     * @param data
     *            - representing object
     * @param writer
     *            - supplied stream
     */
    void writeAsNormalizedNode(T data, NormalizedNodeStreamWriter writer);

    /**
     * Serializes path argument for current node.
     *
     * @param arg
     *            - Binding Path Argument, may be null if Binding Instance
     *            Identifier does not have representation for current node (e.g.
     *            choice or case)
     * @return Yang Path Argument, may be null if Yang Instance Identifier does
     *         not have representation for current node (e.g. case).
     * @throws IllegalArgumentException
     *             - if supplied {@code arg} is not valid.
     */
    @Nullable
    YangInstanceIdentifier.PathArgument serializePathArgument(@Nullable TreeArgument<?> arg);

    /**
     * Deserializes path argument for current node.
     *
     * @param arg
     *            - Yang Path Argument, may be null if Yang Instance Identifier
     *            does not have representation for current node (e.g. case)
     * @return Binding Path Argument, may be null if Binding Instance Identifier
     *         does not have representation for current node (e.g. choice or
     *         case)
     * @throws IllegalArgumentException
     *             - if supplied {@code arg} is not valid.
     */
    @Nullable
    TreeArgument<?> deserializePathArgument(@Nullable YangInstanceIdentifier.PathArgument arg);

    /**
     * Return schema of node codec context.
     *
     * @return {@link Object} as schema of specific node context
     */
    @Nonnull
    Object getSchema();
}
