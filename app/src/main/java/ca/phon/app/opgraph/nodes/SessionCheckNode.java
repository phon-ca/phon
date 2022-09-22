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
package ca.phon.app.opgraph.nodes;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.check.*;
import ca.phon.app.session.editor.*;
import ca.phon.extensions.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.plugin.*;
import ca.phon.project.Project;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.session.*;
import ca.phon.session.check.*;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@OpNodeInfo(name="Session Check", description="Check session for errors", category="Session", showInLibrary=true)
public class SessionCheckNode extends OpNode implements NodeSettings{

	private InputField projectInput = new InputField("project", "Project", Project.class);
	
	private InputField sessionInput = new InputField("session", "Session", Session.class, SessionPath.class);
	
	private OutputField sessionModifiedOutput = new OutputField("sessionModified", "true if session was modified", true, Boolean.class);
	
	private OutputField hasWarningsOutput = new OutputField("hasWarnings", "true if warnings table is not empty", true, Boolean.class);
	
	private OutputField warningsOutput = new OutputField("warnings", "Table of warnings", true, DefaultTableDataSource.class);
	
	private Map<SessionCheck, Boolean> checkMap;
	
	private JPanel settingsPanel;
	private Map<SessionCheck, JCheckBox> checkBoxMap;
	private Map<SessionCheck, SessionCheckUI> checkUIMap;
	
	public SessionCheckNode() {
		super();
		
		putField(projectInput);
		putField(sessionInput);
		putField(sessionModifiedOutput);
		putField(hasWarningsOutput);
		putField(warningsOutput);
		
		checkMap = new LinkedHashMap<>();
		for(IPluginExtensionPoint<SessionCheck> extPt:PluginManager.getInstance().getExtensionPoints(SessionCheck.class)) {
			SessionCheck check = extPt.getFactory().createObject();
			checkMap.put(check, check.performCheckByDefault());
		}
		
		putExtension(NodeSettings.class, this);
	}
	
	public Set<SessionCheck> getChecks() {
		return checkMap.keySet();
	}
	
	public boolean isIncludeCheck(SessionCheck sessionCheck) {
		if(checkBoxMap != null) {
			JCheckBox checkBox = checkBoxMap.get(sessionCheck);
			return checkBox.isSelected();
		} else {
			return checkMap.get(sessionCheck);
		}
	}
	
