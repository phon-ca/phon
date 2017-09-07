/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.script;
import java.net.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mozilla.javascript.Scriptable;

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
