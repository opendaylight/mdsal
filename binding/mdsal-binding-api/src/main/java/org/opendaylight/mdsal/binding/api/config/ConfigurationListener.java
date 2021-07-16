/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.config;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * A specialization of {@link ClusteredDataTreeChangeListener} for receiving configuration changes. Implementations
 * of this interface are guaranteed to not observe {@link #onInitialData()} callback unless we are observing a
 * {@code container} with {@code presence}.
 *
 * @param <T> the DataObject type
 */
@Beta
public interface ConfigurationListener<T extends DataObject> extends ClusteredDataTreeChangeListener<T> {

}
