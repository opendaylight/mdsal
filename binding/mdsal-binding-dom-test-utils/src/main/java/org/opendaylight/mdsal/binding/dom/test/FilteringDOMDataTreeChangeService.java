package org.opendaylight.mdsal.binding.dom.test;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.dom.test.AbstractDOMServiceTestKit.DTCLClassifier;
import org.opendaylight.mdsal.binding.dom.test.AbstractDOMServiceTestKit.DTCLPolicy;
import org.opendaylight.mdsal.dom.api.ClusteredDOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.NoOpListenerRegistration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

final class FilteringDOMDataTreeChangeService implements DOMDataTreeChangeService {
    private final DOMDataTreeChangeService delegate;
    private final DTCLClassifier classifier;

    FilteringDOMDataTreeChangeService(final DOMDataTreeChangeService delegate, final DTCLClassifier classifier) {
        this.delegate = requireNonNull(delegate);
        this.classifier = requireNonNull(classifier);
    }

    @Override
    public <L extends DOMDataTreeChangeListener> ListenerRegistration<L> registerDataTreeChangeListener(
            final DOMDataTreeIdentifier treeId, final L listener) {
        final DTCLPolicy policy = classifier.policyFor(treeId, listener);
        if (DTCLPolicy.PASS.equals(policy)) {
            return delegate.registerDataTreeChangeListener(treeId, listener);
        } else if (DTCLPolicy.IGNORE.equals(policy)) {
            return NoOpListenerRegistration.of(listener);
        } else if (policy instanceof DTCLPolicy.Capture) {
            final Object marker = ((DTCLPolicy.Capture) policy).marker;
            final ListenerRegistration<?> reg = delegate.registerDataTreeChangeListener(treeId,
                listener instanceof ClusteredDataTreeChangeListener
                    ? new CapturingClusteredDTCListener(listener, marker)
                            : new CapturingDTCListener(listener, marker));

            return new AbstractListenerRegistration<>(listener) {
                @Override
                protected void removeRegistration() {
                    reg.close();
                }
            };

        }

        throw new IllegalStateException("Unhandled policy " + policy);
    }


    void addEvent(final CapturedDataTreeChangeEvent dataTreeChanged) {
        // TODO Auto-generated method stub

    }

    private class CapturingDTCListener implements DOMDataTreeChangeListener {
        private final @NonNull DOMDataTreeChangeListener listener;
        private final @NonNull Object marker;

        CapturingDTCListener(final DOMDataTreeChangeListener listener, final Object marker) {
            this.listener = requireNonNull(listener);
            this.marker = requireNonNull(marker);
        }

        @Override
        public final void onDataTreeChanged(final Collection<DataTreeCandidate> changes) {
            addEvent(new DataTreeChanged(listener, marker, changes));
        }

        @Override
        public final void onInitialData() {
            addEvent(new InitialData(listener, marker));
        }
    }

    private final class CapturingClusteredDTCListener extends CapturingDTCListener
            implements ClusteredDOMDataTreeChangeListener {

        CapturingClusteredDTCListener(final DOMDataTreeChangeListener listener, final Object marker) {
            super(listener, marker);
        }
    }

    private abstract static class CapturedDataTreeChangeEvent {
        final @NonNull DOMDataTreeChangeListener listener;
        final @NonNull Object marker;

        CapturedDataTreeChangeEvent(final DOMDataTreeChangeListener listener, final Object marker) {
            this.listener = requireNonNull(listener);
            this.marker = requireNonNull(marker);
        }

        abstract void fire();
    }

    private static final class InitialData extends CapturedDataTreeChangeEvent {
        InitialData(final DOMDataTreeChangeListener listener, final Object marker) {
            super(listener, marker);
        }

        @Override
        void fire() {
            listener.onInitialData();
        }
    }

    private static final class DataTreeChanged extends CapturedDataTreeChangeEvent {
        private final ImmutableList<DataTreeCandidate> changes;

        DataTreeChanged(final DOMDataTreeChangeListener listener, final Object marker,
                final Collection<DataTreeCandidate> changes) {
            super(listener, marker);
            this.changes = ImmutableList.copyOf(changes);
        }

        @Override
        void fire() {
            listener.onDataTreeChanged(changes);
        }
    }
}
