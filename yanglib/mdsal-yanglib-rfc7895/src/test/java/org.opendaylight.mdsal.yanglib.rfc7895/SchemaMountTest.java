/*
 * Copyright © 2019 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.yanglib.rfc7895;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;
import org.codehaus.stax2.ri.dom.DOMWrappingReader;
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
import org.opendaylight.yangtools.yang.parser.impl.YangParserFactoryImpl;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class SchemaMountTest {

    @Test
    public void test() throws YangParserException, IOException, SAXException, XMLStreamException, URISyntaxException {
        SchemaContext schemaContext = YangParserTestUtils.parseYangResourceDirectory("/schemamount");


        final InputStream mountPointResourceAsStream = SchemaMountTest.class.getResourceAsStream(
                "/xmls/mount-point.xml");
        Document mountPointDoc = readXmlToDocument(mountPointResourceAsStream);
        final DOMSource mountPointDomSource = new DOMSource(mountPointDoc.getDocumentElement());
        final Module schemaMountModule = schemaContext.findModules("ietf-yang-schema-mount").iterator().next();
        final ContainerSchemaNode schemaMountsContainer = (ContainerSchemaNode) schemaMountModule.findDataChildByName(
                QName.create(schemaMountModule.getQNameModule(), "schema-mounts")).get();
        DOMSourceMountPointChild mountPointMountPointChild =
                new DOMSourceMountPointChild(mountPointDomSource, schemaMountsContainer);

        final InputStream libraryResourceAsStream = SchemaMountTest.class.getResourceAsStream(
                "/xmls/yang-library.xml");
        Document libraryDoc = readXmlToDocument(libraryResourceAsStream);
        final DOMSource libraryDomSource = new DOMSource(libraryDoc.getDocumentElement());
        final Module libraryModule = schemaContext.findModules("ietf-yang-library").iterator().next();
        final ContainerSchemaNode libraryContainer = (ContainerSchemaNode) libraryModule.findDataChildByName(
                QName.create(libraryModule.getQNameModule(), "modules-state")).get();
        DOMSourceMountPointChild libraryMountPointChild =
                new DOMSourceMountPointChild(libraryDomSource, libraryContainer);
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
    }

    static Document readXmlToDocument(final InputStream xmlContent) throws IOException, SAXException {
        final Document doc = UntrustedXML.newDocumentBuilder().parse(xmlContent);
        doc.getDocumentElement().normalize();
        return doc;
    }

    public class SchemaContextResolverImpl implements SchemaContextResolver {

        @Override
        public SchemaContext resolveSchemaContext(List<SourceReference> librarySources,
                                                  List<SourceReference> requiredSources)
                throws YangParserException {
            List<File> yangFiles = new ArrayList<>();
            for (SourceReference librarySource : librarySources) {
                try {
                    URI directoryPath = YangParserTestUtils.class.getResource("/schemamount/"
                            + librarySource.getIdentifier().toYangFilename())
                            .toURI();
                    yangFiles.add(new File(directoryPath));
                } catch (URISyntaxException e) {
                    throw new YangParserException("Error while creation of SchemaContext - library sources.");
                }
            }
            for (SourceReference requiredSource : requiredSources) {
                try {
                    URI directoryPath = YangParserTestUtils.class.getResource("/schemamount/"
                            + requiredSource.getIdentifier().toYangFilename())
                            .toURI();
                    yangFiles.add(new File(directoryPath));
                } catch (URISyntaxException e) {
                    throw new YangParserException("Error while creation of SchemaContext.");
                }
            }
            return YangParserTestUtils.parseYangFiles(yangFiles);
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
