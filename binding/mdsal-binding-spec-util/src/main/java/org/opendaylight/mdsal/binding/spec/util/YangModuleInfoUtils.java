/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spec.util;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Collection;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

/**
 * Collection of utilities for dealing with how {@link YangModuleInfo} relates to Binding classes.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public final class YangModuleInfoUtils {
    private YangModuleInfoUtils() {
        // Hidden on purpose
    }

    /**
     * Recursively search all specified {@link YangModuleInfo}s, collecting all their imports and return their unique
     * instances.
     *
     * @param infos Root {@link YangModuleInfo}s
     * @return All referenced {@link YangModuleInfo}s
     * @throws NullPointerException if {@code infos} or any of its elements is null
     */
    public static ImmutableSet<YangModuleInfo> uniqueModuleInfos(final Collection<YangModuleInfo> infos) {
        final Builder<YangModuleInfo> builder = ImmutableSet.builder();
        for (YangModuleInfo info : infos) {
            collectModuleInfos(info, builder);
        }

        return builder.build();
    }

    public static ImmutableMultimap<String, YangModuleInfo> indexPackages(final Set<YangModuleInfo> uniqueInfos) {
        final ImmutableMultimap.Builder<String, YangModuleInfo> builder = ImmutableMultimap.builder();
        for (YangModuleInfo info : uniqueInfos) {
            builder.put(info.getClass().getPackage().getName(), info);

        }
        return builder.build();
    }

    public static ImmutableBiMap<String, YangModuleInfo> uniqueIndexPackages(final Set<YangModuleInfo> uniqueInfos) {
        final ImmutableBiMap.Builder<String, YangModuleInfo> builder = ImmutableBiMap.builder();
        for (YangModuleInfo info : uniqueInfos) {
            builder.put(info.getClass().getPackage().getName(), info);
        }
        return builder.build();
    }

    private static void collectModuleInfos(final YangModuleInfo info, final Builder<YangModuleInfo> builder) {
        builder.add(info);
        for (YangModuleInfo importedInfo : info.getImportedModules()) {
            collectModuleInfos(importedInfo, builder);
        }
    }
}
