package org.opendaylight.yangtools.sal.binding.generator.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.sal.binding.generator.spi.TypeProvider;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.yang.types.TypeProviderImpl;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.DataNodeIterator;

final class TypeDefResolver {
    private TypeDefResolver() {
        throw new UnsupportedOperationException();
    }

    /**
     * Converts all extended type definitions of module to the list of
     * <code>Type</code> objects.
     *
     * @param module
     *            module from which is obtained set of type definitions
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if module is null</li>
     *             <li>if name of module is null</li>
     *             </ul>
     * @throws IllegalStateException
     *             if set of type definitions from module is null
     */
    static void allTypeDefinitionsToGenTypes(final Module module, TypeProvider typeProvider, final Map<Module,
            ModuleContext> genCtx) {
        checkArgument(module != null, "Module reference cannot be NULL.");
        checkArgument(module.getName() != null, "Module name cannot be NULL.");
        final DataNodeIterator it = new DataNodeIterator(module);
        final List<TypeDefinition<?>> typeDefinitions = it.allTypedefs();
        checkState(typeDefinitions != null, "Type Definitions for module «module.name» cannot be NULL.");

        for (final TypeDefinition<?> typedef : typeDefinitions) {
            if (typedef != null) {
                final Type type = ((TypeProviderImpl) typeProvider).generatedTypeForExtendedDefinitionType(typedef,
                        typedef);
                if (type != null) {
                    final ModuleContext ctx = genCtx.get(module);
                    ctx.addTypedefType(typedef.getPath(), type);
                    ctx.addTypeToSchema(type,typedef);
                }
            }
        }
    }
}