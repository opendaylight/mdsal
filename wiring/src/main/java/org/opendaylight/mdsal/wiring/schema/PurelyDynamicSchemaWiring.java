/*
 * Copyright (c) 2019 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.wiring.schema;

import java.util.List;
import org.opendaylight.mdsal.dom.broker.schema.ScanningSchemaServiceProvider;

/**
 * {@link SchemaWiring} which only supports dynamically loaded YANG models and DOM (no binding).
 *
 * <p>You must use {@literal @}Inject YangRegisterer and use its {@link YangRegisterer#registerYANGs(List)}
 * to dynamically register YANG models at run-time; if you don't, the MD-SAL DOM APIs won't work.
 *
 * <p>Use {@link PurelyClassLoadingSchemaWiring} if instead you want to only use generated binding code.
 *
 * @author Michael Vorburger.ch
 */
public class PurelyDynamicSchemaWiring extends AbstractDynamicSchemaWiring {

    public PurelyDynamicSchemaWiring() {
        super(new ScanningSchemaServiceProvider());
    }
}
