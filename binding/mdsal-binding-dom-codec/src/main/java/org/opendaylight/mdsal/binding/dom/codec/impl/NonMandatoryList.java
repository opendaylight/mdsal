/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

/**
 * Marker interface for {@link ListNodeCodecContext} and {@link KeyedListNodeCodecContext} instances which are bound
 * to lists which do not have a min-elements bound greater than zero.
 */
interface NonMandatoryList {

}
