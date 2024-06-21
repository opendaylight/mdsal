/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.api.query;

import org.mockito.Mock;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.Foo;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.first.grp.System;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.first.grp.SystemKey;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.second.grp.Alarms;
import org.opendaylight.yang.gen.v1.mdsal.query.norev.third.grp.AffectedUsers;
import org.opendaylight.yang.gen.v1.mdsal426.norev.BooleanCont;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns._default.value.test.norev.DecimalContainer;
import org.opendaylight.yangtools.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Uint64;

@SuppressWarnings("exports")
public class QueryBuilderExamples {
    @Mock
    private QueryFactory factory;

    /*
     * Return all of /foo. Equivalent to a read() of the same identifier.
     */
    public QueryExpression<Foo> selectFoo() {
        return factory
                .querySubtree(InstanceIdentifier.create(Foo.class))
                .build();
    }

    /*
     * Read all of /foo/system[name="some"]. Equivalent to a read() of the same identifier.
     */
    public QueryExpression<System> selectFooSystemSome() {
        return factory
                .querySubtree(InstanceIdentifier.create(Foo.class).child(System.class, new SystemKey("some")))
                .build();
    }

    /*
     * Read all entries in /foo/system. Equivalent to a read(/foo).get().nonnullSystem().
     */
    public QueryExpression<System> selectFooSystem() {
        return factory
                .querySubtree(InstanceIdentifier.create(Foo.class))
                .extractChild(System.class)
                .build();
    }

    /*
     * Read all entries in /foo/system, which have 'alias' set to 'some'.
     */
    public QueryExpression<System> selectFooSystemAliasSome() {
        return factory
                .querySubtree(InstanceIdentifier.create(Foo.class))
                .extractChild(System.class)
                .matching()
                    .leaf(System::getAlias)
                    .valueEquals("some")
                .build();
    }

    /*
     * Read all entries in /foo/system, which have 'alias' containing the string 'needle'.
     */
    public QueryExpression<System> selectFooSystemAliasWithNeedle() {
        return factory
                .querySubtree(InstanceIdentifier.create(Foo.class))
                .extractChild(System.class)
                .matching()
                    .leaf(System::getAlias)
                    .contains("needle")
                .build();
    }

    /*
     * Read all entries in /foo/system/alarms, which have 'critical' leaf present.
     */
    public QueryExpression<Alarms> selectFooSystemAlarmsCritical() {
        return factory
                .querySubtree(InstanceIdentifier.create(Foo.class))
                .extractChild(System.class)
                .extractChild(Alarms.class)
                .matching()
                    .leaf(Alarms::getCritical)
                    .nonNull()
                .build();
    }

    /*
     * Read all entries in /foo/system/alarms, which have 'critical' leaf present and have an entry in 'affected-users'
     * with 'uid' larger than 10.
     *
     * Note this is the same expression as selectFooSystemCriticalUid(), but selects Alarms objects.
     */
    public QueryExpression<Alarms> selectFooSystemAlarmsCriticalUid() {
        return factory
                .querySubtree(InstanceIdentifier.create(Foo.class))
                .extractChild(System.class)
                .extractChild(Alarms.class)
                .matching()
                    .leaf(Alarms::getCritical)
                    .nonNull()
                .and()
                    .childObject(AffectedUsers.class)
                    .leaf(AffectedUsers::getUid)
                    .greaterThan(Uint64.TEN)
                .build();
    }


    /*
     * Read all entries in /foo/system, which have 'critical' leaf present and have an entry in 'affected-users'
     * with 'uid' larger than 10.
     *
     * Note this is the same expression as selectFooSystemAlarmsCriticalUid(), but selects System objects.
     */
    public QueryExpression<System> selectFooSystemCriticalUid() {
        return factory
                .querySubtree(InstanceIdentifier.create(Foo.class))
                .extractChild(System.class)
                .matching()
                    .childObject(Alarms.class)
                    .leaf(Alarms::getCritical)
                    .nonNull()
                .and()
                    .childObject(Alarms.class)
                    .childObject(AffectedUsers.class)
                    .leaf(AffectedUsers::getUid)
                    .greaterThan(Uint64.TEN)
                .build();
    }

    public QueryExpression<BooleanCont> selectBoolean() {
        return factory
                .querySubtree(InstanceIdentifier.create(BooleanCont.class))
                .matching()
                    .leaf(BooleanCont::getIsFoo)
                    .valueEquals(true)
                .build();
    }

    public QueryExpression<DecimalContainer> selectDecimal64() {
        return factory
                .querySubtree(InstanceIdentifier.create(DecimalContainer.class))
                .matching()
                    .leaf(DecimalContainer::getDecimalLeaf5)
                    .valueEquals(Decimal64.valueOf("1.0"))
                .build();
    }
}
