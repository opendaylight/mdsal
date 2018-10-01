/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.javav2.api.RpcActionProviderService;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation.BindingDOMOperationProviderServiceAdapter.AbstractImplAdapter.ActionAdapter;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.impl.operation.BindingDOMOperationProviderServiceAdapter.AbstractImplAdapter.RpcAdapter;
import org.opendaylight.mdsal.binding.javav2.dom.adapter.registration.BindingDOMOperationAdapterRegistration;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.dom.codec.serialized.LazySerializedContainerNode;
import org.opendaylight.mdsal.binding.javav2.runtime.reflection.BindingReflections;
import org.opendaylight.mdsal.binding.javav2.spec.base.Action;
import org.opendaylight.mdsal.binding.javav2.spec.base.Input;
import org.opendaylight.mdsal.binding.javav2.spec.base.InstanceIdentifier;
import org.opendaylight.mdsal.binding.javav2.spec.base.Operation;
import org.opendaylight.mdsal.binding.javav2.spec.base.Output;
import org.opendaylight.mdsal.binding.javav2.spec.base.Rpc;
import org.opendaylight.mdsal.binding.javav2.spec.base.RpcCallback;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMActionImplementation;
import org.opendaylight.mdsal.dom.api.DOMActionInstance;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMActionResult;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationRegistration;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Operation service provider adapter.
 */
@Beta
public class BindingDOMOperationProviderServiceAdapter implements RpcActionProviderService {

    private static final Set<YangInstanceIdentifier> GLOBAL = ImmutableSet.of(YangInstanceIdentifier.builder().build());
    private final BindingToNormalizedNodeCodec codec;
    private final DOMRpcProviderService domRpcRegistry;
    private final DOMActionProviderService domActionRegistry;

    public BindingDOMOperationProviderServiceAdapter(final DOMRpcProviderService domRpcRegistry,
            final DOMActionProviderService domActionRegistry, final BindingToNormalizedNodeCodec codec) {
        this.codec = codec;
        this.domRpcRegistry = domRpcRegistry;
        this.domActionRegistry = domActionRegistry;
    }

    @Override
    public <S extends Rpc<?, ?>, T extends S> ObjectRegistration<T> registerRpcImplementation(final Class<S> type,
            final T implementation) {
        return register(type, implementation, GLOBAL);
    }

    @Override
    public <S extends Rpc<?, ?>, T extends S> ObjectRegistration<T> registerRpcImplementation(final Class<S> type,
            final T implementation, final Set<InstanceIdentifier<?>> paths) {
        return register(type, implementation, toYangInstanceIdentifiers(paths));
    }

    private <S extends Rpc<?, ?>, T extends S> ObjectRegistration<T> register(final Class<S> type,
            final T implementation, final Collection<YangInstanceIdentifier> rpcContextPaths) {
        final SchemaPath path = codec.getRpcPath(type);
        final Set<DOMRpcIdentifier> domRpcs = createDomRpcIdentifiers(path, rpcContextPaths);
        final DOMRpcImplementationRegistration<?> domReg = domRpcRegistry.registerRpcImplementation(
            new RpcAdapter(codec.getCodecRegistry(), type, implementation), domRpcs);
        return new BindingDOMOperationAdapterRegistration<>(implementation, domReg);
    }

    private static Set<DOMRpcIdentifier> createDomRpcIdentifiers(final SchemaPath rpc,
            final Collection<YangInstanceIdentifier> paths) {
        final Set<DOMRpcIdentifier> ret = new HashSet<>();
        for (final YangInstanceIdentifier path : paths) {
            ret.add(DOMRpcIdentifier.create(rpc, path));
        }
        return ret;
    }

    private Collection<YangInstanceIdentifier> toYangInstanceIdentifiers(final Set<InstanceIdentifier<?>> identifiers) {
        final Collection<YangInstanceIdentifier> ret = new ArrayList<>(identifiers.size());
        for (final InstanceIdentifier<?> binding : identifiers) {
            ret.add(codec.toYangInstanceIdentifierCached(binding));
        }
        return ret;
    }

