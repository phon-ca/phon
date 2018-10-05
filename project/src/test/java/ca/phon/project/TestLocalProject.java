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
package ca.phon.project;

import java.io.File;
import java.io.IOException;
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
