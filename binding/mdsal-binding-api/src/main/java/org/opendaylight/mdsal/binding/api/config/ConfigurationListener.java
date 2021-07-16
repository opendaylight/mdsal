/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.config;

import com.google.common.annotations.Beta;
import java.util.EventListener;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * A listener to configuration changes.
 *
 * @param <T> the DataObject type
 */
@Beta
public interface ConfigurationListener<T extends DataObject> extends EventListener {
    /**
     * Invoked when a configuration object changes.
     *
     * @param configuration Updated configuration
     */
    void onConfiguration(@NonNull T configuration);
}
