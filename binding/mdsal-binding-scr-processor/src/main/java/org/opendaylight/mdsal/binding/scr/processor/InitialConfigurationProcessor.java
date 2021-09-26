/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.scr.processor;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic.Kind;
import javax.tools.StandardLocation;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.binding.scr.api.ConfigurationSnapshot;
import org.opendaylight.mdsal.binding.scr.spi.ConfigurationDescriptor;
import org.opendaylight.mdsal.binding.scr.spi.ConfigurationField;

/**
 * An annotation processor to create glue between Service Component Runtime and
 * {@code org.opendaylight.mdsal.binding.api.config.ConfigurationService}. It processes
 * {@code org.opendaylight.mdsal.binding.scr.api.InitialConfiguration} annotations attached to
 * {@link ConfigurationSnapshot} constant fields and generates a descriptor for runtime use.
 */
@MetaInfServices(Processor.class)
@SupportedAnnotationTypes("org.opendaylight.mdsal.binding.scr.api.InitialConfiguration")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public final class InitialConfigurationProcessor extends AbstractProcessor {
    private static final Set<Modifier> CONSTANT_MODIFIERS = Set.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

    private final Map<Name, AnnotatedField> fields = new HashMap<>();

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
    }

    private void processConstantVariable(final VariableElement element) {
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

        final var type = (DeclaredType) element.asType();
        final var typeName = ((TypeElement) type.asElement()).getQualifiedName();

        fields.put(typeName, new AnnotatedField(fqcn, element.getSimpleName()));
    }

    private void emitDescriptor() throws IOException {
        if (fields.isEmpty()) {
            // We have not found any fields: bail out
            notez("Iz nuttin :(");
            return;
        }

        notez("Haz shiniez " + fields);

        final var xmlBody = ConfigurationDescriptor.of(ImmutableMap.copyOf(fields.entrySet().stream()
            .map(entry -> {
                final var field = entry.getValue();
                final var cfg = ConfigurationField.of(entry.getKey().toString(), field.className.toString(),
                    field.fieldName.toString());
                return Map.entry(cfg.typeName, cfg);
            })
            .collect(Collectors.toUnmodifiableList())))
            .toXML();

        final var file = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "",
            ConfigurationDescriptor.METAINF_SCR_CONFIGURATION);
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
