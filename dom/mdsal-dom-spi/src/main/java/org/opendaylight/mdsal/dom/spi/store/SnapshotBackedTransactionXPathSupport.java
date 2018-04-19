/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.spi.store;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.BiMap;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.CheckedValue;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeSnapshot;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathResult;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathSchemaContext;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathSchemaContextFactory;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * XPath evaluation support for {@link SnapshotBackedTransaction}s.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
@ThreadSafe
public final class SnapshotBackedTransactionXPathSupport {
    private static final ConcurrentMap<XPathSchemaContextFactory, SnapshotBackedTransactionXPathSupport>
        CACHED_SUPPORTS = new MapMaker().weakKeys().makeMap();

    // Identity-mapped cache of XPathSchemaContexts
    private final ConcurrentMap<SchemaContext, XPathSchemaContext> cachedContexts = new MapMaker().weakKeys().makeMap();
    private final XPathSchemaContextFactory xpathContextFactory;

    private SnapshotBackedTransactionXPathSupport(final XPathSchemaContextFactory xpathContextFactory) {
        this.xpathContextFactory = requireNonNull(xpathContextFactory);
    }

    /**
     * Create a {@link SnapshotBackedTransactionXPathSupport} backed by a particular {@link XPathSchemaContextFactory}.
     *
     * @param xpathContextFactory Backing {@link XPathSchemaContextFactory}
     * @return A SnapshotBackedTransactionXPathSupport
     */
    public static SnapshotBackedTransactionXPathSupport forXPathContextFactory(
            final XPathSchemaContextFactory xpathContextFactory) {
        return CACHED_SUPPORTS.computeIfAbsent(xpathContextFactory, SnapshotBackedTransactionXPathSupport::new);
    }

    public XPathSchemaContext getXPathContext(final SchemaContext schemaContext) {
        return cachedContexts.computeIfAbsent(schemaContext, xpathContextFactory::createContext);
    }

    /**
     * Evaluate an XPath query in the context of a transaction on specified path.
     *
     * @param transaction Transaction in which to evaluate the query
     * @param path Path of the tree node in which to evaluate the query
     * @param xpath XPath conforming to RFC7950
     * @param prefixMapping Prefix mapping to be used for lookups
     * @return Result of evaluation
     * @throws IllegalArgumentException if the transaction does not have a read aspect
     * @throws NullPointerException if any of the arguments is null
     */
    public CheckedValue<Optional<? extends XPathResult<?>>, XPathException> evaluate(
            final SnapshotBackedTransaction transaction, final YangInstanceIdentifier path, final String xpath,
            final BiMap<String, QNameModule> prefixMapping) {
        checkArgument(transaction instanceof DOMStoreReadTransaction, "Transaction %s does not have a read aspect",
            transaction);

        final DataTreeSnapshot snapshot = transaction.getSnapshot().orElseThrow(
            () -> new IllegalStateException("Transaction " + transaction.getIdentifier() + " is not open"));
        final SchemaContext schemaContext = snapshot.getSchemaContext();

        final XPathSchemaContext context = getXPathContext(schemaContext);

        final Optional<NormalizedNode<?, ?>> optData = snapshot.readNode(path);
        if (!optData.isPresent()) {
            return CheckedValue.ofValue(Optional.empty());
        }

        // Acquire DataSchemaNode for specified path
        final Optional<DataSchemaNode> optSchema = DataSchemaContextTree.from(schemaContext).findChild(path)
                .map(DataSchemaContextNode::getDataSchemaNode);
        if (!optSchema.isPresent()) {
            return CheckedValue.ofException(new XPathException("Failed to find schema for " + path));
        }

        final Optional<? extends XPathResult<?>> result;
        try {
            return CheckedValue.ofValue(context.compileExpression(optSchema.get().getPath(),
                Maps.asConverter(prefixMapping), xpath).evaluate(context.createDocument(optData.get()), path));
        } catch (XPathExpressionException e) {
            return CheckedValue.ofException(e);
        }
    }
}