	public void setIncludeCheck(SessionCheck sessionCheck, boolean inc) {
		checkMap.put(sessionCheck, inc);
		if(checkBoxMap != null) {
			JCheckBox checkBox = checkBoxMap.get(sessionCheck);
			checkBox.setSelected(inc);
		}
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		Project project = (Project)context.get(projectInput);
		if(project == null) throw new ProcessingException(null, new NullPointerException("project"));
		
		AtomicReference<SessionPath> spRef = new AtomicReference<SessionPath>();
		Session session = null;
		var sessionObj = context.get(sessionInput);
		if(sessionObj instanceof SessionPath) {
			spRef.set((SessionPath)sessionObj);
			if(spRef.get().getExtension(SessionEditor.class) != null) {
				session = spRef.get().getExtension(SessionEditor.class).getSession();
			} else {
				try {
					session = project.openSession(spRef.get().getCorpus(), spRef.get().getSession());
				} catch (IOException e) {
					throw new ProcessingException(null, e);
				}
			}
		} else if(sessionObj instanceof Session) {
			session = (Session)sessionObj;
		}
		if(spRef.get() == null)
			spRef.set(new SessionPath(session.getCorpus(), session.getName()));
		
		DefaultTableDataSource warningsTable = new DefaultTableDataSource();
		int col = 0;
		warningsTable.setColumnTitle(col++, "Severity");
		warningsTable.setColumnTitle(col++, "Session");
		warningsTable.setColumnTitle(col++, "Record #");
		warningsTable.setColumnTitle(col++, "Tier");
		warningsTable.setColumnTitle(col++, "Group #");
		warningsTable.setColumnTitle(col++, "Value");
		warningsTable.setColumnTitle(col++, "Message");
		
		SessionValidator validator = new SessionValidator(
				getChecks().stream()
					.filter( this::isIncludeCheck )
					.collect( Collectors.toList() ));
		validator.addValidationListener( (ve) -> {
			Object row[] = new Object[warningsTable.getColumnCount()];
			int c = 0;
			row[c++] = StringUtils.capitalize(ve.getSeverity().toString().toLowerCase());
			row[c++] = spRef.get();
			row[c++] = ve.getRecord() + 1;
			row[c++] = ve.getTierName();
			row[c++] = ve.getGroup() + 1;
			
			var groupVal = ve.getSession().getRecord(ve.getRecord()).getGroup(ve.getGroup()).getTier(ve.getTierName());
			if(groupVal instanceof IExtendable) {
				if(((IExtendable)groupVal).getExtension(UnvalidatedValue.class) != null) {
					groupVal = ((IExtendable)groupVal).getExtension(UnvalidatedValue.class).getValue();
				}
			}
			row[c++] = groupVal;
			row[c++] = ve.toString();
			warningsTable.addRow(row);
		});
		boolean modified = validator.validate(session);
		
		if(modified) {
			if(spRef.get().getExtension(SessionEditor.class) != null) {
				// tell editor a modification has occured and refresh
				final SessionEditor editor = spRef.get().getExtension(SessionEditor.class);
				final EditorEvent<Void> evt = new EditorEvent<>(
						new EditorEventType<>(EditorEventName.MODIFICATION_EVENT + "CHECK_SESSION", Void.class), editor, null);
				editor.getEventManager().queueEvent(evt);
				SwingUtilities.invokeLater( () -> spRef.get().getExtension(SessionEditor.class).setModified(true) );
				
				final EditorEvent<EditorEventType.RecordChangedData> evt2 = new EditorEvent<>(EditorEventType.RecordRefresh, editor,
						new EditorEventType.RecordChangedData(editor.getCurrentRecordIndex(), editor.currentRecord()));
				editor.getEventManager().queueEvent(evt2);
			} else {
				try {
					UUID writeLock = project.getSessionWriteLock(session);
					project.saveSession(session, writeLock);
					project.releaseSessionWriteLock(session, writeLock);
				} catch (IOException e) {
					LogUtil.severe(e);
				}
			}
		}

		context.put(sessionModifiedOutput, Boolean.valueOf(modified));
		context.put(hasWarningsOutput, Boolean.valueOf(warningsTable.getRowCount() > 0));
		context.put(warningsOutput, warningsTable);
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel(new VerticalLayout(5));
			
			checkBoxMap = new LinkedHashMap<>();
			checkUIMap = new LinkedHashMap<>();
			
			for(SessionCheck check:getChecks()) {
				JPanel checkPanel = new JPanel(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridwidth = 1;
				gbc.gridheight = 1;
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weightx = 1.0;
				gbc.weighty = 0.0;
				
				PhonPlugin pluginInfo = check.getClass().getAnnotation(PhonPlugin.class);
				JCheckBox checkBox = new JCheckBox(pluginInfo.name());
				checkBox.setSelected(checkMap.get(check));
				checkBox.setToolTipText(pluginInfo.comments());
				checkPanel.add(checkBox, gbc);
				
				gbc.gridy++;
				gbc.insets = new Insets(0, 20, 0, 0);
				checkBoxMap.put(check, checkBox);
				
				SessionCheckUI checkUI = (new SessionCheckUIFactory()).createUI(check);
				if(checkUI != null) {
					checkUIMap.put(check, checkUI);
					gbc.gridy++;
										
					Component comp = checkUI.getComponent();
					checkBox.addActionListener( (e) -> {
						comp.setEnabled(checkBox.isSelected());
					} );
					checkPanel.add(comp, gbc);
				}
				
				settingsPanel.add(checkPanel);
			}
		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		Properties props = new Properties();
		
		for(SessionCheck check:getChecks()) {
			String keyName = check.getClass().getName() + ".includeCheck";
			props.put(keyName, Boolean.toString(isIncludeCheck(check)));
			props.putAll(check.getProperties());
		}
		
		return props;
	}

	@Override
	public void loadSettings(Properties properties) {
		
	}

}
