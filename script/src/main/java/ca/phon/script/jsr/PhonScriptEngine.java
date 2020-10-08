package ca.phon.script.jsr;

import java.io.Reader;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.mozilla.javascript.ScriptableObject;

import ca.phon.script.PhonScriptContext;

/**
 * {@link ScriptEngine} implementation for the Phon 
 * javascript engine - currently implemented using rhino.
 */
public class PhonScriptEngine extends AbstractScriptEngine
	implements Invocable, Compilable {

	/* 
	 * Context for top-level objects such as the standard objects.
     */
	
	@Override
	public Object eval(String script, ScriptContext context) throws ScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bindings createBindings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScriptEngineFactory getFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompiledScript compile(String script) throws ScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompiledScript compile(Reader script) throws ScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object invokeMethod(Object thiz, String name, Object... args) throws ScriptException, NoSuchMethodException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object invokeFunction(String name, Object... args) throws ScriptException, NoSuchMethodException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getInterface(Class<T> clasz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T getInterface(Object thiz, Class<T> clasz) {
		// TODO Auto-generated method stub
		return null;
	}

}
