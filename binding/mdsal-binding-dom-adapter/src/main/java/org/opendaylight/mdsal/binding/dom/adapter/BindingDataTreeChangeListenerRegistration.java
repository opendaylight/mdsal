/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.concepts.Registration;

class BindingDataTreeChangeListenerRegistration<L extends DataTreeChangeListener<?>>
        extends AbstractListenerRegistration<L> {
    private final Registration domReg;

    BindingDataTreeChangeListenerRegistration(final L listener, final Registration domReg) {
        super(listener);
        this.domReg = requireNonNull(domReg);
    }

    @Override
    protected void removeRegistration() {
        domReg.close();
    }
}
