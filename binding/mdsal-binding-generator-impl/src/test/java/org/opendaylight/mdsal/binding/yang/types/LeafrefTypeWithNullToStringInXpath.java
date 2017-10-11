/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.yang.types;

import java.util.List;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

/**
 * Mock LeafrefDypeDefinition implementation with non-null referenced RevisionAwareXPath.
 *
 * Although RevisionAwareXPath has implemented Override for toString to return null value to reach specific branch
 * in TypeProviderImpl#provideTypeForLeafref method.
 *
 * Created by lukas on 9/17/14.
 */
public class LeafrefTypeWithNullToStringInXpath implements LeafrefTypeDefinition {
    @Override
    public RevisionAwareXPath getPathStatement() {
        return new RevisionAwareXPath() {
            @Override
            public boolean isAbsolute() {
                return false;
            }

            @Override
            public String toString() {
                return null;
            }
        };
    }

    @Override
    public LeafrefTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public Optional<String> getUnits() {
        return Optional.empty();
    }

    @Override
    public Optional<? extends Object> getDefaultValue() {
        return Optional.empty();
    }

    @Override
    public QName getQName() {
        return null;
    }

    @Override
    public SchemaPath getPath() {
        return null;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return null;
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getReference() {
        return Optional.empty();
    }

    @Override
    public Status getStatus() {
        return null;
    }
}
