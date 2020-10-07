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
import java.util.Set;
import org.junit.Before;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
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

    @Before
    public final void setup() throws Exception {
        setupWithRuntimeContext(getRuntimeContext());
    }

    protected Set<YangModuleInfo> getModuleInfos() throws Exception {
        return BindingReflections.cacheModuleInfos(Thread.currentThread().getContextClassLoader());
    }

    protected BindingRuntimeContext getRuntimeContext() throws Exception {
        return RUNTIME_CONTEXT_CACHE.getUnchecked(getModuleInfos());
    }

    protected EffectiveModelContext getSchemaContext() throws Exception {
        return getRuntimeContext().getEffectiveModelContext();
    }

    protected void setupWithRuntimeContext(final BindingRuntimeContext runtimeContext) {
        setupWithSchema(runtimeContext.getEffectiveModelContext());
    }

    /**
     * Setups test with EffectiveModelContext.
     *
     * @param context schema context
     */
    protected void setupWithSchema(final EffectiveModelContext context) {
        // No-op
    }
}
