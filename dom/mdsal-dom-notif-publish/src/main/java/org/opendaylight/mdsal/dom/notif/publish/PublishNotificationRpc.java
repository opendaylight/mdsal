/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.notif.publish;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import javax.xml.transform.dom.DOMSource;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationPublishService;
import org.opendaylight.mdsal.dom.api.DOMRpcException;
import org.opendaylight.mdsal.dom.api.DOMRpcIdentifier;
import org.opendaylight.mdsal.dom.api.DOMRpcImplementation;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser.DomToNormalizedNodeParserFactory;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

final class PublishNotificationRpc implements DOMRpcImplementation {
    private static final QName RPC_QNAME = QName.cachedReference(
        QName.create("urn:org:opendaylight:mdsal:notif-publish", "2015-11-05", "publish-notification"));
    private static final NodeIdentifier IDENTIFIER = NodeIdentifier.create(QName.create(RPC_QNAME, "identifier"));
    private static final NodeIdentifier VALUE = NodeIdentifier.create(QName.create(RPC_QNAME, "value"));
    private static final DOMRpcResult SUCCESS = new DOMRpcResult() {
        @Override
        public NormalizedNode<?, ?> getResult() {
            return null;
        }

        @Override
        public Collection<RpcError> getErrors() {
            return Collections.emptyList();
        }
    };
    private static final Function<Object, DOMRpcResult> SUCCESS_MAPPER = new Function<Object, DOMRpcResult>() {
        @Override
        public DOMRpcResult apply(final Object input) {
            return SUCCESS;
        }
    };

    static final DOMRpcIdentifier RPC = DOMRpcIdentifier.create(SchemaPath.create(true, RPC_QNAME));

    private final DOMNotificationPublishService notifs;
    private final SchemaContext schemaContext;

    PublishNotificationRpc(final DOMNotificationPublishService notifs, final SchemaContext schemaContext) {
        this.notifs = Preconditions.checkNotNull(notifs);
        this.schemaContext = Preconditions.checkNotNull(schemaContext);
    }

    private Entry<SchemaPath, ContainerSchemaNode> getSchema(final String identifier) {
        // FIXME: decode
        throw new UnsupportedOperationException();
    }

    private static NormalizedNode<?, ?> getChild(final NormalizedNode<?, ?> parent, final NodeIdentifier child) {
        final Optional<NormalizedNode<?, ?>> maybeNode = NormalizedNodes.getDirectChild(parent, child);
        Preconditions.checkArgument(maybeNode.isPresent(), "Failed to find child %s", child);

        return maybeNode.get();
    }

    @Override
    public CheckedFuture<DOMRpcResult, DOMRpcException> invokeRpc(final DOMRpcIdentifier rpc, final NormalizedNode<?, ?> input) {
        Verify.verify(RPC.equals(rpc), "Unexpected RPC %s", rpc);

        final NormalizedNode<?, ?> identifierChild = getChild(input, IDENTIFIER);
        Preconditions.checkArgument(identifierChild instanceof LeafNode<?>, "Identifier %s is not a leaf", identifierChild);
        final Object identifier = ((LeafNode<?>)identifierChild).getValue();
        Preconditions.checkArgument(identifier instanceof String, "Identifier %s is not a string", identifier);
        final Entry<SchemaPath, ContainerSchemaNode> schema = getSchema((String) identifier);

        final NormalizedNode<?, ?> valueChild = getChild(input, VALUE);
        Preconditions.checkArgument(valueChild instanceof AnyXmlNode, "Value %s is not an anyxml", valueChild);
        final DOMSource source = ((AnyXmlNode)valueChild).getValue();

        final DomToNormalizedNodeParserFactory factory = DomToNormalizedNodeParserFactory.getInstance(
            XmlUtils.DEFAULT_XML_CODEC_PROVIDER, schemaContext);

        final ContainerNode body = factory.getContainerNodeParser().parse(
            Collections.singleton((Element)source.getNode()), schema.getValue());

        final ListenableFuture<?> f = notifs.offerNotification(new DOMNotification() {
            @Override
            public SchemaPath getType() {
                return schema.getKey();
            }

            @Override
            public ContainerNode getBody() {
                return body;
            }
        });

        return Futures.makeChecked(Futures.transform(f, SUCCESS_MAPPER), PublishNotificationFailedException.MAPPER);
    }
}
