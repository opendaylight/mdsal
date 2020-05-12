/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.backup.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.opendaylight.mdsal.backup.utils.exceptions.DTCSerializationException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;
import org.opendaylight.yangtools.yang.data.codec.binfmt.DataTreeCandidateInputOutput;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeDataInput;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeDataOutput;
import org.opendaylight.yangtools.yang.data.codec.binfmt.NormalizedNodeStreamVersion;
import org.opendaylight.yangtools.yang.data.impl.schema.ReusableImmutableNormalizedNodeStreamWriter;

public final class DTCSerializer {

    private DTCSerializer() {

    }

    public static byte[] serializeDTC(final DataTreeCandidate candidate) throws UnsupportedOperationException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             NormalizedNodeDataOutput dataOutput = NormalizedNodeStreamVersion.current()
                 .newDataOutput(new DataOutputStream(byteArrayOutputStream))) {
            DataTreeCandidateInputOutput.writeDataTreeCandidate(dataOutput, candidate);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new DTCSerializationException("DataTreeCandidate serialization failed", e);
        }
    }

    public static DataTreeCandidate deserializeDTC(final byte[] candidateData) throws UnsupportedOperationException {
        try (DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(candidateData))) {
            NormalizedNodeDataInput nodeDataInput = NormalizedNodeDataInput.newDataInput(dataInputStream);
            DataTreeCandidate dataTreeCandidate =
                DataTreeCandidateInputOutput.readDataTreeCandidate(nodeDataInput,
                    ReusableImmutableNormalizedNodeStreamWriter.create());
            return dataTreeCandidate;
        } catch (IOException e) {
            throw new DTCSerializationException("DataTreeCandidate deserialization failed", e);
        }
    }
}
