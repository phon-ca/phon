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
package ca.phon.ipadictionary.impl;

import java.sql.*;
import java.util.*;

import org.apache.commons.lang3.*;
import org.apache.logging.log4j.*;

import ca.phon.ipadictionary.*;
import ca.phon.ipadictionary.exceptions.*;
import ca.phon.ipadictionary.spi.*;
import ca.phon.util.*;

/**
 * User-defined IPA dictionary where all entries are 
 * stored in the derby IPA database.
 *
 */
public class DatabaseDictionary implements IPADictionarySPI,
	LanguageInfo, AddEntry, RemoveEntry, ClearEntries {
	
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(DatabaseDictionary.class.getName());
	
	/**
	 * Language
	 */
	private Language language;
	
	/**
	 * Constructor
	 */
	public DatabaseDictionary(Language lang) {
		this.language = lang;
		
	}
	
	private void checkLangId(Connection conn) throws SQLException {
		final String checkSQL = 
				"SELECT * FROM language WHERE langId = ?";
		final PreparedStatement checkSt = conn.prepareStatement(checkSQL);
		checkSt.setString(1, getLanguage().toString());
		final ResultSet checkRs = checkSt.executeQuery();
		
		if(!checkRs.next()) {
			final String insertSQL = 
					"INSERT INTO language (langId) VALUES ( ? )";
			final PreparedStatement insertSt = conn.prepareStatement(insertSQL);
			insertSt.setString(1, getLanguage().toString());
			insertSt.execute();
			insertSt.close();
		}
		checkSt.close();
	}

	@Override
	public void removeEntry(String ortho, String ipa)
			throws IPADictionaryExecption {
		Connection conn = IPADatabaseManager.getInstance().getConnection();
		if(conn != null) {
			String qSt =
					"DELETE FROM transcript WHERE orthography = ?"
					+ "  AND ipa = ? AND langId = ?";
			try {
				PreparedStatement pSt = conn.prepareStatement(qSt);
				pSt.setString(1, ortho);
				pSt.setString(2, ipa);
				pSt.setString(3, getLanguage().toString());

				pSt.execute();
				pSt.close();
			} catch (SQLException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	public void addEntry(String ortho, String ipa)
			throws IPADictionaryExecption {
		Connection conn = IPADatabaseManager.getInstance().getConnection();
		if(conn != null) {
			String qSt = "INSERT INTO transcript (langId, orthography, ipa) VALUES( ?, ?, ? )";
			
			try {
				checkLangId(conn);
				PreparedStatement pSt = conn.prepareStatement(qSt);
				pSt.setString(1, getLanguage().toString());
				pSt.setString(2, ortho.toLowerCase());
				pSt.setString(3, ipa);
				
				pSt.execute();
				pSt.close();
			} catch (SQLException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	public Language getLanguage() {
		return this.language;
	}

	@Override
	public String[] lookup(String ortho) throws IPADictionaryExecption {
		Connection conn = IPADatabaseManager.getInstance().getConnection();
		List<String> retVal = new ArrayList<String>();
		
		if(conn != null) {
			ortho = StringUtils.strip(ortho, "?!\"'.\\/@&$()^%#*");
			String qSt = "SELECT * FROM transcript WHERE orthography = ? AND langId = ?";
			try {
				PreparedStatement pSt = conn.prepareStatement(qSt);
				pSt.setString(1, ortho.toLowerCase());
				pSt.setString(2, getLanguage().toString());
				
				java.sql.ResultSet rs = pSt.executeQuery();
				while(rs.next()) {
					retVal.add(rs.getString("IPA"));
				}
				rs.close();
				pSt.close();
			} catch (SQLException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		
		return retVal.toArray(new String[0]);
	}

	@Override
	public void install(IPADictionary dict) {
		dict.putExtension(LanguageInfo.class, this);
		dict.putExtension(AddEntry.class, this);
		dict.putExtension(RemoveEntry.class, this);
	}

	@Override
	public void clear() throws IPADictionaryExecption {
		Connection conn = IPADatabaseManager.getInstance().getConnection();
		
		if(conn != null) {
			String qSt = "DELETTE * FROM transcript WHERE langId = ?";
			try {
				PreparedStatement pSt = conn.prepareStatement(qSt);
				pSt.setString(1, getLanguage().toString());
				
				pSt.executeUpdate();
			} catch (SQLException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		
	}

}
