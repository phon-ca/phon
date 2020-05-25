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
package ca.phon.script;

import java.io.PrintStream;
import java.net.URI;
import java.util.List;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrapFactory;
import org.mozilla.javascript.commonjs.module.ModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.Require;
import org.mozilla.javascript.commonjs.module.RequireBuilder;
import org.mozilla.javascript.commonjs.module.provider.ModuleSourceProvider;
import org.mozilla.javascript.commonjs.module.provider.SoftCachingModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;

import ca.phon.script.js.ExtendableWrapFactory;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;

/**
 * Handles setting up script context, scope and
 * evaluation scripts.
 *
 */
public class PhonScriptContext {
	
	/*
	 * Script prefix - this is added to all scripts in the background
	 * in order to ensure that everything is contained in a compilable
	 * function
	 */
	public final static String SCRIPT_EXPORTS = "exports";

	/*
	 * This wrap factory exposes extensions in IExtendable objects
	 * as properties in the wrapped object.
	 */
	private final static WrapFactory wrapFactory = new ExtendableWrapFactory();
	
	private ContextFactory contextFactory;

	private PrintStream stdOutStream;

	private PrintStream stdErrStream;

	public void redirectStdOut(PrintStream stream) {
		this.stdOutStream = stream;
	}

	public PrintStream getStdOut() {
		return (this.stdOutStream == null ? System.out : this.stdOutStream);
	}

	public void redirectStdErr(PrintStream stream) {
		this.stdErrStream = stream;
	}

	public PrintStream getStdErr() {
		return (this.stdErrStream == null ? System.err : this.stdErrStream);
	}
	
	public ContextFactory getContextFactory() {
		if(contextFactory == null) {
			contextFactory = ContextFactory.getGlobal();
		}
		return contextFactory;
	}

	/**
	 * Enter and return a new script context.  Every call
	 * to this method should have a matching call to {#link {@link #exit()}
	 */
	public Context enter() {
		final ContextFactory factory = getContextFactory();

		final Context retVal = factory.enterContext();
		retVal.setWrapFactory(wrapFactory);

		return retVal;
	}

	public void exit() {
		Context.exit();
	}

	/**
	 * Evaulate the given text and return the result (if any)
	 *
	 * @param script
	 *
	 * @return the return value of the script. May be <code>null</code>
	 *
	 */
	public static Object eval(String text)
		throws PhonScriptException {
		Object retVal = null;

		final PhonScript script = new BasicScript(text);
		final PhonScriptContext ctx = script.getContext();

		try {
			retVal = ctx.exec(ctx.createImporterScope());
		} catch (Exception e) {
			throw new PhonScriptException(e);
		}

		return retVal;
	}

	/**
	 * The script
	 */
	private final PhonScript script;

	/**
	 * Compiled script
	 *
	 */
	private Script compiledScript;

	/**
	 * Evaluated scope - this is the scope that results
	 * from compiling and evaluating the script.
	 */
	private Scriptable evaluatedScope;

	/**
	 * Script parameters
	 */
	private ScriptParameters parameters;

	public PhonScriptContext(PhonScript script) {
		super();
		this.script = script;
	}

	/**
	 * Create a basic Scriptable object
	 *
	 * @return basic Scriptable
	 */
	public Scriptable createBasicScope() {
		final Context ctx = enter();
		final Scriptable retVal = ctx.initStandardObjects();
		exit();
		return retVal;
	}

	/**
	 * Setup scope for script with default imports included
	 * and the <code>require</code> function installed.
	 *
	 * @return the top-level scope for the script
	 */
	public Scriptable createImporterScope()
		throws PhonScriptException {
		final Context ctx = enter();
		final ScriptableObject scope = new ImporterTopLevel(ctx);

		// setup package/class imports
		final StringBuilder importScriptBuilder = new StringBuilder();

		for(String pkgImport:script.getPackageImports()) {
			importScriptBuilder.append("importPackage(");
			importScriptBuilder.append(pkgImport);
			importScriptBuilder.append(")\n");
		}

		for(String classImport:script.getClassImports()) {
			importScriptBuilder.append("importClass(");
			importScriptBuilder.append(classImport);
			importScriptBuilder.append(")\n");
		}

		final String importScriptText = importScriptBuilder.toString();
		Script importScript = null;
		try {
			importScript = ctx.compileString(importScriptText, "<preloader>", 1, null);
			importScript.exec(ctx, scope);
		} catch (Exception e) {
			exit();
			throw new PhonScriptException(e);
		}

		//  setup require paths
		final List<URI> requirePaths = script.getRequirePaths();
	    final ModuleSourceProvider sourceProvider = new UrlModuleSourceProvider(requirePaths, null);
	    final ModuleScriptProvider scriptProvider = new SoftCachingModuleScriptProvider(sourceProvider);
	    final RequireBuilder builder = new RequireBuilder();
	    builder.setModuleScriptProvider(scriptProvider);
	    if(importScript != null)
	    	builder.setPreExec(importScript);

		final Require require = builder.createRequire(ctx, scope);
		require.install(scope);

		exit();

		return scope;
	}

//	/**
//	 * Get the script exports object from the given scope
//	 *
//	 * @param scope
//	 *
//	 * @return exports object for script or <code>null</code> if
//	 *  not found
//	 */
//	public Scriptable getExports(Scriptable scope) {
//		Scriptable retVal = null;
//		if(ScriptableObject.hasProperty(scope, SCRIPT_EXPORTS)) {
//			final Object retObj = ScriptableObject.getProperty(scope, SCRIPT_EXPORTS);
//			if(retObj instanceof Scriptable)
//				retVal = Scriptable.class.cast(retObj);
//		}
//		return retVal;
//	}

