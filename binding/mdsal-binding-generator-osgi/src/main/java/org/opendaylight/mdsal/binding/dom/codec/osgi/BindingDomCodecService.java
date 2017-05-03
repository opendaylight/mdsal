package org.opendaylight.mdsal.binding.dom.codec.osgi;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * The set of loosely-consistent services provided by mdsal-binding-dom-codec. This service does not guarantee that
 * individual methods return a point-in-time consistent snapshot.
 *
 * @author Robert Varga
 */
@Beta
public interface BindingDomCodecService extends SchemaContextProvider, SchemaSourceProvider<YangTextSchemaSource> {
    BindingNormalizedNodeSerializer getSerializer();

    @Override
    default SchemaContext getSchemaContext() {
        return getBindingRuntimeContext().getSchemaContext();
    }

    // This method is only needed by config-manager and its minions.
    @Deprecated
    BindingRuntimeContext getBindingRuntimeContext();
}
