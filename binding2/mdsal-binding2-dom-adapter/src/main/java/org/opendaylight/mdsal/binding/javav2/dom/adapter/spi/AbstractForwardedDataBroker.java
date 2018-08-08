/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.spi;

import com.google.common.annotations.Beta;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.codec.DeserializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class forwards DOM Data Broker and provides de-serialization of DOM data to Binding data.
 */
@Beta
public abstract class AbstractForwardedDataBroker implements Delegator<DOMDataBroker>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractForwardedDataBroker.class);

    // The Broker to whom we do all forwarding
    private final DOMDataBroker domDataBroker;

    private final BindingToNormalizedNodeCodec codec;

    protected AbstractForwardedDataBroker(final DOMDataBroker domDataBroker, final BindingToNormalizedNodeCodec codec) {
        this.domDataBroker = domDataBroker;
        this.codec = codec;
    }

    protected BindingToNormalizedNodeCodec getCodec() {
        return codec;
    }

    @Override
    public DOMDataBroker getDelegate() {
        return domDataBroker;
    }

    protected Map<InstanceIdentifier<? extends TreeNode>, TreeNode> toBinding(
            final InstanceIdentifier<? extends TreeNode> path,
            final Map<YangInstanceIdentifier, ? extends NormalizedNode<?, ?>> normalized) {
        final Map<InstanceIdentifier<? extends TreeNode>, TreeNode> newMap = new HashMap<>();

        for (final Map.Entry<YangInstanceIdentifier, ? extends NormalizedNode<?, ?>> entry : normalized.entrySet()) {
            try {
                final Optional<Entry<InstanceIdentifier<? extends TreeNode>, TreeNode>> potential =
                        getCodec().toBinding(entry);
                if (potential.isPresent()) {
                    final Entry<InstanceIdentifier<? extends TreeNode>, TreeNode> binding = potential.get();
                    newMap.put(binding.getKey(), binding.getValue());
                }
            } catch (final DeserializationException e) {
                LOG.warn("Failed to transform {}, omitting it", entry, e);
            }
        }
        return newMap;
    }

    protected Set<InstanceIdentifier<?>> toBinding(final InstanceIdentifier<?> path,
            final Set<YangInstanceIdentifier> normalized) {
        final Set<InstanceIdentifier<?>> hashSet = new HashSet<>();
        for (final YangInstanceIdentifier normalizedPath : normalized) {
            try {
                final Optional<InstanceIdentifier<? extends TreeNode>> potential =
                        getCodec().toBinding(normalizedPath);
                if (potential.isPresent()) {
                    final InstanceIdentifier<? extends TreeNode> binding = potential.get();
                    hashSet.add(binding);
                } else if (normalizedPath
                        .getLastPathArgument() instanceof YangInstanceIdentifier.AugmentationIdentifier) {
                    hashSet.add(path);
                }
            } catch (final DeserializationException e) {
                LOG.warn("Failed to transform {}, omitting it", normalizedPath, e);
            }
        }
        return hashSet;
    }

    @SuppressWarnings("unchecked")
    protected Optional<TreeNode> toBindingData(final InstanceIdentifier<?> path, final NormalizedNode<?, ?> data) {
        if (path.isWildcarded()) {
            return Optional.empty();
        }
        return (Optional<TreeNode>) getCodec().deserializeFunction(path)
                .apply(Optional.of(data));
    }

    @Override
    public void close() {
        // Intentional NOOP
    }
}
