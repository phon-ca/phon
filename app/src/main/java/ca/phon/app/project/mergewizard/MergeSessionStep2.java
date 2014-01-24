/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.app.project.mergewizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.JXList;

import ca.phon.app.session.RecordFilterPanel;
import ca.phon.project.Project;
import ca.phon.session.AbstractRecordFilter;
import ca.phon.session.Record;
import ca.phon.session.RecordFilter;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.CollatorFactory;

/**
 * Provide a record filter for each session
 * selected in step 1.
 *
 */
public class MergeSessionStep2 extends WizardStep {
	
	final private static Logger LOGGER = Logger.getLogger(MergeSessionStep2.class.getName());
	
	/**
	 * Project
	 */
	private Project project;
	
	/**
	 * Sessions+panels
	 */
	private Map<SessionPath, RecordFilterPanel> panels;
	
	/**
	 * Card layout
	 */
	private CardLayout filterLayout;
	private JPanel cardPanel;
	
	/**
	 * Session list
	 */
	private JXList sessionList;
	
	/**
	 * Header
	 */
	private DialogHeader header;
	
	/**
	 * Constructor
	 */
	public MergeSessionStep2(Project project, List<SessionPath> sessions) {
		super();
		
		this.project = project;
		
		setupFilters(sessions);
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		header = new DialogHeader("Merge Sessions", "Select records for merge.");
		add(header, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		
//		Collator collator = CollatorFactory.defaultCollator();
		List<SessionPath> locations = new ArrayList<SessionPath>();
		locations.addAll(panels.keySet());
		Collections.sort(locations);
		
		JPanel sessionListPanel = new JPanel(new BorderLayout());
		sessionListPanel.setPreferredSize(new Dimension(200, 0));
		sessionListPanel.setBorder(BorderFactory.createTitledBorder("Session list"));
			
		sessionList = new JXList(locations.toArray(new SessionPath[0]));
		sessionList.setCellRenderer(new SessionLocationRenderer());
//		sessionList.setPreferredSize(new Dimension(200, 0));

		if(locations.size() > 0) {
			sessionList.setSelectedIndex(0);
		}
		sessionList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				SessionPath loc = (SessionPath)sessionList.getSelectedValue();
				if(loc != null) {
					filterLayout.show(cardPanel, loc.toString());
				}
			}
			
		});
		JScrollPane sessionScroller = new JScrollPane(sessionList);
		sessionListPanel.add(sessionScroller, BorderLayout.CENTER);
//		sessionList.setBorder(BorderFactory.createTitledBorder("Session"));
		centerPanel.add(sessionListPanel, BorderLayout.WEST);
		
		filterLayout = new CardLayout();
		cardPanel = new JPanel();
		cardPanel.setLayout(filterLayout);
		cardPanel.setBorder(BorderFactory.createTitledBorder("Select Records"));
		
		// add filter panels
		for(SessionPath loc:locations) {
//			SessionLocation loc = sessions.get(i);
			RecordFilterPanel panel = panels.get(loc);
			cardPanel.add(panel, loc.toString());
		}
		
		centerPanel.add(cardPanel, BorderLayout.CENTER);
		add(centerPanel, BorderLayout.CENTER);
	}
	
	public void setSelectedSessions(List<SessionPath> sessions) {
		setupFilters(sessions);
	
		// sort first
		List<SessionPath> sortedSessions = new ArrayList<SessionPath>();
		sortedSessions.addAll(sessions);
		Collections.sort(sortedSessions, 
				new Comparator<SessionPath>() {

					@Override
					public int compare(SessionPath o1, SessionPath o2) {
						Collator collator = CollatorFactory.defaultCollator();
						return collator.compare(o1.toString(), o2.toString());
					}
			
				}
		);

		cardPanel.removeAll();
		DefaultListModel newModel = new DefaultListModel();
		for(SessionPath loc:sortedSessions) {
			newModel.addElement(loc);
			
			RecordFilterPanel panel = panels.get(loc);
			cardPanel.add(panel, loc.toString());
		}
		sessionList.setModel(newModel);
		if(newModel.size() > 0) {
			sessionList.setSelectedIndex(0);
		}
	}
	
	private void setupFilters(List<SessionPath> sessions) {
		if(panels == null) {
			panels = new TreeMap<SessionPath, RecordFilterPanel>();
		}
		
		for(SessionPath loc:sessions) {
			if(!panels.containsKey(loc)) {
				try {
					final Session t = project.openSession(loc.getCorpus(), loc.getSession());
	
					RecordFilterPanel panel = new RecordFilterPanel(project, t);
					panels.put(loc, panel);
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
		}
		
		// remove any old references
		for(SessionPath loc:panels.keySet().toArray(new SessionPath[0])) {
			if(!sessions.contains(loc)) {
//				UtteranceFilterPanel panel = panels.get(loc);
//				
//				if(cardPanel != null) {
//					cardPanel.remove(panel);
//				}
//				
				panels.remove(loc);
			}
		}
	}
	
	// we already have the transcripts loaded, avoid doing it again
	public Session getSessionAtLocation(SessionPath loc) {
		RecordFilterPanel panel = panels.get(loc);
		
		Session retVal = null;
		if(panel != null) {
			retVal = panel.getSession();
		}
		return retVal;
	}
	
	public RecordFilter getFilterForLocation(SessionPath loc) {
		RecordFilterPanel panel = panels.get(loc);
		
		RecordFilter retVal = null;
		if(panel != null) {
			retVal = panel.getRecordFilter();
		} else {
			retVal = new AbstractRecordFilter() {

				@Override
				public boolean checkRecord(Record utt) {
					return true;
				}
				
			};
		}
		
		return retVal;
	}
	
	private class SessionLocationRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel retVal = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			
			SessionPath loc = (SessionPath)value;
			
			String txt = loc.getCorpus() + "." + loc.getSession();
			
			retVal.setText(txt);
			
			return retVal;
		}
		
	}
}
