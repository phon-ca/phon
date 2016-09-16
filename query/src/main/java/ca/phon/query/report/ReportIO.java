/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.query.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import ca.phon.query.report.io.ObjectFactory;
import ca.phon.query.report.io.ReportDesign;

/**
 * Methods for reading/writing report designs.
 *
 */
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
