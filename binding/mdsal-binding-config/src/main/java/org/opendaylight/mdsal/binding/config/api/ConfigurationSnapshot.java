/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.config.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataRoot;

/**
 * An immutable snapshot of a configuration object. The object is available via {@link #configuration()}, which is
 * guaranteed not to block.
 *
 * <p>
 * This interface is useful mostly for OSGi Service Component Runtime integration. Instances of this interface are
 * registered with the OSGi Service Registry by the implementation of {@link ConfigurationProviderService}. Each such
 * registration must also carry a service property with key {@value #CONFIGURATION_TYPE_PROP} and value being a
 * {@link String} corresponding to the configuration object's Fully-Qualified Class Name. OSGi users can therefore
 * consume these as normal component dependencies, with an appropriate filter:
 * <pre>
 *   <code>
 *     // Generated from YANG:
 *     public interface org.opendaylight.yang.gen.v1.SomeComponentConfiguration;
 *
 *     @Component
 *     public SomeComponent {
 *         @Reference(target = "(org.opendaylight.mdsal.binding.ConfigurationType=SomeComponentConfiguration)")
 *         ConfigurationSnapshot<SomeComponentConfiguration> configuration;
 *
 *         // ...
 *    }
 *   </code>
 * </pre>
 * and achieve asynchronous component activation based on configuration availability and changes.
 *
 * <p>
 * When a configuration value changes, the implementation is required to register the updated value before removing the
 * old value. Users can therefore use dynamic reference policy without unnecessary service interruption.
 *
 * @param <T> Configuration object type
 */
@Beta
@NonNullByDefault
public interface ConfigurationSnapshot<T extends ChildOf<? super DataRoot>> extends Immutable {
    String CONFIGURATION_TYPE_PROP = "org.opendaylight.mdsal.binding.ConfigurationType";

    T configuration();
}
