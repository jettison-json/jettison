/**
 * Copyright 2006 Envoi Solutions LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.jettison;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Document;

/**
 * An abstract JSON DOM parser
 * 
 * @author Thomas.Diesler@jboss.com
 * @author <a href="mailto:dejan@nighttale.net">Dejan Bosanac</a>
 * @since 21-Mar-2008
 */
public class AbstractDOMDocumentParser {
	
	private AbstractXMLInputFactory inputFactory;
	
	protected AbstractDOMDocumentParser(AbstractXMLInputFactory inputFactory) {
		this.inputFactory = inputFactory;
	}
	
	public Document parse(InputStream input) throws IOException {
		try {
	         XMLStreamReader streamReader = inputFactory.createXMLStreamReader(input);
	         XMLInputFactory readerFactory = XMLInputFactory.newInstance();
	         XMLEventReader eventReader = readerFactory.createXMLEventReader(streamReader);
	         
	         // Can not create a STaX writer for a DOMResult in woodstox-3.1.1
	         /*XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
			 Document nsDom = getDocumentBuilder().newDocument();
			 DOMResult result = new DOMResult(nsDom);
	         XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(result);*/
	         
	         ByteArrayOutputStream baos = new ByteArrayOutputStream();
	         XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
	         XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(baos);
	         
	         eventWriter.add(eventReader);
	         eventWriter.close();
	         
	         // This parsing step should not be necessary, if we could output to a DOMResult
	         ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	         return getDocumentBuilder().parse(bais);
	         
	         //return nsDom;
			
		} catch (Exception ex) {
			IOException ioex = new IOException("Cannot parse input stream");
			ioex.initCause(ex);
			throw ioex;
		}
	}

	private DocumentBuilder getDocumentBuilder() {
		try {
			return createDocumentBuilderFactory().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Failed to create DocumentBuilder", e);
		}
	}

	/**
	 * Create the DocumentBuilderFactory used to parse the XML generated from
	 * the JSON input. That XML is built from the input, and never legitimately
	 * contains a DOCTYPE, so DOCTYPE declarations are rejected and external
	 * entities, external DTDs and XInclude are disabled.
	 */
	static DocumentBuilderFactory createDocumentBuilderFactory() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		factory.setXIncludeAware(false);
		factory.setExpandEntityReferences(false);
		// All JAXP implementations are required to support secure processing
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		// Defence in depth, for implementations that support these settings
		setFeatureIfSupported(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
		setFeatureIfSupported(factory, "http://xml.org/sax/features/external-general-entities", false);
		setFeatureIfSupported(factory, "http://xml.org/sax/features/external-parameter-entities", false);
		setFeatureIfSupported(factory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		setAttributeIfSupported(factory, XMLConstants.ACCESS_EXTERNAL_DTD, "");
		setAttributeIfSupported(factory, XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		return factory;
	}

	private static void setFeatureIfSupported(DocumentBuilderFactory factory, String feature, boolean value) {
		try {
			factory.setFeature(feature, value);
		} catch (ParserConfigurationException e) {
			// Not supported by this implementation
		}
	}

	private static void setAttributeIfSupported(DocumentBuilderFactory factory, String attribute, Object value) {
		try {
			factory.setAttribute(attribute, value);
		} catch (IllegalArgumentException e) {
			// Not supported by this implementation
		}
	}
}