/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.query.binding.adapter;

import java.util.regex.Pattern;
import org.opendaylight.mdsal.binding.api.query.MatchBuilderValueString;
import org.opendaylight.mdsal.binding.api.query.ValueMatch;
import org.opendaylight.mdsal.binding.spi.query.LambdaDecoder.LambdaTarget;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

final class DefaultMatchBuilderValueString<R extends DataObject, O extends DataObject, T extends DataObject>
        extends AbstractMatchBuilderValue<R, O, T, String> implements MatchBuilderValueString<T> {

    DefaultMatchBuilderValueString(final InstanceIdentifier<R> rootPath, final InstanceIdentifier<O> childPath,
            final InstanceIdentifier<T> targetPath, final LambdaTarget targetLeaf) {
        super(rootPath, childPath, targetPath, targetLeaf);
    }

    @Override
    public ValueMatch<T> startsWith(final String str) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ValueMatch<T> endsWith(final String str) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ValueMatch<T> contains(final String str) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ValueMatch<T> matchesPattern(final Pattern pattern) {
        // TODO Auto-generated method stub
        return null;
    }
}
