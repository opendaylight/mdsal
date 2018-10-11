/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.javav2.runtime.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.opendaylight.mdsal.binding.javav2.runtime.reflection.AugmentationFieldGetter.getGetter;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.mdsal.binding.javav2.spec.structural.Augmentation;
import org.opendaylight.mdsal.binding.javav2.spec.structural.AugmentationHolder;

public class AugmentationFieldGetterTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void getGetterTest() throws Exception {
        assertNotNull(getGetter(AugmentationHolder.class));
        assertTrue(getGetter(AugmentationHolder.class).getAugmentations(mock(AugmentationHolder.class)).isEmpty());
        assertTrue(getGetter(Object.class).getAugmentations(null).isEmpty());
        assertTrue(getGetter(TestAugmentationWrongTypeClass.class).getAugmentations(null).isEmpty());

        final AugmentationFieldGetter augmentationFieldGetter = getGetter(TestAugmentationClass.class);
        final Augmentation augmentation = mock(Augmentation.class);
        final TestAugmentationClass testAugmentationClass = new TestAugmentationClass();

        testAugmentationClass.addAugmentation(augmentation, augmentation);
        assertNotNull(augmentationFieldGetter.getAugmentations(testAugmentationClass));
        assertEquals(1, augmentationFieldGetter.getAugmentations(testAugmentationClass).size());
    }

    @Test(expected = IllegalStateException.class)
    public void getWrongGetterTest() throws Exception {
        final AugmentationFieldGetter augmentationFieldGetter = getGetter(TestAugmentationClass.class);
        augmentationFieldGetter.getAugmentations("");
        fail("Expected IllegalStateException");
    }

    @Test
    public void getNoGetterTest() throws Exception {
        assertTrue(getGetter(Object.class).getAugmentations(null).isEmpty());
    }

    private final class TestAugmentationClass {
        @SuppressWarnings("rawtypes")
        private final Map augmentation = new HashMap();

        @SuppressWarnings({ "rawtypes", "unchecked" })
        void addAugmentation(final Augmentation key, final Augmentation value) {
            this.augmentation.put(key, value);
        }
    }

    private final class TestAugmentationWrongTypeClass {
        @SuppressWarnings("unused")
        private String augmentation;
    }
}
