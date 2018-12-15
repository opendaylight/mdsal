/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter;

import org.slf4j.LoggerFactory;

/**
 * Static JVM-wide configuration.
 */
final class StaticConfiguration {
    /**
     * When we are invoking a Binding-Aware RPC implementation, we can side-step translation through NormalizedNodes,
     * which is an obvious performance win.
     *
     * <p>
     * Unfortunately Binding Specification does not truthfully cover YANG semantics, in that grouping instantiations
     * are not treated separately, which leads to constructs which are Binding-valid, but are actually YANG-invalid.
     * These are usually easily rectified when identified, but the existence of this shortcut means that in single-node
     * scenario we do not detect these mismatches and thus these issues remain unidentified -- only to break when
     * in multi-node scenarios or the shortcut becomes otherwise ineffective.
     *
     * <p>
     * We therefore allow the shortcuts to be globally disabled via a property, which is evaluated when this component
     * loads.
     */
    static final boolean ENABLE_CODEC_SHORTCUT = !Boolean.getBoolean(
        "org.opendaylight.mdsal.binding.dom.adapter.disableCodecShortcut");

    static {
        // Do not retain the logger
        LoggerFactory.getLogger(StaticConfiguration.class).info("Binding-over-DOM codec shortcuts are {}",
                ENABLE_CODEC_SHORTCUT ? "enabled" : "disabled");
    }

    private StaticConfiguration() {

    }
}
