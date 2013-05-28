package ca.phon.query.scripttable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.table.AbstractTableModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import ca.phon.engines.search.report.design.ScriptParameter;
import ca.phon.engines.search.script.QueryScript;
import ca.phon.engines.search.scripttable.io.ObjectFactory;
import ca.phon.engines.search.scripttable.io.ScriptTable;
import ca.phon.engines.search.scripttable.io.ScriptTableColumn;
import ca.phon.script.PhonScript;
import ca.phon.script.params.EnumScriptParam;
import ca.phon.script.params.ScriptParam;
import ca.phon.system.logger.PhonLogger;

public abstract class AbstractScriptTableModel extends AbstractTableModel implements ScriptTableModel {
	
	private static final long serialVersionUID = -4117009557145424149L;
	
	private Map<Integer, String> columnScripts =
			Collections.synchronizedMap(new HashMap<Integer, String>());
	
	private Map<Integer, CompiledScript> columnCompiledScripts = 
			Collections.synchronizedMap(new HashMap<Integer, CompiledScript>());
	
	private Map<Integer, Map<String, Object>> columnStaticMappings =
			Collections.synchronizedMap(new HashMap<Integer, Map<String, Object>>());
	
	@Override
	public Class<?> getColumnClass(int col) {
		Class<?> retVal = super.getColumnClass(col);
		
		final CompiledScript cScript = columnCompiledScripts.get(col);
		if(cScript != null) {
			final Invocable invocable = (Invocable) cScript.getEngine();
			final Map<String, Object> columnMappings = getMappingsAt(0, col);
			final Bindings bindings = cScript.getEngine().getBindings(ScriptContext.ENGINE_SCOPE);
			if(columnMappings != null)
				bindings.putAll(columnMappings);
			
			try {
				cScript.eval(bindings);
				
				final Object val = invocable.invokeFunction("getType", new Object[0]);
				if(val instanceof Class) {
					retVal = (Class<?>)val;
				}
			} catch (NoSuchMethodException nsme) {
			} catch (ScriptException e) {
				e.printStackTrace();
			}
			
		}
		
		return retVal;
	}

	/**
	 * Uses the default column name or the name provided by a method 'getName' 
	 * in the column script.
	 * 
	 * @param col
	 * @return column name
	 */
	@Override
	public String getColumnName(int col) {
		String retVal = super.getColumnName(col);
		
		final CompiledScript cScript = columnCompiledScripts.get(col);
		if(cScript != null) {
			final Invocable invocable = (Invocable) cScript.getEngine();
			final Map<String, Object> columnMappings = getMappingsAt(0, col);
			final Bindings bindings = cScript.getEngine().getBindings(ScriptContext.ENGINE_SCOPE);
			if(columnMappings != null)
				bindings.putAll(columnMappings);
			
			try {
				cScript.eval(bindings);
				
				final Object val = invocable.invokeFunction("getName", new Object[0]);
				if(val != null) {
					retVal = val.toString();
				}
			} catch (NoSuchMethodException nsme) {
			} catch (ScriptException e) {
			}
			
		}
		
		return retVal;
	}

	@Override
	public String getColumnScript(int col) {
		String retVal = "";
		
		retVal = columnScripts.get(col);
		
		return retVal;
	}
	
	@Override
	public int getColumnCount() {
		return columnCompiledScripts.keySet().size();
	}
	
	/**
	 * Script script for specified column.  Script is
	 * assumed to have mimetype 'text/javascript'
	 * 
	 * @param col
	 * @param script
	 * 
	 * @throws ScriptException 
	 */
	public void setColumnScript(int col, String script) 
		throws ScriptException {
		setColumnScript(col, script, "text/javascript");
	}
	
	public void setColumnScript(int col, String script, String mimetype) 
		throws ScriptException {
		final ScriptEngineManager manager = new ScriptEngineManager();
		final ScriptEngine engine = manager.getEngineByMimeType(mimetype);
		
		if(engine instanceof Compilable) {
			final Compilable cEngine = (Compilable)engine;
			final CompiledScript cScript = cEngine.compile(script);
			
			columnCompiledScripts.put(col, cScript);
			columnScripts.put(col, script);
			
			// setup static column mappings
			final Map<String, Object> bindings = new HashMap<String, Object>();
			final PhonScript ps = new PhonScript(script);
			final ScriptParam[] params = ps.getScriptParams();
			
			// setup script parameters
			for(ScriptParam param:params) {
				for(String paramId:param.getParamIds()) {
					final Object paramVal = param.getDefaultValue(paramId);
					bindings.put(paramId, paramVal);
				}
			}
			if(bindings.size() > 0)
				setColumnMappings(col, bindings);
		}
	}

