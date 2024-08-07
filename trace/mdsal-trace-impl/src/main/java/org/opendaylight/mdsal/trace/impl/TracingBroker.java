/*
 * Copyright (c) 2016 Red Hat, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.trace.impl;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeChangeListener;
import org.opendaylight.mdsal.dom.api.DOMDataTreeIdentifier;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMDataTreeWriteTransaction;
import org.opendaylight.mdsal.dom.api.DOMTransactionChain;
import org.opendaylight.mdsal.trace.api.TracingDOMDataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mdsaltrace.rev160908.Config;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTree;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("checkstyle:JavadocStyle")
//...because otherwise it whines about the elements in the @code block even though it's completely valid Javadoc
/**
 * TracingBroker logs "write" operations and listener registrations to the md-sal. It logs the instance identifier path,
 * the objects themselves, as well as the stack trace of the call invoking the registration or write operation.
 * It works by operating as a "bump on the stack" between the application and actual DataBroker, intercepting write
 * and registration calls and writing to the log.
 *
 * <p>In addition, it (optionally) can also keep track of the stack trace of all new transaction allocations
 * (including TransactionChains, and transactions created in turn from them), in order to detect and report leaks
 * from transactions which were not closed.
 *
 * <h1>Wiring:</h1>
 * TracingBroker is designed to be easy to use. In fact, for bundles using Blueprint to inject their DataBroker
 * TracingBroker can be used without modifying your code at all in two simple steps:
 * <ol>
 * <li>
 * Simply add the dependency "mdsaltrace-features" to
 * your Karaf pom:
 * <pre>
 * {@code
 *  <dependency>
 *    <groupId>org.opendaylight.controller</groupId>
 *    <artifactId>features-mdsal-trace</artifactId>
 *    <version>1.7.0-SNAPSHOT</version>
 *    <classifier>features</classifier>
 *    <type>xml</type>
 *    <scope>runtime</scope>
 *  </dependency>
 * }
 * </pre>
 * </li>
 * <li>
 * Then just "feature:install odl-mdsal-trace" before you install your "real" feature(s) and you're done.
 * Beware that with Karaf 4 due to <a href="https://bugs.opendaylight.org/show_bug.cgi?id=9068">Bug 9068</a>
 * you'll probably have to use feature:install's --no-auto-refresh flag when installing your "real" feature.
 * </li>
 * </ol>
 * This works because the mdsaltrace-impl bundle registers its service implementing DOMDataBroker with a higher
 * rank than sal-binding-broker. As such, any OSGi service lookup for DataBroker will receive the TracingBroker.
 * <p> </p>
 * <h1>Avoiding log bloat:</h1>
 * TracingBroker can be configured to only print registrations or write ops pertaining to certain subtrees of the
 * md-sal. This can be done in the code via the methods of this class or via a config file. TracingBroker uses a more
 * convenient but non-standard representation of the instance identifiers. Each instance identifier segment's
 * class.getSimpleName() is used separated by a '/'.
 * <p> </p>
 * <h1>Known issues</h1>
 * <ul>
 *     <li>
 *        Filtering by paths. For some registrations the codec that converts back from the DOM to binding paths is
 *        busted. As such, an aproximated path is used in the output. For now it is recommended not to use
 *        watchRegistrations and allow all registrations to be logged.
 *     </li>
 * </ul>
 */
public class TracingBroker implements TracingDOMDataBroker {
    private static final Logger LOG = LoggerFactory.getLogger(TracingBroker.class);

    private static final int STACK_TRACE_FIRST_RELEVANT_FRAME = 2;

    private final BindingCodecTree codec;
    private final DOMDataBroker delegate;
    private final List<Watch> registrationWatches = new ArrayList<>();
    private final List<Watch> writeWatches = new ArrayList<>();

    private final boolean isDebugging;
    private final CloseTrackedRegistry<TracingTransactionChain> transactionChainsRegistry;
    private final CloseTrackedRegistry<TracingReadOnlyTransaction> readOnlyTransactionsRegistry;
    private final CloseTrackedRegistry<TracingWriteTransaction> writeTransactionsRegistry;
    private final CloseTrackedRegistry<TracingReadWriteTransaction> readWriteTransactionsRegistry;

    private class Watch {
        final String iidString;
        final LogicalDatastoreType store;

        Watch(final String iidString, final LogicalDatastoreType storeOrNull) {
            store = storeOrNull;
            this.iidString = iidString;
        }

