/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Test Model Provider designated to load test resources and provide Schema Context
 * for testing of TypeProviderImpl
 */
public final class TypeProviderModel {

    public static final String TEST_TYPE_PROVIDER_MODULE_NAME = "test-type-provider";

    private static final String BASE_YANG_TYPES_PATH = "/base-yang-types.yang";
    private static final String TEST_TYPE_PROVIDER_PATH = "/"+TEST_TYPE_PROVIDER_MODULE_NAME+".yang";
    private static final String TEST_TYPE_PROVIDER_B_PATH = "/test-type-provider-b.yang";

    private static File getFile(final String resourceName) throws Exception {
        return new File(TypeProviderModel.class.getResource(resourceName).toURI());
    }

    private static List<File> provideTestModelStreams() throws Exception {
        final List<File> arrayList = new ArrayList<>();

        arrayList.add(getFile(BASE_YANG_TYPES_PATH));
        arrayList.add(getFile(TEST_TYPE_PROVIDER_PATH));
        arrayList.add(getFile(TEST_TYPE_PROVIDER_B_PATH));
        return arrayList;
    }

    public static SchemaContext createTestContext() throws Exception {
        return YangParserTestUtils.parseYangFiles(provideTestModelStreams());
    }
}
