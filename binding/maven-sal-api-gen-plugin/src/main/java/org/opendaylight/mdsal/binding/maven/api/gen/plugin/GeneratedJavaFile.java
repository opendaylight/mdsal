/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.maven.api.gen.plugin;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.Writer;
import java.util.function.Supplier;
import org.opendaylight.yangtools.plugin.generator.api.AbstractGeneratedTextFile;
import org.opendaylight.yangtools.plugin.generator.api.GeneratedFileLifecycle;

final class GeneratedJavaFile extends AbstractGeneratedTextFile {
    private final Supplier<String> bodySupplier;

    GeneratedJavaFile(final GeneratedFileLifecycle lifecycle, final Supplier<String> bodySupplier) {
        super(lifecycle);
        this.bodySupplier = requireNonNull(bodySupplier);
    }

    @Override
    protected void writeBody(final Writer output) throws IOException {
        output.write(bodySupplier.get());
    }
}
