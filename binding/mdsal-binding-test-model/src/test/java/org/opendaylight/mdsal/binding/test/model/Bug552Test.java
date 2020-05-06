/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.test.model;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.bug1234.rev200427.Bug1234Data;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonWriterFactory;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;


public class Bug552Test {
    @Test
    public void bug1234test() throws IOException {
        final SchemaContext schemaContext = YangParserTestUtils.parseYangResource("/yang/bug552.yang");
        final Writer writer = new StringWriter();
        @NonNull final QName rpc = QName.create("bug552", "ref_test", Revision.of("2020-04-27"));
        @NonNull final QName output = QName.create("bug552", "output", Revision.of("2020-04-27"));
        final NormalizedNodeStreamWriter jsonWriter = JSONNormalizedNodeStreamWriter.createNestedWriter(
                JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext),
                SchemaPath.create(true,rpc, output), null, JsonWriterFactory.createJsonWriter(writer, 2));
        jsonWriter.startLeafNode(YangInstanceIdentifier.NodeIdentifier
                .create(QName.create("bug552", "outputref", Revision.of("2020-04-27"))));
        jsonWriter.scalarValue(Bug1234Data.OutputA.DownTest);
    }
}
