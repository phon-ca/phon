package ca.phon.session.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.session.Tier;
import ca.phon.session.TierListener;
import ca.phon.session.UnvalidatedValue;

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

	/**
	 * Tier listeners, using a {@link WeakHashMap} so that listeners
	 * are removed when their references are no longer needed.  The second
	 * {@link Boolean} parameter is unused
	 */
	private final Map<TierListener<T>, Boolean> tierListeners;
	
	/**
	 * Extension support
	 */
	private final ExtensionSupport extSupport = new ExtensionSupport(Tier.class, this);
	
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
		
		extSupport.initExtensions();
		
		final WeakHashMap<TierListener<T>, Boolean> weakHash = 
				new WeakHashMap<TierListener<T>, Boolean>();
		tierListeners = Collections.synchronizedMap(weakHash);
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
		final T oldVal = (idx < numberOfGroups() ? getGroup(idx) : null);
		synchronized(tierData) {
			if(!grouped && idx > 0) {
				throw new ArrayIndexOutOfBoundsException(idx);
			}
			if(tierData.size() <= idx)
				tierData.add(idx, val);
			else
				tierData.set(idx, val);
		}
		fireTierGroupChanged(idx, oldVal, val);
	}
	
	@Override
	public void addGroup() {
		final Class<? extends T> type = getDeclaredType();
		try {
			final T obj = type.newInstance();
			addGroup(obj);
		} catch (InstantiationException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (IllegalAccessException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	@Override
	public void addGroup(int idx) {
		final Class<? extends T> type = getDeclaredType();
		try {
			final T obj = type.newInstance();
			addGroup(idx, obj);
		} catch (InstantiationException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (IllegalAccessException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
		fireTierGroupAdded(numberOfGroups()-1, val);
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
		fireTierGroupAdded(idx, val);
	}

	@Override
	public void removeGroup(int idx) {
		final T val = getGroup(idx);
		synchronized(tierData) {
			if(!grouped && idx > 0) {
				throw new ArrayIndexOutOfBoundsException(idx);
			}
			tierData.remove(idx);
		}
		fireTierGroupRemoved(idx, val);
	}

	@Override
	public void removeAll() {
		synchronized(tierData) {
			tierData.clear();
		}
		fireTierGroupsCleared();
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
	
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		
		if(isGrouped()) {
			buffer.append("[");
			for(int i = 0; i < numberOfGroups(); i++) {
				if(i > 0) buffer.append("] [");
				final T grpVal = getGroup(i);
				String grpTxt = (grpVal != null ? grpVal.toString() : "");
				if(grpTxt.length() == 0) {
					// XXX Check for unvalidated values
					if(grpVal instanceof IExtendable) {
						final IExtendable extGrp = (IExtendable)grpVal;
						final UnvalidatedValue uv = extGrp.getExtension(UnvalidatedValue.class);
						if(uv != null) 
							grpTxt = uv.getValue();
					}
				}
				if(grpVal != null)
					buffer.append(grpTxt);
			}
			buffer.append("]");
		} else {
			if(numberOfGroups() > 0) {
				final T grpVal = getGroup(0);
				String grpTxt = (grpVal != null ? grpVal.toString() : "");
				if(grpTxt.length() == 0 && grpVal instanceof IExtendable) {
					final IExtendable extGrp = (IExtendable)grpVal;
					final UnvalidatedValue uv = extGrp.getExtension(UnvalidatedValue.class);
					if(uv != null) 
						grpTxt = uv.getValue();
				}
				buffer.append(grpTxt);
			}
		}
		
		return buffer.toString();
	}

	/*
	 * Tier Listeners
	 */
	@Override
	public void addTierListener(TierListener<T> listener) {
		tierListeners.put(listener, Boolean.TRUE);
	}

	@Override
	public void removeTierListener(TierListener<T> listener) {
		tierListeners.remove(listener);
	}
	
	private void fireTierGroupAdded(int index, T value) {
		for(TierListener<T> listener:tierListeners.keySet()) {
			listener.groupAdded(this, index, value);
		}
	}
	
	private void fireTierGroupRemoved(int index, T value) {
		for(TierListener<T> listener:tierListeners.keySet()) {
			listener.groupRemoved(this, index, value);
		}
	}
	
	private void fireTierGroupChanged(int index, T oldValue, T value) {
		for(TierListener<T> listener:tierListeners.keySet()) {
			listener.groupChanged(this, index, oldValue, value);
		}
	}
	
	private void fireTierGroupsCleared() {
		for(TierListener<T> listener:tierListeners.keySet()) {
			listener.groupsCleared(this);
		}
	}

	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}
	
	
}
