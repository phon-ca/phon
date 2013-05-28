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

import ca.phon.application.project.IPhonProject;
import ca.phon.application.transcript.AbstractUtteranceFilter;
import ca.phon.application.transcript.ITranscript;
import ca.phon.application.transcript.IUtterance;
import ca.phon.application.transcript.SessionLocation;
import ca.phon.application.transcript.UtteranceFilter;
import ca.phon.gui.DialogHeader;
import ca.phon.gui.components.UtteranceFilterPanel;
import ca.phon.gui.wizard.WizardStep;
import ca.phon.system.logger.PhonLogger;
import ca.phon.util.CollatorFactory;

/**
 * Provide a record filter for each session
 * selected in step 1.
 *
 */
public class MergeSessionStep2 extends WizardStep {
	
	/**
	 * Project
	 */
	private IPhonProject project;
	
	/**
	 * Sessions+panels
	 */
	private Map<SessionLocation, UtteranceFilterPanel> panels;
	
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
	public MergeSessionStep2(IPhonProject project, List<SessionLocation> sessions) {
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
		List<SessionLocation> locations = new ArrayList<SessionLocation>();
		locations.addAll(panels.keySet());
		Collections.sort(locations);
		
		JPanel sessionListPanel = new JPanel(new BorderLayout());
		sessionListPanel.setPreferredSize(new Dimension(200, 0));
		sessionListPanel.setBorder(BorderFactory.createTitledBorder("Session list"));
			
		sessionList = new JXList(locations.toArray(new SessionLocation[0]));
		sessionList.setCellRenderer(new SessionLocationRenderer());
//		sessionList.setPreferredSize(new Dimension(200, 0));

		if(locations.size() > 0) {
			sessionList.setSelectedIndex(0);
		}
		sessionList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				SessionLocation loc = (SessionLocation)sessionList.getSelectedValue();
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
		for(SessionLocation loc:locations) {
//			SessionLocation loc = sessions.get(i);
			UtteranceFilterPanel panel = panels.get(loc);
			cardPanel.add(panel, loc.toString());
		}
		
		centerPanel.add(cardPanel, BorderLayout.CENTER);
		add(centerPanel, BorderLayout.CENTER);
	}
	
	public void setSelectedSessions(List<SessionLocation> sessions) {
		setupFilters(sessions);
	
		// sort first
		List<SessionLocation> sortedSessions = new ArrayList<SessionLocation>();
		sortedSessions.addAll(sessions);
		Collections.sort(sortedSessions, 
				new Comparator<SessionLocation>() {

					@Override
					public int compare(SessionLocation o1, SessionLocation o2) {
						Collator collator = CollatorFactory.defaultCollator();
						return collator.compare(o1.toString(), o2.toString());
					}
			
				}
		);

		cardPanel.removeAll();
		DefaultListModel newModel = new DefaultListModel();
		for(SessionLocation loc:sortedSessions) {
			newModel.addElement(loc);
			
			UtteranceFilterPanel panel = panels.get(loc);
			cardPanel.add(panel, loc.toString());
		}
		sessionList.setModel(newModel);
		if(newModel.size() > 0) {
			sessionList.setSelectedIndex(0);
		}
	}
	
	private void setupFilters(List<SessionLocation> sessions) {
		if(panels == null) {
			panels = new TreeMap<SessionLocation, UtteranceFilterPanel>();
		}
		
		for(SessionLocation loc:sessions) {
			if(!panels.containsKey(loc)) {
				try {
					ITranscript t = project.getTranscript(loc.getCorpus(), loc.getSession());
	
					UtteranceFilterPanel panel = new UtteranceFilterPanel(project, t);
					panels.put(loc, panel);
				} catch (IOException e) {
					PhonLogger.warning(e.toString());
				}
			}
		}
		
		// remove any old references
		for(SessionLocation loc:panels.keySet().toArray(new SessionLocation[0])) {
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
	public ITranscript getSessionAtLocation(SessionLocation loc) {
		UtteranceFilterPanel panel = panels.get(loc);
		
		ITranscript retVal = null;
		if(panel != null) {
			retVal = panel.getSession();
		}
		return retVal;
	}
	
	public UtteranceFilter getFilterForLocation(SessionLocation loc) {
		UtteranceFilterPanel panel = panels.get(loc);
		
		UtteranceFilter retVal = null;
		if(panel != null) {
			retVal = panel.getRecordFilter();
		} else {
			retVal = new AbstractUtteranceFilter() {

				@Override
				public boolean checkUtterance(IUtterance utt) {
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
			
			SessionLocation loc = (SessionLocation)value;
			
			String txt = loc.getCorpus() + "." + loc.getSession();
			
			retVal.setText(txt);
			
			return retVal;
		}
		
	}
}
