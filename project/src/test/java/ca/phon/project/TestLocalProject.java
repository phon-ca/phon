package ca.phon.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ca.phon.project.exceptions.ProjectConfigurationException;

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
		final ProjectFactory projectFactory = new ProjectFactory();
		final Project project = projectFactory.openProject(new File("src/test/resources/TestCorpus"));
		
		Assert.assertNotNull(project);
		Assert.assertEquals("TestCorpus", project.getName());
		Assert.assertEquals("969c122a-6049-41c0-a347-4ecdebae8e89", project.getUUID().toString());
	}

	@Test
	public void testCorpusList() throws IOException, ProjectConfigurationException {
		final ProjectFactory projectFactory = new ProjectFactory();
		final Project project = projectFactory.openProject(new File("src/test/resources/TestCorpus"));
		
		Assert.assertNotNull(project);
		Assert.assertEquals(Collections.singleton("Anne").toString(), project.getCorpora().toString());
	}
	
	@Test
	public void testSessionList() throws IOException, ProjectConfigurationException {
		final ProjectFactory projectFactory = new ProjectFactory();
		final Project project = projectFactory.openProject(new File("src/test/resources/TestCorpus"));
		
		Assert.assertNotNull(project);
		Assert.assertEquals(Collections.singleton("TestSession").toString(), project.getCorpusSessions("Anne").toString());
	}
	
}
