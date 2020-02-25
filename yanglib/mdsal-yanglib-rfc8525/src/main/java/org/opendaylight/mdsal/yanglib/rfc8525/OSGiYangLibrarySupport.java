/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc8525;

import com.google.common.annotations.Beta;
import java.io.IOException;
import org.opendaylight.binding.runtime.api.BindingRuntimeGenerator;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeFactory;
import org.opendaylight.mdsal.yanglib.api.SchemaContextResolver;
import org.opendaylight.mdsal.yanglib.api.YangLibSupport;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContextFactory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@Component(immediate = true)
public final class OSGiYangLibrarySupport implements YangLibSupport {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiYangLibrarySupport.class);

    @Reference
    YangParserFactory parserFactory = null;
    @Reference
    BindingRuntimeGenerator generator = null;
    @Reference
    BindingCodecTreeFactory codecFactory = null;

    private YangLibrarySupport delegate;

    @Override
    public MountPointContextFactory createMountPointContextFactory(final MountPointIdentifier mountId,
            final SchemaContextResolver resolver) {
        return delegate.createMountPointContextFactory(mountId, resolver);
    }

    @Activate
    void activate() throws YangParserException, IOException {
        delegate = new YangLibrarySupport(parserFactory, generator, codecFactory);
        LOG.info("RFC8525 YANG Library support activated");
    }

    @Deactivate
    void deactivate() {
        delegate = null;
        LOG.info("RFC8525 YANG Library support deactivated");
    }
}
