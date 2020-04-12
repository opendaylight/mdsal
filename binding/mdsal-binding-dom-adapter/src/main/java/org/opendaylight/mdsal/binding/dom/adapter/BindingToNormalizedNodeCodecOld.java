/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.binding.runtime.api.ClassLoadingStrategy;
import org.opendaylight.binding.runtime.api.DefaultBindingRuntimeContext;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTree;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.impl.codec.DeserializationException;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextListener;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A combinations of {@link BindingCodecTreeFactory} and {@link BindingNormalizedNodeSerializer}, with internal
 * caching of instance identifiers.
 *
 * <p>
 * NOTE: this class is non-final to allow controller adapter migration without duplicated code.
 */
@Singleton
public class BindingToNormalizedNodeCodecOld implements EffectiveModelContextListener,
        AutoCloseable {

    private static final long WAIT_DURATION_SEC = 5;
    private static final Logger LOG = LoggerFactory.getLogger(BindingToNormalizedNodeCodecOld.class);

    private final BindingNormalizedNodeCodecRegistry codecRegistry;
    private final ClassLoadingStrategy classLoadingStrategy;
    private final BindingRuntimeGenerator generator;
    private final FutureSchema futureSchema;

    private ListenerRegistration<?> listenerRegistration;

    @Inject
    public BindingToNormalizedNodeCodecOld(final BindingRuntimeGenerator generator,
            final ClassLoadingStrategy classLoadingStrategy, final BindingNormalizedNodeCodecRegistry codecRegistry) {
        this(generator, classLoadingStrategy, codecRegistry, false);
    }

    @Beta
    public BindingToNormalizedNodeCodecOld(final BindingRuntimeContext runtimeContext) {
        generator = (final SchemaContext context) -> {
            throw new UnsupportedOperationException("Static context assigned");
        };
        classLoadingStrategy = runtimeContext.getStrategy();
        codecRegistry = new BindingNormalizedNodeCodecRegistry(runtimeContext);
        // TODO: this should have a specialized constructor or not be needed
        futureSchema = FutureSchema.create(0, TimeUnit.SECONDS, false);
        futureSchema.onRuntimeContextUpdated(runtimeContext);
    }

    public BindingToNormalizedNodeCodecOld(final BindingRuntimeGenerator generator,
            final ClassLoadingStrategy classLoadingStrategy, final BindingNormalizedNodeCodecRegistry codecRegistry,
            final boolean waitForSchema) {
        this.generator = requireNonNull(generator, "generator");
        this.classLoadingStrategy = requireNonNull(classLoadingStrategy, "classLoadingStrategy");
        this.codecRegistry = requireNonNull(codecRegistry, "codecRegistry");
        this.futureSchema = FutureSchema.create(WAIT_DURATION_SEC, TimeUnit.SECONDS, waitForSchema);
    }

    public static BindingToNormalizedNodeCodecOld newInstance(final BindingRuntimeGenerator generator,
            final ClassLoadingStrategy classLoadingStrategy, final DOMSchemaService schemaService) {
        final BindingNormalizedNodeCodecRegistry codecRegistry = new BindingNormalizedNodeCodecRegistry();
        BindingToNormalizedNodeCodecOld instance = new BindingToNormalizedNodeCodecOld(generator, classLoadingStrategy,
            codecRegistry, true);
        instance.listenerRegistration = schemaService.registerSchemaContextListener(instance);
        return instance;
    }

    /**
     * Returns a Binding-Aware instance identifier from normalized
     * instance-identifier if it is possible to create representation.
     *
     * <p>
     * Returns Optional.empty for cases where target is mixin node except
     * augmentation.
     */
    public final Optional<InstanceIdentifier<? extends DataObject>> toBinding(final YangInstanceIdentifier normalized)
                    throws DeserializationException {
        try {
            return Optional.ofNullable(codecRegistry.fromYangInstanceIdentifier(normalized));
        } catch (final IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public void onModelContextUpdated(final EffectiveModelContext newModelContext) {
        final BindingRuntimeContext runtimeContext = DefaultBindingRuntimeContext.create(
            generator.generateTypeMapping(newModelContext), classLoadingStrategy);
        codecRegistry.onBindingRuntimeContextUpdated(runtimeContext);
        futureSchema.onRuntimeContextUpdated(runtimeContext);
    }

    public final BindingNormalizedNodeCodecRegistry getCodecRegistry() {
        return codecRegistry;
    }

    @Override
    @PreDestroy
    public void close() {
        if (listenerRegistration != null) {
            listenerRegistration.close();
        }
    }

    protected @NonNull Entry<InstanceIdentifier<?>, BindingDataObjectCodecTreeNode<?>> getSubtreeCodec(
            final YangInstanceIdentifier domIdentifier) {

        final BindingCodecTree currentCodecTree = codecRegistry.getCodecContext();
        final InstanceIdentifier<?> bindingPath = codecRegistry.fromYangInstanceIdentifier(domIdentifier);
        checkArgument(bindingPath != null);
        /**
         * If we are able to deserialize YANG instance identifier, getSubtreeCodec must
         * return non-null value.
         */
        final BindingDataObjectCodecTreeNode<?> codecContext = currentCodecTree.getSubtreeCodec(bindingPath);
        return new SimpleEntry<>(bindingPath, codecContext);
    }

    final BindingRuntimeContext runtimeContext() {
        return futureSchema.runtimeContext();
    }
}
