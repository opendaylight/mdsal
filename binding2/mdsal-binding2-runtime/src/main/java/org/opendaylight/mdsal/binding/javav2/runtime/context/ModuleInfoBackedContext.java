/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.runtime.context;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Optional;
import com.google.common.io.ByteSource;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.opendaylight.mdsal.binding.javav2.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.javav2.generator.api.ModuleInfoRegistry;
import org.opendaylight.mdsal.binding.javav2.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.YangModuleInfo;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaContextResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module info context.
 */
@Beta
public class ModuleInfoBackedContext extends GeneratedClassLoadingStrategy
        implements ModuleInfoRegistry, SchemaContextProvider, SchemaSourceProvider<YangTextSchemaSource> {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleInfoBackedContext.class);

    private final YangTextSchemaContextResolver ctxResolver = YangTextSchemaContextResolver.create("binding-context");
    private final ConcurrentMap<String, WeakReference<ClassLoader>> packageNameToClassLoader =
            new ConcurrentHashMap<>();
    private final ConcurrentMap<SourceIdentifier, YangModuleInfo> sourceIdentifierToModuleInfo =
            new ConcurrentHashMap<>();
    private final ClassLoadingStrategy backingLoadingStrategy;


    private ModuleInfoBackedContext(final ClassLoadingStrategy loadingStrategy) {
        this.backingLoadingStrategy = loadingStrategy;
    }

    /**
     * Create new module info context.
     *
     * @return new module info context
     */
    public static ModuleInfoBackedContext create() {
        return new ModuleInfoBackedContext(getTCCLClassLoadingStrategy());
    }

    /**
     * Create new module info context based on specific loading strategy.
     *
     * @param loadingStrategy
     *            - specific loading strategy
     * @return new module info cotext based on specific loading strategy
     */
    public static ModuleInfoBackedContext create(final ClassLoadingStrategy loadingStrategy) {
        return new ModuleInfoBackedContext(loadingStrategy);
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

    // TODO finish schema parsing and expose as SchemaService
    // Unite with current SchemaService
    // Implement remove ModuleInfo to update SchemaContext

    /**
     * Resolving of schema context.
     *
     * @return optional of schema context
     */
    public Optional<SchemaContext> tryToCreateSchemaContext() {
        return Optional.fromJavaUtil(ctxResolver.getSchemaContext());
    }

    private boolean resolveModuleInfo(final Class<?> cls) {
        try {
            return resolveModuleInfo(BindingReflections.getModuleInfo(cls));
        } catch (final Exception e) {
            throw new IllegalStateException(String.format("Failed to resolve module information for class %s", cls), e);
        }
    }

    private boolean resolveModuleInfo(final YangModuleInfo moduleInfo) {

        final SourceIdentifier identifier = sourceIdentifierFrom(moduleInfo);
        final YangModuleInfo previous = sourceIdentifierToModuleInfo.putIfAbsent(identifier, moduleInfo);
        final ClassLoader moduleClassLoader = moduleInfo.getClass().getClassLoader();
        try {
            if (previous == null) {
                final String modulePackageName = moduleInfo.getClass().getPackage().getName();
                packageNameToClassLoader.putIfAbsent(modulePackageName,
                        new WeakReference<>(moduleClassLoader));
                ctxResolver.registerSource(toYangTextSource(identifier, moduleInfo));
                for (final YangModuleInfo importedInfo : moduleInfo.getImportedModules()) {
                    resolveModuleInfo(importedInfo);
                }
            } else {
                return false;
            }
        } catch (final Exception e) {
            LOG.error("Not including {} in YANG sources because of error.", moduleInfo, e);
        }
        return true;
    }

    private static YangTextSchemaSource toYangTextSource(final SourceIdentifier identifier, final YangModuleInfo moduleInfo) {
        return new YangTextSchemaSource(identifier) {

            @Override
            public InputStream openStream() throws IOException {
                return moduleInfo.getModuleSourceStream();
            }

            @Override
            protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
                return toStringHelper;
            }
        };
    }

    private static SourceIdentifier sourceIdentifierFrom(final YangModuleInfo moduleInfo) {
        final String rev = moduleInfo.getRevision();
        return RevisionSourceIdentifier.create(moduleInfo.getName(), rev == null ? java.util.Optional.empty()
                : java.util.Optional.of(Revision.valueOf(moduleInfo.getRevision())));
    }

    /**
     * Add new module info into context.
     *
     * @param moduleInfos
     *            - new module info
     */
    public void addModuleInfos(final Iterable<? extends YangModuleInfo> moduleInfos) {
        for (final YangModuleInfo yangModuleInfo : moduleInfos) {
            registerModuleInfo(yangModuleInfo);
        }
    }

    @Override
    public ObjectRegistration<YangModuleInfo> registerModuleInfo(final YangModuleInfo yangModuleInfo) {
        final YangModuleInfoRegistration registration = new YangModuleInfoRegistration(yangModuleInfo, this);
        resolveModuleInfo(yangModuleInfo);
        return registration;
    }

    @Override public CheckedFuture<? extends YangTextSchemaSource, SchemaSourceException> getSource(
        final SourceIdentifier sourceIdentifier) {
        final YangModuleInfo yangModuleInfo = sourceIdentifierToModuleInfo.get(sourceIdentifier);

        if (yangModuleInfo == null) {
            LOG.debug("Unknown schema source requested: {}, available sources: {}", sourceIdentifier, sourceIdentifierToModuleInfo.keySet());
            return Futures
                .immediateFailedCheckedFuture(new SchemaSourceException("Unknown schema source: " + sourceIdentifier));
        }

        return Futures
            .immediateCheckedFuture(YangTextSchemaSource.delegateForByteSource(sourceIdentifier, new ByteSource() {
                @Override public InputStream openStream() throws IOException {
                        return yangModuleInfo.getModuleSourceStream();
                }
            }));
    }

    private static class YangModuleInfoRegistration extends AbstractObjectRegistration<YangModuleInfo> {

        private final ModuleInfoBackedContext context;

        public YangModuleInfoRegistration(final YangModuleInfo instance, final ModuleInfoBackedContext context) {
            super(instance);
            this.context = context;
        }

        @Override
        protected void removeRegistration() {
            context.remove(this);
        }

    }

    private void remove(final YangModuleInfoRegistration registration) {
        // FIXME implement
    }

    @Override
    public SchemaContext getSchemaContext() {
        final Optional<SchemaContext> contextOptional = tryToCreateSchemaContext();
        if (contextOptional.isPresent()) {
            return contextOptional.get();
        }
        throw new IllegalStateException("Unable to recreate SchemaContext, error while parsing");
    }
}
