/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * @author nite
 *
 */
@Beta
@NonNullByDefault
public interface DOMInstanceNotificationService extends DOMService {

    Registration registerNotificationListener(DOMDataTreeIdentifier path, QName type,
        DOMInstanceNotificationListener listener, Executor executor);

    default Registration registerNotificationListener(final DOMDataTreeIdentifier path, final QName type,
            final DOMInstanceNotificationListener listener) {
        return registerNotificationListener(path, type, listener, MoreExecutors.directExecutor());
    }
}
