package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class Bug4145Test {
    @Test
    public void bug4145Test() throws URISyntaxException, IOException, YangSyntaxErrorException, ReactorException {
        File resourceFile = new File(getClass().getResource(
                "/bug-4145/foo.yang").toURI());

        SchemaContext context = RetestUtils.parseYangSources(resourceFile);

        List<Type> generateTypes = new BindingGeneratorImpl(false)
                .generateTypes(context);
        assertNotNull(generateTypes);
        assertTrue(generateTypes.size() > 0);
    }
}
