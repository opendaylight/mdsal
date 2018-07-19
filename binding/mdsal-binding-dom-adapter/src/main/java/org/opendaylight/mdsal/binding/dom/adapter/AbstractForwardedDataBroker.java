/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import com.google.common.base.Optional;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.codec.DeserializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractForwardedDataBroker extends AbstractBindingAdapter<@NonNull DOMDataBroker> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractForwardedDataBroker.class);

    protected AbstractForwardedDataBroker(final DOMDataBroker domDataBroker, final BindingToNormalizedNodeCodec codec) {
        super(codec, domDataBroker);
    }

    protected Map<InstanceIdentifier<?>, DataObject> toBinding(final InstanceIdentifier<?> path,
            final Map<YangInstanceIdentifier, ? extends NormalizedNode<?, ?>> normalized) {
        final Map<InstanceIdentifier<?>, DataObject> newMap = new HashMap<>();

        for (final Map.Entry<YangInstanceIdentifier, ? extends NormalizedNode<?, ?>> entry : normalized.entrySet()) {
            try {
                final Optional<Entry<InstanceIdentifier<? extends DataObject>, DataObject>> potential
                        = getCodec().toBinding(entry);
                if (potential.isPresent()) {
                    final Entry<InstanceIdentifier<? extends DataObject>, DataObject> binding = potential.get();
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
                final Optional<InstanceIdentifier<? extends DataObject>> potential
                        = getCodec().toBinding(normalizedPath);
                if (potential.isPresent()) {
                    final InstanceIdentifier<? extends DataObject> binding = potential.get();
                    hashSet.add(binding);
                } else if (normalizedPath.getLastPathArgument()
                        instanceof YangInstanceIdentifier.AugmentationIdentifier) {
                    hashSet.add(path);
                }
            } catch (final DeserializationException e) {
                LOG.warn("Failed to transform {}, omitting it", normalizedPath, e);
            }
        }
        return hashSet;
    }

    @SuppressWarnings("unchecked")
    protected java.util.Optional<DataObject> toBindingData(final InstanceIdentifier<?> path,
            final NormalizedNode<?, ?> data) {
        if (path.isWildcarded()) {
            return java.util.Optional.empty();
        }
        return (java.util.Optional<DataObject>) getCodec().getCodecRegistry().deserializeFunction(path)
                .apply(java.util.Optional.of(data));
    }
}
