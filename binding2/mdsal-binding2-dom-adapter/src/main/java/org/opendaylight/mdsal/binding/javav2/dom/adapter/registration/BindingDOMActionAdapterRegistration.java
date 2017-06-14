/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.registration;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.spec.base.Action;
import org.opendaylight.mdsal.binding.javav2.spec.base.TreeNode;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementationRegistration;
import org.opendaylight.yangtools.concepts.AbstractObjectRegistration;

/**
 * Registration of Binding - DOM Action adapter.
 *
 * @param <T>
 *            - {@link Action} type
 */
@Beta
public class BindingDOMActionAdapterRegistration<T extends Action<? extends TreeNode, ?, ?>>
        extends AbstractObjectRegistration<T> {

    private final DOMRpcImplementationRegistration<?> reg;

    // FIXME : DOM part doesn't work with Yang 1.1 - Action registration isn't implemented yet.
    public BindingDOMActionAdapterRegistration(final T instance, final DOMRpcImplementationRegistration<?> reg) {
        super(instance);
        this.reg = reg;
    }

    @Override
    protected void removeRegistration() {
        reg.close();
    }
}
