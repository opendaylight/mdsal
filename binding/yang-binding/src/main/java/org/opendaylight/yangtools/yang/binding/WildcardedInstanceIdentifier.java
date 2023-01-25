/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.base.Preconditions;
import java.io.Serial;
import org.eclipse.jdt.annotation.NonNull;

public class WildcardedInstanceIdentifier<T extends DataObject> extends InstanceIdentifier<T> {

    //FIXME what value to assign
    @Serial
    private static final long serialVersionUID = 2L;

    @Override
    public  boolean isWildcarded() {
        return true;
    }

    WildcardedInstanceIdentifier(Class<T> type, Iterable<PathArgument> pathArguments,
            boolean wildcarded, int hash) {
        super(type, pathArguments, wildcarded, hash);
    }

    public final WildcardedInstanceIdentifierBuilder<T> builder() {
        return new WildcardedInstanceIdentifierBuilder<>(Item.of(getTargetType()), pathArguments, hashCode());
    }

    @SuppressWarnings("unchecked")
    static <N extends DataObject> @NonNull WildcardedInstanceIdentifier<N> trustedCreate(final PathArgument arg,
            final Iterable<PathArgument> pathArguments, final int hash, boolean wildcarded) {
        Preconditions.checkArgument(wildcarded, "Must be wildcarded.");

        return new WildcardedInstanceIdentifier<>((Class<N>) arg.getType(), pathArguments, true, hash);
    }
}
