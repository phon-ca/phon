package ca.phon.script;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.commonjs.module.ModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.Require;
import org.mozilla.javascript.commonjs.module.RequireBuilder;
import org.mozilla.javascript.commonjs.module.provider.ModuleSourceProvider;
import org.mozilla.javascript.commonjs.module.provider.MultiModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.provider.SoftCachingModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;

import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParamsLexer;
import ca.phon.script.params.ScriptParamsParser;
import ca.phon.script.rewrite.ScriptRewriter;

/**
 * Wrapper for a phon script.  The script 
 * should be written in ECMAScript/javascript.
 * 
 * This class includes helper methods for compiling,
 * setting up a rhino context, and setting up parameter
 * lists for the script.
 * 
 * Script paremeters are injected into the context
 * for the script before execution.
 * 
 */
public class PhonScript {
	
	private final Logger LOGGER = Logger.getLogger(PhonScript.class.getName());
	
	/** Default imports for scripts */
	private final String scriptPkgImports[] = {
			"Packages.ca.phon.ipa",
			"Packages.ca.phon.orthography",
			"Packages.ca.phon.phonex",
			"Packages.ca.phon.syllable",
			"Packages.ca.phon.project",
			"Packages.ca.phon.session",
			"Packages.ca.phon.ipa.features",
			"Packages.ca.phon.script.params"
	};
	
	private final String scriptClazzImports[] = {
			"Packages.ca.phon.util.Range"
	};
	
	/** The URL we have loaded (if used) */
	private URL scriptFile = null;
	
	/** The script buffer */
	private final StringBuffer scriptBuffer = new StringBuffer();
	
	private ScriptParam[] params = null;
	
	/**
	 * Constructor
	 * 
	 */
	public PhonScript() {
		this("");
	}
	
	public PhonScript(String script) {
		super();
		this.scriptBuffer.append(script);
	}
	
	public PhonScript(File file) 
		throws IOException {
		super();
		setLocation(file.toURI().toURL());
//		scriptBuffer = new StringBuffer();
//		readFromFile(file);
//		params = getScriptParams();
	}
	
	public PhonScript(URL url)
		throws IOException {
		super();
		setLocation(url);
//		scriptBuffer = new StringBuffer();
//		readFromURL(url);
//		params = getScriptParams();
	}
	
