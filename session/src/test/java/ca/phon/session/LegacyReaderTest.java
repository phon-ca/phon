/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.session;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Formatter;

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
