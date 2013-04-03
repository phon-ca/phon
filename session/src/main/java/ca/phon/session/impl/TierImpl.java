package ca.phon.session.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.phon.session.Tier;
import ca.phon.session.TierDescription;

public class TierImpl<T> implements Tier<T> {
	
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
			tierData.size();
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

}
