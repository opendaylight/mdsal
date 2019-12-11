package org.opendaylight.mdsal.binding.dom.test;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.test.AbstractDOMServiceTestKit.DTCLClassifier;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataBrokerExtension;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeService;
import org.opendaylight.mdsal.dom.spi.ForwardingDOMDataBroker;

final class FilteringDOMDataBroker<D extends DOMDataBroker> extends ForwardingDOMDataBroker {
    private final @NonNull ClassToInstanceMap<DOMDataBrokerExtension> extensions;
    private final @NonNull D delegate;

    FilteringDOMDataBroker(final D delegate, final DTCLClassifier classifier) {
        this.delegate = requireNonNull(delegate);
        final DOMDataTreeChangeService delegateDtcs = delegate.getExtensions()
                .getInstance(DOMDataTreeChangeService.class);
        extensions = delegateDtcs == null ? ImmutableClassToInstanceMap.of()
                : ImmutableClassToInstanceMap.of(DOMDataTreeChangeService.class,
                    new FilteringDOMDataTreeChangeService(delegateDtcs, classifier));
    }

    @Override
    public ClassToInstanceMap<DOMDataBrokerExtension> getExtensions() {
        return extensions;
    }

    @Override
    protected D delegate() {
        return delegate;
    }
}