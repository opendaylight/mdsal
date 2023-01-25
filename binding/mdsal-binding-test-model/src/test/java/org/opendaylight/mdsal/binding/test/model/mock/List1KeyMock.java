/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model.mock;

import com.google.common.base.MoreObjects;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.of.migration.test.model.rev150210.aug.grouping.List1;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;
import org.opendaylight.yangtools.yang.binding.Identifier;

/**
 * This class represents the key of {@link List1} class.
 *
 * @see List1
 *
 */
@Generated("mdsal-binding-generator")
public class List1KeyMock
        implements Identifier<List1Mock> {
    private static final long serialVersionUID = -3288230944589236942L;
    private final String _attrStr;


    /**
     * Constructs an instance.
     *
     * @param _attrStr the entity attrStr
     * @throws NullPointerException if any of the arguments are null
     */
    public List1KeyMock(@NonNull String _attrStr) {
        this._attrStr = CodeHelpers.requireKeyProp(_attrStr, "attrStr");
    }

    /**
     * Creates a copy from Source Object.
     *
     * @param source Source object
     */
    public List1KeyMock(List1KeyMock source) {
        this._attrStr = source._attrStr;
    }


    /**
     * Return attrStr, guaranteed to be non-null.
     *
     * @return {@code String} attrStr, guaranteed to be non-null.
     */
    public @NonNull String getAttrStr() {
        return _attrStr;
    }


    @Override
    public int hashCode() {
        return CodeHelpers.wrapperHashCode(_attrStr);
    }

    @Override
    public final boolean equals(Object obj) {
        return this == obj || obj instanceof List1KeyMock other
                && Objects.equals(_attrStr, other._attrStr);
    }

    @Override
    public String toString() {
        final var helper = MoreObjects.toStringHelper(
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.md.sal.of
                        .migration.test.model.rev150210.aug.grouping.List1Key.class);
        CodeHelpers.appendValue(helper, "attrStr", _attrStr);
        return helper.toString();
    }
}


