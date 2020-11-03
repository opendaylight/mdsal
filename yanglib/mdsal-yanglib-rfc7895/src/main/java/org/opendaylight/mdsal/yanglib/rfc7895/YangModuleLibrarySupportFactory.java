/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc7895;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.ServiceLoader;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeFactory;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.mdsal.yanglib.api.LegacyYangLibraryContentBuilder;
import org.opendaylight.mdsal.yanglib.api.YangLibSupport;
import org.opendaylight.mdsal.yanglib.api.YangLibSupportFactory;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;

@Beta
@MetaInfServices
@NonNullByDefault
public final class YangModuleLibrarySupportFactory implements YangLibSupportFactory<LegacyYangLibraryContentBuilder> {
    private final BindingCodecTreeFactory codecFactory;
    private final BindingRuntimeGenerator generator;

    public YangModuleLibrarySupportFactory() {
        this(load(BindingRuntimeGenerator.class), load(BindingCodecTreeFactory.class));
    }

    public YangModuleLibrarySupportFactory(final BindingRuntimeGenerator generator,
            final BindingCodecTreeFactory codecFactory) {
        this.generator = requireNonNull(generator);
        this.codecFactory = requireNonNull(codecFactory);
    }

    @Override
    public YangLibSupport<LegacyYangLibraryContentBuilder> createYangLibSupport(final YangParserFactory parserFactory)
            throws YangParserException {
        return new YangModuleLibrarySupport(parserFactory, generator, codecFactory);
    }

    private static <T> T load(final Class<T> clazz) {
        return ServiceLoader.load(clazz).findFirst()
                .orElseThrow(() -> new IllegalStateException("Failed to find a " + clazz.getSimpleName() + " service"));
    }
}
