/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model.mock;

import com.google.common.base.MoreObjects;
import java.lang.Class;
import java.lang.NullPointerException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import javax.annotation.processing.Generated;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.yang.extension.yang.ext.rev130709.$YangModuleInfoImpl;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 *
 * <p>
 * This class represents the following YANG schema fragment defined in module <b>opendaylight-of-migration-test-model</b>
 * <pre>
 * list list1 {
 *   key attr-str;
 *   leaf attr-str {
 *     type string;
 *   }
 *   list list1-2 {
 *     key attr-int;
 *     leaf attr-int {
 *       type int32;
 *     }
 *     leaf attr-str {
 *       type string;
 *     }
 *   }
 * }
 * </pre>
 * <p>To create instances of this class use {@link List1Builder}.
 * @see List1Builder
 * @see List1Key
 *
 */
@Generated("mdsal-binding-generator")
public interface List1Mock
        extends
        ChildOf<DataRoot>,
        Augmentable<List1Mock>,
        Identifiable<List1KeyMock>
{

    /**
     * YANG identifier of the statement represented by this class.
     */
    public static final @NonNull QName QNAME = $YangModuleInfoImpl.qnameOf("list1");

    @Override
    default Class<List1Mock> implementedInterface() {
        return List1Mock.class;
    }

    /**
     * Default implementation of {@link Object#hashCode()} contract for this interface.
     * Implementations of this interface are encouraged to defer to this method to get consistent hashing
     * results across all implementations.
     *
     * @param obj Object for which to generate hashCode() result.
     * @return Hash code value of data modeled by this interface.
     * @throws NullPointerException if {@code obj} is null
     */
    static int bindingHashCode(final @NonNull List1Mock obj) {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(obj.getAttrStr());
        result = prime * result + Objects.hashCode(obj.getList12());
        for (var augmentation : obj.augmentations().values()) {
            result += augmentation.hashCode();
        }
        return result;
    }

    /**
     * Default implementation of {@link Object#equals(Object)} contract for this interface.
     * Implementations of this interface are encouraged to defer to this method to get consistent equality
     * results across all implementations.
     *
     * @param thisObj Object acting as the receiver of equals invocation
     * @param obj Object acting as argument to equals invocation
     * @return True if thisObj and obj are considered equal
     * @throws NullPointerException if {@code thisObj} is null
     */
    static boolean bindingEquals(final @NonNull List1Mock thisObj, final Object obj) {
        if (thisObj == obj) {
            return true;
        }
        final List1Mock other = CodeHelpers.checkCast(List1Mock.class, obj);
        if (other == null) {
            return false;
        }
        if (!Objects.equals(thisObj.getAttrStr(), other.getAttrStr())) {
            return false;
        }
        if (!Objects.equals(thisObj.getList12(), other.getList12())) {
            return false;
        }
        return thisObj.augmentations().equals(other.augmentations());
    }

    /**
     * Default implementation of {@link Object#toString()} contract for this interface.
     * Implementations of this interface are encouraged to defer to this method to get consistent string
     * representations across all implementations.
     *
     * @param obj Object for which to generate toString() result.
     * @return {@link String} value of data modeled by this interface.
     * @throws NullPointerException if {@code obj} is null
     */
    static String bindingToString(final @NonNull List1Mock obj) {
        final var helper = MoreObjects.toStringHelper("List1");
        CodeHelpers.appendValue(helper, "attrStr", obj.getAttrStr());
        CodeHelpers.appendValue(helper, "list12", obj.getList12());
        CodeHelpers.appendAugmentations(helper, "augmentation", obj);
        return helper.toString();
    }

    @Override
    List1KeyMock key();

    /**
     * Return attrStr, or {@code null} if it is not present.
     *
     * @return {@code String} attrStr, or {@code null} if it is not present.
     *
     */
    String getAttrStr();

    /**
     * Return attrStr, guaranteed to be non-null.
     *
     * @return {@code String} attrStr, guaranteed to be non-null.
     * @throws NoSuchElementException if attrStr is not present
     *
     */
    default @NonNull String requireAttrStr() {
        return CodeHelpers.require(getAttrStr(), "attrstr");
    }

    /**
     * Return list12, or {@code null} if it is not present.
     *
     * @return {@code Map<List12Key, List12>} list12, or {@code null} if it is not present.
     *
     */
    @Nullable Map<List12KeyMock, List12Mock> getList12();

    /**
     * Return list12, or an empty list if it is not present.
     *
     * @return {@code Map<List12Key, List12>} list12, or an empty list if it is not present.
     *
     */
    default @NonNull Map<List12KeyMock, List12Mock> nonnullList12() {
        return CodeHelpers.nonnull(getList12());
    }

}

