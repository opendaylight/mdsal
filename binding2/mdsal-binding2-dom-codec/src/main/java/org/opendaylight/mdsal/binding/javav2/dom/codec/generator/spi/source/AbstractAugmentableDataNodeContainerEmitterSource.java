/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.generator.spi.source;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.impl.StreamWriterGenerator;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.spi.generator.AbstractStreamWriterGenerator;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.javav2.spec.runtime.TreeNodeSerializerImplementation;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;

@Beta
public abstract class AbstractAugmentableDataNodeContainerEmitterSource
        extends AbstractDataNodeContainerSerializerSource {

    private static final String AUGMENTABLE_SERIALIZER = "AUGMENTABLE_SERIALIZER";

    public AbstractAugmentableDataNodeContainerEmitterSource(final AbstractStreamWriterGenerator generator,
            final GeneratedType type, final DataNodeContainer node) {
        super(generator, type, node);
        /*
         * Eventhough intuition says the serializer could reference the
         * generator directly, that is not true in OSGi environment -- so we
         * need to resolve the reference first and inject it as a static
         * constant.
         */
        staticConstant(AUGMENTABLE_SERIALIZER, TreeNodeSerializerImplementation.class,
                StreamWriterGenerator.AUGMENTABLE);
    }

    @Override
    protected void emitAfterBody(final StringBuilder builder) {
        builder.append(statement(invoke(AUGMENTABLE_SERIALIZER, "serialize", REGISTRY, INPUT, STREAM)));
    }
}
