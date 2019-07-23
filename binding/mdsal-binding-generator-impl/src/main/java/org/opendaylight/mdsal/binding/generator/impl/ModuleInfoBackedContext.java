/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.checkerframework.checker.lock.qual.Holding;
import org.eclipse.jdt.annotation.NonNull;
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
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaContextResolver;
import org.opendaylight.yangtools.yang.parser.repo.YangTextSchemaSourceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModuleInfoBackedContext extends GeneratedClassLoadingStrategy
        implements ModuleInfoRegistry, SchemaContextProvider, SchemaSourceProvider<YangTextSchemaSource> {
    private static final class RegisteredModuleInfo {
        private final YangTextSchemaSourceRegistration reg;
        private final YangModuleInfo info;
        private final ClassLoader loader;

        // This flag can only be moved from true to false with the lock on the context being held, hence no further
        // action is needed
        private boolean implicit;

        RegisteredModuleInfo(final YangModuleInfo info, final YangTextSchemaSourceRegistration reg,
                final ClassLoader loader, final boolean implicit) {
            this.info = requireNonNull(info);
            this.reg = requireNonNull(reg);
            this.loader = requireNonNull(loader);
            this.implicit = implicit;
        }
    }

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

    @GuardedBy("this")
    private final ListMultimap<String, RegisteredModuleInfo> packageToInfoReg =
            MultimapBuilder.hashKeys().arrayListValues().build();
    @GuardedBy("this")
    private final ListMultimap<SourceIdentifier, RegisteredModuleInfo> sourceToInfoReg =
            MultimapBuilder.hashKeys().arrayListValues().build();

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
        checkState(contextOptional.isPresent(), "Unable to recreate SchemaContext, error while parsing");
        return contextOptional.get();
    }

    @Override
    public Class<?> loadClass(final String fullyQualifiedName) throws ClassNotFoundException {
        // This performs an explicit check for binding classes
        final String modulePackageName = BindingReflections.getModelRootPackageName(fullyQualifiedName);

        synchronized (this) {
            // Try to find a loaded class loader
            // FIXME: two-step process, try explicit registrations first
            for (RegisteredModuleInfo reg : packageToInfoReg.get(modulePackageName)) {
                return ClassLoaderUtils.loadClass(reg.loader, fullyQualifiedName);
            }

            // We have not found a matching registration, consult the backing strategy
            if (backingLoadingStrategy == null) {
                throw new ClassNotFoundException(fullyQualifiedName);
            }

            final Class<?> cls = backingLoadingStrategy.loadClass(fullyQualifiedName);
            final YangModuleInfo moduleInfo;
            try {
                moduleInfo = BindingReflections.getModuleInfo(cls);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to resolve module information for class " + cls, e);
            }

            registerImplicitModuleInfo(requireNonNull(moduleInfo));
            return cls;
        }
    }

    @Override
    public synchronized ObjectRegistration<YangModuleInfo> registerModuleInfo(final YangModuleInfo yangModuleInfo) {
        return register(requireNonNull(yangModuleInfo));
    }

    @Override
    public ListenableFuture<? extends YangTextSchemaSource> getSource(final SourceIdentifier sourceIdentifier) {
        return ctxResolver.getSource(sourceIdentifier);
    }

    public synchronized void addModuleInfos(final Iterable<? extends YangModuleInfo> moduleInfos) {
        for (YangModuleInfo yangModuleInfo : moduleInfos) {
            register(requireNonNull(yangModuleInfo));
        }
    }

    // TODO finish schema parsing and expose as SchemaService
    // Unite with current SchemaService

    public Optional<SchemaContext> tryToCreateSchemaContext() {
        return ctxResolver.getSchemaContext();
    }

    @Holding("this")
    private ObjectRegistration<YangModuleInfo> register(final @NonNull YangModuleInfo moduleInfo) {
        final Builder<RegisteredModuleInfo> regBuilder = ImmutableList.builder();
        for (YangModuleInfo info : flatDependencies(moduleInfo)) {
            regBuilder.add(registerExplicitModuleInfo(info));
        }
        final ImmutableList<RegisteredModuleInfo> regInfos = regBuilder.build();

        return new AbstractObjectRegistration<YangModuleInfo>(moduleInfo) {
            @Override
            protected void removeRegistration() {
                unregister(regInfos);
            }
        };
    }

    /*
     * Perform implicit registration of a YangModuleInfo and any of its dependencies. If there is a registration for
     * a particular source, we do not create a duplicate registration.
     */
    @Holding("this")
    private void registerImplicitModuleInfo(final @NonNull YangModuleInfo moduleInfo) {
        for (YangModuleInfo info : flatDependencies(moduleInfo)) {
            final Class<?> infoClass = info.getClass();
            final SourceIdentifier sourceId = sourceIdentifierFrom(info);
            if (sourceToInfoReg.containsKey(sourceId)) {
                LOG.debug("Skipping implicit registration of {} as source {} is already registered", info, sourceId);
                continue;
            }

            final YangTextSchemaSourceRegistration reg;
            try {
                reg = ctxResolver.registerSource(toYangTextSource(sourceId, info));
            } catch (YangSyntaxErrorException | SchemaSourceException | IOException e) {
                LOG.warn("Failed to register info {}, ignoring it", e);
                continue;
            }

            final RegisteredModuleInfo regInfo = new RegisteredModuleInfo(info, reg, infoClass.getClassLoader(), true);
            sourceToInfoReg.put(sourceId, regInfo);
            packageToInfoReg.put(BindingReflections.getModelRootPackageName(infoClass.getPackage()), regInfo);
        }
    }

    /*
     * Perform explicit registration of a YangModuleInfo. These are registered even if there is a pre-existing
     * registration for a particular source:
     * - if the pre-existing registration is implicit, it is tied to the explicit registration and cleaned up when
     *   that goes away
     * - if the pre-existing registration is explicit, the source ends up being registered twice and each instance
     *   is unregistered with the registration which caused it to be populated
     */
    @Holding("this")
    private RegisteredModuleInfo registerExplicitModuleInfo(final @NonNull YangModuleInfo info) {
        final SourceIdentifier sourceId = sourceIdentifierFrom(info);

        for (RegisteredModuleInfo reg : sourceToInfoReg.get(sourceId)) {
            if (reg.implicit) {
                // Promote from implicit to explicit registration and return it
                reg.implicit = false;
                return reg;
            }

            // We found an explicit registration, there can be no implicit registration present
            break;
        }

        // Create an explicit registration
        final YangTextSchemaSourceRegistration reg;
        try {
            reg = ctxResolver.registerSource(toYangTextSource(sourceId, info));
        } catch (YangSyntaxErrorException | SchemaSourceException | IOException e) {
            throw new IllegalStateException("Failed to register info " + info, e);
        }

        final Class<?> infoClass = info.getClass();
        final RegisteredModuleInfo regInfo = new RegisteredModuleInfo(info, reg, infoClass.getClassLoader(), false);
        sourceToInfoReg.put(sourceId, regInfo);
        packageToInfoReg.put(BindingReflections.getModelRootPackageName(infoClass.getPackage()), regInfo);
        return regInfo;
    }

    synchronized void unregister(final ImmutableList<RegisteredModuleInfo> regInfos) {
        for (RegisteredModuleInfo regInfo : regInfos) {
            final SourceIdentifier sourceId = sourceIdentifierFrom(regInfo.info);
            if (!sourceToInfoReg.remove(sourceId, regInfo)) {
                LOG.warn("Failed to find {} registered under {}", regInfo, sourceId);
            }

            final String packageName = BindingReflections.getModelRootPackageName(regInfo.info.getClass().getPackage());
            if (!packageToInfoReg.remove(packageName, regInfo)) {
                LOG.warn("Failed to find {} registered under {}", regInfo, packageName);
            }

            regInfo.reg.close();
        }
    }

    private static @NonNull YangTextSchemaSource toYangTextSource(final SourceIdentifier identifier,
            final YangModuleInfo moduleInfo) {
        return YangTextSchemaSource.delegateForByteSource(identifier, moduleInfo.getYangTextByteSource());
    }

    private static SourceIdentifier sourceIdentifierFrom(final YangModuleInfo moduleInfo) {
        final QName name = moduleInfo.getName();
        return RevisionSourceIdentifier.create(name.getLocalName(), name.getRevision());
    }

    private static List<YangModuleInfo> flatDependencies(final YangModuleInfo moduleInfo) {
        // Flatten the modules being registered, with the triggering module being first...
        final Set<YangModuleInfo> requiredInfos = new LinkedHashSet<>();
        flatDependencies(requiredInfos, moduleInfo);

        // ... now reverse the order in an effort to register dependencies first (triggering module last)
        final List<YangModuleInfo> intendedOrder = new ArrayList<>(requiredInfos);
        Collections.reverse(intendedOrder);

        return intendedOrder;
    }

    private static void flatDependencies(final Set<YangModuleInfo> set, final YangModuleInfo moduleInfo) {
        if (set.add(moduleInfo)) {
            for (YangModuleInfo dep : moduleInfo.getImportedModules()) {
                flatDependencies(set, dep);
            }
        }
    }
}
