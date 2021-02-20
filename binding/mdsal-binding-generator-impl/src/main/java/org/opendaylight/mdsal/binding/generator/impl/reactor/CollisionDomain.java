/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;

final class CollisionDomain {
    abstract class Member {

    }

    private final class Primary extends Member {
        private final String localName;

        private List<Secondary> secondaries = List.of();

        Primary(final String localName) {
            this.localName = requireNonNull(localName);
        }

        void addSecondary(final Secondary secondary) {
            if (secondaries.isEmpty()) {
                secondaries = new ArrayList<>();
            }
            secondaries.add(requireNonNull(secondary));
        }
    }

    private final class Secondary extends Member {
        private final Primary primary;
        private final String suffix;

        Secondary(final Primary primary, final String suffix) {
            this.primary = requireNonNull(primary);
            this.suffix = requireNonNull(suffix);
        }
    }

    private List<Member> members = List.of();

    @NonNull Member addPrimary(final String localName) {
        final Primary primary = new Primary(localName);
        addMember(primary);
        return primary;
    }

    @NonNull Member addSecondary(final Member primary, final String suffix) {
        verify(primary instanceof Primary, "Unexpected primary %s", primary);
        final Primary cast = (Primary) primary;
        final Secondary secondary = new Secondary(cast, suffix);
        cast.addSecondary(secondary);
        addMember(secondary);
        return secondary;
    }

    private void addMember(final Member member) {
        if (members.isEmpty()) {
            members = new ArrayList<>();
        }
        members.add(member);
    }
}
