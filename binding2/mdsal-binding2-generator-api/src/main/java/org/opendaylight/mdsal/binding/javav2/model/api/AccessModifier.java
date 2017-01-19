/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.model.api;

import com.google.common.annotations.Beta;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

/**
 *
 * Enum definition which provides access modifiers that are described
 * in Java programming language
 *
 */
@Beta
public enum AccessModifier {
    DEFAULT {
        @Override
        boolean matchesMember(Member member) {
            return member.getModifiers() == 0; //no modifiers, package-private in fact
        }
    },

    PRIVATE {
        @Override
        boolean matchesMember(Member member) {
            return Modifier.isPrivate(member.getModifiers());
        }
    },

    PUBLIC {
        @Override
        boolean matchesMember(Member member) {
            return Modifier.isPublic(member.getModifiers());
        }
    },

    PROTECTED {
        @Override
        boolean matchesMember(Member member) {
            return Modifier.isProtected(member.getModifiers());
        }
    };

    /**
     *
     * @param member reflects identifying information about a single member (a field or a method) or a
     *               constructor.
     * @return Return {@code true} if the integer argument includes the
     *         {@code public} modifier, {@code false} otherwise.
     */
    abstract boolean matchesMember(Member member);

}
