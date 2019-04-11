/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * {@link SchemaContextProvider} which internally uses (a cached version of the) {@link ModuleInfoBackedContext}.
 *
 * <p>This is useful for environments such as tests (which will frequently recreate
 * this class, and caching is thus important) or "simple" standalone (non-OSGi)
 * runtime environments with a fixed classpath and no dynamic (re)loading of
 * {@link YangModuleInfo}s.
 *
 * @author Michael Vorburger.ch
 */
@Beta
public class FixedModuleInfoSchemaContextProvider
        implements SchemaContextProvider, SchemaSourceProvider<YangTextSchemaSource> {

    private static final ModuleInfoBackedContext MODULE_CONTEXT = ModuleInfoBackedContext.create();

    private static final LoadingCache<ImmutableSet<YangModuleInfo>, SchemaContext> SCHEMA_CONTEXT_CACHE =
            CacheBuilder.newBuilder().weakValues().build(new CacheLoader<ImmutableSet<YangModuleInfo>, SchemaContext>() {
                @Override
                public SchemaContext load(final ImmutableSet<YangModuleInfo> key) {
                    MODULE_CONTEXT.addModuleInfos(key);
                    return MODULE_CONTEXT.tryToCreateSchemaContext().get();
                }
            });

    public ImmutableSet<YangModuleInfo> getModuleInfos() throws ExecutionException {
        return BindingReflections.cacheModuleInfos(Thread.currentThread().getContextClassLoader());
    }

    @Override
    public SchemaContext getSchemaContext() {
        final ImmutableSet<YangModuleInfo> infos;
        try {
            infos = getModuleInfos();
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to acquire YangModuleInfos", e);
        }

        return SCHEMA_CONTEXT_CACHE.getUnchecked(infos);
    }

    @Override
    public ListenableFuture<? extends YangTextSchemaSource> getSource(SourceIdentifier sourceIdentifier) {
        return MODULE_CONTEXT.getSource(sourceIdentifier);
    }
}
