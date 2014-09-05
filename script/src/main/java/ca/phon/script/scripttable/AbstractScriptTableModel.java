package ca.phon.script.scripttable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrapFactory;

import ca.phon.script.BasicScript;
import ca.phon.script.PhonScript;
import ca.phon.script.PhonScriptContext;
import ca.phon.script.PhonScriptException;
import ca.phon.script.scripttable.io.ObjectFactory;
import ca.phon.script.scripttable.io.ScriptTable;
import ca.phon.script.scripttable.io.ScriptTableColumn;

/**
 * <p>Abstract implementation of {@link ScriptTableModel}.  The class include
 * method for reading and writing the table model to xml files.</p>
 * 
 */
public abstract class AbstractScriptTableModel extends AbstractTableModel implements ScriptTableModel {
	
	private static final Logger LOGGER = Logger
			.getLogger(AbstractScriptTableModel.class.getName());
	
	private static final long serialVersionUID = -4117009557145424149L;
	
	private Map<Integer, PhonScript> columnScripts =
			Collections.synchronizedMap(new HashMap<Integer, PhonScript>());
	
	private Map<Integer, Map<String, Object>> columnStaticMappings =
			Collections.synchronizedMap(new HashMap<Integer, Map<String, Object>>());
	
	@Override
	public Class<?> getColumnClass(int col) {
		Class<?> retVal = super.getColumnClass(col);
		
		final PhonScript script = getColumnScript(col);
		final PhonScriptContext cScript = script.getContext();
		if(cScript != null) {
			try {
//				final WrapFactory wf = ctx.getWrapFactory();
//				final Scriptable parentScope = cScript.createImporterScope();
//				// setup column mappings
//				final Map<String, Object> columnMappings = getMappingsAt(0, col);
//				for(String key:columnMappings.keySet()) {
//					final Object val = columnMappings.get(key);
//					final Object wrappedVal = wf.wrap(ctx, parentScope, val, null);
//					parentScope.put(key, parentScope, wrappedVal);
//				}
				final Scriptable scope = cScript.getEvaluatedScope();
				
				if(cScript.hasFunction(scope, "getType", 0)) {
					final Object getTypeVal = cScript.callFunction(scope, "getType", new Object[0]);
					if(getTypeVal instanceof Class) {
						retVal = (Class<?>)getTypeVal;
					}
				}
			} catch (PhonScriptException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
		
		final PhonScript script = getColumnScript(col);
		final PhonScriptContext cScript = script.getContext();
		if(cScript != null) {
			try {
				final Scriptable scope = cScript.getEvaluatedScope();
				final Scriptable parentScope = createCellScope(cScript, 0, col);
				scope.setParentScope(parentScope);
				
				if(cScript.hasFunction(scope, "getName", 0)) {
					Object getNameVal = cScript.callFunction(scope, "getName", new Object[0]);
					if(getNameVal != null) {
						if(getNameVal instanceof NativeJavaObject) {
							retVal = ((NativeJavaObject)getNameVal).unwrap().toString();
						} else {
							retVal = getNameVal.toString();
						}
					}
				}
			} catch (PhonScriptException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
		return retVal;
	}

	@Override
	public PhonScript getColumnScript(int col) {
		PhonScript retVal = new BasicScript("");
		
		if(columnScripts.containsKey(col)) {
			retVal = columnScripts.get(col);
		}
		
		return retVal;
	}
	
	@Override
	public int getColumnCount() {
		return columnScripts.keySet().size();
	}
	
	/**
	 * Setup script for specified column
	 * 
	 * @param col
	 * @param script
	 * 
	 * @throws ScriptException
	 */
	public void setColumnScript(int col, String script) 
		throws PhonScriptException {
		setColumnScript(col, new BasicScript(script));
	}
	
	/**
	 * Setup script for specified column.  Script is
	 * assumed to have mimetype 'text/javascript'
	 * 
	 * @param col
	 * @param script
	 * 
	 * @throws ScriptException 
	 */
	public void setColumnScript(int col, PhonScript script) 
		throws PhonScriptException {
		columnScripts.put(col, script);
	}
	
//	// in case we eventually support other languages...
//	private void setColumnScript(int col, String script, String mimetype) 
//		throws PhonScriptException {
//		final ScriptEngineManager manager = new ScriptEngineManager();
//		final ScriptEngine engine = manager.getEngineByMimeType(mimetype);
//		
//		if(engine instanceof Compilable) {
//			final Compilable cEngine = (Compilable)engine;
//			final CompiledScript cScript = cEngine.compile(script);
//			
//			columnCompiledScripts.put(col, cScript);
//			columnScripts.put(col, script);
//			
//			// setup static column mappings
//			final Map<String, Object> bindings = new HashMap<String, Object>();
//			final PhonScript ps = new BasicScript(script);
//			final PhonScriptContext context = ps.getContext();
//			final ScriptParam[] params = context.getScriptParams();
//			
//			// setup script parameters
//			for(ScriptParam param:params) {
//				for(String paramId:param.getParamIds()) {
//					final Object paramVal = param.getDefaultValue(paramId);
//					bindings.put(paramId, paramVal);
//				}
//			}
//			if(bindings.size() > 0)
//				setColumnMappings(col, bindings);
//		}
//	}

//	@Override
	private String getColumnScriptMimetype(int col) {
		String retVal = "text/javascript";
		
//		final CompiledScript cScript = columnCompiledScripts.get(col);
//		if(cScript != null) {
//			final ScriptEngine se = cScript.getEngine();
//			final List<String> mimetypes = se.getFactory().getMimeTypes();
//			if(mimetypes.size() > 0) {
//				retVal = mimetypes.get(0);
//			}
//		}
		
		return retVal;
	}

	private final AtomicReference<Scriptable> importerScopeRef = new AtomicReference<Scriptable>();
	@Override
	public Object getValueAt(int row, int col) {
		Object retVal = null;
		
		final PhonScript script = getColumnScript(col);
		final PhonScriptContext cScript = script.getContext();
		if(cScript != null) {
			final Context ctx = PhonScriptContext.enter();
			try {
				final Scriptable parentScope = createCellScope(cScript, row, col);
				Scriptable scope = importerScopeRef.get();
				if(scope == null) {
					scope = cScript.createImporterScope();
					importerScopeRef.getAndSet(scope);
				}
				scope.setParentScope(parentScope);
				cScript.getCompiledScript().exec(ctx, scope);
				
				if(cScript.hasFunction(scope, "getValue", 0)) {
					retVal = cScript.callFunction(scope, "getValue", new Object[0]);
				} else if(scope.has("retVal", scope)) {
					retVal = scope.get("retVal", scope);
				}
				
				if(retVal != null) {
					if(retVal instanceof NativeJavaObject) {
						retVal = ((NativeJavaObject)retVal).unwrap();
					}
				}
			} catch (PhonScriptException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			} finally {
				PhonScriptContext.exit();
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
	 * Create a scope with all of the static mapping for a column setup.
	 * 
	 * @param row
	 * @param col
	 */
	protected Scriptable createCellScope(PhonScriptContext ctx, int row, int col) {
		final Context context = PhonScriptContext.enter();
		final WrapFactory wf = context.getWrapFactory();
		final Scriptable retVal = ctx.createBasicScope();

		try {
			ctx.installParams(retVal);
		} catch(PhonScriptException pse) {
			LOGGER.log(Level.WARNING, pse.getLocalizedMessage(), pse);
		}
		
		final Map<String, Object> mappings = getMappingsAt(row, col);
		for(String key:mappings.keySet()) {
			final Object obj = mappings.get(key);
			final Object wrappedObj = wf.wrap(context, retVal, obj, null);
			ScriptableObject.putProperty(retVal, key, wrappedObj);
		}
		
		PhonScriptContext.exit();
		
		return retVal;
	}
	
	/**
	 * Removes all columns.
	 * 
	 */
	public void removeAllColumns() {
		this.columnScripts.clear();
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
			for(Integer colIdx:columnScripts.keySet()) {
				final ScriptTableColumn sCol = factory.createScriptTableColumn();
				sCol.setIndex(colIdx);
				sCol.setMimetype(getColumnScriptMimetype(colIdx));
				sCol.setScript(getColumnScript(colIdx).getScript());
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
					setColumnScript(column.getIndex(), new BasicScript(column.getScript()));
				} catch (PhonScriptException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
		} catch (JAXBException e) {
			throw new IOException(e);
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}
	
}
