/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.testkit.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.dom.testkit.DOMServiceTestKit;
import org.opendaylight.mdsal.dom.testkit.spi.AbstractDOMServiceTestKit.ListenerPolicy;

@Beta
public abstract class AbstractBindingServiceTestKit<D extends DataBroker> implements AutoCloseable {

    @FunctionalInterface
    @NonNullByDefault
    public interface TreeChangeListenerClassifier {

        ListenerPolicy policyFor(DataTreeIdentifier<?> treeId, DataTreeChangeListener<?> listener);
    }

    private final TreeChangeListenerClassifier classifier;
    private final DOMServiceTestKit domTestKit;

    private volatile CapturingDataBrokerImpl<D> dataBroker;
    private volatile BindingNormalizedNodeCodecRegistry codecRegistry;

    protected AbstractBindingServiceTestKit(final DOMServiceTestKit domTestKit) {
        this(domTestKit, (treeId, listener) -> ListenerPolicy.PASS);
    }

    protected AbstractBindingServiceTestKit(final DOMServiceTestKit domTestKit,
            final TreeChangeListenerClassifier classifier) {
        this.domTestKit = requireNonNull(domTestKit);
        this.classifier = requireNonNull(classifier);
    }

    public final DOMServiceTestKit domTestKit() {
        return domTestKit;
    }

    public final BindingNormalizedNodeCodecRegistry codecRegistry() {
        // TODO Auto-generated method stub
        return null;
    }

    public final CapturingDataBroker dataBroker() {
        CapturingDataBrokerImpl<D> local = dataBroker;
        if (local == null) {
            synchronized (this) {
                local = dataBroker;
                if (local == null) {
                    dataBroker = local = new CapturingDataBrokerImpl<>(createDataBroker(), classifier);
                }
            }
        }
        return local;
    }

    @Override
    public void close()  {
        if (dataBroker != null) {
            closeDataBroker(dataBroker.delegate());
        }
     }

    protected abstract @NonNull D createDataBroker();

    protected abstract void closeDataBroker(D dataBroker);
}
