/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.singleton.common.spi.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.LinkedList;
import java.util.List;
import org.opendaylight.yangtools.concepts.Path;

/**
 * Test helper class. {@link Path} for testing only
 */
public class TestInstanceIdentifier implements Path<TestInstanceIdentifier> {

    static final TestInstanceIdentifier EMPTY_INSTANCE = new TestInstanceIdentifier(ImmutableList.of());

    private final ImmutableList<String> path;

    /**
     * {@link TestInstanceIdentifier} constructor
     *
     * @param path - path
     */
    public TestInstanceIdentifier(final Iterable<? extends TestInstanceIdentifier> path) {
        Preconditions.checkArgument(path != null);
        final List<String> tiis = new LinkedList<>();
        for (final TestInstanceIdentifier t : path) {
            tiis.add(t.toString());
        }
        this.path = ImmutableList.copyOf(tiis);
    }

    TestInstanceIdentifier(final String path) {
        this.path = ImmutableList.of(path);
    }

    @Override
    public boolean contains(final TestInstanceIdentifier other) {
        return path.contains(other);
    }

}
