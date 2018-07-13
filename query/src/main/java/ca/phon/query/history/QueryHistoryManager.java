package ca.phon.query.history;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import ca.phon.util.PrefHelper;

/**
 * Responsible for loading and saving query history files. Also includes
 * utility methods for some useful query history functions like adding
 * to the history.
 */
public class QueryHistoryManager {
	
	private final static Logger LOGGER = Logger.getLogger(QueryHistoryManager.class.getName());

	public final static String QUERY_HISTORY_FOLDER = QueryHistoryManager.class.getName() + ".queryHistoryFolder";
	public final static String DEFAULT_HISTORY_FOLDER = PrefHelper.getUserDataFolder() + File.separator + "query_history";
	private String queryHistoryFolder = PrefHelper.get(QUERY_HISTORY_FOLDER, DEFAULT_HISTORY_FOLDER);

	public final static String QUERY_HISTORY_MAXLENGTH = QueryHistoryManager.class.getName() + ".maxLength";
	public final static int DEFAULT_QUERY_HISTORY_MAXLENGTH = 100;
	private int maxLength = PrefHelper.getInt(QUERY_HISTORY_MAXLENGTH, DEFAULT_QUERY_HISTORY_MAXLENGTH);
	
	public QueryHistoryManager() {
	}
	
	public String getQueryHistoryFolder() {
		return this.queryHistoryFolder;
	}

	public void setQueryHistoryFolder(String folder) {
		PrefHelper.getUserPreferences().put(QUERY_HISTORY_FOLDER, folder);
		this.queryHistoryFolder = PrefHelper.get(QUERY_HISTORY_FOLDER, DEFAULT_HISTORY_FOLDER);
	}
	
	public int getHistoryMaxLength() {
		return this.maxLength;
	}
	
	public void setHistoryMaxLength(int maxLength) {
		PrefHelper.getUserPreferences().putInt(QUERY_HISTORY_MAXLENGTH, maxLength);
		this.maxLength = PrefHelper.getInt(QUERY_HISTORY_MAXLENGTH, DEFAULT_QUERY_HISTORY_MAXLENGTH);
	}

	public void createQueryHistoryFolder() {
		final File queryHistoryFolder = new File(getQueryHistoryFolder());
		if(!queryHistoryFolder.exists()) {
			queryHistoryFolder.mkdirs();
		}
	}
	
	public QueryHistoryType loadQueryHistory(File historyFile) throws IOException {
		return loadQueryHistory(new FileInputStream(historyFile));
	}
	
	public QueryHistoryType loadQueryHistory(InputStream inputStream) throws IOException {
		final ObjectFactory objFactory = new ObjectFactory();
		try {
			final JAXBContext ctx = JAXBContext.newInstance(objFactory.getClass());
			final Unmarshaller unmarshaller = ctx.createUnmarshaller();
			
			final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			final XMLStreamReader streamReader = inputFactory.createXMLStreamReader(inputStream);
			
			final JAXBElement<QueryHistoryType> queryHistoryEle = unmarshaller.unmarshal(streamReader, QueryHistoryType.class);
			return queryHistoryEle.getValue();
		} catch(JAXBException | XMLStreamException e) {
			throw new IOException(e);
		} finally {
			inputStream.close();
		}
	}
	
	public void saveQueryHistory(QueryHistoryType queryHistory, File queryHistoryFile) throws IOException {
		createQueryHistoryFolder();
		saveQueryHistory(queryHistory, new FileOutputStream(queryHistoryFile));
	}
	
	public void saveQueryHistory(QueryHistoryType queryHistory, OutputStream outputStream) throws IOException {
		final ObjectFactory objFactory = new ObjectFactory();
		try {
			final JAXBContext ctx = JAXBContext.newInstance(objFactory.getClass());
			final Marshaller marshaller = ctx.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			marshaller.marshal(objFactory.createQueryHistory(queryHistory), outputStream);
		} catch(JAXBException e) {
			throw new IOException(e);
		} finally {
			outputStream.close();
		}
	}
	
	/**
	 * Saves the query history for the given query name.
	 * 
	 * @param queryHistory
	 * @param queryName
	 * @throws IOException
	 */
	public void saveQueryHistory(QueryHistoryType queryHistory, String queryName) throws IOException {
		final File queryHistoryFile = new File(getQueryHistoryFolder(), queryName + ".xml");
		saveQueryHistory(queryHistory, queryHistoryFile);
	}
	
	/**
	 * Load or creates and returns the query history for the given query name.
	 * 
	 * @param queryName
	 * @return
	 */
	public QueryHistoryType getQueryHistory(String queryName) {
		final File queryHistoryFile = new File(getQueryHistoryFolder(), queryName + ".xml");
		final ObjectFactory factory = new ObjectFactory();
		QueryHistoryType retVal = null;
		try {
			retVal = loadQueryHistory(queryHistoryFile);
		} catch (IOException e) {
			LOGGER.info(e.getMessage());
			retVal = factory.createQueryHistoryType();
		}
		return retVal;
	}
	
	/**
	 * Add to the query history.  New items are added to the beginning of the list like a stack.
	 * Items at the end of the list greater than maxLength are removed.
	 *
	 * @param queryHistory
	 * @param queryInfo
	 * @return list of removed query history elements (if any)
	 */
	public List<QueryInfoType> addQueryInfo(QueryHistoryType queryHistory, QueryInfoType queryInfo) {
		List<QueryInfoType> retVal = new ArrayList<>();
		
		// search for existing query based on hash of parameters
		int queryIndex = -1;
		for(int i = 0; i < queryHistory.getQuery().size(); i++) {
			final QueryInfoType current = queryHistory.getQuery().get(i);
			if(current.getHash().equalsIgnoreCase(queryInfo.getHash())) {
				queryIndex = i;
				break;
			}
		}
		QueryInfoType qi = (queryIndex >= 0 ? queryHistory.getQuery().remove(queryIndex) : queryInfo);
		if(queryIndex >= 0) {
			qi.setDate(queryInfo.getDate());
		}
		qi.setParams(queryInfo.getParams());
		queryHistory.getQuery().add(qi);
		
		while(queryHistory.getQuery().size() > getHistoryMaxLength()) {
			retVal.add(queryHistory.getQuery().remove(queryHistory.getQuery().size()-1));
		}
		
		return retVal;
	}
	
}