    @Override
    public <S extends Action<? extends TreeNode, ?, ?, ?>, T extends S, P extends TreeNode> ObjectRegistration<T>
            registerActionImplementation(final Class<S> type, final T implementation,
                final LogicalDatastoreType datastore, final Set<DataTreeIdentifier<P>> validNodes) {
        final SchemaPath path = codec.getActionPath(type);
        final ObjectRegistration<ActionAdapter> domReg = domActionRegistry.registerActionImplementation(
            new ActionAdapter(codec.getCodecRegistry(), type, implementation),
            DOMActionInstance.of(path, codec.toDOMDataTreeIdentifiers(validNodes)));
        return new BindingDOMOperationAdapterRegistration<>(implementation, domReg);
    }

    public abstract static class AbstractImplAdapter<D> {
        protected final BindingNormalizedNodeCodecRegistry codec;
        protected final D delegate;
        private final QName inputQname;

        AbstractImplAdapter(final BindingNormalizedNodeCodecRegistry codec, final Class<? extends Operation> clazz,
                   final D delegate) {
            this.codec = requireNonNull(codec);
            this.delegate = requireNonNull(delegate);
            inputQname = QName.create(BindingReflections.getQNameModule(clazz), "input").intern();
        }

        TreeNode deserialize(final SchemaPath path, final NormalizedNode<?, ?> input) {
            if (input instanceof LazySerializedContainerNode) {
                return ((LazySerializedContainerNode) input).bindingData();
            }
            final SchemaPath inputSchemaPath = path.createChild(inputQname);
            return codec.fromNormalizedNodeOperationData(inputSchemaPath, (ContainerNode) input);
        }

        public static final class RpcAdapter extends AbstractImplAdapter<Rpc> implements DOMRpcImplementation {

            RpcAdapter(final BindingNormalizedNodeCodecRegistry codec, final Class<? extends Operation> clazz,
                    final Rpc<?, ?> delegate) {
                super(codec, clazz, delegate);
            }

            @SuppressWarnings("unchecked")
            @Nonnull
            @Override
            public FluentFuture<DOMRpcResult> invokeRpc(@Nonnull final DOMRpcIdentifier rpc,
                                                        @Nullable final NormalizedNode<?, ?> input) {
                final TreeNode bindingInput = input != null ? deserialize(rpc.getType(), input) : null;
                final SettableFuture<RpcResult<?>> bindingResult = SettableFuture.create();
                CompletableFuture.runAsync(() -> delegate.invoke((Input<?>) bindingInput,
                    new RpcCallback<Output<?>>() {
                        @Override
                        public void onSuccess(final Output<?> output) {
                            bindingResult.set(RpcResultBuilder.success(output).build());
                        }

                        @Override
                        public void onFailure(final Throwable error) {
                            bindingResult.set(RpcResultBuilder.failed().withError(ErrorType.APPLICATION,
                                error.getMessage(), error).build());
                        }
                    })
                );
                return LazyDOMRpcResultFuture.create(codec,bindingResult);
            }
        }

        public static final class ActionAdapter extends AbstractImplAdapter<Action>
                implements DOMActionImplementation {

            ActionAdapter(final BindingNormalizedNodeCodecRegistry codec, final Class<? extends Operation> clazz,
                    final Action<?, ?, ?, ?> delegate) {
                super(codec, clazz, delegate);
            }

            @Override
            @SuppressWarnings("unchecked")
            @Nonnull
            public FluentFuture<? extends DOMActionResult> invokeAction(final SchemaPath type,
                    final DOMDataTreeIdentifier path, final ContainerNode input) {
                final TreeNode bindingInput = input != null ? deserialize(type, input) : null;
                final SettableFuture<RpcResult<?>> bindingResult = SettableFuture.create();
                CompletableFuture.runAsync(() -> delegate.invoke((Input<?>) bindingInput,
                    codec.fromYangInstanceIdentifier(path.getRootIdentifier()),
                    new RpcCallback<Output<?>>() {
                        @Override
                        public void onSuccess(final Output<?> output) {
                            bindingResult.set(RpcResultBuilder.success(output).build());
                        }

                        @Override
                        public void onFailure(final Throwable error) {
                            bindingResult.set(RpcResultBuilder.failed().withError(ErrorType.APPLICATION,
                                error.getMessage(), error).build());
                        }
                    })
                );
                return LazyDOMActionResultFuture.create(codec, bindingResult);
            }
        }
    }
}
