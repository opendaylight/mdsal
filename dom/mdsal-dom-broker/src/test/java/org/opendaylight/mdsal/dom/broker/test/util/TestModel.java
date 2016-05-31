/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.broker.test.util;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;

public class TestModel {

    public static final QName TEST_QNAME =
            QName.create("urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test", "2014-03-13", "test");
    public static final QName TEST2_QNAME =
            QName.create("urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test", "2014-03-13", "test2");
    public static final QName OUTER_LIST_QNAME = QName.create(TEST_QNAME, "outer-list");
    public static final QName INNER_LIST_QNAME = QName.create(TEST_QNAME, "inner-list");
    public static final QName OUTER_CHOICE_QNAME = QName.create(TEST_QNAME, "outer-choice");
    public static final QName ID_QNAME = QName.create(TEST_QNAME, "id");
    public static final QName NAME_QNAME = QName.create(TEST_QNAME, "name");
    public static final QName VALUE_QNAME = QName.create(TEST_QNAME, "value");
    public static final QName INNER_CONTAINER = QName.create(TEST_QNAME, "inner-container");
    public static final QName ANOTHER_SHARD_CONTAINER = QName.create(TEST_QNAME, "another-shard");
    public static final QName NEW_SHARD_LIST = QName.create(TEST_QNAME, "new-shard-list");
    public static final QName SHARDED_VALUE_1 = QName.create(TEST_QNAME, "sharded-value-1");
    public static final QName SHARDED_VALUE_2 = QName.create(TEST_QNAME, "sharded-value-2");
    public static final QName ANOTHER_SHARD_VALUE = QName.create(TEST_QNAME, "another-shard-value");
    public static final QName TWO_QNAME = QName.create(TEST_QNAME,"two");
    public static final QName THREE_QNAME = QName.create(TEST_QNAME,"three");
    public static final YangInstanceIdentifier TEST_PATH = YangInstanceIdentifier.of(TEST_QNAME);
    public static final YangInstanceIdentifier TEST2_PATH = YangInstanceIdentifier.of(TEST2_QNAME);
    public static final YangInstanceIdentifier OUTER_LIST_PATH =
            YangInstanceIdentifier.builder(TEST_PATH).node(OUTER_LIST_QNAME).build();
    public static final YangInstanceIdentifier INNER_LIST_PATH =
            YangInstanceIdentifier.builder(OUTER_LIST_PATH).node(INNER_LIST_QNAME).build();
    public static final YangInstanceIdentifier INNER_CONTAINER_PATH =
            YangInstanceIdentifier.builder(TEST_PATH).node(INNER_CONTAINER).build();
    public static final YangInstanceIdentifier ANOTHER_SHARD_PATH =
            YangInstanceIdentifier.builder(INNER_CONTAINER_PATH).node(ANOTHER_SHARD_CONTAINER).build();
    public static final YangInstanceIdentifier NEW_SHARD_LIST_PATH =
            YangInstanceIdentifier.builder(ANOTHER_SHARD_PATH).node(NEW_SHARD_LIST).build();
    private static final String DATASTORE_TEST_YANG = "/odl-datastore-test.yang";

    public static SchemaContext createTestContext() throws ReactorException {
        return parseYangStreams(Collections.singletonList(getInputStream()));
    }

    private static InputStream getInputStream() {
        return TestModel.class.getResourceAsStream(DATASTORE_TEST_YANG);
    }

    private static SchemaContext parseYangStreams(List<InputStream> streams)
            throws SourceException, ReactorException {

        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();
        return reactor.buildEffective(streams);
    }
}
