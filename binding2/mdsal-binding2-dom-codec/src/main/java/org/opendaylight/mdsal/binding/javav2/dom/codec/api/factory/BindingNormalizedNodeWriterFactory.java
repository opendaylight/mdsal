/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.api.factory;

import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.Instantiable;
import org.opendaylight.mdsal.binding.javav2.spec.base.Notification;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;

/**
 * Factory for {@link BindingStreamEventWriter}, which provides stream writers
 * which translates data and delegates calls to
 * {@link NormalizedNodeStreamWriter}.
 *
 */
public interface BindingNormalizedNodeWriterFactory {

    /**
     * Creates a {@link BindingStreamEventWriter} for data tree path which will
     * translate to NormalizedNode model and invoke proper events on supplied
     * {@link NormalizedNodeStreamWriter}.
     *
     * <p>
     * Also provides translation of supplied Instance Identifier to
     * {@link YangInstanceIdentifier} so client code, does not need to translate
     * that separately.
     *
     * <p>
     * If {@link YangInstanceIdentifier} is not needed, please use
     * {@link #newWriter(InstanceIdentifier, NormalizedNodeStreamWriter)} method
     * to conserve resources.
     *
     * @param path
     *            - Binding Path in conceptual data tree, for which writer
     *            should be instantiated
     * @param domWriter
     *            - Stream writer on which events will be invoked
     * @return Instance Identifier and {@link BindingStreamEventWriter} which
     *         will write to supplied {@link NormalizedNodeStreamWriter}
     * @throws IllegalArgumentException
     *             - if supplied Instance Identifier is not valid
     */
    @Nonnull
    Entry<YangInstanceIdentifier, BindingStreamEventWriter> newWriterAndIdentifier(
            @Nonnull InstanceIdentifier<? extends TreeNode> path, @Nonnull NormalizedNodeStreamWriter domWriter);

    /**
     * Creates a {@link BindingStreamEventWriter} for data tree path which will
     * translate to NormalizedNode model and invoke proper events on supplied
     * {@link NormalizedNodeStreamWriter}.
     *
     * <p>
     * This variation does not provide YANG instance identifier and is useful
     * for use-cases, where {@link InstanceIdentifier} translation is done in
     * other way, or YANG instance identifier is unnecessary (e.g.
     * notifications, operations).
     *
     * @param path
     *            - Binding Path in conceptual data tree, for which writer
     *            should be instantiated
     * @param domWriter
     *            - Stream writer on which events will be invoked
     * @return {@link BindingStreamEventWriter} which will write to supplied
     *         {@link NormalizedNodeStreamWriter}
     * @throws IllegalArgumentException
     *             - if supplied Instance Identifier is not valid
     */
    @Nonnull
    BindingStreamEventWriter newWriter(@Nonnull InstanceIdentifier<? extends TreeNode> path,
            @Nonnull NormalizedNodeStreamWriter domWriter);

    /**
     * Creates a {@link BindingStreamEventWriter} for operation data which will
     * translate to NormalizedNode model and invoke proper events on supplied
     * {@link NormalizedNodeStreamWriter}.
     *
     * @param operationInputOrOutput
     *            - binding class representing operation (RPC, Action) input or
     *            output, for which writer should be instantiated
     * @param domWriter
     *            - stream writer on which events will be invoked
     * @return {@link BindingStreamEventWriter} which will write to supplied
     *         {@link NormalizedNodeStreamWriter}
     */
    @Nonnull
    BindingStreamEventWriter newOperationWriter(@Nonnull Class<? extends Instantiable<?>> operationInputOrOutput,
            @Nonnull NormalizedNodeStreamWriter domWriter);

    /**
     * Creates a {@link BindingStreamEventWriter} for notification which will
     * translate to NormalizedNode model and invoke proper events on supplied
     * {@link NormalizedNodeStreamWriter}.
     *
     * @param notification
     *            - binding class representing notification, for which writer
     *            should be instantiated
     * @param domWriter
     *            - stream writer on which events will be invoked
     * @return {@link BindingStreamEventWriter} which will write to supplied
     *         {@link NormalizedNodeStreamWriter}
     */
    @Nonnull
    BindingStreamEventWriter newNotificationWriter(@Nonnull Class<? extends Notification<?>> notification,
            @Nonnull NormalizedNodeStreamWriter domWriter);
}
