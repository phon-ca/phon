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
package ca.phon.session.impl;

import java.util.*;

import ca.phon.formatter.*;
import ca.phon.session.spi.*;

public class TierImpl<T> implements TierSPI<T> {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(TierImpl.class.getName());
	
	/**
	 * Declared type
	 */
	private final Class<T> declaredType;
	
	/**
	 * name
	 */
	private final String tierName;
	
	/**
	 * is this a grouped tier
	 */
	private final boolean grouped;
	
	/**
	 * Group data
	 */
	private final List<T> tierData =
			Collections.synchronizedList(new ArrayList<T>());

	
	
	/**
	 * Constructor
	 * 
	 * @param name
	 * @param type
	 * @param grouped
	 */
	TierImpl(String name, Class<T> type, boolean grouped) {
		super();
		this.tierName = name;
		this.declaredType = type;
		this.grouped = grouped;

	}

	@Override
	public int numberOfGroups() {
		int retVal = 0;
		synchronized(tierData) {
			retVal = tierData.size();
		}
		if(retVal > 0 && !grouped)
			retVal = 1;
		return retVal;
	}

	@Override
	public T getGroup(int idx) {
		T retVal = null;
		synchronized(tierData) {
			if(grouped && (idx < 0 || idx >= numberOfGroups())) {
				throw new ArrayIndexOutOfBoundsException(idx);
			}
			if(!grouped && idx == 0 && tierData.size() == 0) {
				// create a new object to return
				try {
					final T val = getDeclaredType().newInstance();
					tierData.add(val);
				} catch (InstantiationException e) {
					LOGGER.warn( e.getMessage(), e);
				} catch (IllegalAccessException e) {
					LOGGER.warn( e.getMessage(), e);
				}
			}
			retVal = tierData.get(idx);
		}
		return retVal;
	}

	@Override
	public void setGroup(int idx, T val) {
		synchronized(tierData) {
			if(!grouped && idx > 0) {
				throw new ArrayIndexOutOfBoundsException(idx);
			}
			
			// add empty groups if necessary
			while(idx > tierData.size()) {
				tierData.add(getDeclaredType().cast(FormatterUtil.parse(getDeclaredType(), "")));
			}
			if(idx == tierData.size())
				tierData.add(val);
			else
				tierData.set(idx, val);
		}
		
	}
	
	@Override
	public void addGroup() {
		final Class<? extends T> type = getDeclaredType();
		try {
			final T obj = type.newInstance();
			addGroup(obj);
		} catch (InstantiationException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		} catch (IllegalAccessException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	}
	
	@Override
	public void addGroup(int idx) {
		final Class<? extends T> type = getDeclaredType();
		try {
			final T obj = type.newInstance();
			addGroup(idx, obj);
		} catch (InstantiationException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		} catch (IllegalAccessException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void addGroup(T val) {
		synchronized(tierData) {
			if(!grouped && tierData.size() > 0) {
				throw new ArrayIndexOutOfBoundsException("Un-grouped tiers may only have one group.");
			}
			tierData.add(val);
		}
	}
	
	@Override
	public void addGroup(int idx, T val) {
		synchronized (tierData) {
			if(!grouped && tierData.size() > 0) {
				throw new ArrayIndexOutOfBoundsException("Un-grouped tiers may only have one group.");
			}
			if(idx < 0 || idx > tierData.size()) { 
				throw new ArrayIndexOutOfBoundsException(idx);
			}
			if(val == null) {
				throw new NullPointerException();
			}
			tierData.add(idx, val);
		}
	}

	@Override
	public T removeGroup(int idx) {
		synchronized(tierData) {
			if(!grouped && idx > 0) {
				throw new ArrayIndexOutOfBoundsException(idx);
			}
			return tierData.remove(idx);
		}
	}

	@Override
	public void removeAll() {
		synchronized(tierData) {
			tierData.clear();
		}
	}

	@Override
	public boolean isGrouped() {
		return grouped;
	}
	
	@Override
	public String getName() {
		return tierName;
	}

	@Override
	public Class<T> getDeclaredType() {
		return declaredType;
	}
	
}
