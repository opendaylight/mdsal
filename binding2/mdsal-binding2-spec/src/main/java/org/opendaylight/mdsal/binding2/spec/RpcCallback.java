/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding2.spec;

import com.google.common.annotations.Beta;

@Beta
public interface RpcCallback<O> {

    void onSuccess(O output);

    void onFailure(Throwable e);


}
