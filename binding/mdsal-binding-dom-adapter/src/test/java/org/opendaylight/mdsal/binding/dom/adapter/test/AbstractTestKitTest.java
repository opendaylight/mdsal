package org.opendaylight.mdsal.binding.dom.adapter.test;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.After;
import org.junit.Before;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.testkit.DOMTestKit;
import org.opendaylight.yangtools.yang.binding.DataObject;

public abstract class AbstractTestKitTest {
    private final @Nullable Class<? extends DataObject> modelClass;

    AdapterTestKit testkit;

    AbstractTestKitTest() {
        this.modelClass = null;
    }

    AbstractTestKitTest(final Class<? extends DataObject> modelClass) {
        this.modelClass = requireNonNull(modelClass);
    }

    @Before
    public void setup() throws Exception {
        final DOMTestKit domTestkit = modelClass == null ? new DOMTestKit()
                : new DOMTestKit(Set.of(BindingReflections.getModuleInfo(modelClass)), DOMTestKit.BOTH_DATASTORES);

        testkit = new AdapterTestKit(domTestkit);
    }

    @After
    public void teardown() {
        testkit.close();
    }

    final DOMDataBroker getDomBroker() {
        return testkit.domTestKit().domDataBroker();
    }

    final DataBroker getDataBroker() {
        return testkit.dataBroker();
    }
}
