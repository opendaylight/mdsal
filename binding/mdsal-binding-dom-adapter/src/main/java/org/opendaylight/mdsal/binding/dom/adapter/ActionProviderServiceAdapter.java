/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.api.ActionProviderService;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.dom.adapter.BindingDOMAdapterBuilder.Factory;
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
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

@NonNullByDefault
public final class ActionProviderServiceAdapter extends AbstractBindingAdapter<DOMActionProviderService>
        implements ActionProviderService {
    private static final class Builder extends BindingDOMAdapterBuilder<ActionProviderService> {
        Builder(final AdapterContext adapterContext) {
            super(adapterContext);
        }

        @Override
        protected ActionProviderService createInstance(final ClassToInstanceMap<DOMService> delegates) {
            return new ActionProviderServiceAdapter(adapterContext(),
                delegates.getInstance(DOMActionProviderService.class));
        }

        @Override
        public Set<? extends Class<? extends DOMService>> getRequiredDelegates() {
            return ImmutableSet.of(DOMActionProviderService.class);
        }
    }

    static final Factory<ActionProviderService> BUILDER_FACTORY = Builder::new;

    ActionProviderServiceAdapter(final AdapterContext adapterContext, final DOMActionProviderService delegate) {
        super(adapterContext, delegate);
    }

    @Override
    public <O extends DataObject, P extends InstanceIdentifier<O>, T extends Action<P, ?, ?>, S extends T>
            ObjectRegistration<S> registerImplementation(final Class<T> actionInterface, final S implementation,
                final LogicalDatastoreType datastore, final Set<DataTreeIdentifier<O>> validNodes) {
        final Absolute path = currentSerializer().getActionPath(actionInterface);
        final ObjectRegistration<DOMActionImplementation> reg = getDelegate().registerActionImplementation(
            new Impl(adapterContext(),
                NodeIdentifier.create(YangConstants.operationOutputQName(path.getLastComponent().getModule())),
                actionInterface, implementation), ImmutableSet.of());
        return new AbstractObjectRegistration<>(implementation) {
            @Override
            protected void removeRegistration() {
                reg.close();
            }
        };
    }

    private static final class Impl implements DOMActionImplementation {
        private final Class<? extends Action<?, ?, ?>> actionInterface;
        private final AdapterContext adapterContext;
        private final Action implementation;
        private final NodeIdentifier outputName;

        Impl(final AdapterContext adapterContext, final NodeIdentifier outputName,
                final Class<? extends Action<?, ?, ?>> actionInterface, final Action<?, ?, ?> implementation) {
            this.adapterContext = requireNonNull(adapterContext);
            this.outputName = requireNonNull(outputName);
            this.actionInterface = requireNonNull(actionInterface);
            this.implementation = requireNonNull(implementation);
        }

        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public ListenableFuture<? extends DOMActionResult> invokeAction(final Absolute type,
                final DOMDataTreeIdentifier path, final ContainerNode input) {
            final CurrentAdapterSerializer codec = adapterContext.currentSerializer();

            final ListenableFuture<RpcResult<?>> userFuture = implementation.invoke(
                verifyNotNull(codec.fromYangInstanceIdentifier(path.getRootIdentifier())),
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
