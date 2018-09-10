/*
 * Copyright © 2018 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.util;

/**
 * Managed transaction chains provide managed semantics around transaction chains, <em>i.e.</em> chains which provide
 * transactions which are automatically submitted or cancelled.
 */
public interface ManagedTransactionChain extends ManagedTransactionFactory {
}
