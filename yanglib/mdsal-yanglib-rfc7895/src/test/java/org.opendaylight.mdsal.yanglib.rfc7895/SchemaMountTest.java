/*
 * Copyright © 2019 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc7895;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;
import org.codehaus.stax2.ri.dom.DOMWrappingReader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.mdsal.yanglib.api.SchemaContextResolver;
import org.opendaylight.mdsal.yanglib.api.SourceReference;
import org.opendaylight.mdsal.yanglib.api.YangLibSupport;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointChild;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContext;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointContextFactory;
import org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier;
import org.opendaylight.yangtools.rfc8528.data.api.YangLibraryConstants;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.xml.XmlParserStream;
import org.opendaylight.yangtools.yang.data.impl.schema.AbstractMountPointChild;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.impl.YangParserFactoryImpl;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class SchemaMountTest {

    private static final YangModuleInfo INET_TYPES_MODULE =
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715
                    .$YangModuleInfoImpl.getInstance();
    private static final YangModuleInfo YANG_TYPES_MODULE =
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715
                    .$YangModuleInfoImpl.getInstance();
    private static final YangModuleInfo YANG_LIBRARY_MODULE =
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.library.rev160621
                    .$YangModuleInfoImpl.getInstance();
    private static final YangModuleInfo YANG_SCHEMA_MOUNT_MODULE =
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.schema.mount.rev190114
                    .$YangModuleInfoImpl.getInstance();

    private static Map<SourceIdentifier, YangTextSchemaSource> MODELS;

    @BeforeClass
    public static void prepareModels() {
        MODELS = new HashMap<>();
        addModel(createYangTextSchemaSource(INET_TYPES_MODULE));
        addModel(createYangTextSchemaSource(YANG_TYPES_MODULE));
        addModel(createYangTextSchemaSource(YANG_LIBRARY_MODULE));
        addModel(createYangTextSchemaSource(YANG_SCHEMA_MOUNT_MODULE));
        addModel(createYangTextSchemaSource("/schemamount/mount-network@2019-08-09.yang"));
        addModel(createYangTextSchemaSource("/schemamount/mounted@2019-08-09.yang"));
    }

    private static void addModel(YangTextSchemaSource yangTextSchemaSource) {
        MODELS.put(yangTextSchemaSource.getIdentifier(), yangTextSchemaSource);
    }

    private static YangTextSchemaSource createYangTextSchemaSource(YangModuleInfo moduleInfo) {
        return YangTextSchemaSource.delegateForByteSource(RevisionSourceIdentifier
                .create(moduleInfo.getName().getLocalName(), moduleInfo.getName().getRevision()),
                moduleInfo.getYangTextByteSource());
    }

    private static YangTextSchemaSource createYangTextSchemaSource(String resourcePath) {
        return YangTextSchemaSource.forResource(SchemaMountTest.class, resourcePath);
    }

    @Test
    public void test() throws YangParserException, IOException, SAXException, XMLStreamException, URISyntaxException {
        SchemaContext schemaContext =
                YangParserTestUtils.parseSources(StatementParserMode.DEFAULT_MODE, null, MODELS.values());

        DOMSourceMountPointChild mountPointMountPointChild = createDomSourceMountPointChild(schemaContext,
                "/xmls/mount-point.xml", "ietf-yang-schema-mount", "schema-mounts");

        DOMSourceMountPointChild libraryMountPointChild = createDomSourceMountPointChild(schemaContext,
                "/xmls/yang-library.xml", "ietf-yang-library", "modules-state");
        Map<YangLibraryConstants.ContainerName, MountPointChild> libraryMap = new HashMap<>();
        libraryMap.put(YangLibraryConstants.ContainerName.RFC7895, libraryMountPointChild);

        YangParserFactory yangParserFactory = new YangParserFactoryImpl();
        YangLibSupport yangLibSupport =
                new YangModuleLibrarySupportFactory().createYangLibSupport(yangParserFactory);
        MountPointContextFactory mountPointContextFactory =
                yangLibSupport.createMountPointContextFactory(MountPointIdentifier
                                .of(QName.create("ietf-netconf", "2019-08-14", "netconf1")),
                        new SchemaContextResolverImpl());

        MountPointContext mountPointContext = mountPointContextFactory
                .createContext(libraryMap, mountPointMountPointChild);

        final InputStream dataResourceAsStream = SchemaMountTest.class.getResourceAsStream(
                "/xmls/mounted-data.xml");
        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(dataResourceAsStream);
        final Module dataMountModule = schemaContext.findModules("mount-network").iterator().next();
        final ContainerSchemaNode dataMountsContainer = (ContainerSchemaNode) dataMountModule
                .findDataChildByName(QName.create(dataMountModule.getQNameModule(), "networks")).get();

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, mountPointContext, dataMountsContainer);
        xmlParser.parse(reader);

        final NormalizedNode<?, ?> transformedInput = result.getResult();
        assertNotNull(transformedInput);
    }

    private DOMSourceMountPointChild createDomSourceMountPointChild(SchemaContext schemaContext,
            String pathToXmlDataResource, String moduleName, String containerName)
            throws IOException, SAXException {
        final InputStream resourceAsStream = SchemaMountTest.class.getResourceAsStream(pathToXmlDataResource);
        Document document = readXmlToDocument(resourceAsStream);
        final DOMSource domSource = new DOMSource(document.getDocumentElement());
        final Module module = schemaContext.findModules(moduleName).iterator().next();
        final ContainerSchemaNode containerSchemaNode = (ContainerSchemaNode) module.findDataChildByName(
                QName.create(module.getQNameModule(), containerName)).get();
        DOMSourceMountPointChild mountPointChild =
                new DOMSourceMountPointChild(domSource, containerSchemaNode);
        return mountPointChild;
    }

    private Document readXmlToDocument(final InputStream xmlContent) throws IOException, SAXException {
        final Document doc = UntrustedXML.newDocumentBuilder().parse(xmlContent);
        doc.getDocumentElement().normalize();
        return doc;
    }

    public class SchemaContextResolverImpl implements SchemaContextResolver {

        @Override
        public SchemaContext resolveSchemaContext(List<SourceReference> librarySources,
                                                  List<SourceReference> requiredSources)
                throws YangParserException {
            Set<YangTextSchemaSource> sources = new HashSet<>();
            sources.addAll(findSources(librarySources));
            sources.addAll(findSources(requiredSources));
            return YangParserTestUtils.parseSources(StatementParserMode.DEFAULT_MODE, null, sources);
        }

        private Set<YangTextSchemaSource> findSources(List<SourceReference> sourceReferences)
                throws YangParserException {
            Set<YangTextSchemaSource> sources = new HashSet<>();
            for (SourceReference librarySource : sourceReferences) {
                YangTextSchemaSource yangTextSchemaSource = MODELS.get(librarySource.getIdentifier());
                if (yangTextSchemaSource == null) {
                    throw new YangParserException("Error while creation of SchemaContext. Missing library model: "
                            + librarySource.getIdentifier());
                }
                sources.add(yangTextSchemaSource);
            }
            return sources;
        }
    }

    public class DOMSourceMountPointChild extends AbstractMountPointChild {
        private final DOMSource source;
        private final DataSchemaNode dataSchemaNode;

        public DOMSourceMountPointChild(final DOMSource source, DataSchemaNode dataSchemaNode) {
            this.source = requireNonNull(source);
            this.dataSchemaNode = requireNonNull(dataSchemaNode);
        }

        @Override
        public void writeTo(final NormalizedNodeStreamWriter writer, final MountPointContext mountCtx)
                throws IOException {

            final XmlParserStream xmlParser;
            try {
                xmlParser = XmlParserStream.create(writer, mountCtx, dataSchemaNode);
            } catch (IllegalArgumentException e) {
                throw new IOException("Failed to instantiate XML parser", e);
            }

            try {
                final XMLStreamReader reader = new DOMSourceXMLStreamReader(source);
                xmlParser.parse(reader).flush();
            } catch (XMLStreamException | URISyntaxException | SAXException e) {
                throw new IOException("Failed to parse payload", e);
            }
        }
    }

    public class DOMSourceXMLStreamReader extends DOMWrappingReader {
        DOMSourceXMLStreamReader(final DOMSource src) throws XMLStreamException {
            super(src, true, true);
        }

        public Object getProperty(final String name) {
            return null;
        }

        public boolean isPropertySupported(final String name) {
            return false;
        }

        public boolean setProperty(final String name, final Object value) {
            return false;
        }

        protected void throwStreamException(final String msg, final Location loc) throws XMLStreamException {
            throw new XMLStreamException(msg, loc);
        }
    }
}
