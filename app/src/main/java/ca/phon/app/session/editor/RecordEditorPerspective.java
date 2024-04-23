/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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

import bibliothek.gui.dock.common.perspective.CControlPerspective;
import bibliothek.gui.dock.common.perspective.CGridPerspective;
import bibliothek.gui.dock.common.perspective.CPerspective;
import bibliothek.gui.dock.common.perspective.CWorkingPerspective;
import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import ca.phon.app.log.LogUtil;
import ca.phon.util.PrefHelper;
import ca.phon.util.resources.*;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Perspective for record editor docking views.  This class
 * is just a pointer to the name and location of where the
 * perspective lives.  It also provides methods for managing
 * the list of available perspectives.
 */
public class RecordEditorPerspective {

	public final static String LAST_USED_PERSPECTIVE_NAME = "Previous";

	private final static String PERSPECTIVE_LIST = "META-INF/perspectives/perspectives.list";

	public final static File PERSPECTIVES_FOLDER = new File(PrefHelper.getUserDataFolder() +
			File.separator + "perspectives");

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
					LogUtil.info("Could not delete perspective file: " + file.getAbsolutePath());
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

	/**
	 * perspective
	 */
	private CPerspective perspective;

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
		this.perspective = null;
	}

	public RecordEditorPerspective(String name, CPerspective perspective) {
		super();
		this.name = name;
		this.location = null;
		this.perspective = perspective;
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
	 * Return perspective or load from file if not already loaded
	 */
	public CPerspective getPerspective(CControlPerspective perspectives) {
		if(perspective == null) {
			try(InputStream is = getLocation().openStream()) {
				final XElement xele = XIO.readUTF(is);
				perspective = perspectives.readXML( xele );
			} catch (IOException e) {
				LogUtil.warning(e);
			}
		}
		return perspective;
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
