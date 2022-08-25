/*
 * Copyright (C) 2005-2022 Gregory Hedlund & Yvan Rose
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

package ca.phon.util.alignedTypesDatabase;

import java.io.*;
import java.util.zip.*;

/**
 * Serialization methods {@link AlignedTypesDatabase} including compression.
 *
 */
public final class AlignedTypesDatabaseIO {

	/** Default extension for uncompressed database files */
	public final static String DB_EXT = ".atdb";

	/** Default extension for compressed database files */
	public final static String DBZ_EXT = ".atdz";

	private final static String INVALID_NAME_MSG = "Invalid file name, extension must be " + DB_EXT + " or " + DBZ_EXT;

	public static AlignedTypesDatabase readFromFile(String filename) throws IOException {
		return readFromFile(new File(filename));
	}

	public static AlignedTypesDatabase readFromFile(String filename, boolean compressed) throws IOException {
		return readFromFile(new File(filename), compressed);
	}

	public static AlignedTypesDatabase readFromFile(File dbFile) throws IOException {
		return readFromFile(dbFile, dbFile.getName().endsWith(DBZ_EXT));
	}

	public static AlignedTypesDatabase readFromFile(File dbFile, boolean compressed) throws IOException {
		checkExtension(dbFile, compressed);
		if(compressed) {
			final String basename = dbFile.getName().substring(0, dbFile.getName().length()-DBZ_EXT.length());
			final ZipFile zipFile = new ZipFile(dbFile);
			final ZipEntry zipEntry = zipFile.getEntry(basename + DB_EXT);
			if(zipEntry == null)
				throw new IOException("No database entry found in file " + dbFile.getAbsolutePath());
			try(final ObjectInputStream oin = new ObjectInputStream(zipFile.getInputStream(zipEntry))) {
				return (AlignedTypesDatabase) oin.readObject();
			} catch (ClassNotFoundException e) {
				throw new IOException(e);
			} finally {
				zipFile.close();
			}
		} else {
			try(final ObjectInputStream oin = new ObjectInputStream(new FileInputStream(dbFile))) {
				return (AlignedTypesDatabase) oin.readObject();
			} catch (ClassNotFoundException e) {
				throw new IOException(e);
			}
		}
	}

	public static void writeToFile(AlignedTypesDatabase db, String filename) throws IOException {
		writeToFile(db, new File(filename));
	}

	public static void writeToFile(AlignedTypesDatabase db, String filename, boolean compressed) throws IOException {
		writeToFile(db, new File(filename), compressed);
	}

	public static void writeToFile(AlignedTypesDatabase db, File dbFile) throws IOException {
		writeToFile(db, dbFile, dbFile.getName().endsWith(DBZ_EXT));
	}

	public static void writeToFile(AlignedTypesDatabase db, File dbFile, boolean compressed) throws IOException {
		checkExtension(dbFile, compressed);
		if(compressed) {
			final String basename = dbFile.getName().substring(0, dbFile.getName().length()-DBZ_EXT.length());
			try(final ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(dbFile))) {
				ZipEntry zipEntry = new ZipEntry(basename + DB_EXT);
				zout.putNextEntry(zipEntry);

				final ObjectOutputStream out = new ObjectOutputStream(zout);
				out.writeObject(db);
				out.flush();

				zout.closeEntry();
			}
		} else {
			try(final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dbFile))) {
				out.writeObject(db);
				out.flush();
			}
		}
	}

	private static void checkExtension(File file, boolean compressed) throws IOException {
		if(compressed) {
			if(!file.getName().endsWith(DBZ_EXT))
				throw new IOException(INVALID_NAME_MSG);
		} else {
			if(!file.getName().endsWith(DB_EXT))
				throw new IOException(INVALID_NAME_MSG);
		}
	}

}
