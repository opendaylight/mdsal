/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.StandardLocation;
import javax.xml.transform.stream.StreamSource;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.binding.scr.api.ConfigurationSnapshot;
import org.opendaylight.mdsal.binding.scr.spi.ConfigurationDescriptors;
import org.xml.sax.SAXException;

/**
 * An annotation processor to create glue between Service Component Runtime and
 * {@code org.opendaylight.mdsal.binding.api.config.ConfigurationService}. It processes
 * {@code org.opendaylight.mdsal.binding.scr.api.InitialConfiguration} annotations attached to
 * {@link ConfigurationSnapshot} constant fields and generates a descriptor for runtime use.
 */
@MetaInfServices(Processor.class)
@SupportedAnnotationTypes("org.opendaylight.mdsal.binding.scr.api.InitialConfiguration")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
// FIXME: alright, the codegen part is probably not what we want to do. At the end of the day, we could enforce the
//        constants to be publicly available and:
//        - generate an DescriptorConstants.META_INF_SCR_CONFIGURATION, which will describe which DataObject interfaces
//          are of interest
//        - generate a @Requirement(namespace = ExtenderNamespace.EXTENDER_NAMESPACE) annotation in classes which are
//          receiving objects
//        - have an OSGi extender, which will process the configuration-snapshot.xml file for each component, and will
//          instantiate a ConfigurationService-using proxy, which will provide the appropriate ConfigurationSnapshot
//          service in ServiceRegistry
//
//        This way we leave a minimal footprint and can also implement alternative runtime support, if this pattern ends
//        up being useful.
public final class ReferenceProcessor extends AbstractProcessor {
    private static final Set<Modifier> CONSTANT_MODIFIERS = Set.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

    private final Map<Name, ConfigurationField> fields = new HashMap<>();

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        processRound(annotations, roundEnv);

        if (roundEnv.processingOver()) {
            try {
                emitDescriptor();
            } catch (IOException e) {
                error(e.getLocalizedMessage(), null);
            } finally {
                fields.clear();
            }
        }

        return true;
    }

    private void processRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            // Nothing to search for
            return;
        }

        notez("Lookz 4 " + annotations);
        for (var annotation : annotations) {
            for (var element : roundEnv.getElementsAnnotatedWith(annotation)) {
                if (element instanceof VariableElement && element.getModifiers().containsAll(CONSTANT_MODIFIERS)) {
                    processConstantVariable((VariableElement) element);
                } else {
                    error("@InitialConfguration must be applied to a 'public static final' field", element);
                }
            }
        }
        // FIXME: we also need to specialize references to include proper:
        //          @Reference(target = "(org.opendaylight.mdsal.binding.ConfigurationType=SomeComponentConfiguration)")
        //        or somesuch selector, but that's job for process-classes step
    }

    private void processConstantVariable(VariableElement element) {
        final var enclosing = element.getEnclosingElement();
        if (!(enclosing instanceof TypeElement)) {
            error("@InitialConfguration must be used on a field", element);
            return;
        }
        final var fqcn = ((TypeElement) enclosing).getQualifiedName();
        if (fqcn.contentEquals("")) {
            error("@InitialConfguration must be used on a field of a non-anonymous class", element);
            return;
        }

        // FIXME: we need to extract and validate that the target type exists and has proper type
        final Name typeName = null;

        fields.put(typeName, new ConfigurationField(fqcn, element.getSimpleName()));
    }

    private void emitDescriptor() throws IOException {
        if (fields.isEmpty()) {
            // We have not found any fields: bail out
            notez("Iz nuttin :(");
            return;
        }

        notez("Haz shiniez " + fields);

        final var sb = new StringBuilder()
            .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            .append("<cfg:initial xmlns:cfg=\"http://org.opendaylight.mdsal/xmlns/scr-configuration/v0.1.0\">\n");

        for (var entry : fields.entrySet()) {
            sb.append("<field type=\"").append(entry.getKey()).append("\"\n");

            final var field = entry.getValue();
            sb.append("       class=\"").append(field.className).append("\"\n");
            sb.append("       name=\"").append(field.fieldName).append("\"/>\n");
        }

        final String xmlBody = sb.append("</cfg:initial>").toString();
        try {
            ConfigurationDescriptors.descriptorSchema().newValidator().validate(
                new StreamSource(new StringReader(xmlBody)));
        } catch (SAXException e) {
            throw new IOException("Refusing to write invalid descriptor", e);
        }

        final var file = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "",
            ConfigurationDescriptors.METAINF_SCR_CONFIGURATION);
        try (var out = new PrintWriter(file.openWriter())) {
            out.append(xmlBody);
        }
    }

    private void note(final String message, final Element element) {
        log(Kind.NOTE, message, element);
    }

    private void error(final String message, final Element element) {
        log(Kind.ERROR, message, element);
    }

    private void log(final Kind kind, final String message, final Element element) {
        processingEnv.getMessager().printMessage(Kind.WARNING, message, element);
    }

    // FIXME: debug only remove at some point
    private void notez(final String message) {
        note(message, null);
    }
}
