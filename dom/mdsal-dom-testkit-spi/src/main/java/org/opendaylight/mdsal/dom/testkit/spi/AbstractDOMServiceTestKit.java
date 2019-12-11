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
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

@Beta
public abstract class AbstractDOMServiceTestKit<D extends DOMDataBroker> extends EffectiveModelContextTestKit
        implements AutoCloseable {
    /**
     * Policy decision on {@link DataTreeChangeListener} registrations.
     */
    @NonNullByDefault
    public static class DTCLPolicy {
        /**
         * Halt all events except the first notification of initial data. This has the effect of allowing the listener
         * to settle on initial state, but since no further events are delivered, that state is held steady.
         */
        public static final DTCLPolicy HALT = new DTCLPolicy();
        /**
         * Filter all events emitted towards the listener registration instance. This means that in addition to the
         * effects of {@link #HALT}, even the initial event will be suppressed, hence the listener will remain
         * uninitialized.
         */
        public static final DTCLPolicy IGNORE = new DTCLPolicy();
        /**
         * Emit all events towards the listener registration asynchronously in a background threadpool.
         */
        public static final DTCLPolicy PASS = new DTCLPolicy();

        DTCLPolicy() {
            // Prevent outside instantiation
        }

        /**
         * Capture all events emitted towards the listener registration instance. This allows fine-grained control over
         * timing of events delivered to multiple co-operating listeners.
         *
         * @param marker Marker object to aid filtering.
         */
        public static DTCLPolicy capture(final Object marker) {
            return new Capture(marker);
        }

        public static final class Capture extends DTCLPolicy {
            protected final Object marker;

            Capture(final Object marker) {
                this.marker = requireNonNull(marker);
            }
        }
    }

    @FunctionalInterface
    @NonNullByDefault
    public interface DOMTreeChangeListenerClassifier {

        DTCLPolicy policyFor(DOMDataTreeIdentifier treeId, DOMDataTreeChangeListener listener);
    }

    private static final DOMTreeChangeListenerClassifier PASS_CLASSIFIER = (treeId, listener) -> DTCLPolicy.PASS;

    private final ImmutableSet<LogicalDatastoreType> datastoreTypes;
    private final DOMTreeChangeListenerClassifier classifier;

    private volatile CapturingDOMDataBrokerImpl<D> domDataBroker;
    private volatile DOMSchemaService domSchemaService;

    protected AbstractDOMServiceTestKit() {
        datastoreTypes = ImmutableSet.of();
        classifier = PASS_CLASSIFIER;
    }

    protected AbstractDOMServiceTestKit(final Set<LogicalDatastoreType> datastoreTypes) {
        this.datastoreTypes = ImmutableSet.copyOf(datastoreTypes);
        classifier = PASS_CLASSIFIER;
    }

    protected AbstractDOMServiceTestKit(final Set<LogicalDatastoreType> datastoreTypes,
            final DOMTreeChangeListenerClassifier classifier) {
        this.datastoreTypes = ImmutableSet.copyOf(datastoreTypes);
        this.classifier = requireNonNull(classifier);
    }

    protected AbstractDOMServiceTestKit(final Set<YangModuleInfo> moduleInfos,
            final Set<LogicalDatastoreType> datastoreTypes) {
        super(moduleInfos);
        this.datastoreTypes = ImmutableSet.copyOf(datastoreTypes);
        this.classifier = PASS_CLASSIFIER;
    }

    protected AbstractDOMServiceTestKit(final Set<YangModuleInfo> moduleInfos,
            final Set<LogicalDatastoreType> datastoreTypes, final DOMTreeChangeListenerClassifier classifier) {
        super(moduleInfos);
        this.datastoreTypes = ImmutableSet.copyOf(datastoreTypes);
        this.classifier = requireNonNull(classifier);
    }

    public final CapturingDOMDataBroker domDataBroker() {
        CapturingDOMDataBrokerImpl local = domDataBroker;
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

    protected abstract void closeDomDataBroker(@NonNull D dataBroker);

    public abstract DOMActionProviderService domActionProviderService();
    public abstract DOMActionService domActionService();
    public abstract DOMRpcProviderService domRpcProviderService();
    public abstract DOMRpcService domRpcService();

    protected abstract @NonNull D createDomDataBroker(@NonNull Map<LogicalDatastoreType, DOMStore> datastores);

    private @NonNull CapturingDOMDataBrokerImpl<D> createDomDataBroker() {
        final Map<LogicalDatastoreType, DOMStore> datastores = new HashMap<>();
        for (LogicalDatastoreType type : datastoreTypes) {
            final InMemoryDOMDataStore store = new InMemoryDOMDataStore(type.toString(),
                // Default to concurrency?
                Executors.newSingleThreadExecutor());
            store.onGlobalContextUpdated(effectiveModelContext());
            datastores.put(type, store);
        }
        return new CapturingDOMDataBrokerImpl<>(createDomDataBroker(datastores), classifier);
    }
}
