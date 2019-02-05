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
package ca.phon.script.params.history;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import ca.phon.script.PhonScript;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;

/**
 * Manager for {@link ParamHistoryType} objects.  This method provides the application
 * logic for the generated JAXB classes. Methods in this class should be used for
 * manipulating {@link ParamHistoryType}s.
 * 
 */
public class ParamHistoryManager {
		
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(ParamHistoryManager.class.getName());
	
	public static ParamHistoryType loadParamHistoryFromFile(File paramHistoryFile) throws IOException {
		return loadParamHistoryFromStream(new FileInputStream(paramHistoryFile));
	}
	
	public static ParamHistoryType loadParamHistoryFromStream(InputStream inputStream) throws IOException {
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			
			final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			final JAXBElement<ParamHistoryType> ele =
					unmarshaller.unmarshal(inputFactory.createXMLEventReader(inputStream), ParamHistoryType.class);
			return ele.getValue();
		} catch (JAXBException | XMLStreamException e) {
			throw new IOException(e);
		}
	}
	
	public static void saveParamHistory(ParamHistoryType paramHistory, File paramHistoryFile) throws IOException {
		// make parent folders if necessary
		if(!paramHistoryFile.getParentFile().exists()) {
			paramHistoryFile.getParentFile().mkdirs();
		}
		
		saveParamHistory(paramHistory, new FileOutputStream(paramHistoryFile));
	}
	
	public static void saveParamHistory(ParamHistoryType paramHistory, OutputStream outputStream) throws IOException {
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
			final Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			final ObjectFactory factory = new ObjectFactory();
			marshaller.marshal(factory.createParamHistory(paramHistory), outputStream);
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}

	private final ParamHistoryType paramHistory;
	
	public ParamHistoryManager(ParamHistoryType paramHistory) {
		super();
		this.paramHistory = paramHistory;
	}
	
	public ParamHistoryManager(File paramHistoryFile) throws IOException {
		super();
		this.paramHistory = loadParamHistoryFromFile(paramHistoryFile);
	}
	
	public ParamHistoryManager(InputStream inputStream) throws IOException {
		super();
		this.paramHistory = loadParamHistoryFromStream(inputStream);
	}
	
	public ParamHistoryType getParamHistory() {
		return this.paramHistory;
	}
	
	public int size() {
		synchronized (paramHistory) {
			return paramHistory.getParamSet().size();
		}
	}
	
	public int indexOf(String hash) {
		synchronized(paramHistory) {
			int retVal = -1;
			for(int i = 0; i < paramHistory.getParamSet().size(); i++) {
				if(paramHistory.getParamSet().get(i).getHash().equals(hash)) {
					retVal = i;
					break;
				}
			}
			return retVal;
		}
	}
	
	public int indexOfName(String name) {
		synchronized(paramHistory) {
			int retVal = -1;
			for(int i = 0; i < paramHistory.getParamSet().size(); i++) {
				if(paramHistory.getParamSet().get(i).getName() != null
						&& paramHistory.getParamSet().get(i).getName().equals(name)) {
					retVal = i;
					break;
				}
			}
			return retVal;
		}
	}
	
	public ParamSetType getParamSetByName(String name) {
		int paramSetIdx = indexOfName(name);
		if(paramSetIdx >= 0)
			return getParamSet(paramSetIdx);
		else
			return null;
	}
	
	public ParamSetType getParamSet(PhonScript script) throws PhonScriptException {
		final String hash = getScriptParameters(script).getHashString();
		return getParamSet(hash);
	}
	
	public ParamSetType getParamSet(String hash) {
		int paramSetIdx = indexOf(hash);
		if(paramSetIdx >= 0)
			return getParamSet(paramSetIdx);
		else
			return null;
	}
	
	public ParamSetType getParamSet(int idx) {
		synchronized(paramHistory) {
			return paramHistory.getParamSet().get(idx);
		}
	}
	
	public ParamSetType removeParamSet(String hash) {
		int paramSetIdx = indexOf(hash);
		if(paramSetIdx >= 0)
			return removeParamSet(paramSetIdx);
		else
			return null;
	}
	
	public ParamSetType removeParamSet(int idx) {
		ParamSetType retVal = null;
		synchronized(paramHistory) {
			retVal = paramHistory.getParamSet().remove(idx);
		}
		fireParamSetRemoved(retVal);
		return retVal;
	}
	
	public List<ParamSetType> getUnnamedParamSets() {
		synchronized(paramHistory) {
			return paramHistory.getParamSet().stream()
					.filter( (ps) -> ps.getName() == null || ps.getName().trim().length() == 0 )
					.collect( Collectors.toList() );
		}
	}
	
	public List<ParamSetType> getNamedParamSets() {
		synchronized(paramHistory) {
			return paramHistory.getParamSet().stream()
					.filter( (ps) -> ps.getName() != null && ps.getName().trim().length() > 0 )
					.collect( Collectors.toList() );
		}
	}
	
	public ParamSetType nameParamSet(String name, PhonScript script) throws PhonScriptException {
		return nameParamSet(name, getScriptParameters(script));
	}
	
	public ParamSetType nameParamSet(String name, ScriptParameters params) {
		final String hash = params.getHashString();
		ParamSetType paramSet = getParamSet(hash);
		if(paramSet == null) {
			paramSet = addParamSet(params);
		}
		paramSet.setName(name);
		
		fireParamSetRenamed(paramSet);
		
		return paramSet;
	}
	
	/**
	 * Name a param set which already exists.
	 * 
	 * @param name
	 * @param hash
	 * @return
	 */
	public ParamSetType nameParamSet(String name, String hash) {
		ParamSetType paramSet = getParamSet(hash);
		if(paramSet == null) return null;
		
		paramSet.setName(name);
		
		fireParamSetRenamed(paramSet);
		
		return paramSet;
	}
	
	public ScriptParameters getScriptParameters(PhonScript script) throws PhonScriptException {
		return script.getContext().getScriptParameters(script.getContext().getEvaluatedScope());
	}
	
	public ParamSetType addParamSet(PhonScript script) throws PhonScriptException {
		return addParamSet(getScriptParameters(script));
	}
	
	public ParamSetType addParamSet(ScriptParameters params) {
		final ObjectFactory factory = new ObjectFactory();
		final ParamSetType paramSet = factory.createParamSetType();
		paramSet.setHash(params.getHashString());
		
		final ZonedDateTime date = ZonedDateTime.now(ZoneId.systemDefault());
		GregorianCalendar gcal = GregorianCalendar.from(date);
		try {
			XMLGregorianCalendar xcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);	
			paramSet.setDate(xcal);
		} catch (DatatypeConfigurationException e) {
			LOGGER.warn( e.getLocalizedMessage(), e);
		}
		for(ScriptParam param:params) {
			if(param.hasChanged()) {
				for(String paramId:param.getParamIds()) {
					final ParamType paramType = factory.createParamType();
					paramType.setId(paramId);
					paramType.setValue(param.getValue(paramId).toString());
					paramSet.getParam().add(paramType);
				}
			}
		}
		
		addParamSet(paramSet);
		
		return paramSet;
	}
	
	/**
	 * Add the given param set. If these parameters already exist
	 * in the file, the old reference is moved to the end of the list
	 * and date is updated.
	 * 
	 * @param paramSet
	 * @return the paramSet modified by this method (could be different than the param)
	 */
	public ParamSetType addParamSet(ParamSetType paramSet) {
		final String hash = paramSet.getHash();
		
		ParamSetType retVal = null;
		synchronized(paramHistory) {
			int idx = -1;
			for(int i = 0; i < paramHistory.getParamSet().size(); i++) {
				final ParamSetType ps = paramHistory.getParamSet().get(i);
				if(ps.getHash().equals(hash)) {
					idx = i;
					break;
				}
			}
			retVal = (idx >= 0 ? paramHistory.getParamSet().remove(idx) : paramSet);
			if(idx >= 0) {
				// update date and parameters
				// there could be changes which are not included in hash (i.e., separator collapsed state)
				retVal.setDate(paramSet.getDate());
				retVal.getParam().clear();
				for(ParamType param:paramSet.getParam())
					retVal.getParam().add(param);
			}
			
			paramHistory.getParamSet().add(retVal);
		}
		if(retVal != null)
			fireParamSetAdded(retVal);
		return retVal;
	}
	
	public void save(File paramHistoryFile) throws IOException {
		saveParamHistory(paramHistory, paramHistoryFile);
	}
	
	public void removeAll() {
		List<ParamSetType> paramSets = new ArrayList<>();
		synchronized(paramHistory) {
			paramSets = new ArrayList<>(paramHistory.getParamSet());
			paramHistory.getParamSet().clear();
		}
		if(paramSets.size() > 0) {
			fireParamHistoryEvent(new ParamHistoryEvent(EventType.PARAM_SET_REMOVED, paramSets));
		}
	}
	
	public List<ParamSetType> removeAllUnnamedParamSets() {
		List<ParamSetType> retVal = new ArrayList<>();
		synchronized(paramHistory) {
			retVal = 
				getParamHistory().getParamSet().stream()
					.filter( (ps) -> ps.name == null || ps.name.trim().length() == 0 )
					.collect( Collectors.toList() );
			
			getParamHistory().getParamSet().removeAll(retVal);
			
		}
		if(retVal.size() > 0) {
			fireParamHistoryEvent(new ParamHistoryEvent(EventType.PARAM_SET_REMOVED, retVal));
		}
		return retVal;
	}
	
	/**
	 * Update hash values of all entries using provided script.
	 * (used for stock param sets after loading)
	 * 
	 * @param script
	 */
	public void fixHashes(PhonScript script) throws PhonScriptException {
		synchronized(paramHistory) {
			for(int i = 0; i < paramHistory.getParamSet().size(); i++) {
				final ParamSetType ps = paramHistory.getParamSet().get(i);
				
				script.resetContext();
				
				final ScriptParameters scriptParams = 
						script.getContext().getScriptParameters(script.getContext().getEvaluatedScope());
				final Map<String, Object> paramMap = new HashMap<>();
				for(int j = 0; j < ps.getParam().size(); j++) {
					final ParamType param = ps.getParam().get(j);
					paramMap.put(param.getId(), param.getValue());
				}
				
				scriptParams.loadFromMap(paramMap);
				
				// update hash
				ps.setHash(scriptParams.getHashString());
			}
		}
	}
	
	/* Listeners */
	private final List<ParamHistoryListener> listeners = new ArrayList<>();
	
	public void fireParamSetAdded(ParamSetType paramSet) {
		fireParamHistoryEvent(new ParamHistoryEvent(EventType.PARAM_SET_ADDED, paramSet));
	}
	
	public void fireParamSetRemoved(ParamSetType paramSet) {
		fireParamHistoryEvent(new ParamHistoryEvent(EventType.PARAM_SET_REMOVED, paramSet));
	}
	
	public void fireParamSetRenamed(ParamSetType paramSet) {
		fireParamHistoryEvent(new ParamHistoryEvent(EventType.PARAM_SET_RENAMED, paramSet));
	}
	
	public void fireParamHistoryEvent(ParamHistoryEvent event) {
		synchronized(listeners) {
			for(ParamHistoryListener listener:listeners) {
				listener.paramHistoryEvent(event);
			}
		}
	}
	
	public void addParamHistoryListener(ParamHistoryListener listener) {
		synchronized(listeners) {
			listeners.add(listener);
		}
	}
	
	public void removeParamHistoryListener(ParamHistoryListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	/**
	 * Event types
	 */
	public static enum EventType {
		PARAM_SET_ADDED,
		PARAM_SET_REMOVED,
		PARAM_SET_RENAMED;
	}
	
	public static class ParamHistoryEvent {
		
		private EventType eventType;
		
		private List<ParamSetType> paramSets;
		
		public ParamHistoryEvent(EventType eventType, ParamSetType paramSet) {
			this(eventType, Collections.singletonList(paramSet));
		}
		
		public ParamHistoryEvent(EventType eventType, List<ParamSetType> paramSets) {
			this.eventType = eventType;
			this.paramSets = paramSets;
		}

		public EventType getEventType() {
			return eventType;
		}

		public void setEventType(EventType eventType) {
			this.eventType = eventType;
		}

		public List<ParamSetType> getParamSets() {
			return paramSets;
		}

		public void setParamSets(List<ParamSetType> paramSets) {
			this.paramSets = paramSets;
		}
		
	}
	
	public static interface ParamHistoryListener {
		
		public void paramHistoryEvent(ParamHistoryEvent event);
		
	}
	
}
