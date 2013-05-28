/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.query.script;

import java.util.Collection;
import java.util.Map;

import ca.phon.engines.search.db.QueryFactory;
import ca.phon.engines.search.db.QueryManager;
import ca.phon.engines.search.db.Result;
import ca.phon.engines.search.db.ResultSet;

/**
 * A js host object used to collect results during a search.
 */
public class SResults {
	
	/** The actual results */
	private ResultSet searchResults;
	
	private final QueryFactory qFactory;
	
	/**
	 * Constructor
	 * 
	 * @param transcript the transcript
	 */
	public SResults(ResultSet s) {
		super();
		
		final QueryManager qManager = QueryManager.getSharedInstance();
		qFactory = qManager.createQueryFactory();
		
//		this.searchResults = new Sear(t);
		this.searchResults = s;
//		searchResults.setSearchString("Javascript");
//		searchResults.setSearchType("Scripted Search");
	}
	
	public void add(ResultValue result) {
		add(result, "", "LINEAR");
	}
	
	public void add(ResultValue result, String meta) {
		add(result, meta, "LINEAR");
	}
	
	/**
	 * Add a result.
	 * 
	 * @param result
	 */
	public void add(ResultValue result, String meta, String type) {
		final Result dbResult = qFactory.createResult();
		dbResult.setRecordIndex(result.getRecordIndex());
		dbResult.setSchema(type);
		
		if(dbResult != null) {
			dbResult.getMetadata().put(Result.DEFAULT_META, meta);
			final ca.phon.engines.search.db.ResultValue rv = qFactory.createResultValue();
			
			rv.setTierName(result.getTier());
			rv.setGroupIndex(result.getGroupIndex());
			rv.setRange(result.getDataRange());
			rv.setData(result.toString());
			
			dbResult.getResultValues().add(rv);
		}
		searchResults.getResults().add(dbResult);
	}
	
	public void add(ResultValue result, Map<String, String> meta, String type) {
		final Result dbResult = qFactory.createResult();
		dbResult.setRecordIndex(result.getRecordIndex());
		dbResult.setSchema(type);
		
		if(dbResult != null) {
			dbResult.getMetadata().putAll(meta);
			final ca.phon.engines.search.db.ResultValue rv = qFactory.createResultValue();
			
			rv.setTierName(result.getTier());
			rv.setGroupIndex(result.getGroupIndex());
			rv.setRange(result.getDataRange());
			rv.setData(result.toString());
			
			dbResult.getResultValues().add(rv);
		}
		searchResults.getResults().add(dbResult);
	}
	
	public void add(Collection<ResultValue> results) {
		add(results, "", "LINEAR");
	}
	
	public void add(Collection<ResultValue> results, String meta) {
		add(results, meta, "LINEAR");
	}
	
	/**
	 * Add a list of results as a single result value.
	 * 
	 * @param result
	 */
	public void add(Collection<ResultValue> results, String meta, String type) {
		add(results.toArray(new ResultValue[0]), meta, type);
	}
	
	public void add(Collection<ResultValue> results, Map<String, String> meta) {
		add(results, meta, "LINEAR");
	}
	
	public void add(Collection<ResultValue> results, Map<String, String> meta, String type) {
		add(results.toArray(new ResultValue[0]), meta, type);
	}
	
	public void add(ResultValue[] results) {
		add(results, "", "LINEAR");
	}
	
	public void add(ResultValue[] results, String meta) {
		add(results, meta, "LINEAR");
	}
	
	/**
	 * Add an array of results.
	 */
	public void add(ResultValue[] results, String meta, String type) {
		// first, make sure all of the result come
		// from the same record
		int uttIndex = -1;
		for(ResultValue rv:results) {
			if(uttIndex < 0)
				uttIndex = rv.getRecordIndex();
			else {
				if(uttIndex != rv.getRecordIndex())
					throw new IllegalArgumentException("Result values must come from the same record");
			}
		}

		final Result dbResult = qFactory.createResult();
		dbResult.setRecordIndex(uttIndex);
		dbResult.setSchema(type);
		
		if(dbResult != null) {
			dbResult.getMetadata().put(Result.DEFAULT_META, meta);
			
			for(ResultValue result:results) {
				final ca.phon.engines.search.db.ResultValue rv = qFactory.createResultValue();
				rv.setTierName(result.getTier());
				rv.setGroupIndex(result.getGroupIndex());
				rv.setRange(result.getDataRange());
				rv.setData(result.toString());
				dbResult.getResultValues().add(rv);
			}
			
		}
		searchResults.getResults().add(dbResult);
	}
	
	public void add(ResultValue[] results, Map<String, String> meta) {
		add(results, meta, "LINEAR");
	}
	
	public void add(ResultValue[] results, Map<String, String> meta, String type) {
		// first, make sure all of the result come
		// from the same record
		int uttIndex = -1;
		for(ResultValue rv:results) {
			if(uttIndex < 0)
				uttIndex = rv.getRecordIndex();
			else {
				if(uttIndex != rv.getRecordIndex())
					throw new IllegalArgumentException("Result values must come from the same record");
			}
		}

		final Result dbResult = qFactory.createResult();
		dbResult.setRecordIndex(uttIndex);
		dbResult.setSchema(type);
		
		if(dbResult != null) {
			dbResult.getMetadata().putAll(meta);
			
			for(ResultValue result:results) {
				final ca.phon.engines.search.db.ResultValue rv = qFactory.createResultValue();
				rv.setTierName(result.getTier());
				rv.setGroupIndex(result.getGroupIndex());
				rv.setRange(result.getDataRange());
				rv.setData(result.toString());
				
				dbResult.getResultValues().add(rv);
			}
			
		}
		searchResults.getResults().add(dbResult);
	}

//	/**
//	 * @return the searchResults
//	 */
//	public SearchResults getSearchResults() {
//		return searchResults;
//	}

	public int size() {
		return searchResults.size();
	}
	
	public int getLength() {
		return size();
	}
	
}
