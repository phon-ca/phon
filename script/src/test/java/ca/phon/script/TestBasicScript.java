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
import java.net.*;

import org.junit.Test;
import org.junit.runner.*;
import org.junit.runners.*;
import org.mozilla.javascript.*;

import junit.framework.Assert;


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
