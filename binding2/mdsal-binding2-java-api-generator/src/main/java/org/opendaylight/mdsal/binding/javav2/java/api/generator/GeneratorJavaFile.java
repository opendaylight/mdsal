/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.mdsal.binding.javav2.java.api.generator;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.opendaylight.mdsal.binding.javav2.java.api.generator.util.JavaCodePrettyPrint;
import org.opendaylight.mdsal.binding.javav2.model.api.CodeGenerator;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.javav2.model.api.Type;
import org.opendaylight.mdsal.binding.javav2.model.api.UnitName;
import org.opendaylight.mdsal.binding.javav2.model.api.GeneratedTypeForBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Generates files with JAVA source code for every specified type.
 */
@Beta
public final class GeneratorJavaFile {

    private static final Logger LOG = LoggerFactory.getLogger(GeneratorJavaFile.class);
    private static final Splitter BSDOT_SPLITTER = Splitter.on(".");

    /**
     * List of <code>CodeGenerator</code> instances.
     */
    private final List<CodeGenerator> generators = new ArrayList<>();

    /**
     * Set of <code>Type</code> instances for which the JAVA code is generated.
     */
    private final Collection<? extends Type> types;

    /**
     * BuildContext used for instantiating files
     */
    private final BuildContext buildContext;

    /**
     * Creates instance of this class with the set of <code>types</code> for
     * which the JAVA code is generated.
     *
     * The instances of concrete JAVA code generator are created.
     *
     * @param buildContext
     *            build context to use for accessing files
     * @param types
     *            set of types for which JAVA code should be generated
     */
    public GeneratorJavaFile(final BuildContext buildContext, final Collection<? extends Type> types) {
        this.buildContext = Preconditions.checkNotNull(buildContext);
        this.types = Preconditions.checkNotNull(types);
        this.generators.add(new EnumGenerator());
        this.generators.add(new InterfaceGenerator());
        this.generators.add(new BuilderGenerator());
        this.generators.add(new TOGenerator());
    }

    /**
     * Generates <code>List</code> of files for collection of types. All files are stored
     * to sub-folders of base directory <code>persistentSourcesDirectory</code>. Subdirectories
     * are generated according to packages to which the type belongs (e. g. if
     * type belongs to the package <i>org.pcg</i> then in <code>persistentSourcesDirectory</code>
     * is created directory <i>org</i> which contains <i>pcg</i>).
     *
     * @param generatedSourcesDirectory expected output directory for generated sources configured by
     *            user
     * @param persistentSourcesDirectory base directory
     * @return list of generated files
     * @throws IOException thrown in case of I/O error
     */
    public List<File> generateToFile(final File generatedSourcesDirectory, final File persistentSourcesDirectory)
            throws IOException {
        final List<File> result = new ArrayList<>();
        for (final Type type : this.types) {
            if (type != null) {
                for (final CodeGenerator generator : this.generators) {
                    File generatedJavaFile = null;
                    if (type instanceof GeneratedTransferObject
                            && ((GeneratedTransferObject) type).isUnionTypeBuilder()) {
                        final File packageDir = packageToDirectory(persistentSourcesDirectory, type.getPackageName());
                        final File file = new File(packageDir, generator.getUnitName(type) + ".java");
                        if (!file.exists()) {
                            generatedJavaFile = generateTypeToJavaFile(persistentSourcesDirectory, type, generator);
                        }
                    } else {
                        generatedJavaFile = generateTypeToJavaFile(generatedSourcesDirectory, type, generator);
                    }
                    if (generatedJavaFile != null) {
                        result.add(generatedJavaFile);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Creates the package directory path as concatenation of
     * <code>parentDirectory</code> and parsed <code>packageName</code>. The
     * parsing of <code>packageName</code> is realized as replacement of the
     * package name dots with the file system separator.
     *
     * @param parentDirectory
     *            <code>File</code> object with reference to parent directory
     * @param packageName
     *            string with the name of the package
     * @return <code>File</code> object which refers to the new directory for
     *         package <code>packageName</code>
     */
    public static File packageToDirectory(final File parentDirectory, final String packageName) {
        if (packageName == null) {
            throw new IllegalArgumentException("Package Name cannot be NULL!");
        }

        final StringBuilder dirPathBuilder = new StringBuilder();
        final Iterator<String> packageElementsItr = BSDOT_SPLITTER.split(packageName).iterator();
        if (packageElementsItr.hasNext()) {
            dirPathBuilder.append(packageElementsItr.next());
        }

        while (packageElementsItr.hasNext()) {
            dirPathBuilder.append(File.separator);
            dirPathBuilder.append(packageElementsItr.next());
        }

        return new File(parentDirectory, dirPathBuilder.toString());
    }

    /**
     * Generates <code>File</code> for <code>type</code>. All files are stored
     * to sub-folders of base directory <code>parentDir</code>. Subdirectories
     * are generated according to packages to which the type belongs (e. g. if
     * type belongs to the package <i>org.pcg</i> then in <code>parentDir</code>
     * is created directory <i>org</i> which contains <i>pcg</i>).
     *
     * @param parentDir
     *            directory where should be the new file generated
     * @param type
     *            JAVA <code>Type</code> for which should be JAVA source code
     *            generated
     * @param generator
     *            code generator which is used for generating of the source code
     * @return file which contains JAVA source code
     * @throws IOException
     *             if the error during writing to the file occurs
     * @throws IllegalArgumentException
     *             if <code>type</code> equals <code>null</code>
     * @throws IllegalStateException
     *             if string with generated code is empty
     */
    private File generateTypeToJavaFile(final File parentDir, final Type type, final CodeGenerator generator)
            throws IOException {
        if (parentDir == null) {
            LOG.warn("Parent Directory not specified, files will be generated "
                    + "accordingly to generated Type package path.");
        }
        if (type == null) {
            LOG.error("Cannot generate Type into Java File because " + "Generated Type is NULL!");
            throw new IllegalArgumentException("Generated Type Cannot be NULL!");
        }
        if (generator == null) {
            LOG.error("Cannot generate Type into Java File because " + "Code Generator instance is NULL!");
            throw new IllegalArgumentException("Code Generator Cannot be NULL!");
        }

        if (generator.isAcceptable(type)) {
            File packageDir;
            if (generator instanceof BuilderGenerator) {
                Preconditions.checkState(type instanceof GeneratedTypeForBuilder, type.getFullyQualifiedName());
                packageDir = packageToDirectory(parentDir, ((GeneratedTypeForBuilder)type).getPackageNameForBuilder());
            } else {
                packageDir = packageToDirectory(parentDir, type.getPackageName());
            }

            if (!packageDir.exists()) {
                packageDir.mkdirs();
            }

            final String generatedCode = JavaCodePrettyPrint.perform(generator.generate(type));
            Preconditions.checkState(!generatedCode.isEmpty(), "Generated code should not be empty!");
            final File file = new File(packageDir, ((UnitName) generator.getUnitName(type)).getValue() + ".java");

            if (file.exists()) {
                LOG.warn("Naming conflict for type '{}': file with same name already exists and will not be generated.",
                    type.getFullyQualifiedName());
                return null;
            }

            try (final OutputStream stream = this.buildContext.newFileOutputStream(file)) {
                try (final Writer fw = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
                    try (final BufferedWriter bw = new BufferedWriter(fw)) {
                        bw.write(generatedCode);
                    }
                } catch (final IOException e) {
                    LOG.error("Failed to write generate output into {}", file.getPath(), e);
                    throw e;
                }
            }
            return file;

        }
        return null;
    }

}