        private String toIidCompString(final YangInstanceIdentifier iid) {
            StringBuilder builder = new StringBuilder();
            toPathString(iid, builder);
            return builder.append('/').toString();
        }

        private boolean isParent(final String parent, final String child) {
            int parentOffset = 0;
            if (parent.length() > 0 && parent.charAt(0) == '<') {
                parentOffset = parent.indexOf('>') + 1;
            }

            int childOffset = 0;
            if (child.length() > 0 && child.charAt(0) == '<') {
                childOffset = child.indexOf('>') + 1;
            }

            return child.startsWith(parent.substring(parentOffset), childOffset);
        }

        @SuppressWarnings({ "checkstyle:hiddenField", "hiding" })
        public boolean subtreesOverlap(final YangInstanceIdentifier iid, final LogicalDatastoreType store) {
            if (this.store != null && !this.store.equals(store)) {
                return false;
            }

            String otherIidString = toIidCompString(iid);
            return isParent(iidString, otherIidString) || isParent(otherIidString, iidString);
        }

        @SuppressWarnings({ "checkstyle:hiddenField", "hiding" })
        public boolean eventIsOfInterest(final YangInstanceIdentifier iid, final LogicalDatastoreType store) {
            if (this.store != null && !this.store.equals(store)) {
                return false;
            }

            return isParent(iidString, toPathString(iid));
        }
    }

    public TracingBroker(final DOMDataBroker delegate, final Config config, final BindingCodecTree codec) {
        this.delegate = requireNonNull(delegate, "delegate");
        this.codec = requireNonNull(codec, "codec");
        configure(config);

        isDebugging = Boolean.TRUE.equals(config.getTransactionDebugContextEnabled());
        final String db = "DataBroker";
        transactionChainsRegistry     = new CloseTrackedRegistry<>(db, "createTransactionChain()", isDebugging);
        readOnlyTransactionsRegistry  = new CloseTrackedRegistry<>(db, "newReadOnlyTransaction()", isDebugging);
        writeTransactionsRegistry     = new CloseTrackedRegistry<>(db, "newWriteOnlyTransaction()", isDebugging);
        readWriteTransactionsRegistry = new CloseTrackedRegistry<>(db, "newReadWriteTransaction()", isDebugging);
    }

    private void configure(final Config config) {
        registrationWatches.clear();
        Set<String> paths = config.getRegistrationWatches();
        if (paths != null) {
            for (String path : paths) {
                watchRegistrations(path, null);
            }
        }

        writeWatches.clear();
        paths = config.getWriteWatches();
        if (paths != null) {
            for (String path : paths) {
                watchWrites(path, null);
            }
        }
    }

    /**
     * Log registrations to this subtree of the md-sal.
     * @param iidString the iid path of the root of the subtree
     * @param store Which LogicalDataStore? or null for both
     */
    public void watchRegistrations(final String iidString, final LogicalDatastoreType store) {
        LOG.info("Watching registrations to {} in {}", iidString, store);
        registrationWatches.add(new Watch(iidString, store));
    }

    /**
     * Log writes to this subtree of the md-sal.
     * @param iidString the iid path of the root of the subtree
     * @param store Which LogicalDataStore? or null for both
     */
    public void watchWrites(final String iidString, final LogicalDatastoreType store) {
        LOG.info("Watching writes to {} in {}", iidString, store);
        Watch watch = new Watch(iidString, store);
        writeWatches.add(watch);
    }

    private boolean isRegistrationWatched(final YangInstanceIdentifier iid, final LogicalDatastoreType store) {
        if (registrationWatches.isEmpty()) {
            return true;
        }

        for (Watch regInterest : registrationWatches) {
            if (regInterest.subtreesOverlap(iid, store)) {
                return true;
            }
        }

        return false;
    }

    boolean isWriteWatched(final YangInstanceIdentifier iid, final LogicalDatastoreType store) {
        if (writeWatches.isEmpty()) {
            return true;
        }

        for (Watch watch : writeWatches) {
            if (watch.eventIsOfInterest(iid, store)) {
                return true;
            }
        }

        return false;
    }

    static void toPathString(final DataObjectReference<?> iid, final StringBuilder builder) {
        for (var pathArg : iid.steps()) {
            builder.append('/').append(pathArg.type().getSimpleName());
        }
    }

