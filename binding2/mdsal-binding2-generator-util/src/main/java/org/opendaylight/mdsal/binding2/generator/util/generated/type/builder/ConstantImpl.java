/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding2.generator.util.generated.type.builder;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding2.model.api.Constant;
import org.opendaylight.mdsal.binding2.model.api.Type;

@Beta
final class ConstantImpl implements Constant {

    private final Type definingType;
    private final Type type;
    private final String name;
    private final Object value;

    public ConstantImpl(final Type definingType, final Type type, final String name, final Object value) {
        super();
        this.definingType = definingType;
        this.type = type;
        this.name = name;
        this.value = value;
    }


    @Override
    public Type getDefiningType() {
        return definingType;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String toFormattedString() {
        //TODO implement
        return null;
    }

    //TODO implement hashCode(), equals(), toString()
}
