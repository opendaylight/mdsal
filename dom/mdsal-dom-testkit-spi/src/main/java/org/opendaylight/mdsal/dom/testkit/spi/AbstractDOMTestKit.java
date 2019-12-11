/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.testkit.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMActionProviderService;
import org.opendaylight.mdsal.dom.api.DOMActionService;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.mdsal.dom.spi.FixedDOMSchemaService;
import org.opendaylight.mdsal.dom.spi.store.DOMStore;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStore;
import org.opendaylight.mdsal.dom.store.inmemory.InMemoryDOMDataStoreConfigProperties;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

@Beta
public abstract class AbstractDOMTestKit<D extends DOMDataBroker> extends EffectiveModelContextTestKit
        implements AutoCloseable {
    /**
     * Policy decision on {@code DataTreeChangeListener} registrations.
     */
    @NonNullByDefault
    public static class ListenerPolicy {
        /**
         * Halt all events except the first notification of initial data. This has the effect of allowing the listener
         * to settle on initial state, but since no further events are delivered, that state is held steady.
         */
        public static final ListenerPolicy HALT = new ListenerPolicy();
        /**
         * Filter all events emitted towards the listener registration instance. This means that in addition to the
         * effects of {@link #HALT}, even the initial event will be suppressed, hence the listener will remain
         * uninitialized.
         */
        public static final ListenerPolicy IGNORE = new ListenerPolicy();
        /**
         * Emit all events towards the listener registration asynchronously in a background threadpool.
         */
        public static final ListenerPolicy PASS = new ListenerPolicy();

        ListenerPolicy() {
            // Prevent outside instantiation
        }

        /**
         * Capture all events emitted towards the listener registration instance. This allows fine-grained control over
         * timing of events delivered to multiple co-operating listeners.
         *
         * @param marker Marker object to aid filtering.
         */
        public static ListenerPolicy capture(final Object marker) {
            return new Capture(marker);
        }

        public static final class Capture extends ListenerPolicy {
            private final Object marker;

            Capture(final Object marker) {
                this.marker = requireNonNull(marker);
            }

            public Object getMarker() {
                return marker;
            }
        }
    }

    @FunctionalInterface
    @NonNullByDefault
    public interface DOMListenerClassifier {

        ListenerPolicy policyFor(DOMDataTreeIdentifier treeId, DOMDataTreeChangeListener listener);
    }

    @NonNullByDefault
    public abstract static class DOMListenerEvent extends AbstractListenerEvent<DOMDataTreeChangeListener> {
        DOMListenerEvent(final DOMDataTreeChangeListener listener, final Object marker) {
            super(marker, listener);
        }

        abstract void fire();
    }

    public static final @NonNull DOMListenerClassifier PASS_CLASSIFIER = (treeId, listener) -> ListenerPolicy.PASS;

    private final ImmutableSet<LogicalDatastoreType> datastoreTypes;
    private final DOMListenerClassifier classifier;

    private volatile CapturingDOMDataBrokerImpl<D> domDataBroker;
    private volatile DOMSchemaService domSchemaService;

    protected AbstractDOMTestKit() {
        datastoreTypes = ImmutableSet.of();
        classifier = PASS_CLASSIFIER;
    }

    protected AbstractDOMTestKit(final Set<LogicalDatastoreType> datastoreTypes) {
        this.datastoreTypes = ImmutableSet.copyOf(datastoreTypes);
        classifier = PASS_CLASSIFIER;
    }

    protected AbstractDOMTestKit(final Set<LogicalDatastoreType> datastoreTypes,
            final DOMListenerClassifier classifier) {
        this.datastoreTypes = ImmutableSet.copyOf(datastoreTypes);
        this.classifier = requireNonNull(classifier);
    }

    protected AbstractDOMTestKit(final Set<YangModuleInfo> moduleInfos,
            final Set<LogicalDatastoreType> datastoreTypes) {
        super(moduleInfos);
        this.datastoreTypes = ImmutableSet.copyOf(datastoreTypes);
        this.classifier = PASS_CLASSIFIER;
    }

    protected AbstractDOMTestKit(final Set<YangModuleInfo> moduleInfos,
            final Set<LogicalDatastoreType> datastoreTypes, final DOMListenerClassifier classifier) {
        super(moduleInfos);
        this.datastoreTypes = ImmutableSet.copyOf(datastoreTypes);
        this.classifier = requireNonNull(classifier);
    }

    public final CapturingDOMDataBroker domDataBroker() {
        CapturingDOMDataBrokerImpl<D> local = domDataBroker;
        if (local == null) {
            synchronized (this) {
                local = domDataBroker;
                if (local == null) {
                    domDataBroker = local = createDomDataBroker();
                }
            }
        }
        return local;
    }

    public final DOMSchemaService domSchemaService() {
        DOMSchemaService local = domSchemaService;
        if (local == null) {
            synchronized (this) {
                local = domSchemaService;
                if (local == null) {
                    domSchemaService = local = FixedDOMSchemaService.of(this::effectiveModelContext);
                }
            }
        }
        return local;
    }

    @Override
    public void close() {
        if (domDataBroker != null) {
            closeDomDataBroker(domDataBroker.delegate());
            domDataBroker = null;
        }
    }

    public abstract DOMActionProviderService domActionProviderService();

    public abstract DOMActionService domActionService();

    public abstract DOMRpcProviderService domRpcProviderService();

    public abstract DOMRpcService domRpcService();

    protected abstract void closeDomDataBroker(@NonNull D dataBroker);

    protected abstract @NonNull D createDomDataBroker(@NonNull Map<LogicalDatastoreType, DOMStore> datastores);

    protected @NonNull DOMStore createDomStore(final @NonNull LogicalDatastoreType datastoreType) {
        final InMemoryDOMDataStore store = new InMemoryDOMDataStore(datastoreType.toString(), datastoreType,
            // We do not run DTCLs concurrently to avoid ping-ponging databroker/capture locks
            Executors.newSingleThreadExecutor(),
            InMemoryDOMDataStoreConfigProperties.DEFAULT_MAX_DATA_CHANGE_LISTENER_QUEUE_SIZE,
            false);
        store.onGlobalContextUpdated(effectiveModelContext());
        return store;
    }

    private @NonNull CapturingDOMDataBrokerImpl<D> createDomDataBroker() {
        final Map<LogicalDatastoreType, DOMStore> datastores = new HashMap<>();
        for (LogicalDatastoreType type : datastoreTypes) {
            datastores.put(type, createDomStore(type));
        }
        return new CapturingDOMDataBrokerImpl<>(createDomDataBroker(datastores), classifier);
    }
}
