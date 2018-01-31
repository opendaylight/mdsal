/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.dom.api;

import java.util.Collection;
import java.util.EventListener;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathResult;

/**
 * Interface implemented by data consumers, e.g. processes wanting to act on data
 * after it has been introduced to the conceptual data tree.
 */
public interface DOMXPathListener extends EventListener {
    /**
     * Invoked whenever the result of the XPath expression changes. The logical changes are reported, and may be subject
     * to state compression as specified when the listener is registered.
     *
     * @param changes The set of changes being reported.
     */
    void onXPathResultChanged(@Nonnull Collection<XPathResult<?>> changes);

    /**
     * Invoked when an XPath listening failure occurs. This can be triggered, for example, when a
     * connection to external subtree source is broken. The listener will not receive any other
     * callbacks, but its registration still needs to be closed to prevent resource leak.
     *
     * @param causes Collection of failure causes, may not be null or empty.
     */
    // FIXME: should be a different exception
    void onXPathFailed(@Nonnull Collection<DOMDataTreeListeningException> causes);
}
