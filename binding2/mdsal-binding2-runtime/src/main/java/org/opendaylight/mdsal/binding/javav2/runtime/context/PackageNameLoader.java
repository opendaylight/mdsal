/*
 * Copyright (c) 2018 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.runtime.context;

import static org.opendaylight.mdsal.binding.javav2.generator.util.BindingGeneratorUtil.packageNameWithNamespacePrefix;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.mdsal.binding.javav2.generator.util.JavaIdentifierNormalizer;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.BindingNamespaceType;
import org.opendaylight.mdsal.binding.javav2.util.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.Module;

/**
 * This class holds caches of (namespace) package names of modules.
 */
@Beta
@NotThreadSafe
public final class PackageNameLoader {
    private static final LoadingCache<Module, String> ROOT_PACKAGE_NAME = CacheBuilder.newBuilder()
        .weakKeys().build(new CacheLoader<Module, String>() {

            @Override
            public String load(@Nonnull final Module key) {
                return JavaIdentifierNormalizer.normalizeFullPackageName(
                    BindingMapping.getRootPackageName(key));
            }
        });

    private static final LoadingCache<String, Map<BindingNamespaceType, String>> NS_PACKAGE_NAMES =
        CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<String, Map<BindingNamespaceType, String>>() {

            @Override
            public Map<BindingNamespaceType, String> load(@Nonnull final String key) {
                final Map<BindingNamespaceType, String> namespaces = Maps.newHashMap();
                namespaces.put(BindingNamespaceType.Key, packageNameWithNamespacePrefix(key,
                    BindingNamespaceType.Key));
                namespaces.put(BindingNamespaceType.Identity, packageNameWithNamespacePrefix(key,
                    BindingNamespaceType.Identity));
                namespaces.put(BindingNamespaceType.Grouping, packageNameWithNamespacePrefix(key,
                    BindingNamespaceType.Grouping));
                namespaces.put(BindingNamespaceType.Typedef, packageNameWithNamespacePrefix(key,
                    BindingNamespaceType.Typedef));
                namespaces.put(BindingNamespaceType.Data, packageNameWithNamespacePrefix(key,
                    BindingNamespaceType.Data));
                namespaces.put(BindingNamespaceType.Notification, packageNameWithNamespacePrefix(key,
                    BindingNamespaceType.Notification));
                namespaces.put(BindingNamespaceType.Operation, packageNameWithNamespacePrefix(key,
                    BindingNamespaceType.Notification));
                return namespaces;
            }
        });

    private PackageNameLoader() {
        //Not to be initialized.
    }

    public static String normalizedRootPackageName(final Module module) {
        return ROOT_PACKAGE_NAME.getUnchecked(module);
    }

    public static String normalizedNSPackageName(final Module module, final BindingNamespaceType namespaceType) {
        return NS_PACKAGE_NAMES.getUnchecked(normalizedRootPackageName(module)).get(namespaceType);
    }
}
