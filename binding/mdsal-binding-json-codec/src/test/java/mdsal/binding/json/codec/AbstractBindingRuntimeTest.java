package mdsal.binding.json.codec;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.spi.BindingRuntimeHelpers;

public abstract class AbstractBindingRuntimeTest {
    private static BindingRuntimeContext runtimeContext;

    @BeforeClass
    public static void beforeClass() {
        runtimeContext = BindingRuntimeHelpers.createRuntimeContext();
    }

    @AfterClass
    public static void afterClass() {
        runtimeContext = null;
    }

    public static final BindingRuntimeContext getRuntimeContext() {
        return runtimeContext;
    }
}
