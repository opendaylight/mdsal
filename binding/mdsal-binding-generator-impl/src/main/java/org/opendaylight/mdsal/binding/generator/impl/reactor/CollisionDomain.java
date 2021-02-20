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
import org.opendaylight.yangtools.yang.common.AbstractQName;

final class CollisionDomain {
    abstract class Member {
        private final NamingStrategy currentStrategy = NamingStrategy.CLASSIC;
        private String currentClass;
        private String currentPackage;


    }

    private final class Primary extends Member {
        private final AbstractQName localName;

        private List<Secondary> secondaries = List.of();

        Primary(final AbstractQName localName) {
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
    private boolean solved;

    @NonNull Member addPrimary(final AbstractQName localName) {
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

    boolean findSolution() {
        if (solved) {
            // Already solved, nothing to do
            return false;
        }
        if (members.size() < 2) {
            // Zero or one member: no conflict possible
            solved = true;
            return false;
        }

        // FIXME: implement this

        return false;

//      * The first chunk is here: we walk all the modules and assign simple names (for use with classes) and package
//      * names (to host them). Since all members will generate at least one named Java entity -- a class or a method,
//      * we start off with assigning Java Identifier for use as the class name. We then assign package names, where
//      * the assigned simple name serves as the primary source of truth -- hence we are making sure we have patterns
//      * which hint at which container-like class is the source of the package name.
//
//
//      // First we need to process each module's children's descendants, so that 'augment' statements can freely
//      // reference their names.
//      for (ModuleGenerator module : children) {
//          for (Generator child : module) {
//              if (child instanceof AbstractCompositeGenerator<?>) {
//                  ((AbstractCompositeGenerator<?>) child).setDescendantSimpleNames();
//              }
//          }
//      }
//
//      // Now we can proceed with assigning all modules' direct children, as any augmentation references should be
//      // successfully resolved. As we complete each module's simple name assignment also trigger package name
//      // assignment
//      for (ModuleGenerator module : children) {
//          // TODO: can this create conflicts somewhere?
//          module.setAssignedName(module.preferredName());
//
//          // Real complexity is in the implementation method. It is expected to deal with the details of resolving
//          // naming conflicts at each tree level, so that each sibling has a unique name.
//          module.setChildSimpleNames();
//
//          // Once we have simple names, assign package names. Note that modules have their package names assigned
//          // at construction, as they are root invariants.
//          module.setChildPackageNames();
//      }
    }

    private void addMember(final Member member) {
        if (members.isEmpty()) {
            members = new ArrayList<>();
        }
        members.add(member);
    }
}
