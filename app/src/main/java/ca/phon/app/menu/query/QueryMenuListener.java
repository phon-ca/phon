package ca.phon.app.menu.query;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.project.ProjectFrameExtension;
import ca.phon.app.query.QueryEditorEP;
import ca.phon.app.query.QueryEditorWindow;
import ca.phon.app.query.QueryHistory;
import ca.phon.plugin.PluginAction;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.project.Project;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.query.script.QueryScriptLibrary;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.util.resources.ResourceLoader;

/**
 * Dynamic building of query menu
 */
public class QueryMenuListener implements MenuListener {
	
	public void menuCanceled(MenuEvent e) {
		
	}

	public void menuDeselected(MenuEvent e) {
	
	}

	public void menuSelected(MenuEvent e) {
		final JMenu queryMenu = (JMenu)e.getSource();
		queryMenu.removeAll();
		
		final ProjectFrameExtension pfe = CommonModuleFrame.getCurrentFrame().getExtension(ProjectFrameExtension.class);
		final Project project = (pfe == null ? null : pfe.getProject());
		if(project == null) return;
//		
		final QueryScriptLibrary queryScriptLibrary = new QueryScriptLibrary();
		
		// add stock scripts
		final ResourceLoader<QueryScript> stockScriptLoader = queryScriptLibrary.stockScriptFiles();
		final Iterator<QueryScript> stockScriptIterator = stockScriptLoader.iterator();
		while(stockScriptIterator.hasNext()) {
			final QueryScript qs = stockScriptIterator.next();
			
			final JMenuItem sItem = new JMenuItem(new QueryScriptCommand(project, qs));
			queryMenu.add(sItem);
		}
//
//		// add user library scripts
//		List<JMenuItem> libItems = new ArrayList<JMenuItem>();
//		for(File libScriptFile:queryScriptLibrary.userScriptFiles()) {
//			final SearchAction act = new  SearchAction(project, libScriptFile);
//			JMenuItem sItem = new JMenuItem(act);
//			
//			libItems.add(sItem);
//		}
//		if(libItems.size() > 0) {
//			JMenuItem libSepItem = new JMenuItem("User Library");
//			libSepItem.setEnabled(false);
//			
//			queryMenu.add(libSepItem);
//			for(JMenuItem itm:libItems)
//				queryMenu.add(itm);
//		}
//
//		if(project != null) {
//			// add project scripts
//			List<JMenuItem> projItems = new ArrayList<JMenuItem>();
//			
//			for(File projScriptFile:queryScriptLibrary.projectScriptFiles(project)) {
//				final SearchAction act = new SearchAction(project, projScriptFile);
//				JMenuItem sItem = new JMenuItem(act);
//
//				projItems.add(sItem);
//			}
//			if(projItems.size() > 0) {
//				JMenuItem projSepItem = new JMenuItem("Project Scripts");
//				projSepItem.setEnabled(false);
//
//				queryMenu.add(projSepItem);
//				for(JMenuItem itm:projItems)
//					queryMenu.add(itm);
//			}
//		}
//		
		final JMenuItem scriptItem = new JMenuItem(new QueryScriptEditorCommand(project));
		final JMenuItem historyItem = new JMenuItem(new QueryHistoryCommand(project));
		
		queryMenu.addSeparator();
		queryMenu.add(scriptItem);
		queryMenu.add(historyItem);
	}

}
