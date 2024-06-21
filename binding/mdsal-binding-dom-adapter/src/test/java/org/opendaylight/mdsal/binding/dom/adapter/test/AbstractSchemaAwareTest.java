/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.test;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import java.util.ServiceLoader;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Before;
import org.opendaylight.yangtools.binding.meta.YangModelBindingProvider;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.yangtools.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

public abstract class AbstractSchemaAwareTest {
    private static final LoadingCache<Set<YangModuleInfo>, BindingRuntimeContext> RUNTIME_CONTEXT_CACHE =
            CacheBuilder.newBuilder().weakValues().build(
                new CacheLoader<Set<YangModuleInfo>, BindingRuntimeContext>() {
                    @Override
                    public BindingRuntimeContext load(final Set<YangModuleInfo> key) {
                        return BindingRuntimeHelpers.createRuntimeContext(key);
                    }
                });
    private static final LoadingCache<ClassLoader, ImmutableSet<YangModuleInfo>> MODULE_INFO_CACHE =
        CacheBuilder.newBuilder().weakKeys().weakValues().build(
            new CacheLoader<ClassLoader, ImmutableSet<YangModuleInfo>>() {
                @Override
                public ImmutableSet<YangModuleInfo> load(final ClassLoader key) {
                    return BindingRuntimeHelpers.loadModuleInfos(key);
                }
            });


    @Before
    public final void setup() throws Exception {
        setupWithRuntimeContext(getRuntimeContext());
    }

    protected Set<YangModuleInfo> getModuleInfos() throws Exception {
        return cacheModuleInfos(Thread.currentThread().getContextClassLoader());
    }

    protected BindingRuntimeContext getRuntimeContext() throws Exception {
        return RUNTIME_CONTEXT_CACHE.getUnchecked(getModuleInfos());
    }

    protected EffectiveModelContext modelContext() throws Exception {
        return getRuntimeContext().modelContext();
    }

    protected void setupWithRuntimeContext(final BindingRuntimeContext runtimeContext) {
        setupWithSchema(runtimeContext.modelContext());
    }

    /**
     * Setups test with EffectiveModelContext.
     *
     * @param context schema context
     */
    protected void setupWithSchema(final EffectiveModelContext context) {
        // No-op
    }

    /**
     * Loads {@link YangModuleInfo} instances available on supplied {@link ClassLoader}, assuming the set of available
     * information does not change. Subsequent accesses may return cached values.
     *
     * <p>
     * {@link YangModuleInfo} are discovered using {@link ServiceLoader} for {@link YangModelBindingProvider}.
     * {@link YangModelBindingProvider} are simple classes which holds only pointers to actual instance
     * {@link YangModuleInfo}.
     *
     * <p>
     * When {@link YangModuleInfo} is available, all dependencies are recursively collected into returning set by
     * collecting results of {@link YangModuleInfo#getImportedModules()}.
     *
     * @param loader Class loader for which {@link YangModuleInfo} should be retrieved.
     * @return Set of {@link YangModuleInfo} available for supplied classloader.
     */
    protected static final @NonNull ImmutableSet<YangModuleInfo> cacheModuleInfos(final ClassLoader loader) {
        return MODULE_INFO_CACHE.getUnchecked(loader);
    }
}
