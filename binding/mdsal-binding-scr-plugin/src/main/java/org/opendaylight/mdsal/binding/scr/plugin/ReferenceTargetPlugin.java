/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.plugin;

import static net.bytebuddy.matcher.ElementMatchers.declaresField;
import static net.bytebuddy.matcher.ElementMatchers.declaresMethod;
import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;

import java.io.IOException;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType.Builder;
import org.osgi.service.component.annotations.Reference;

/**
 * A plugin for {@code bytebuddy-maven-plugin} to adjust {@code @Reference} annotations to include the proper
 * {@code target=""} stanza.
 */
public final class ReferenceTargetPlugin extends Plugin.ForElementMatcher implements Plugin.Factory {

    public ReferenceTargetPlugin() {
        super(declaresMethod(isAnnotatedWith(Reference.class))
            .or(declaresField(isAnnotatedWith(Reference.class))));
    }

    @Override
    public Plugin make() {
        return this;
    }

    @Override
    public Builder<?> apply(final Builder<?> builder, final TypeDescription typeDescription,
            final ClassFileLocator classFileLocator) {
        // FIXME: adjust @Reference to
        //        @Reference(target = "(org.opendaylight.mdsal.binding.ConfigurationType=SomeComponentConfiguration)")
        // FIXME: generate a @Requirement(namespace = ExtenderNamespace.EXTENDER_NAMESPACE) annotation on classes
        return null;
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub

    }
}
