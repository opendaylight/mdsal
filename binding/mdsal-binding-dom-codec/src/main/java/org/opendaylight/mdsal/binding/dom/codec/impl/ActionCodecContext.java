/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

final class ActionCodecContext {
    private final SchemaPath path;
    private final DataContainerCodecContext<?, ContainerSchemaNode> input;
    private final DataContainerCodecContext<?, ContainerSchemaNode> output;

    ActionCodecContext(final SchemaPath path, final DataContainerCodecContext<?, ContainerSchemaNode> input,
        final DataContainerCodecContext<?, ContainerSchemaNode> output) {
        this.path = requireNonNull(path);
        this.input = requireNonNull(input);
        this.output = requireNonNull(output);
    }

    SchemaPath getPath() {
        return path;
    }

    BindingStreamEventWriter createInputWriter(NormalizedNodeStreamWriter domWriter) {
        return input.createWriter(domWriter);
    }

    BindingStreamEventWriter createOutputWriter(NormalizedNodeStreamWriter domWriter) {
        return output.createWriter(domWriter);
    }
}
