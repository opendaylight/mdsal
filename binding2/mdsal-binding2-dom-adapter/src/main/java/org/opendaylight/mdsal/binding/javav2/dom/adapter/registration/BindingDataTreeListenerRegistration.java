/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.registration;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.api.DataTreeListener;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

/**
 * Registration of data tree listener.
 *
 * @param <L>
 *            - {@link DataTreeListener} type
 */
@Beta
public class BindingDataTreeListenerRegistration<L extends DataTreeListener>
        extends AbstractListenerRegistration<L> {

    private final ListenerRegistration<?> domReg;

    public BindingDataTreeListenerRegistration(final L listener, final ListenerRegistration<?> domReg) {
        super(listener);
        this.domReg = requireNonNull(domReg);
    }

    @Override
    protected void removeRegistration() {
        domReg.close();
    }
}
