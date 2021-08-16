/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.adapter.osgi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.osgi.framework.ServiceReference;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DictTest {
    @Mock
    private ServiceReference<?> ref;
    @Mock
    private BindingService service;

    private Dict dict;

    @Before
    public void before() {
        doReturn(new String[] { "foo", "bar", ServiceProperties.PREFIX + "xyzzy" }).when(ref).getPropertyKeys();
        doReturn("foo").when(ref).getProperty("foo");
        doReturn("bar").when(ref).getProperty("bar");

        dict = Dict.fromReference(ref, service);
    }

    @Test
    public void testSize() {
        assertEquals(3, dict.size());
    }

    @Test
    public void testIsEmpty() {
        assertFalse(dict.isEmpty());
    }

    @Test
    public void testKeys() {
        assertEquals(Set.of("foo", "bar", AbstractAdaptedService.DELEGATE),
            ImmutableSet.copyOf(dict.keys().asIterator()));
    }

    @Test
    public void testElements() {
        assertEquals(Set.of("foo", "bar", service),
            ImmutableSet.copyOf(dict.elements().asIterator()));
    }

    @Test
    public void testGet() {
        assertNull(dict.get("xyzzy"));
        assertEquals("foo", dict.get("foo"));
        assertEquals("bar", dict.get("bar"));
        assertSame(service, dict.get(AbstractAdaptedService.DELEGATE));
    }
}
