/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.api.ActionProviderService;
import org.opendaylight.mdsal.binding.api.ActionSpec;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMActionImplementation;
import org.opendaylight.mdsal.dom.api.DOMActionInstance;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.mdsal.dom.spi.DefaultDOMRpcResult;
import org.opendaylight.yangtools.binding.Action;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public final class ActionProviderServiceAdapter extends AbstractBindingAdapter<DOMActionProviderService>
        implements ActionProviderService {
    private static final class Builder extends BindingDOMAdapterBuilder<ActionProviderService> {
        Builder(final AdapterContext adapterContext) {
            super(adapterContext);
        }

        @Override
        protected ActionProviderService createInstance(final ClassToInstanceMap<DOMService<?, ?>> delegates) {
            return new ActionProviderServiceAdapter(adapterContext(),
                delegates.getInstance(DOMActionProviderService.class));
        }

        @Override
        public Set<? extends Class<? extends DOMService<?, ?>>> getRequiredDelegates() {
            return ImmutableSet.of(DOMActionProviderService.class);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ActionProviderServiceAdapter.class);

    static final Factory<ActionProviderService> BUILDER_FACTORY = Builder::new;

    ActionProviderServiceAdapter(final AdapterContext adapterContext, final DOMActionProviderService delegate) {
        super(adapterContext, delegate);
    }

    @Override
    public <P extends DataObject, A extends Action<? extends InstanceIdentifier<P>, ?, ?>>
            Registration registerImplementation(final ActionSpec<A, P> spec, final A implementation,
                final LogicalDatastoreType datastore, final Set<? extends InstanceIdentifier<P>> validNodes) {
        final CurrentAdapterSerializer serializer = currentSerializer();
        final Absolute actionPath = serializer.getActionPath(spec);
        final Impl impl = new Impl(adapterContext(), actionPath, spec.type(), implementation);
        final DOMActionInstance instance = validNodes.isEmpty()
            // Register on the entire datastore
            ? DOMActionInstance.of(actionPath, DOMDataTreeIdentifier.of(datastore, YangInstanceIdentifier.of()))
                // Register on specific instances
                : DOMActionInstance.of(actionPath, validNodes.stream()
                    .map(node -> serializer.toDOMDataTreeIdentifier(DataTreeIdentifier.of(datastore, node)))
                    .collect(Collectors.toUnmodifiableSet()));


        return getDelegate().registerActionImplementation(impl, instance);
    }

    private static final class Impl implements DOMActionImplementation {
        private final Class<? extends Action<?, ?, ?>> actionInterface;
        private final AdapterContext adapterContext;
        @SuppressWarnings("rawtypes")
        private final Action implementation;
        private final NodeIdentifier outputName;

        Impl(final AdapterContext adapterContext, final Absolute actionPath,
                final Class<? extends Action<?, ?, ?>> actionInterface, final Action<?, ?, ?> implementation) {
            this.adapterContext = requireNonNull(adapterContext);
            outputName = NodeIdentifier.create(
                YangConstants.operationOutputQName(actionPath.lastNodeIdentifier().getModule()));
            this.actionInterface = requireNonNull(actionInterface);
            this.implementation = requireNonNull(implementation);
        }

        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public ListenableFuture<? extends DOMRpcResult> invokeAction(final Absolute type,
                final DOMDataTreeIdentifier path, final ContainerNode input) {
            final CurrentAdapterSerializer codec = adapterContext.currentSerializer();
            final var instance = codec.fromYangInstanceIdentifier(path.path());
            if (instance == null) {
                // Not representable: return an error
                LOG.debug("Path {} is not representable in binding, rejecting invocation", path);
                return Futures.immediateFuture(new DefaultDOMRpcResult(RpcResultBuilder.newError(
                    ErrorType.APPLICATION, ErrorTag.INVALID_VALUE, "Supplied path cannot be represented")));
            }
            if (instance.isWildcarded()) {
                // A wildcard path: return an error
                LOG.debug("Path {} maps to a wildcard {}, rejecting invocation", path, instance);
                return Futures.immediateFuture(new DefaultDOMRpcResult(RpcResultBuilder.newError(
                    ErrorType.APPLICATION, ErrorTag.INVALID_VALUE,
                    "Supplied path does not identify a concrete instance")));
            }

            final ListenableFuture<RpcResult<?>> userFuture = implementation.invoke(instance.toLegacy(),
                codec.fromNormalizedNodeActionInput(actionInterface, input));
            if (userFuture instanceof BindingOperationFluentFuture) {
                // If we are looping back through our future we can skip wrapping. This can happen if application
                // forwards invocations between multiple instantiations of the same action.
                return (BindingOperationFluentFuture) userFuture;
            }

            return new BindingOperationFluentFuture(userFuture, actionInterface, outputName, adapterContext);
        }
    }
}