    String toPathString(final YangInstanceIdentifier  yiid) {
        StringBuilder sb = new StringBuilder();
        toPathString(yiid, sb);
        return sb.toString();
    }


    private void toPathString(final YangInstanceIdentifier yiid, final StringBuilder sb) {
        var iid = codec.getInstanceIdentifierCodec().toBinding(yiid);
        if (null == iid) {
            reconstructIidPathString(yiid, sb);
        } else {
            toPathString(iid, sb);
        }
    }

    private static void reconstructIidPathString(final YangInstanceIdentifier yiid, final StringBuilder sb) {
        sb.append("<RECONSTRUCTED FROM: \"").append(yiid.toString()).append("\">");
        for (YangInstanceIdentifier.PathArgument pathArg : yiid.getPathArguments()) {
            sb.append('/').append(pathArg.getNodeType().getLocalName());
        }
    }

    String getStackSummary() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        StringBuilder sb = new StringBuilder();
        for (int i = STACK_TRACE_FIRST_RELEVANT_FRAME; i < stack.length; i++) {
            StackTraceElement frame = stack[i];
            sb.append("\n\t(TracingBroker)\t").append(frame.getClassName()).append('.').append(frame.getMethodName());
        }

        return sb.toString();
    }

    @Override
    public DOMDataTreeReadWriteTransaction newReadWriteTransaction() {
        return new TracingReadWriteTransaction(delegate.newReadWriteTransaction(), this, readWriteTransactionsRegistry);
    }

    @Override
    public DOMDataTreeWriteTransaction newWriteOnlyTransaction() {
        return new TracingWriteTransaction(delegate.newWriteOnlyTransaction(), this, writeTransactionsRegistry);
    }

    @Override
    public DOMTransactionChain createTransactionChain() {
        return new TracingTransactionChain(delegate.createTransactionChain(), this, transactionChainsRegistry);
    }

    @Override
    public DOMTransactionChain createMergingTransactionChain() {
        return new TracingTransactionChain(delegate.createMergingTransactionChain(), this, transactionChainsRegistry);
    }

    @Override
    public DOMDataTreeReadTransaction newReadOnlyTransaction() {
        return new TracingReadOnlyTransaction(delegate.newReadOnlyTransaction(), readOnlyTransactionsRegistry);
    }

    @Override
    public <T extends Extension> T extension(final Class<T> type) {
        final var ext = delegate.extension(type);
        if (DataTreeChangeExtension.class.equals(type) && ext instanceof DataTreeChangeExtension treeChange) {
            return type.cast(new DataTreeChangeExtension() {
                @Override
                public Registration registerTreeChangeListener(final DOMDataTreeIdentifier treeId,
                        final DOMDataTreeChangeListener listener) {
                    notifyIfWatched("Non-clustered", treeId, listener);
                    return treeChange.registerTreeChangeListener(treeId, listener);
                }

                @Override
                @Deprecated(since = "13.0.0", forRemoval = true)
                public Registration registerLegacyTreeChangeListener(final DOMDataTreeIdentifier treeId,
                        final DOMDataTreeChangeListener listener) {
                    notifyIfWatched("Non-clustered", treeId, listener);
                    return treeChange.registerLegacyTreeChangeListener(treeId, listener);
                }

                private void notifyIfWatched(final String kind, final DOMDataTreeIdentifier treeId,
                        final DOMDataTreeChangeListener listener) {
                    final var rootId = treeId.path();
                    if (isRegistrationWatched(rootId, treeId.datastore()) && LOG.isWarnEnabled()) {
                        LOG.warn("{} registration (registerDataTreeChangeListener) for {} from {}.", kind,
                            toPathString(rootId), getStackSummary());
                    }
                }
            });
        }
        return ext;
    }

    @Override
    public boolean printOpenTransactions(final PrintStream ps, final int minOpenTXs) {
        if (transactionChainsRegistry.getAllUnique().isEmpty()
            && readOnlyTransactionsRegistry.getAllUnique().isEmpty()
            && writeTransactionsRegistry.getAllUnique().isEmpty()
            && readWriteTransactionsRegistry.getAllUnique().isEmpty()) {

            ps.println("No open transactions, great!");
            return false;
        }

        ps.println(getClass().getSimpleName() + " found some not yet (or never..) closed transaction[chain]s!");
        ps.println("[NB: If no stack traces are shown below, then "
                 + "enable transaction-debug-context-enabled in mdsaltrace_config.xml]");
        ps.println();
        // Flag to track if we really found any real leaks with more (or equal) to minOpenTXs
        boolean hasFound = print(readOnlyTransactionsRegistry, ps, "  ", minOpenTXs);
        hasFound |= print(writeTransactionsRegistry, ps, "  ", minOpenTXs);
        hasFound |= print(readWriteTransactionsRegistry, ps, "  ", minOpenTXs);

        // Now print details for each non-closed TransactionChain
        // incl. in turn each ones own read/Write[Only]TransactionsRegistry
        Set<CloseTrackedRegistryReportEntry<TracingTransactionChain>>
            entries = transactionChainsRegistry.getAllUnique();
        if (!entries.isEmpty()) {
            ps.println("  " + transactionChainsRegistry.getAnchor() + " : "
                    + transactionChainsRegistry.getCreateDescription());
        }
        for (CloseTrackedRegistryReportEntry<TracingTransactionChain> entry : entries) {
            ps.println("    " + entry.getNumberAddedNotRemoved() + "x TransactionChains opened but not closed here:");
            printStackTraceElements(ps, "      ", entry.getStackTraceElements());
            @SuppressWarnings("resource")
            TracingTransactionChain txChain = (TracingTransactionChain) entry
                .getExampleCloseTracked().getRealCloseTracked();
            hasFound |= print(txChain.getReadOnlyTransactionsRegistry(), ps, "        ", minOpenTXs);
            hasFound |= print(txChain.getWriteTransactionsRegistry(), ps, "        ", minOpenTXs);
            hasFound |= print(txChain.getReadWriteTransactionsRegistry(), ps, "        ", minOpenTXs);
        }
        ps.println();

        return hasFound;
    }

    final void logEmptySet(final YangInstanceIdentifier yiid) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Empty data set write to {}", toPathString(yiid));
        }
    }

    @SuppressFBWarnings(value = "SLF4J_SIGN_ONLY_FORMAT", justification = "pre-formatted logs")
    static final void logOperations(final Object identifier, final List<?> operations) {
        if (LOG.isWarnEnabled()) {
            LOG.warn("Transaction {} contains the following operations:", identifier);
            for (var operation : operations) {
                LOG.warn("{}", operation);
            }
        }
    }

    private <T extends CloseTracked<T>> boolean print(final CloseTrackedRegistry<T> registry, final PrintStream ps,
            final String indent, final int minOpenTransactions) {
        Set<CloseTrackedRegistryReportEntry<T>> unsorted = registry.getAllUnique();
        if (unsorted.size() < minOpenTransactions) {
            return false;
        }

        List<CloseTrackedRegistryReportEntry<T>> entries = new ArrayList<>(unsorted);
        entries.sort((o1, o2) -> Long.compare(o2.getNumberAddedNotRemoved(), o1.getNumberAddedNotRemoved()));

        if (!entries.isEmpty()) {
            ps.println(indent + registry.getAnchor() + " : " + registry.getCreateDescription());
        }
        entries.forEach(entry -> {
            ps.println(indent + "  " + entry.getNumberAddedNotRemoved()
                + "x transactions opened here, which are not closed:");
            printStackTraceElements(ps, indent + "    ", entry.getStackTraceElements());
        });
        if (!entries.isEmpty()) {
            ps.println();
        }
        return true;
    }

    private void printStackTraceElements(final PrintStream ps, final String indent,
            final List<StackTraceElement> stackTraceElements) {
        boolean ellipsis = false;
        for (final StackTraceElement stackTraceElement : stackTraceElements) {
            if (isStackTraceElementInteresting(stackTraceElement)) {
                ps.println(indent + stackTraceElement);
                ellipsis = false;
            } else if (!ellipsis) {
                ps.println(indent + "(...)");
                ellipsis = true;
            }
        }
    }

    private boolean isStackTraceElementInteresting(final StackTraceElement element) {
        final String className = element.getClassName();
        return !className.startsWith(getClass().getPackage().getName())
            && !className.startsWith(CloseTracked.class.getPackage().getName())
            && !className.startsWith("Proxy")
            && !className.startsWith("akka")
            && !className.startsWith("scala")
            && !className.startsWith("sun.reflect")
            && !className.startsWith("java.lang.reflect")
            && !className.startsWith("org.apache.aries.blueprint")
            && !className.startsWith("org.osgi.util.tracker");
    }
}
