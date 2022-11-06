/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.query;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.binding.api.query.DescendantQueryBuilder;
import org.opendaylight.mdsal.binding.api.query.QueryFactory;
import org.opendaylight.mdsal.binding.api.query.QueryStructureException;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingSchemaMapping;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@Component
@MetaInfServices
@Singleton
public final class DefaultQueryFactory implements QueryFactory {
    private record MethodId(@NonNull DataNodeContainer parent, @NonNull String methodName) {
        MethodId {
            requireNonNull(parent);
            requireNonNull(methodName);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(DefaultQueryFactory.class);

    private final LoadingCache<MethodId, NodeIdentifier> knownMethods = CacheBuilder.newBuilder().build(
        new CacheLoader<MethodId, NodeIdentifier>() {
            @Override
            public NodeIdentifier load(final MethodId key) {
                final DataNodeContainer parent = key.parent;
                final String methodName = key.methodName;

                for (DataSchemaNode child : parent.getChildNodes()) {
                    if (methodName.equals(BindingSchemaMapping.getGetterMethodName(child))) {
                        return NodeIdentifier.create(child.getQName());
                    }
                }
                throw new QueryStructureException("Failed to find schema matching " + methodName + " in " + parent);
            }
        });
    private final @NonNull BindingCodecTree codec;

    public DefaultQueryFactory() {
        this(ServiceLoader.load(BindingCodecTree.class).findFirst().orElseThrow());
    }

    @Inject
    @Activate
    public DefaultQueryFactory(@Reference final BindingCodecTree codec) {
        this.codec = requireNonNull(codec);
        LOG.debug("Binding Query activated");
    }

    @Deactivate
    @SuppressWarnings("static-method")
    void deactivate() {
        LOG.debug("Binding Query deactivated");
    }

    @Override
    public <T extends DataObject> DescendantQueryBuilder<T> querySubtree(final InstanceIdentifier<T> rootPath) {
        return new DefaultDescendantQueryBuilder<>(this, rootPath);
    }

    @NonNull BindingCodecTree codec() {
        return codec;
    }

    @NonNull NodeIdentifier findChild(final DataNodeContainer parent, final String methodName) {
        try {
            return knownMethods.get(new MethodId(parent, methodName));
        } catch (ExecutionException e) {
            LOG.debug("Failed to find method for {}", methodName, e);
            final Throwable cause = e.getCause();
            Throwables.throwIfUnchecked(cause);
            throw new IllegalStateException("Failed to load cache", e);
        }
    }
}
