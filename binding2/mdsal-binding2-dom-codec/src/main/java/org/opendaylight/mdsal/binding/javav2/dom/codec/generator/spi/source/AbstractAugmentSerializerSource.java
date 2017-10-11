/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.dom.codec.generator.spi.source;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.opendaylight.mdsal.binding.javav2.dom.codec.generator.spi.generator.AbstractGenerator;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
public abstract class AbstractAugmentSerializerSource extends AbstractDataNodeContainerSerializerSource {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractAugmentSerializerSource.class);
    private final List<AugmentationSchemaNode> augmentationSchemas;

    public AbstractAugmentSerializerSource(final AbstractGenerator generator, final GeneratedType type,
                                           final List<AugmentationSchemaNode> augmentationSchemas) {
        super(generator, type, augmentationSchemas.get(0));
        this.augmentationSchemas = Preconditions.checkNotNull(augmentationSchemas);
    }

    @Override
    protected Collection<DataSchemaNode> getChildNodes() {
        List<DataSchemaNode> childNodes = new ArrayList<>();
        for (AugmentationSchemaNode schema : this.augmentationSchemas) {
            childNodes.addAll(schema.getChildNodes());
        }
        return childNodes;
    }

}
