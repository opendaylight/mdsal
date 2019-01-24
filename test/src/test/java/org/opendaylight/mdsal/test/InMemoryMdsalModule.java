/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.test;

import org.opendaylight.infrautils.inject.guice.AutoWiringModule;
import org.opendaylight.infrautils.inject.guice.GuiceClassPathBinder;

/**
 * Guice Module which binds the mdsal (not controller) {@link DataBroker} & Co.
 * in-memory implementation.
 *
 * @author Michael Vorburger.ch
 */
public class InMemoryMdsalModule extends AutoWiringModule {

    public InMemoryMdsalModule(GuiceClassPathBinder classPathBinder) {
        super(classPathBinder, "org.opendaylight.mdsal");
    }
}
