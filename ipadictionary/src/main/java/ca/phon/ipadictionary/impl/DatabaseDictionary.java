/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.ipadictionary.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import ca.phon.ipadictionary.IPADictionary;
import ca.phon.ipadictionary.exceptions.IPADictionaryExecption;
import ca.phon.ipadictionary.spi.AddEntry;
import ca.phon.ipadictionary.spi.ClearEntries;
import ca.phon.ipadictionary.spi.IPADictionarySPI;
import ca.phon.ipadictionary.spi.LanguageInfo;
import ca.phon.ipadictionary.spi.RemoveEntry;
import ca.phon.util.Language;

/**
 * User-defined IPA dictionary where all entries are 
 * stored in the derby IPA database.
 *
 */
public class DatabaseDictionary implements IPADictionarySPI,
	LanguageInfo, AddEntry, RemoveEntry, ClearEntries {
	
	private static final Logger LOGGER = Logger
			.getLogger(DatabaseDictionary.class.getName());
	
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
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
	}

}
