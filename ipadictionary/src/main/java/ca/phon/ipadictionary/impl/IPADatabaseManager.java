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
package ca.phon.ipadictionary.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;
import org.apache.logging.log4j.LogManager;

import ca.phon.ipadictionary.IPADictionaryLibrary;
import ca.phon.util.Language;
import ca.phon.util.PrefHelper;
import ca.phon.worker.PhonWorker;

/**
 * Handles creation and connections to the IPA transcription
 * database.
 */
public class IPADatabaseManager {
	
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(IPADatabaseManager.class.getName());

	/** The database name */
	private final static String _dbName = "ipadb";
	
	/** The database location */
	private final static String _dbLoc = PrefHelper.getUserDataFolder();
	
	/** The database driver */
	private final static String _driverName = "org.apache.derby.jdbc.EmbeddedDriver";
	
	/* Load the driver */
	static {
		try {
			LOGGER.info("[IPADatabaseManager]: Loading JDBC driver - " + _driverName);
			Class.forName(_driverName);
		} catch (ClassNotFoundException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
		
		PhonWorker.getShutdownThread().invokeLater(new Runnable() {

			@Override
			public void run() {
				IPADatabaseManager.getInstance().shutdown();
			}
			
		});
	}
	
	/**
	 * Get the database jdbc connection string.
	 * 
	 */
	public static String getConnectionString() {
		String retVal = 
			"jdbc:derby:"+_dbLoc+File.separator+_dbName + ";shutdown=true";
		return retVal;
	}
	
	
	private static IPADatabaseManager _instance = null;

	/**
	 * Get the database instance.
	 */
	public static IPADatabaseManager getInstance() {
		if(_instance == null) {
			_instance = new IPADatabaseManager();
		}
		return _instance;
	}
	
	/** Connection pool
	 * 
	 */
	private MiniConnectionPoolManager connPool;
	
	/** Keep one connection for every thread */
	private Map<Thread, Connection> connections =
		new LinkedHashMap<Thread, Connection>();
	
	/** Stored prepared statements */
	private Map<Connection, Map<String, PreparedStatement>> connStatements
		= new LinkedHashMap<Connection, Map<String, PreparedStatement>>();
	
	/**
	 * Hidden constructor
	 */
	private IPADatabaseManager() {
		// we assume database has been created
		// and filled with data
		
		// setup connection pool
		EmbeddedConnectionPoolDataSource dataSource = new EmbeddedConnectionPoolDataSource();
		
		File dbLoc = new File(_dbLoc + File.separator + _dbName);
		boolean create = false;
		if(!dbLoc.exists()) {
			dataSource.setCreateDatabase("create");
			create = true;
		}
		dataSource.setDatabaseName(_dbLoc + File.separator + _dbName);
		
		connPool = new MiniConnectionPoolManager(dataSource, 10);
		
		if(create)
			createDatabase();
		
		LOGGER.info("[IPADatabaseManager]: Database location = " + _dbLoc + File.separator + _dbName +
				", maxConnections = 10");

	}
	
	/**
	 * Create a new connection to the database.
	 * 
	 * @return a jdbc db connection, <CODE>null</CODE> if an
	 * error occured
	 */
	public Connection getConnection() {
		cleanupConnections();
		// get the current thread
		Thread currentThread = Thread.currentThread();
		
		Connection retVal = connections.get(currentThread);
		if(retVal == null) {
			try {
				retVal = connPool.getConnection();
			} catch (SQLException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
			connections.put(currentThread, retVal);
		}
		
		return retVal;
	}
	
	/**
	 * Cleanup connections for dead threads
	 */
	private void cleanupConnections() {
		List<Thread> toRemove = new ArrayList<Thread>();
		
		for(Thread th:connections.keySet()) {
			if(!th.isAlive()) {
				LOGGER.info("[QueryDBManager]: Cleaning up connection for thread - " + 
						th.getName());
				Connection conn = connections.get(th);
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.error(e.getLocalizedMessage(), e);
				}
				toRemove.add(th);
			}
		}
		
		for(Thread th:toRemove) {
			connections.remove(th);
		}
	}
	
	/**
	 * Shutdown the database.
	 * 
	 */
	public void shutdown() {
		// close all of the open prepared statements
		LOGGER.info("[IPADatabaseManager]: Closing prepared statements.");
		for(Connection conn:connStatements.keySet()) {
			Map<String, PreparedStatement> statements = connStatements.get(conn);
			for(PreparedStatement pst:statements.values()) {
				try {
					pst.close();
				} catch (SQLException e) {
					LOGGER.error(e.getLocalizedMessage(), e);
				}
			}
			statements.clear();
		}
		connStatements.clear();
		
		// close opened connections
		for(Connection conn:connections.values()) {
			try {
				conn.close();
			} catch (SQLException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		
		try {
			DriverManager.getConnection(getConnectionString());
			LOGGER.info("[IPADatabaseManager]: Database shutdown normally");
		} catch (SQLException e) {
			// A known derby issue is to get XJ015 when 
			// shutting down the database
			if ( e.getSQLState().equals("XJ015") ) {
				LOGGER.warn(e.toString());
			} else {
				LOGGER.info("[IPADatabaseManager]: Database shutdown normally");
			}
		}
	}
	
	/**
	 * Get the prepared statement for the given
	 * sql string.  If the statement does not exist
	 * then it's created.
	 * 
	 * @param sql
	 * @param genKeys
	 */
	public PreparedStatement prepareStatement(Connection conn, String sql) 
		throws SQLException {
		
		Map<String, PreparedStatement> statements =
			connStatements.get(conn);
		if(statements == null) {
			statements = new LinkedHashMap<String, PreparedStatement>();
			connStatements.put(conn, statements);
		}
		
		PreparedStatement retVal = statements.get(sql);
		
		if(retVal == null) {
			retVal = conn.prepareStatement(sql);
			statements.put(sql, retVal);
		}
		
		return retVal;
	}
	
	/**
	 * Get the prepared statement for the given
	 * sql string.  If the statement does not exist
	 * then it's created.
	 * 
	 * @param sql
	 * @param genKeys
	 */
	public PreparedStatement prepareStatement(Connection conn, String sql, int genKeys) 
		throws SQLException {
		
		Map<String, PreparedStatement> statements = 
			connStatements.get(conn);
		if(statements == null) {
			statements = new LinkedHashMap<String, PreparedStatement>();
			connStatements.put(conn, statements);
		}
		
		PreparedStatement retVal = statements.get(sql);
		
		if(retVal == null) {
			retVal = conn.prepareStatement(sql, genKeys);
			statements.put(sql, retVal);
		}
		
		return retVal;
	}
	
	/**
	 * Get the prepared statement for the given
	 * sql string.  If the statement does not exist
	 * then it's created.
	 * 
	 * @param sql
	 * @param genKeys
	 */
	public PreparedStatement prepareStatement(Connection conn, String sql, int rsType, int concur) 
		throws SQLException {
		
		Map<String, PreparedStatement> statements = 
			connStatements.get(conn);
		if(statements == null) {
			statements = new LinkedHashMap<String, PreparedStatement>();
			connStatements.put(conn, statements);
		}
		
		PreparedStatement retVal = statements.get(sql);
		
		if(retVal == null) {
			retVal = conn.prepareStatement(sql, rsType, concur);
			statements.put(sql, retVal);
		}
		
		return retVal;
	}
	
	/*
	 * Table creation queries
	 * 
	 */
	private final static String langTbl = 
		"CREATE TABLE language ("		+
		"langId VARCHAR(10) NOT NULL, "	+
		"name VARCHAR(256), "			+
		"PRIMARY KEY (langId)"			+
		")";
	
	private final static String transcriptTbl =
		"CREATE TABLE transcript ("		+
		"langId VARCHAR(10) NOT NULL,"	+
		"orthography VARCHAR(256) NOT NULL,"		+
		"ipa VARCHAR(256) NOT NULL,"				+
		"PRIMARY KEY (langId, orthography, ipa)," +
		"CONSTRAINT tblLang_id " +
		"FOREIGN KEY (langId) " +
		"REFERENCES language(langId) " +
		"ON DELETE CASCADE"	+
		")";
	
	/**
	 * Initialize database.  Called when the database first
	 * needs to be created.  Fills data from text files in
	 * the /data/dictionary directory.
	 */
	private void createDatabase() {
		if(!createTables()) {
			for(Language lang:IPADictionaryLibrary.getInstance().availableLanguages()) {
				String langId = lang.toString();
				String name = lang.getPrimaryLanguage().getName();
				
				// add a new database entry for this language
				Connection conn = IPADatabaseManager.getInstance().getConnection();
				if(conn != null) {
					String qSt = "INSERT INTO language (langId, name) VALUES ( ? , ? )";
					try {
						PreparedStatement pSt = conn.prepareStatement(qSt);
						pSt.setString(1, langId);
						pSt.setString(2, name);
						
						pSt.execute();
					} catch (SQLException e) {
						LOGGER.error(
								e.getLocalizedMessage(), e);
					}
				}
			}
		} else {
			LOGGER.error( 
					"[IPADatabaseManager]: Could not create tables");
		}
		
	}
	
	/**
	 * Create the tables in the database
	 */
	private boolean createTables() {
		boolean retVal = false;
		// language table
		Connection conn = getConnection();
		
		if(conn != null) {
			try {
				PreparedStatement st1 = 
					conn.prepareStatement(langTbl);
				retVal = st1.execute();
				
				PreparedStatement st2 = 
					conn.prepareStatement(transcriptTbl);
				retVal &= st2.execute();
				
			} catch (SQLException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		
		return retVal;
	}
	
	/**
	 * Create a new dictionary with given language and name
	 * 
	 * @param langId
	 * @param name
	 * 
	 * @return <code>true</code> if dictionary was created, <code>false</code>
	 *  otherwise
	 */
	public boolean createDictionary(String langId, String name) {
		final Set<Language> currentLangs = getAvailableLanguages();
		boolean alreadyExists = false;
		for(Language lang:currentLangs) if(lang.toString().equals(langId)) alreadyExists = true;
		if(alreadyExists)
			throw new IllegalArgumentException("Language with id '" + langId + "' already exists.");
		
		// check language id
		try {
			Language.parseLanguage(langId);
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to parse language id", e);
		}
		
		// add a new database entry for this language
		Connection conn = getConnection();
		if(conn != null) {
			String qSt = "INSERT INTO language (langId, name) VALUES ( ? , ? )";
			try {
				PreparedStatement pSt = conn.prepareStatement(qSt);
				pSt.setString(1, langId);
				pSt.setString(2, name);
				
				pSt.execute();
				return true;
			} catch (SQLException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
				return false;
			}
		}
		return false;
	}
	
	public boolean clearDictionary(String langId) {
		// add a new database entry for this language
		Connection conn = getConnection();
		if(conn != null) {
			String qSt = "DELETE FROM language WHERE langId = ?";
			try {
				PreparedStatement pSt = conn.prepareStatement(qSt);
				pSt.setString(1, langId);
				
				pSt.execute();
				return true;
			} catch (SQLException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
				return false;
			}
		}
		return false;
	}
	
	/**
	 * Return a list of available languages in the database.
	 * 
	 * @return list of languages
	 */
	public Set<Language> getAvailableLanguages() {
		final Set<Language> retVal = new TreeSet<Language>();
		
		Connection conn = getConnection();
		if(conn != null) {
			final String qSt = "SELECT * FROM language ORDER BY langId";
			try {
				final PreparedStatement pSt = conn.prepareStatement(qSt);
				final ResultSet rs = pSt.executeQuery();
				
				while(rs.next()) {
					final String langId = rs.getString("langId");
					if(langId != null) {
						try {
							final Language lang = Language.parseLanguage(langId);
							retVal.add(lang);
						} catch (IllegalArgumentException e) {
							LOGGER.error(
									e.getLocalizedMessage(), e);
						}
					}
				}
			} catch (SQLException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		
		return retVal;
	}
	
	public void saveDataToFile(String textFile, String langId) 
		throws IOException {
		final String querySt = 
				"SELECT orthography, ipa FROM transcript WHERE langId = ? ORDER BY orthography";
		
		final File outFile = new File(textFile);
		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outFile), "UTF-8"));
		
		Connection conn = getConnection();
		// add language to langauge table
		try {
			PreparedStatement pSt = 
				conn.prepareStatement(querySt);
			
			pSt.setString(1, langId);
			
			final StringBuilder sb = new StringBuilder();
			final ResultSet rs = pSt.executeQuery();
			while(rs.next()) {
				sb.append(rs.getString("orthography")).append('\t').append(rs.getString("ipa")).append('\n');
				writer.write(sb.toString());
				sb.setLength(0);
			}
			writer.flush();
			writer.close();
		} catch (SQLException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	}
	
	/**
	 * Fill database with data from plain text file.
	 * File should be in format <ortho>WS<ipa> (where WS is whitespace.)
	 */
	public void addDataFromFile(String textFile, String langId, String langName) 
		throws IOException {
		
		String langSt = 
			"INSERT INTO language VALUES ( ? , ? )";
		String transSt = 
			"INSERT INTO transcript (langId, orthography, ipa) VALUES ( ?, ?, ? )";
		Connection conn = getConnection();
		
		final String regex = "(.+)" +  // the orthography
			"(?:\\p{Space})" +			// the spacer (not caputured)
			"(.+)";			// the transcript
		final Pattern pattern = Pattern.compile(regex);
		
		// add language to language table
		try {
			PreparedStatement pSt = 
				conn.prepareStatement(langSt);
			
			pSt.setString(1, langId);
			pSt.setString(2, langName);
			
			int r = pSt.executeUpdate();
			if(r == 0) {
				LOGGER.warn(
						"Could not add language \"" + langId + "\" with name \"" + langName + "\"");
			}
		} catch (SQLException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(textFile), "UTF-8"));
		String line = null;
		while((line = in.readLine()) != null) {
			Matcher m = pattern.matcher(line);
			if(m.matches()) {
				String ortho = m.group(1);
				String ipa = m.group(2);
				
				try {
					PreparedStatement pSt = 
						conn.prepareStatement(transSt);
					pSt.setString(1, langId);
					pSt.setString(2, ortho);
					pSt.setString(3, ipa);
					
					int r = pSt.executeUpdate();
					if(r == 0) {
						LOGGER.warn(
								"Could not add transcript \"" + ipa + "\" for orthography \"" + ortho + "\"");
					}
				} catch (SQLException e) {
					LOGGER.error(e.getLocalizedMessage(), e);
				}
			}
		}
		in.close();
	}

	public boolean dropDictionary(String lang) {
		String langSt = 
				"DELETE FROM language WHERE langId = ?";
		final Connection conn = getConnection();
		
		boolean retVal = true;
		try {
			PreparedStatement pSt = conn.prepareStatement(langSt);
			pSt.setString(1, lang);
			
			int r = pSt.executeUpdate();
			if(r == 0) {
				retVal = false;
			}
		} catch (SQLException e) {
			LOGGER.error(e.getLocalizedMessage(), e);
			retVal = false;
		}
		return retVal;
	}

}
