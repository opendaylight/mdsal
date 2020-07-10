package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertNotNull;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal572Test {
    @Test
    public void mdsal572Test() {
        final List<Type> generateTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
                "/mdsal572.yang"));
        assertNotNull(generateTypes);
    }
}
