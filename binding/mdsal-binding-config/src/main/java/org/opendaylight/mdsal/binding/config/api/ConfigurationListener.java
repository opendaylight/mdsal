/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.config.api;

import com.google.common.annotations.Beta;
import java.util.EventListener;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataRoot;

/**
 * Simple listener for configuration object updates.
 *
 * <T> Configuration object type
 */
@Beta
@NonNullByDefault
@FunctionalInterface
public interface ConfigurationListener<T extends ChildOf<? super DataRoot>> extends EventListener {
    /**
     * Invoked when a configuration object becomes available
     *
     * @param configuration Configuration object
     */
    void onConfiguration(T configuration);
}
