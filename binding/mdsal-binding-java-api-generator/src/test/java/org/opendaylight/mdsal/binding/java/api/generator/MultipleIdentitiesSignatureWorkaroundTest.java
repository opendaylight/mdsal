/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;

/**
 module union-with-identityrefs {
    yang-version 1;
    namespace "urn:opendaylight:yang:union:identityrefs:test";
    prefix "unionidentityrefstest";

    description "test union with identityrefs";

    revision "2022-04-08";

    identity two-legged-animal;

    identity human {
        base two-legged-animal;
    }

    identity four-legged-animal;

    identity horse {
        base four-legged-animal;
    }

    identity unicorn {
        base horse;
    }

    typedef test-union {
        type union {
            type identityref {base two-legged-animal};
            type identityref {base four-legged-animal};
        }
    }
 }
 */
public class MultipleIdentitiesSignatureWorkaroundTest extends BaseCompilationTest {

    // defined as the base identity in YANG
    interface TwoLeggedAnimal extends BaseIdentity {}

    interface Human extends TwoLeggedAnimal {}

    // defined as the base identity in YANG
    interface FourLeggedAnimal extends BaseIdentity {}

    interface Horse extends FourLeggedAnimal {}
    interface Unicorn extends Horse {}

    class TestUnionImpl {
        Class<? extends TwoLeggedAnimal> _twoLeggedAnimal;
        Class<? extends FourLeggedAnimal> _fourLeggedAnimal;

        TestUnionImpl (Class<? extends BaseIdentity> identity) {
            if (isValidIdentity(identity, TwoLeggedAnimal.class)) {
                _twoLeggedAnimal = (Class<? extends TwoLeggedAnimal>) identity;
                _fourLeggedAnimal = null;
            } else if (isValidIdentity(identity, FourLeggedAnimal.class)) {
                _fourLeggedAnimal = (Class<? extends FourLeggedAnimal>) identity;
                _twoLeggedAnimal = null;
            } else {
                throw new ClassCastException("Missing identityref for identity " + identity.getName());
            }
        }
    }

    /**
     *
     * Something like this needs to be generated in the union class, so it can be used in the constructor.
     */
    private static boolean isValidIdentity(Class<? extends BaseIdentity> provided,
        Class<? extends BaseIdentity> expected) {
        try {
            provided.asSubclass(expected);
        } catch (Exception ex){
            return false;
        }
        return true;
    }

    @Test
    public void testErasures() {
        TestUnionImpl union2Legs = new TestUnionImpl(Human.class);
        assertEquals(Human.class, union2Legs._twoLeggedAnimal);
        assertNull(union2Legs._fourLeggedAnimal);

        TestUnionImpl union4Legs = new TestUnionImpl(Horse.class);
        assertEquals(Horse.class, union4Legs._fourLeggedAnimal);
        assertNull(union4Legs._twoLeggedAnimal);

        TestUnionImpl union4LegsExtended = new TestUnionImpl(Unicorn.class);
        assertEquals(Unicorn.class, union4LegsExtended._fourLeggedAnimal);
        assertNull(union4LegsExtended._twoLeggedAnimal);
    }
}
