/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring;

import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.mdsal.binding.generator.impl.GeneratedClassLoadingStrategy;

/**
 * Wiring for DI of mdsal's binding related services in a standalone environment.
 *
 * @author Michael Vorburger.ch
 */
@Singleton
public class BindingWiring2 {
    // TODO rename this BindingWiring after org.opendaylight.mdsal.binding.dom.adapter.BindingWiring is AdapterWiring

    // TODO If this ClassLoadingStrategy is the only thing that remains here, then remove nd work into AdapterWiring

    private final ClassLoadingStrategy classLoadingStrategy =
            GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy();

    public ClassLoadingStrategy getClassLoadingStrategy() {
        return classLoadingStrategy;
    }
}
