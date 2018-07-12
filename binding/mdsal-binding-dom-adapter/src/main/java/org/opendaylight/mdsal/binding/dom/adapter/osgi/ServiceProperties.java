/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import com.google.common.annotations.Beta;

/**
 * Properties recognized and manipulated by {@link DynamicBindingAdapter}. All properties starting with
 * {@code odl.mdsal.binding.adapter.} are stripped from the re-exported service.
 *
 * @author Robert Varga
 */
@Beta
public final class ServiceProperties {
    static final String PREFIX = "odl.mdsal.binding.adapter.";

    /**
     * Instruction to ignore the service. When a service with this property is found, {@link DynamicBindingAdapter}
     * will completely ignore it.
     */
    public static final String IGNORE_PROP = "odl.mdsal.binding.adapter.ignore";

    /**
     * Prefix for properties which should be replaced. For any property with a name which starts with this prefix,
     * {@link DynamicBindingAdapter} will strip this prefix and advertise the resulting property in the re-exported
     * service.
     */
    public static final String OVERRIDE_PREFIX = "odl.mdsal.binding.adapter.override.";

    private ServiceProperties() {

    }
}
