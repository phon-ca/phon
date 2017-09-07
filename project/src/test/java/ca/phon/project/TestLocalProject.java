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
package ca.phon.project;

import java.io.*;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.project.exceptions.ProjectConfigurationException;
import junit.framework.Assert;

/**
 * Tests for the LocalProject implementation.
 * 
 */
@RunWith(JUnit4.class)
public class TestLocalProject {
	
	/**
	 * Test basic project info
	 * @throws ProjectConfigurationException 
	 * @throws IOException 
	 */
	@Test
	public void testProjectInfo() throws IOException, ProjectConfigurationException {
		final ProjectFactory projectFactory = new DefaultProjectFactory();
		final Project project = projectFactory.openProject(new File("src/test/resources/TestCorpus"));
		
		Assert.assertNotNull(project);
		Assert.assertEquals("TestCorpus", project.getName());
		Assert.assertEquals("969c122a-6049-41c0-a347-4ecdebae8e89", project.getUUID().toString());
	}

	@Test
	public void testCorpusList() throws IOException, ProjectConfigurationException {
		final ProjectFactory projectFactory = new DefaultProjectFactory();
		final Project project = projectFactory.openProject(new File("src/test/resources/TestCorpus"));
		
		Assert.assertNotNull(project);
		Assert.assertEquals(Collections.singleton("Anne").toString(), project.getCorpora().toString());
	}
	
	@Test
	public void testSessionList() throws IOException, ProjectConfigurationException {
		final ProjectFactory projectFactory = new DefaultProjectFactory();
		final Project project = projectFactory.openProject(new File("src/test/resources/TestCorpus"));
		
		Assert.assertNotNull(project);
		Assert.assertEquals(Collections.singleton("TestSession").toString(), project.getCorpusSessions("Anne").toString());
	}
	
}
