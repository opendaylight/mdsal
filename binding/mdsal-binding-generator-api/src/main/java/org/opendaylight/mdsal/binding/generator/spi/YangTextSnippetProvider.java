/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.spi;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

/**
 * SPI interface for plugging YANG snippet generation into a BindingGenerator.
 */
@Beta
@NonNullByDefault
public interface YangTextSnippetProvider {
    /**
     * Generate a YANG snippet for specified SchemaNode.
     *
     * @param schemaNode SchemaNode for which to generate a snippet
     * @return YANG snippet
     */
    String generateYangSnippet(SchemaNode schemaNode);

    /**
     * Generate a YANG snippet for specified Module.
     *
     * @param module Module for which to generate a snippet
     * @return YANG snippet
     */
    String generateYangSnippet(Module module);
}
