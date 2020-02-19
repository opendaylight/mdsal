/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import com.google.common.annotations.Beta;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.inject.Singleton;
import org.kohsuke.MetaInfServices;
import org.opendaylight.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link BindingRuntimeGenerator}.
 */
@Beta
@MetaInfServices
@Singleton
@Component(immediate = true)
public final class DefaultBindingRuntimeGenerator implements BindingRuntimeGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultBindingRuntimeGenerator.class);

    @Override
    public BindingRuntimeTypes generateTypeMapping(final SchemaContext context) {
        GeneratorUtils.checkContext(context);

        final Map<SchemaNode, JavaTypeName> renames = new IdentityHashMap<>();
        for (;;) {
            try {
                return new RuntimeTypeGenerator(context, renames).toTypeMapping();
            } catch (RenameMappingException e) {
                GeneratorUtils.rename(renames, e);
            }
        }
    }

    @Activate
    @SuppressWarnings("static-method")
    void activate() {
        LOG.info("Binding/YANG type support activated");
    }

    @Deactivate
    @SuppressWarnings("static-method")
    void deactivate() {
        LOG.info("Binding/YANG type support deactivated");
    }
}
