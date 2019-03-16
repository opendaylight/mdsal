/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.ActionProviderService;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMAdapterBuilder.Factory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMActionImplementation;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMActionResult;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMService;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@Beta
@NonNullByDefault
// FIXME: make this class non-public once the controller user is gone
public final class ActionProviderServiceAdapter extends AbstractBindingAdapter<DOMActionProviderService>
        implements ActionProviderService {
    private static final class Builder extends BindingDOMAdapterBuilder<ActionProviderService> {
        @Override
        protected ActionProviderService createInstance(final @Nullable BindingToNormalizedNodeCodec codec,
                final ClassToInstanceMap<DOMService> delegates) {
            final DOMActionProviderService domAction = delegates.getInstance(DOMActionProviderService.class);
            return new ActionProviderServiceAdapter(requireNonNull(codec), domAction);
        }

        @Override
        public Set<? extends Class<? extends DOMService>> getRequiredDelegates() {
            return ImmutableSet.of(DOMActionProviderService.class);
        }
    }

    static final Factory<ActionProviderService> BUILDER_FACTORY = Builder::new;

    ActionProviderServiceAdapter(final BindingToNormalizedNodeCodec codec, final DOMActionProviderService delegate) {
        super(codec, delegate);
    }

    @Deprecated
    public static ActionProviderServiceAdapter create(final BindingToNormalizedNodeCodec codec,
            final DOMActionProviderService delegate) {
        return new ActionProviderServiceAdapter(codec, delegate);
    }

    @Override
    public <O extends DataObject, P extends InstanceIdentifier<O>,
        T extends org.opendaylight.yangtools.yang.binding.Action<P, ?, ?>, S extends T>
        ObjectRegistration<S> registerImplementation(final Class<T> actionInterface, final S implementation,
                final LogicalDatastoreType datastore, final Set<DataTreeIdentifier<O>> validNodes) {
        final SchemaPath path = getCodec().getActionPath(actionInterface);
        final ObjectRegistration<DOMActionImplementation> reg = getDelegate().registerActionImplementation(
            new Impl(getCodec(),
                NodeIdentifier.create(YangConstants.operationOutputQName(path.getLastComponent().getModule())),
                actionInterface, implementation), ImmutableSet.of());
        return new AbstractObjectRegistration<S>(implementation) {
            @Override
            protected void removeRegistration() {
                reg.close();
            }
        };
    }

    private static final class Impl implements DOMActionImplementation {
        private final Class<? extends Action<?, ?, ?>> actionInterface;
        private final Action implementation;
        private final BindingNormalizedNodeSerializer codec;
        private final NodeIdentifier outputName;

        Impl(final BindingNormalizedNodeSerializer codec, final NodeIdentifier outputName,
                final Class<? extends Action<?, ?, ?>> actionInterface, final Action<?, ?, ?> implementation) {
            this.codec = requireNonNull(codec);
            this.outputName = requireNonNull(outputName);
            this.actionInterface = requireNonNull(actionInterface);
            this.implementation = requireNonNull(implementation);
        }

        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public ListenableFuture<? extends DOMActionResult> invokeAction(final SchemaPath type,
                final DOMDataTreeIdentifier path, final ContainerNode input) {
            final ListenableFuture<RpcResult<?>> userFuture = implementation.invoke(
                codec.fromYangInstanceIdentifier(path.getRootIdentifier()),
                codec.fromNormalizedNodeActionInput(actionInterface, input));
            if (userFuture instanceof BindingOperationFluentFuture) {
                // If we are looping back through our future we can skip wrapping. This can happen if application
                // forwards invocations between multiple instantiations of the same action.
                return (BindingOperationFluentFuture) userFuture;
            }

            return new BindingOperationFluentFuture(userFuture, actionInterface, outputName, codec);
        }
    }
}
