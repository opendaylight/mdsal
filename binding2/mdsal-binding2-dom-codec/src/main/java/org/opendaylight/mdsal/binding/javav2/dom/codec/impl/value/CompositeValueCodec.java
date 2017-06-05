/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.dom.codec.impl.value;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.concepts.Codec;

@Beta
final class CompositeValueCodec extends ValueTypeCodec {

    private final SchemaUnawareCodec bindingToSimpleType;

    @SuppressWarnings("rawtypes")
    private final Codec bindingToDom;

    CompositeValueCodec(final SchemaUnawareCodec extractor, @SuppressWarnings("rawtypes") final Codec delegate) {
        this.bindingToSimpleType = extractor;
        this.bindingToDom = delegate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object deserialize(Object input) {
        return bindingToSimpleType.deserialize(bindingToDom.deserialize(input));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object serialize(Object input) {
        return bindingToDom.serialize(bindingToSimpleType.serialize(input));
    }
}
