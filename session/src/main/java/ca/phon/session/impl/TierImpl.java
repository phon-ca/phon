package ca.phon.session.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.session.Tier;
import ca.phon.session.TierDescription;

public class TierImpl<T> implements Tier<T> {
	
	private final static Logger LOGGER = Logger.getLogger(TierImpl.class.getName());
	
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
			if(!grouped && idx > 0) {
				throw new ArrayIndexOutOfBoundsException(idx);
			}
			if(!grouped && idx == 0 && tierData.size() == 0) {
				// create a new object to return
				try {
					final T val = getDeclaredType().newInstance();
					tierData.add(val);
				} catch (InstantiationException e) {
					LOGGER.log(Level.WARNING, e.getMessage(), e);
				} catch (IllegalAccessException e) {
					LOGGER.log(Level.WARNING, e.getMessage(), e);
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
			if(tierData.size() <= idx)
				tierData.add(idx, val);
			else
				tierData.set(idx, val);
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
	public void removeGroup(int idx) {
		synchronized(tierData) {
			if(!grouped && idx > 0) {
				throw new ArrayIndexOutOfBoundsException(idx);
			}
			tierData.remove(idx);
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
	
	@Override
	public Iterator<T> iterator() {
		return tierData.iterator();
	}
	
}
