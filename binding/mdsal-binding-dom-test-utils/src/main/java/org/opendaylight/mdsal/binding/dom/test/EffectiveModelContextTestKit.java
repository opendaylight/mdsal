/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.test;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * A simple testkit which maintains a lazily-populated EffectiveModelContext based either on an explicit list of
 * {@link YangModuleInfo} instances or based on all instances discovered through Thread Context Class Loader at
 * instantiation time.
 */
@Beta
public class EffectiveModelContextTestKit {
    private static final LoadingCache<Set<YangModuleInfo>, EffectiveModelContext> MODULE_CONTEXT_CACHE =
            CacheBuilder.newBuilder().weakValues().build(new CacheLoader<Set<YangModuleInfo>, EffectiveModelContext>() {
                @Override
                public EffectiveModelContext load(final Set<YangModuleInfo> key) {
                    return ModuleInfoBackedContext.cacheContext(
                        GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(), ImmutableSet.copyOf(key))
                            .tryToCreateModelContext().get();
                }
            });

    private final ImmutableSet<YangModuleInfo> moduleInfos;

    private volatile EffectiveModelContext modelContext;

    public EffectiveModelContextTestKit() {
        this(BindingReflections.cacheModuleInfos(Thread.currentThread().getContextClassLoader()));
    }

    public EffectiveModelContextTestKit(final Set<YangModuleInfo> moduleInfos) {
        this.moduleInfos = ImmutableSet.copyOf(moduleInfos);
    }

    public final @NonNull EffectiveModelContext effectiveModelContext() {
        EffectiveModelContext local = modelContext;
        if (local == null) {
            synchronized (this) {
                local = modelContext;
                if (local == null) {
                    modelContext = local = MODULE_CONTEXT_CACHE.getUnchecked(moduleInfos);
                }
            }
        }
        return local;
    }
}
