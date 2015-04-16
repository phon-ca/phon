/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.session;

import java.util.Formatter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * JUnit test for legacy session reader
 *
 */
@RunWith(JUnit4.class)
public class LegacyReaderTest {

	@Test
	public void roundTrip() throws Exception {
//		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
//		
//		final InputStream stream = getClass().getClassLoader().getResourceAsStream("tests/TestSession.xml");
//		final byte[] buffer = new byte[1024];
//		int read = -1;
//		while((read = stream.read(buffer)) > 0) {
//			byteStream.write(buffer, 0, read);
//		}
//		stream.close();
//		
//		final byte[] orig = byteStream.toByteArray();
//		final ByteArrayInputStream origStream = new ByteArrayInputStream(orig);
//		
//		final SessionInputFactory inputFactory = new SessionInputFactory();
//		final SessionReader reader = inputFactory.createReader("phonbank", "1.2");
//		Assert.assertNotNull(reader);
//		
//		final Session session = reader.readSession(origStream);
//		Assert.assertNotNull(session);
//		
//		byteStream = new ByteArrayOutputStream();
//		
//		final SessionOutputFactory outputFactory = new SessionOutputFactory();
//		final SessionWriter writer = outputFactory.createWriter("1.2");
//		writer.writeSession(session, byteStream);
//		final byte[] rt = byteStream.toByteArray();
//		
//		final MessageDigest origDigest = MessageDigest.getInstance("SHA-1");
//		origDigest.update(orig);
//		
//		final MessageDigest rtDigest = MessageDigest.getInstance("SHA-1");
//		rtDigest.update(rt);
//		
//		final String origDigestSt = toHexString(origDigest.digest());
//		final String rtDigestSt = toHexString(rtDigest.digest());
//		Assert.assertEquals(origDigestSt, rtDigestSt);
	}
	
	private static String toHexString(byte[] buffer) {
		final Formatter formatter = new Formatter();
		for(byte b:buffer) {
			formatter.format("%02x", b);
		}
		final String retVal = formatter.toString();
		formatter.close();
		return retVal;
	}
	
}
