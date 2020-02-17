/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc8525;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.ServiceLoader;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.binding.generator.api.BindingRuntimeGenerator;
import org.opendaylight.mdsal.yanglib.api.YangLibSupport;
import org.opendaylight.mdsal.yanglib.api.YangLibSupportFactory;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;

@MetaInfServices
@NonNullByDefault
public final class YangLibrarySupportFactory implements YangLibSupportFactory {
    private final BindingRuntimeGenerator generator;

    public YangLibrarySupportFactory() {
        this(ServiceLoader.load(BindingRuntimeGenerator.class).findFirst()
            .orElseThrow(() -> new IllegalStateException("Failed to find a BindingRuntimeGenerator service")));
    }

    public YangLibrarySupportFactory(final BindingRuntimeGenerator generator) {
        this.generator = requireNonNull(generator);
    }

    @Override
    public YangLibSupport createYangLibSupport(final YangParserFactory parserFactory)
            throws YangParserException, IOException {
        return new YangLibrarySupport(parserFactory, generator);
    }
}