	// load data
	private void loadData() {
		if(scriptBuffer.length() == 0 && getLocation() != null) {
			try {
				readFromURL(getLocation());
				updateScriptParams();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}

	/**
	 * Get the script text.
	 * 
	 * @param rewrite set to true to rewrite custom syntax sections
	 * of the script. (e.g., Feature set literals - ^{ 'Vowel', 'High' }^)
	 * @return
	 */
	public String getScript(boolean rewrite) {
		loadData();
		String retVal = scriptBuffer.toString();
		
		if(rewrite) {
			try {
				retVal = ScriptRewriter.rewriteScript(retVal);
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.warning(e.toString());
			}
		}
		
		return retVal;
	}

	public void setScript(String script) {
		this.scriptBuffer.setLength(0);
		this.scriptBuffer.append(script);
		updateScriptParams();
	}
	
	/**
	 * Update script parameters
	 */
	public void updateScriptParams() {
		ScriptParam[] newParams = getScriptParams();
		ScriptParam.copyParams((params == null ? new ScriptParam[0] : params), newParams);
		params = newParams;
	}
	
	/**
	 * Setup script parameters on the given scope.
	 * 
	 * @param scope
	 */
	public void installScriptParams(Scriptable scope) {
		// setup params
		if(params != null) {
			for(ScriptParam param:params) {
				for(String id:param.getParamIds()) {
					Object wrappedParam = Context.javaToJS(param.getValue(id), scope);
					
					// check for dot syntax.  If found,
					// find the scriptable object parent
					Scriptable parentObj = scope;
					
					final StringBuffer paramBuffer = new StringBuffer(id);
					while(paramBuffer.indexOf(".") > 0) {
						final String parentId = paramBuffer.substring(0, paramBuffer.indexOf("."));
						paramBuffer.delete(0, paramBuffer.indexOf(".")+1);
						
						final Object pObj = ScriptableObject.getProperty(parentObj, parentId);
						if(pObj == null) break;
						
						if(pObj instanceof Scriptable) {
							parentObj = (Scriptable)pObj;
						}
					}
					final String childId = paramBuffer.toString();
					
					ScriptableObject.putProperty(parentObj, childId, wrappedParam);
				}
			}
		}
	}

	/**
	 * Get the script params 
	 * from the script text.
	 * 
	 * @return the script params.  This is an
	 * array with zero elements if the script
	 * does not include a parsable
	 * <pre>
	 * params = ...;
	 * </pre>
	 * section.
	 */
	private ScriptParam[] _getScriptParams() {
		ScriptParam[] retVal = new ScriptParam[0];
		
		final String s = getScript(false);
		
		// first, find any params defined in comments
		// attempt to extract script params comment
		String scriptParamsRegex = "/\\*" + // 
			"\\s*(params.*;)\\s*" +
			"\\*/";
		Pattern p = Pattern.compile(scriptParamsRegex, Pattern.MULTILINE|Pattern.DOTALL);
		Matcher m = p.matcher(s);
		
		if(m.find()) {
			// make sure the section can be parsed
			String paramsString = m.group(1);
			
			// parse params
			ByteArrayInputStream sInput = new ByteArrayInputStream(new byte[0]);
			try {
				sInput = new ByteArrayInputStream(
				paramsString.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {}
			
			ANTLRInputStream input = null;
			try {
				input = new ANTLRInputStream(sInput);
				
				ScriptParamsLexer lexer = new ScriptParamsLexer(input);
				CommonTokenStream tokens = new CommonTokenStream(lexer);
				ScriptParamsParser parser = new ScriptParamsParser(tokens);
				
				parser.params();
				retVal = parser.getScriptParams();
			}  catch (IOException ioe) {
				LOGGER.warning(ioe.toString());
			} catch (RecognitionException e) {
				LOGGER.warning(e.toString());
			}
		}
		
		final List<ScriptParam> staticParams = Arrays.asList(retVal);
		final List<ScriptParam> params = new ArrayList<ScriptParam>(staticParams);
		
		// now call the scripts param_setup function (if available)
		if(hasFunction("param_setup", 1)) {
			Context scriptContext = 
					(new ContextFactory()).enterContext();
				
			Scriptable scope = setupScope(scriptContext);
			
			String scriptText = getScript(true);
			// compile script
			Script script;
			try {
				script = scriptContext.compileString(scriptText, "js", 1, null);
				script.exec(scriptContext, scope);
				
				ScriptableObject.callMethod(scriptContext, scope, "param_setup", new Object[]{ params });
			} catch (Exception e) {
				// ignore error - some scripts are not organized into
				// functions, they must use the default 'params' comment
				// to setup script parameters
//				PhonLogger.severe( 
//						"Compilation error: " + e.toString());
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
		return params.toArray(new ScriptParam[0]);
	}

	/**
	 * Returns the script with script params comment removed.
	 * 
	 * @return script
	 */
	public String stripScriptParams() {
		String script = getScript(false);
		String scriptParamsRegex = "/\\*" + //
			"\\s*(params.*;)\\s*" +
			"\\*/";
		Pattern p = Pattern.compile(scriptParamsRegex, Pattern.MULTILINE|Pattern.DOTALL);
		Matcher m = p.matcher(script);
		
		String retVal = script;
		if(m.find()) {
			retVal = m.replaceFirst("");
		}
		return retVal;
	}
	
	/**
	 * Returns the script params for this script object.
	 */
	public ScriptParam[] getScriptParams() {
		loadData();
		if(params == null) {
			params = _getScriptParams();
		}
		return params;
	}
	
	public void setScriptParams(ScriptParam[] params) {
		this.params = params;
	}
	
	protected void readFromURL(URL url)
		throws IOException {
		readFromStream(url.openStream());
	}
	
	protected void readFromStream(InputStream is)
		throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				is, "UTF-8"));
		
		String line = null;
		while((line = in.readLine()) != null) {
			scriptBuffer.append(line + "\n");
		}
		in.close();
		
		updateScriptParams();
	}
	
	public boolean hasFunction(String funcName, int numParams) {
		boolean retVal = false;
		
		Context scriptContext = 
			(new ContextFactory()).enterContext();
		
		Scriptable scope = setupScope(scriptContext);
		
		String scriptText = getScript(true);
		// compile script
		Script script;
		try {
			script = scriptContext.compileString(scriptText, "js", 1, null);
			script.exec(scriptContext, scope);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
//			PhonLogger.severe( 
//					"Compilation error: " + e.toString());
			return false;
		}
		
		boolean hasQr =
			ScriptableObject.hasProperty(scope, funcName);
		
		if(hasQr) {
			Object qrObj = 
				ScriptableObject.getProperty(scope, funcName);
			
			if(qrObj instanceof Scriptable) {
				Scriptable qrFunc = (Scriptable)qrObj;
				
				// check arity of function
				Integer arity = 
					(Integer)ScriptableObject.getProperty(qrFunc, "arity");
				if(arity != null && arity == numParams) {
					retVal = true;
				}
			}
		}
		
		Context.exit();
		
		return retVal;
	}
	
	/**
	 * Get the list of default package imports.
	 * 
	 * @return list of deafult pkg imports
	 */
	protected List<String> defaultPackageImports() {
		return Arrays.asList(this.scriptPkgImports);
	}
	
	protected List<String> defaultClassImports() {
		return Arrays.asList(this.scriptClazzImports);
	}
	
	/**
	 * Get the list of folder that will be used as
	 * script resource folders.
	 * 
	 * Scripts found in these folders may be used
	 * with the <code>require()</code> function.
	 * 
	 * @return list of script folders
	 */
	public List<String> scriptFolders() {
		return new ArrayList<String>();
	}
	
	/**
	 * Setup script top level scope.
	 * 
	 * 
	 */
	public Scriptable setupScope(Context scriptContext) {
		ScriptableObject scope = new ImporterTopLevel(scriptContext);
		//scriptContext.se
		String importScriptText = "";
		for(String imp:defaultPackageImports()) {
			importScriptText += "importPackage(" + imp + ");\n";
		}
		for(String imp:defaultClassImports()) {
			importScriptText += "importClass(" + imp + ");\n";
		}
		
		final Script importScript = scriptContext.compileString(importScriptText, "<preloader>", 0, null);
		importScript.exec(scriptContext, scope);
//		scriptContext.evaluateString(scope, importScript, "<preloader>", 0, null);
		
		// setup module loader
		List<URI> paths = new ArrayList<URI>();
		for(String scriptFolder:scriptFolders()) {
			final File f = new File(scriptFolder);
			final URI uri = f.toURI();
			paths.add(uri);
		}
		
	    ModuleSourceProvider sourceProvider = new UrlModuleSourceProvider(paths, null);
	    ModuleScriptProvider scriptProvider = new SoftCachingModuleScriptProvider(sourceProvider);
	    
	    RequireBuilder builder = new RequireBuilder();
	    builder.setModuleScriptProvider(scriptProvider);
	    builder.setPreExec(importScript);
			    
		Require require = builder.createRequire(scriptContext, scope);
		require.install(scope);
		
		return scope;
	}
	
	public Script compileScript(Context scriptContext) {
		Script retVal = null;
		try {
			retVal = scriptContext.compileString(getScript(true), "js", 1, null);
		} catch (Exception e) {
			LOGGER.severe(e.toString());
		}
		return retVal;
	}
	
	/** 
	 * Get the script's file location.
	 * May be <code>null</code>
	 */
	public URL getLocation() {
		return this.scriptFile;
	}
	
	public void setLocation(URL location) {
		this.scriptFile = location;
	}
	
	/**
	 * Get the script's name.
	 */
	public String getName() {
		String retVal = "Untitled";
		
		if(scriptFile != null) {
			retVal = scriptFile.getFile();
			
			final int extIdx = retVal.lastIndexOf('.');
			if(extIdx > 0) {
				retVal = retVal.substring(0, extIdx);
			}
		}
		
		return retVal;
	}
	
	/*
	 * Buffer delegates
	 */
	public StringBuffer append(String str) {
		return scriptBuffer.append(str);
	}

	public StringBuffer delete(int start, int end) {
		return scriptBuffer.delete(start, end);
	}

	public StringBuffer insert(int offset, String str) {
		return scriptBuffer.insert(offset, str);
	}

	@Override
	public String toString() {
		return scriptBuffer.toString();
	}
	
}
