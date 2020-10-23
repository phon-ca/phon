/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.query.report;

import java.io.*;

import javax.xml.bind.*;
import javax.xml.stream.*;

import ca.phon.query.report.io.*;

/**
 * Methods for reading/writing report designs.
 *
 * @deprecated
 */
@Deprecated
public class ReportIO {
	
	// internal control for report versions.
	private final static String REPORT_VERSION = "2.0";
	
	/**
	 * Read in a report from the given path
	 * 
	 * @param path
	 * @return the report
	 * @throws IOException
	 */
	public static ReportDesign readDesign(String path) throws IOException {
		return readDesign(new File(path));
	}
	
	/**
	 * Read in a report from the given file.
	 * 
	 * @param file
	 * @return the report
	 * @throws IOException
	 */
	public static ReportDesign readDesign(File file) throws IOException {
		return readDesign(new FileInputStream(file));
	}
	
	/**
	 * Read in a report from the given stream.
	 * 
	 * @param is
	 * @return design
	 * @throws IOException 
	 */
	public static ReportDesign readDesign(InputStream is) throws IOException {
		ReportDesign retVal = null;
		try {
			JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			
			final XMLInputFactory factory = XMLInputFactory.newFactory();
			final XMLStreamReader reader = factory.createXMLStreamReader(is);
			final JAXBElement<ReportDesign> designEle =
					unmarshaller.unmarshal(reader, ReportDesign.class);
			retVal = designEle.getValue();
			
			if(!retVal.getVersion().equals(REPORT_VERSION)) {
				throw new IOException("Report version " + retVal.getVersion() + " no longer supported.  Must be " + REPORT_VERSION);
			}
		} catch (JAXBException e) {
			throw new IOException(e);
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
		
		return retVal;
	}
	
	/**
	 * Write design report to given path
	 * 
	 * @param report
	 * @param path
	 * @throws IOException
	 */
	public static void writeDesign(ReportDesign design, String path) throws IOException {
		writeDesign(design, new File(path));
	}
	
	/**
	 * Write design report to given path
	 * 
	 * @param report
	 * @param file
	 * @throws IOException
	 */
	public static void writeDesign(ReportDesign design, File file) throws IOException {
		writeDesign(design, new FileOutputStream(file));
	}
	
	/**
	 * Write design report to given path
	 * 
	 * @param report
	 * @param path
	 * @throws IOException
	 */
	public static void writeDesign(ReportDesign design, OutputStream os) throws IOException {
		try {
			design.setVersion(REPORT_VERSION);
			
			ObjectFactory factory = new ObjectFactory();
			JAXBContext context = JAXBContext.newInstance(factory.getClass());
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			marshaller.marshal(factory.createReportDesign(design), os);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}
}
