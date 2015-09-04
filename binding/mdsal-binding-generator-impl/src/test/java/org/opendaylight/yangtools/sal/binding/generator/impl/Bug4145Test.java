package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import java.net.URISyntaxException;
import java.io.File;
import java.util.List;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.junit.Test;

public class Bug4145Test {
    @Test
    public void bug4145Test() throws URISyntaxException, IOException, YangSyntaxErrorException {
        File resourceFile = new File(getClass().getResource(
                "/bug-4145/foo.yang").toURI());
        File resourceDir = resourceFile.getParentFile();

        YangParserImpl parser = YangParserImpl.getInstance();
        SchemaContext context = parser.parseFile(resourceFile, resourceDir);

        List<Type> generateTypes = new BindingGeneratorImpl(false)
                .generateTypes(context);
        assertNotNull(generateTypes);
        assertTrue(generateTypes.size() > 0);
    }
}
