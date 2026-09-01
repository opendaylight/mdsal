/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.store.inmemory;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

final class TestModel {
    public static final QName TEST_QNAME =
            QName.create("urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test", "2014-03-13", "test");
    public static final QName OUTER_LIST_QNAME = QName.create(TEST_QNAME, "outer-list");
    public static final QName INNER_LIST_QNAME = QName.create(TEST_QNAME, "inner-list");
    public static final QName OUTER_CHOICE_QNAME = QName.create(TEST_QNAME, "outer-choice");
    public static final QName ID_QNAME = QName.create(TEST_QNAME, "id");
    public static final QName NAME_QNAME = QName.create(TEST_QNAME, "name");
    public static final QName VALUE_QNAME = QName.create(TEST_QNAME, "value");
    public static final QName TWO_QNAME = QName.create(TEST_QNAME, "two");
    public static final QName THREE_QNAME = QName.create(TEST_QNAME, "three");
    public static final YangInstanceIdentifier TEST_PATH = YangInstanceIdentifier.of(TEST_QNAME);
    public static final YangInstanceIdentifier OUTER_LIST_PATH =
            YangInstanceIdentifier.builder(TEST_PATH).node(OUTER_LIST_QNAME).build();

    public static final QName MANDATORY_DATA_TEST_QNAME =
            QName.create("urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test",
                    "2014-03-13",
                    "mandatory-data-test");
    public static final QName OPTIONAL_QNAME = QName.create(MANDATORY_DATA_TEST_QNAME, "optional-data");
    public static final QName MANDATORY_QNAME = QName.create(MANDATORY_DATA_TEST_QNAME, "mandatory-data");
    public static final YangInstanceIdentifier MANDATORY_DATA_TEST_PATH =
            YangInstanceIdentifier.of(MANDATORY_DATA_TEST_QNAME);

    private TestModel() {
        // Hidden on purpose
    }

    static EffectiveModelContext createTestContext() {
        return YangParserTestUtils.parseYang("""
            module odl-datastore-test {
              yang-version 1;
              namespace "urn:opendaylight:params:xml:ns:yang:controller:md:sal:dom:store:test";
              prefix "store-test";

              revision "2014-03-13" {
                description "Initial revision.";
              }

              container test {
                presence "presence container";
                list outer-list {
                  key id;
                  leaf id {
                    type uint16;
                  }
                  choice outer-choice {
                    case one {
                      leaf one {
                        type string;
                      }
                    }
                    case two-three {
                      leaf two {
                        type string;
                      }
                      leaf three {
                        type string;
                      }
                    }
                  }
                  list inner-list {
                    ordered-by user;
                    key name;
                    leaf name {
                      type string;
                    }
                    leaf value {
                      type string;
                    }
                  }
                }
              }

              container mandatory-data-test {
                  presence "needs to be present when empty";

                  leaf optional-data {
                      type string;
                  }
                  leaf mandatory-data {
                      type string;
                      mandatory true;
                  }
             }
            }""");
    }
}