	@Override
	public String getColumnScriptMimetype(int col) {
		String retVal = "text/javascript";
		
		final CompiledScript cScript = columnCompiledScripts.get(col);
		if(cScript != null) {
			final ScriptEngine se = cScript.getEngine();
			final List<String> mimetypes = se.getFactory().getMimeTypes();
			if(mimetypes.size() > 0) {
				retVal = mimetypes.get(0);
			}
		}
		
		return retVal;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Object retVal = null;
		
		final CompiledScript cScript = columnCompiledScripts.get(col);
		if(cScript != null) {
			final Map<String, Object> mappings = getMappingsAt(row, col);
			final Bindings bindings = cScript.getEngine().getBindings(ScriptContext.ENGINE_SCOPE);
			bindings.putAll(mappings);
			
			final Invocable invocable = (Invocable)cScript.getEngine();
			try {
				retVal = cScript.eval(bindings);
				if(bindings.containsKey("retVal")) {
					retVal = bindings.get("retVal");
				}
			} catch (ScriptException se) {
				se.printStackTrace();
			}
			
			try {
				Object fVal = invocable.invokeFunction("getValue", new Object[0]);
				if(fVal != null) {
					retVal = fVal;
				}
			} catch (ScriptException e) {
			} catch (NoSuchMethodException e) {
			}
		}
		
		return retVal;
	}
	
	/**
	 * Static mappings for scripts can be set here.
	 * These mappings will be returned for
	 * {@link #getMappingsAt(int, int)}
	 * 
	 * @param col
	 * @param mappings
	 */
	public void setColumnMappings(int col, Map<String, Object> mappings) {
		columnStaticMappings.put(col, mappings);
	}
	
	@Override
	public Map<String, Object> getMappingsAt(int row, int col) {
		final Map<String, Object> retVal = new HashMap<String, Object>();
		if(columnStaticMappings.containsKey(col)) {
			retVal.putAll(columnStaticMappings.get(col));
		}
		return retVal;
	}
	
	/**
	 * Removes all columns.
	 * 
	 */
	public void removeAllColumns() {
		this.columnCompiledScripts.clear();
		super.fireTableStructureChanged();
	}
	
	/**
	 * Write the table model schema in xml to the given stream.
	 * 
	 * @param file
	 * 
	 * @throws IOException
	 */
	public void writeXML(OutputStream os)
		throws IOException {
		try {
			final JAXBContext context = JAXBContext.newInstance(ScriptTable.class);
			final Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			final ObjectFactory factory = new ObjectFactory();
			final ScriptTable schema = factory.createScriptTable();
			for(Integer colIdx:columnCompiledScripts.keySet()) {
				final ScriptTableColumn sCol = factory.createScriptTableColumn();
				sCol.setIndex(colIdx);
				sCol.setMimetype(getColumnScriptMimetype(colIdx));
				sCol.setScript(getColumnScript(colIdx));
				schema.getColumn().add(sCol);
				
				
			}
			
			marshaller.marshal(factory.createTable(schema), os);
		} catch (JAXBException ex) {
			throw new IOException(ex);
		}
	}
	
	/**
	 * Read table schema from xml
	 * 
	 * @param is
	 * 
	 * @throws IOException
	 */
	public void readXML(InputStream is) 
		throws IOException {
		try {
			final JAXBContext context = JAXBContext.newInstance(ScriptTable.class);
			final Unmarshaller unmarshaller = context.createUnmarshaller();
			
			final XMLInputFactory inputFactory = XMLInputFactory.newFactory();
			final XMLEventReader eventReader = inputFactory.createXMLEventReader(is);
			final JAXBElement<ScriptTable> schemaEle = 
					unmarshaller.unmarshal(eventReader, ScriptTable.class);
			final ScriptTable schema = schemaEle.getValue();
			
			for(ScriptTableColumn column:schema.getColumn()) {
				try {
					setColumnScript(column.getIndex(), column.getScript(), column.getMimetype());
				} catch (ScriptException se) {
					se.printStackTrace();
				}
			}
		} catch (JAXBException e) {
			throw new IOException(e);
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}
	
}
