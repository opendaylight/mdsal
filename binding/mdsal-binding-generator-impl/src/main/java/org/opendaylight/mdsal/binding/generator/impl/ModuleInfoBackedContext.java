/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.generator.api.ModuleInfoRegistry;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaContextResolver;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaSourceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModuleInfoBackedContext extends GeneratedClassLoadingStrategy
        implements ModuleInfoRegistry, SchemaContextProvider, SchemaSourceProvider<YangTextSchemaSource> {
    private static final Logger LOG = LoggerFactory.getLogger(ModuleInfoBackedContext.class);

    private static final LoadingCache<ClassLoadingStrategy,
        LoadingCache<ImmutableSet<YangModuleInfo>, ModuleInfoBackedContext>> CONTEXT_CACHES = CacheBuilder.newBuilder()
            .weakKeys().build(new CacheLoader<ClassLoadingStrategy,
                LoadingCache<ImmutableSet<YangModuleInfo>, ModuleInfoBackedContext>>() {
                    @Override
                    public LoadingCache<ImmutableSet<YangModuleInfo>, ModuleInfoBackedContext> load(
                            final ClassLoadingStrategy strategy) {
                        return CacheBuilder.newBuilder().weakValues().build(
                            new CacheLoader<Set<YangModuleInfo>, ModuleInfoBackedContext>() {
                                @Override
                                public ModuleInfoBackedContext load(final Set<YangModuleInfo> key) {
                                    final ModuleInfoBackedContext context = ModuleInfoBackedContext.create(strategy);
                                    context.addModuleInfos(key);
                                    return context;
                                }
                            });
                    }
            });

    private final YangTextSchemaContextResolver ctxResolver = YangTextSchemaContextResolver.create("binding-context");
    private final ConcurrentMap<String, WeakReference<ClassLoader>> packageNameToClassLoader =
            new ConcurrentHashMap<>();
    private final ConcurrentMap<SourceIdentifier, YangTextSchemaSourceRegistration> sourceIdentifierToSchemaSource =
            new ConcurrentHashMap<>();
    private final ClassLoadingStrategy backingLoadingStrategy;

    private ModuleInfoBackedContext(final ClassLoadingStrategy loadingStrategy) {
        this.backingLoadingStrategy = loadingStrategy;
    }

    @Beta
    public static ModuleInfoBackedContext cacheContext(final ClassLoadingStrategy loadingStrategy,
            final ImmutableSet<YangModuleInfo> infos) {
        return CONTEXT_CACHES.getUnchecked(loadingStrategy).getUnchecked(infos);
    }

    public static ModuleInfoBackedContext create() {
        return new ModuleInfoBackedContext(getTCCLClassLoadingStrategy());
    }

    public static ModuleInfoBackedContext create(final ClassLoadingStrategy loadingStrategy) {
        return new ModuleInfoBackedContext(loadingStrategy);
    }

    @Override
    public SchemaContext getSchemaContext() {
        final Optional<SchemaContext> contextOptional = tryToCreateSchemaContext();
        if (contextOptional.isPresent()) {
            return contextOptional.get();
        }
        throw new IllegalStateException("Unable to recreate SchemaContext, error while parsing");
    }

    @Override
    public Class<?> loadClass(final String fullyQualifiedName) throws ClassNotFoundException {
        final String modulePackageName = BindingReflections.getModelRootPackageName(fullyQualifiedName);
        final WeakReference<ClassLoader> classLoaderRef = packageNameToClassLoader.get(modulePackageName);
        if (classLoaderRef != null) {
            final ClassLoader classLoader = classLoaderRef.get();
            if (classLoader != null) {
                return ClassLoaderUtils.loadClass(classLoader, fullyQualifiedName);
            }
        }

        if (backingLoadingStrategy == null) {
            throw new ClassNotFoundException(fullyQualifiedName);
        }

        final Class<?> cls = backingLoadingStrategy.loadClass(fullyQualifiedName);
        if (BindingReflections.isBindingClass(cls)) {
            resolveModuleInfo(cls);
        }

        return cls;
    }

    @Override
    public ObjectRegistration<YangModuleInfo> registerModuleInfo(final YangModuleInfo yangModuleInfo) {
        YangModuleInfoRegistration registration = new YangModuleInfoRegistration(yangModuleInfo, this);
        resolveModuleInfo(yangModuleInfo);
        return registration;
    }

    @Override
    public ListenableFuture<? extends YangTextSchemaSource> getSource(final SourceIdentifier sourceIdentifier) {
        return ctxResolver.getSource(sourceIdentifier);
    }

    public void addModuleInfos(final Iterable<? extends YangModuleInfo> moduleInfos) {
        for (YangModuleInfo yangModuleInfo : moduleInfos) {
            registerModuleInfo(yangModuleInfo);
        }
    }

    // TODO finish schema parsing and expose as SchemaService
    // Unite with current SchemaService
    // Implement remove ModuleInfo to update SchemaContext

    public Optional<SchemaContext> tryToCreateSchemaContext() {
        return ctxResolver.getSchemaContext();
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private boolean resolveModuleInfo(final Class<?> cls) {
        try {
            return resolveModuleInfo(BindingReflections.getModuleInfo(cls));
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Failed to resolve module information for class %s", cls), e);
        }
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    private boolean resolveModuleInfo(final YangModuleInfo moduleInfo) {
        final Class<?> infoClass = moduleInfo.getClass();
        final ClassLoader moduleClassLoader = infoClass.getClassLoader();
        final String modulePackageName = infoClass.getPackage().getName();
        final WeakReference<ClassLoader> reference = packageNameToClassLoader.get(modulePackageName);
        if (reference != null && moduleClassLoader == reference.get()) {
            return false;
        }

        try {
            final SourceIdentifier identifier = sourceIdentifierFrom(moduleInfo);
            packageNameToClassLoader.putIfAbsent(modulePackageName, new WeakReference<>(moduleClassLoader));
            YangTextSchemaSourceRegistration reg = sourceIdentifierToSchemaSource.putIfAbsent(identifier,
                    ctxResolver.registerSource(toYangTextSource(identifier, moduleInfo)));
            // If the classloader for an identifier is updated then just remove old registration, so that
            // GC can take effect for the old classloader&classes.
            // here it just keeps the latest module info, further to introduce multiple maps to maintain multiple
            // module infos for the same identifier.
            if (reg != null) {
                reg.close();
            }

            for (YangModuleInfo importedInfo : moduleInfo.getImportedModules()) {
                resolveModuleInfo(importedInfo);
            }
        } catch (Exception e) {
            LOG.error("Not including {} in YANG sources because of error.", moduleInfo, e);
        }
        return true;
    }

    private void remove(final YangModuleInfoRegistration registration) {
        final YangModuleInfo moduleInfo = registration.getInstance();
        final Class<?> infoClass = moduleInfo.getClass();
        final String modulePackageName = infoClass.getPackage().getName();
        final WeakReference<ClassLoader> reference = packageNameToClassLoader.get(modulePackageName);
        if (reference != null && infoClass.getClassLoader() != reference.get()) {
            return;
        }

        final SourceIdentifier identifier = sourceIdentifierFrom(moduleInfo);
        requireNonNull(sourceIdentifierToSchemaSource.remove(identifier)).close();
        packageNameToClassLoader.remove(modulePackageName);
        // FIXME: Do not remove imported module info in case they may be depended by other modules.
    }

    private static YangTextSchemaSource toYangTextSource(final SourceIdentifier identifier,
            final YangModuleInfo moduleInfo) {
        return YangTextSchemaSource.delegateForByteSource(identifier, moduleInfo.getYangTextByteSource());
    }

    private static SourceIdentifier sourceIdentifierFrom(final YangModuleInfo moduleInfo) {
        final QName name = moduleInfo.getName();
        return RevisionSourceIdentifier.create(name.getLocalName(), name.getRevision());
    }

    private static class YangModuleInfoRegistration extends AbstractObjectRegistration<YangModuleInfo> {
        private final ModuleInfoBackedContext context;

        YangModuleInfoRegistration(final YangModuleInfo instance, final ModuleInfoBackedContext context) {
            super(instance);
            this.context = context;
        }

        @Override
        protected void removeRegistration() {
            context.remove(this);
        }
    }
}
