package ca.phon.script;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mozilla.javascript.Scriptable;


@RunWith(JUnit4.class)
public class TestBasicScript {

	@Test
	public void testBasicScript() throws PhonScriptException {
		final String script = "x = 1 + 1\n";
		
		final BasicScript bs = new BasicScript(script);
		final PhonScriptContext ctx = bs.getContext();
		final Scriptable scope = ctx.createImporterScope();
		final Object val = ctx.exec(scope);
		
		Assert.assertEquals(Integer.class, val.getClass());
		Assert.assertEquals(2, (int)Integer.valueOf(val.toString()));
		
		Assert.assertEquals(val, scope.get("x", scope));
	}
	
	@Test
	public void testRequire() throws PhonScriptException, URISyntaxException {
		final String script = 
				"var TestExport = require('test_require').testExport;\nx = (new TestExport()).value;\n";
		
		final URL testURL = getClass().getClassLoader().getResource("lib/");
		
		final BasicScript bs = new BasicScript(script);
		bs.addRequirePath(testURL.toURI());
		
		final PhonScriptContext ctx = bs.getContext();
		final Scriptable scope = ctx.createImporterScope();
		final Object val = ctx.exec(scope);
		
		Assert.assertEquals(Integer.class, val.getClass());
		Assert.assertEquals(2, (int)Integer.valueOf(val.toString()));
		
		Assert.assertEquals(val, scope.get("x", scope));
	}
	
}