	/**
	 * Does the script have a function with the given
	 * name and arity.
	 *
	 * @param name
	 * @param arity
	 *
	 * @return <code>true</code> if a function with the given
	 *  name and arity is found, <code>false</code> otherwise
	 */
	public boolean hasFunction(Scriptable scope, String name, int arity) {
		boolean retVal = false;
		enter();
		// check arity
		if(ScriptableObject.hasProperty(scope, name)) {
			final Object funcObj = ScriptableObject.getProperty(scope, name);
			if(funcObj instanceof Scriptable) {
				final Scriptable func = Scriptable.class.cast(funcObj);
				if(ScriptableObject.hasProperty(func, "arity")) {
					final Integer funcArity =
							(Integer)ScriptableObject.getProperty(func, "arity");
					retVal = (funcArity == arity);
				}
			}
		}
		exit();

		return retVal;
	}

	public synchronized Scriptable getEvaluatedScope()
		throws PhonScriptException {
		return getEvaluatedScope(null);
	}
	
	public synchronized void resetEvaulatedScope() {
		this.evaluatedScope = null;
	}

	/**
	 * Get the evaluated scope for the script.  The evaluated scope
	 * is the Scriptable object resulting from evaulating the script.
	 *
	 * The evaluated scope is useful for working with functions
	 * defined in the scropt.
	 *
	 * @param parentScope may be <code>null</code>
	 *
	 * @return the evaluated scope
	 */
	public synchronized Scriptable getEvaluatedScope(Scriptable parentScope)
		throws PhonScriptException {
		if(evaluatedScope == null) {
			final Context ctx = enter();

			final Scriptable evScope = createImporterScope();

			if(parentScope != null)
				evScope.setParentScope(parentScope);
			final Script compiledScript = getCompiledScript();

			try {
				compiledScript.exec(ctx, evScope);
			} catch (Exception e) {
				throw new PhonScriptException(e);
			} finally {
				exit();
			}

			evaluatedScope = evScope;
		}
		return evaluatedScope;
	}

	/**
	 * Return the compiled version of the script.567\l
	 * 	 *
	 * @return compiled script
	 * @throws
	 */
	public Script getCompiledScript()
		throws PhonScriptException {
		final Context ctx = enter();
		if(compiledScript == null) {
			final String scriptText = script.getScript();
			try {
				compiledScript = ctx.compileString(scriptText, "", 1, null);
			} catch (Exception e) {
				throw new PhonScriptException(e);
			}
		}
		exit();
		return compiledScript;
	}

	/**
	 * Get the phon script object associated with this context
	 *
	 * @return the PhonScript
	 */
	public PhonScript getPhonScript() {
		return this.script;
	}

	/**
	 * Get the parameters for the script.
	 *
	 * @param scope
	 * @return script parameters
	 *
	 * @throws PhonScriptException
	 */
	public ScriptParameters getScriptParameters(Scriptable scope)
		throws PhonScriptException {
		if(parameters == null) {
			final String paramComment = ScriptParameters.extractParamsComment(script.getScript());

			if(paramComment.trim().startsWith("params")) {
				parameters = ScriptParameters.parseScriptParams(paramComment);
			} else {
				parameters = new ScriptParameters();
			}

			// check for setup_params function
			if(hasFunction(scope, "setup_params", 1)) {
				callFunction(scope, "setup_params", parameters);
			}
		}
		return parameters;
	}

	/**
	 * Evaluate script using given scope
	 *
	 * @param scope
	 * @return result of executing the script or <code>null</code>
	 *  if not applicable
	 *
	 * @throws PhonScriptException
	 */
	public Object exec(Scriptable scope)
		throws PhonScriptException {
		Object retVal = null;
		final Context ctx = enter();
		try {
			retVal = getCompiledScript().exec(ctx, scope);
		} catch(Exception e) {
			throw new PhonScriptException(e);
		} finally {
			exit();
		}
		return retVal;
	}

	/**
	 * Call the specified function with the given
	 * arguments.
	 *
	 * @param scope
	 * @param method
	 * @param args
	 *
	 * @return
	 */
	public Object callFunction(Scriptable scope, String name, Object ... args)
		throws PhonScriptException {
		Object retVal = null;

		enter();
		try {
			retVal = ScriptableObject.callMethod(scope, name, args);
		} catch(Exception e) {
			throw new PhonScriptException(e);
		}
		exit();

		return retVal;
	}

	public WrapFactory getWrapFactory() {
		return wrapFactory;
	}

	/**
	 * Install these script params into the given scope
	 *
	 * @param context
	 * @param scope
	 */
	public void installParams(Scriptable scope)
		throws PhonScriptException {
		final Context ctx = enter();
		final WrapFactory wrapFactory = ctx.getWrapFactory();
		for(ScriptParam param:getScriptParameters(scope)) {
			for(String paramId:param.getParamIds()) {
				final Object paramObj = param.getValue(paramId);
				final Object wrappedObj = wrapFactory.wrap(ctx, scope, paramObj, null);

				// check for dot syntax
				Scriptable parentObj = scope;

				final StringBuffer paramBuffer = new StringBuffer(paramId);
				int dotIdx = -1;
				while((dotIdx = paramBuffer.indexOf(".")) >= 0) {
					final String parentId = paramBuffer.substring(0, dotIdx);
					paramBuffer.delete(0, dotIdx+1);

					final Object pObj = ScriptableObject.getProperty(parentObj, parentId);
					if(pObj == null) {
						break;
					}

					if(pObj instanceof Scriptable) {
						parentObj = Scriptable.class.cast(pObj);
					}
				}
				final String childId = paramBuffer.toString();

				ScriptableObject.putProperty(parentObj, childId, wrappedObj);
			}
		}
		exit();
	}

}
