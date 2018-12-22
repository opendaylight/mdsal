/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.util;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.generator.api.ClassLoaderResolver;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.binding.spec.util.YangModuleInfoUtils;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ClassLoaderResolver} backed by classloaders mapped through {@link YangModuleInfo}. Usable in multi-classloader
 * and single-classloader environments.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public final class ModuleInfoClassLoaderResolver implements ClassLoaderResolver {
    private static final Logger LOG = LoggerFactory.getLogger(ModuleInfoClassLoaderResolver.class);

    private final ImmutableMap<String, ClassLoader> packageToClassLoader;
    private final ImmutableSet<ClassLoader> classLoaders;

    private ModuleInfoClassLoaderResolver(final Map<String, ClassLoader> packageToClassLoader) {
        this.packageToClassLoader = ImmutableMap.copyOf(packageToClassLoader);
        this.classLoaders = ImmutableSet.copyOf(packageToClassLoader.values());
    }

    public static ModuleInfoClassLoaderResolver of(final YangModuleInfo... infos) {
        return of(Arrays.asList(infos));
    }

    public static ModuleInfoClassLoaderResolver of(final Collection<YangModuleInfo> infos) {
        return of(YangModuleInfoUtils.uniqueIndexPackages(YangModuleInfoUtils.uniqueModuleInfos(infos)));
    }

    public static ModuleInfoClassLoaderResolver of(final BiMap<String, YangModuleInfo> infos) {
        return new ModuleInfoClassLoaderResolver(Maps.transformValues(infos,
            info -> verifyNotNull(info.getClass().getClassLoader(), "%s does not have a class loader", info)));
    }

    @Override
    public Optional<ClassLoader> findClassLoader(final String fqcn) {
        final String modulePackageName = BindingReflections.getModelRootPackageName(fqcn);
        return Optional.ofNullable(packageToClassLoader.get(modulePackageName));
    }

    @Override
    public Set<ClassLoader> getClassLoaders() {
        return classLoaders;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("mapping", packageToClassLoader).toString();
    }
}
