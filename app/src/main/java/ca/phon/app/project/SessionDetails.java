package ca.phon.app.project;

import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.*;

import org.codehaus.groovy.transform.LazyASTTransformation;
import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.log.LogUtil;
import ca.phon.project.Project;
import ca.phon.util.icons.*;

public class SessionDetails extends JPanel {

	private final Project project;
	
	private String corpus;
	
	private String session;
	
	private JLabel fileLabel;
	private JLabel modifiedLabel;
	private JLabel recordsLabel;
	
	public SessionDetails(Project project) {
		super();
		
		this.project = project;
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		fileLabel = new JLabel();
		
		modifiedLabel = new JLabel();
		
		recordsLabel = new JLabel();
		
		final JPanel detailsPanel = new JPanel(new GridBagLayout());
		detailsPanel.setOpaque(false);
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		detailsPanel.add(new JLabel("File:"), gbc);
		++gbc.gridx;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 5, 0, 0);
		detailsPanel.add(fileLabel, gbc);
		
		++gbc.gridy;
		gbc.gridx = 0;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		detailsPanel.add(new JLabel("Modified:"), gbc);
		++gbc.gridx;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 5, 0, 0);
		detailsPanel.add(modifiedLabel, gbc);
		
		++gbc.gridy;
		gbc.gridx = 0;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		detailsPanel.add(new JLabel("Records:"), gbc);
		++gbc.gridx;
		gbc.insets = new Insets(0, 5, 0, 0);
		gbc.weightx = 1.0;
		detailsPanel.add(recordsLabel, gbc);
		
		detailsPanel.setBorder(BorderFactory.createTitledBorder("Details"));
		
		add(detailsPanel, BorderLayout.NORTH);
	}
	
	public String getCorpus() {
		return this.corpus;
	}
	
	public void setCorpus(String corpus) {
		this.corpus = corpus;
	}
	
	public String getSession() {
		return this.session;
	}
	
	public void setSession(String session) {
		this.session = session;
		update();
	}
	
	public void setSession(String corpus, String session) {
		setCorpus(corpus);
		setSession(session);
	}
	
	private void update() {
		if(this.corpus != null && this.session != null && project.getCorpusSessions(corpus).contains(session)) {
			final String sessionPath = project.getSessionPath(corpus, session);
			final File f = new File(sessionPath);
			final String name = f.getName();
			
			fileLabel.setText(name);
			fileLabel.setIcon(IconManager.getInstance().getSystemIconForPath(sessionPath, IconSize.SMALL));
			fileLabel.setToolTipText(sessionPath);
			
			try {
				int numRecords = project.numberOfRecordsInSession(corpus, session);
				recordsLabel.setText("" + numRecords);
			} catch (IOException e) {
				LogUtil.warning(e);
			}
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd@h:mma");
			final LocalDateTime time = project.getSessionModificationTime(corpus, session);
			modifiedLabel.setText(formatter.format(time));
		} else {
			fileLabel.setText("");
			fileLabel.setToolTipText("");
			fileLabel.setIcon(null);
			
			modifiedLabel.setText("");
			recordsLabel.setText("");
		}
	}
	
}
