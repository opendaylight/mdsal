/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.adapter.impl;

import java.util.Dictionary;
import java.util.Hashtable;
import javassist.ClassPool;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.impl.StreamWriterGenerator;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.mdsal.binding.javav2.dom.codec.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.javav2.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.mdsal.binding.javav2.runtime.javassist.JavassistUtils;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Factory class for creating and initializing the BindingToNormalizedNodeCodec instances.
 */
public class BindingToNormalizedNodeCodecFactory {

    public static final ClassPool CLASS_POOL = ClassPool.getDefault();
    public static final JavassistUtils JAVASSIST = JavassistUtils.forClassPool(CLASS_POOL);

    /**
     * Creates a new BindingToNormalizedNodeCodec instance.
     *
     * @return the BindingToNormalizedNodeCodec instance
     */
    public static BindingToNormalizedNodeCodec newInstance() {
        final BindingNormalizedNodeCodecRegistry codecRegistry =
                new BindingNormalizedNodeCodecRegistry(StreamWriterGenerator.create(JAVASSIST));
        return new BindingToNormalizedNodeCodec(
                (GeneratedClassLoadingStrategy) GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy(),
                codecRegistry, true);
    }

    /**
     * Registers the given instance with the SchemaService as a SchemaContextListener.
     *
     * @param instance
     *            the BindingToNormalizedNodeCodec instance
     * @param schemaService
     *            the SchemaService.
     * @return the ListenerRegistration
     */
    public static ListenerRegistration<SchemaContextListener>
            registerInstance(final BindingToNormalizedNodeCodec instance, final DOMSchemaService schemaService) {
        return schemaService.registerSchemaContextListener(instance);
    }

    /**
     * This method is called via blueprint to register a BindingToNormalizedNodeCodec instance with the OSGI
     * service registry. This is done in code instead of directly via blueprint because the
     * BindingToNormalizedNodeCodec instance must be advertised with the actual class for backwards
     * compatibility with CSS modules and blueprint will try to create a proxy wrapper which is problematic
     * with BindingToNormalizedNodeCodec because it's final and has final methods which can't be proxied.
     *
     * @param instance
     *            the BindingToNormalizedNodeCodec instance
     * @param bundleContext
     *            the BundleContext
     * @return ServiceRegistration instance
     */
    public static ServiceRegistration<BindingToNormalizedNodeCodec>
            registerOSGiService(final BindingToNormalizedNodeCodec instance, final BundleContext bundleContext) {
        final Dictionary<String, String> props = new Hashtable<>();

        // Set the appropriate service properties so the corresponding CSS module is restarted if this
        // blueprint container is restarted
        props.put("config-module-namespace", "urn:opendaylight:params:xml:ns:yang:controller:md:sal:binding:impl");
        props.put("config-module-name", "runtime-generated-mapping");
        props.put("config-instance-name", "runtime-mapping-singleton");
        return bundleContext.registerService(BindingToNormalizedNodeCodec.class, instance, props);
    }
}
