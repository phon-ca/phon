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
package ca.phon.app.session.editor;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;

import ca.phon.util.PrefHelper;
import ca.phon.util.resources.ClassLoaderHandler;
import ca.phon.util.resources.FolderHandler;
import ca.phon.util.resources.ResourceLoader;

/**
 * Perspective for record editor docking views.  This class
 * is just a pointer to the name and location of where the
 * perspective lives.  It also provides methods for managing
 * the list of available perspectives.
 */
public class RecordEditorPerspective {

	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(RecordEditorPerspective.class.getName());

	public final static String DEFAULT_PERSPECTIVE_NAME = "Default";

	public final static String LAST_USED_PERSPECTIVE_NAME = "Previous";

	private final static String PERSPECTIVE_LIST = "META-INF/layouts/layouts.list";

	public final static File PERSPECTIVES_FOLDER = new File(PrefHelper.getUserDataFolder() +
			File.separator + "layouts");

	/**
	 * Resource loader
	 */
	private static ResourceLoader<RecordEditorPerspective> resourceLoader;

	private static ResourceLoader<RecordEditorPerspective> getLoader() {
		if(resourceLoader == null) {
			resourceLoader = new ResourceLoader<RecordEditorPerspective>();
			// add handlers
			resourceLoader.addHandler(new PerspectiveClassLoaderHandler());
			resourceLoader.addHandler(new PerspectiveFolderHandler());
		}
		return resourceLoader;
	}

	/**
	 * Get the list of available perspectives
	 */
	public static RecordEditorPerspective[] availablePerspectives() {
		final List<RecordEditorPerspective> retVal =
				new ArrayList<RecordEditorPerspective>();
		final Iterator<RecordEditorPerspective> itr =
				getLoader().iterator();
		while(itr.hasNext()) {
			retVal.add(itr.next());
		}
		return retVal.toArray(new RecordEditorPerspective[0]);
	}

	/**
	 * Get stock perspectives.
	 *
	 * @return resource loader for stock perspectives
	 */
	public static ResourceLoader<RecordEditorPerspective> getStockPerspectives() {
		final ResourceLoader<RecordEditorPerspective> retVal = new ResourceLoader<>();
		retVal.addHandler(new PerspectiveClassLoaderHandler());
		return retVal;
	}

	/**
	 * Get user perspectives.
	 *
	 * @return resource loader for user perspectives
	 */
	public static ResourceLoader<RecordEditorPerspective> getUserPerspectives() {
		final ResourceLoader<RecordEditorPerspective> retVal = new ResourceLoader<>();
		retVal.addHandler(new PerspectiveFolderHandler());
		return retVal;
	}

	/**
	 * Attempt to delete the given perspective
	 *
	 * @param perspective
	 */
	public static void deletePerspective(RecordEditorPerspective perspective) {
		try {
			final File file = new File(perspective.location.toURI());
			if(file.canWrite()) {
				if(!file.delete()) {
					LOGGER.warn("Could not remove file: " + file.getAbsolutePath());
				}
			}
		} catch (URISyntaxException e) {
		}
	}

	/**
	 * Get the perspective with the given name.
	 *
	 * @param name
	 */
	public static RecordEditorPerspective getPerspective(String name) {
		RecordEditorPerspective retVal = null;

		for(RecordEditorPerspective perspective:availablePerspectives()) {
			if(perspective.getName().equalsIgnoreCase(name)) {
				retVal = perspective;
			}
		}

		return retVal;
	}

	/**
	 * perspective name
	 */
	private String name;

	/**
	 * perspective location
	 */
	private URL location;

	public RecordEditorPerspective() {
		super();
	}

	public RecordEditorPerspective(String name) {
		super();
		this.name = name;
	}

	public RecordEditorPerspective(String name, URL location) {
		super();
		this.name = name;
		this.location = location;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public URL getLocation() {
		return location;
	}

	public void setLocation(URL location) {
		this.location = location;
	}

	/**
	 * Loads perspectives from the classpath.  Each perspective
	 * file should be listed in the file {@link #PERSPECTIVE_LIST}
	 */
	private static class PerspectiveClassLoaderHandler extends ClassLoaderHandler<RecordEditorPerspective> {

		public PerspectiveClassLoaderHandler() {
			super();
			super.loadResourceFile(PERSPECTIVE_LIST);
		}

		@Override
		public RecordEditorPerspective loadFromURL(URL url) throws IOException {
			String name = URLDecoder.decode(url.getPath(), "UTF-8");
			name = name.substring(0, name.lastIndexOf('.'));
			name = name.substring(name.lastIndexOf('/')+1);

			return new RecordEditorPerspective(name, url);
		}

	}

	/**
	 * Scans for .xml files in $USER_PREF_DIR/perspectives.
	 *
	 */
	private static class PerspectiveFolderHandler extends FolderHandler<RecordEditorPerspective> {

		public PerspectiveFolderHandler() {
			super(PERSPECTIVES_FOLDER);
			if(!PERSPECTIVES_FOLDER.exists()) {
				PERSPECTIVES_FOLDER.mkdirs();
			}
			super.setFileFilter(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					return pathname.getName().endsWith(".xml");
				}
			});
		}

		@Override
		public RecordEditorPerspective loadFromFile(File f) throws IOException {
			final String name = f.getName().substring(0, f.getName().lastIndexOf('.'));
			return new RecordEditorPerspective(name, f.toURI().toURL());
		}

	}
}
