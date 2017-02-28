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
package ca.phon.app.opgraph.analysis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.undo.UndoableEdit;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.components.GraphOutline;
import ca.gedge.opgraph.app.components.OpGraphTreeModel;
import ca.gedge.opgraph.app.edits.graph.AddNodeEdit;
import ca.gedge.opgraph.extensions.CompositeNode;
import ca.phon.app.opgraph.editor.DefaultOpgraphEditorModel;
import ca.phon.app.opgraph.editor.OpgraphEditorModel;
import ca.phon.app.opgraph.wizard.NodeWizardReportTemplate;
import ca.phon.app.opgraph.wizard.ReportTemplateView;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.app.session.SessionSelector;
import ca.phon.project.Project;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.workspace.Workspace;

import static ca.phon.app.opgraph.wizard.WizardExtensionEvent.EventType;

public class AnalysisOpGraphEditorModel extends DefaultOpgraphEditorModel {

	private JPanel debugSettings;
	
	private ReportTemplateView reportTemplateView;
	
	private JComboBox<Project> projectList;
	
	private SessionSelector sessionSelector;

	private AnalysisWizardExtension wizardExt;
	
	public AnalysisOpGraphEditorModel() {
		this(new OpGraph());
	}
	
	public AnalysisOpGraphEditorModel(OpGraph opgraph) {
		super(opgraph);
		
		WizardExtension ext = opgraph.getExtension(WizardExtension.class);
		if(ext != null && !(ext instanceof AnalysisWizardExtension)) {
			throw new IllegalArgumentException("Graph is not an analysis document.");
		}
		if(ext == null) {
			ext = new AnalysisWizardExtension(opgraph);
			opgraph.putExtension(WizardExtension.class, ext);
		}
		wizardExt = (AnalysisWizardExtension)ext;
		
		init();
	}

	private void init() {
		final GraphOutline graphOutline = getGraphOutline();
		final OpGraphTreeModel model = graphOutline.getModel();
		final OpGraph graph = model.getGraph();
		final WizardExtension ext = graph.getExtension(WizardExtension.class);
		
		ext.addWizardExtensionListener( (e) -> {
			switch(e.getEventType()) {
			case NODE_MAKRED_AS_OPTIONAL:
			case NODE_MAKRED_AS_NONOPTIONAL:
			case NODE_ADDED_TO_SETTINGS:
			case NODE_REMOVED_FROM_SETTINGS:
			case NODE_MARKED_AS_REQUIRED:
			case NODE_MAKRED_AS_NOT_REQUIRED:
				model.nodeChanged(e.getNode());
				break;
				
			default:
				break;
			}
		});
		
		final AnalysisCellRenderer renderer = new AnalysisCellRenderer(graphOutline.getTree().getCellRenderer());
		graphOutline.getTree().setCellRenderer(renderer);
		
		getDocument().getUndoSupport().addUndoableEditListener( (e) -> {
			final UndoableEdit edit = e.getEdit();
			if(edit instanceof AddNodeEdit) {
				final AnalysisWizardExtension graphExtension = 
						(AnalysisWizardExtension)getWizardExtension();
				
				final OpNode addedNode = ((AddNodeEdit)edit).getNode();
				if(addedNode instanceof CompositeNode) {
					final OpGraph addedGraph = ((CompositeNode)addedNode).getGraph();
					
					final WizardExtension wizardExt = addedGraph.getExtension(WizardExtension.class);
					if(wizardExt != null && wizardExt instanceof AnalysisWizardExtension) {
						final AnalysisWizardExtension analysisExt = (AnalysisWizardExtension)wizardExt;
						
						for(OpNode node:analysisExt) {
							graphExtension.addNode(node);
							graphExtension.setNodeForced(node, analysisExt.isNodeForced(node));
						}
						
						for(OpNode optionalNode:analysisExt.getOptionalNodes()) {
							graphExtension.addOptionalNode(optionalNode);
							graphExtension.setOptionalNodeDefault(optionalNode, analysisExt.getOptionalNodeDefault(optionalNode));
						}
						
						// copy report template
						final NodeWizardReportTemplate prefixTemplate = graphExtension.getReportTemplate("Report Prefix");
						final NodeWizardReportTemplate suffixTemplate = graphExtension.getReportTemplate("Report Suffix");
						final NodeWizardReportTemplate pt =
								analysisExt.getReportTemplate("Report Prefix");
						if(pt != null) {
							if(!prefixTemplate.getTemplate().contains(pt.getTemplate())) {
								prefixTemplate.setTemplate(prefixTemplate.getTemplate() + "\n" + pt.getTemplate());
							}
						}
						
						final NodeWizardReportTemplate st = 
								analysisExt.getReportTemplate("Report Suffix");
						if(st != null) {
							if(!suffixTemplate.getTemplate().contains(st.getTemplate())) {
								suffixTemplate.setTemplate(suffixTemplate.getTemplate() + "\n" + st.getTemplate());
							}
						}
					}
				}
			}
		});
	}

	public SessionSelector getSessionSelector() {
		return this.sessionSelector;
	}
	
	private WizardExtension getWizardExtension() {
		return getDocument().getRootGraph().getExtension(WizardExtension.class);
	}
	
	@Override
	public GraphOutline getGraphOutline() {
		return super.getGraphOutline();
	}

	@Override
	protected Map<String, JComponent> getViewMap() {
		final Map<String, JComponent> retVal = super.getViewMap();
		retVal.put("Report Template", getReportTemplateView());
		retVal.put("Debug Settings", getDebugSettings());
		return retVal;
	}
	
	protected JComponent getReportTemplateView() {
		if(reportTemplateView == null) {
			reportTemplateView = new ReportTemplateView(getDocument());
		}
		return reportTemplateView;
	}
	
	protected JComponent getDebugSettings() {
		if(debugSettings == null) {
			debugSettings = new JPanel();
			
			final Workspace workspace = Workspace.userWorkspace();
			projectList = new JComboBox<Project>(workspace.getProjects().toArray(new Project[0]));
			projectList.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Project"), 
					projectList.getBorder()));
			
			projectList.addItemListener( (e) -> {
				sessionSelector.setProject((Project)projectList.getSelectedItem());
			} );
			
			sessionSelector = new SessionSelector();
			final JScrollPane sessionScroller = new JScrollPane(sessionSelector);
			sessionScroller.setBorder(BorderFactory.createTitledBorder("Sessions"));
			
			debugSettings.setLayout(new BorderLayout());
			debugSettings.add(projectList, BorderLayout.NORTH);
			debugSettings.add(sessionScroller, BorderLayout.CENTER);
		}
		return debugSettings;
	}

	@Override
	public Rectangle getInitialViewBounds(String viewName) {
		Rectangle retVal = new Rectangle();
		switch(viewName) {
		case "Canvas":
			retVal.setBounds(200, 0, 600, 600);
			break;
			
		case "Debug Settings":
			retVal.setBounds(0, 200, 200, 200);
			break;
			
		case "Report Template":
			retVal.setBounds(0, 0, 200, 200);
			break;
			
		case "Console":
			retVal.setBounds(0, 200, 200, 200);
			break;
			
		case "Debug":
			retVal.setBounds(0, 200, 200, 200);
			break;
			
		case "Connections":
			retVal.setBounds(800, 200, 200, 200);
			break;
			
		case "Library":
			retVal.setBounds(0, 0, 200, 200);
			break;
			
		case "Settings":
			retVal.setBounds(800, 0, 200, 200);
			break;
			
		default:
			retVal.setBounds(0, 0, 200, 200);
			break;
		}
		return retVal;
	}

	@Override
	public boolean isViewVisibleByDefault(String viewName) {
		return super.isViewVisibleByDefault(viewName)
				|| viewName.equals("Debug Settings")
				|| viewName.equals("Report Template");
	}

	@Override
	public String getDefaultFolder() {
		return super.getDefaultFolder();
	}
	
	@Override
	public String getTitle() {
		return "Analysis Composer";
	}

	@Override
	public boolean validate() {
		return super.validate();
	}

	@Override
	public void setupContext(OpContext context) {
		super.setupContext(context);
		
		context.put("_project", projectList.getSelectedItem());
		context.put("_selectedSessions", sessionSelector.getSelectedSessions());
	}
	
	private class AnalysisCellRenderer extends DefaultTreeCellRenderer {
		
		private TreeCellRenderer parentRenderer;
		
		private ImageIcon oIcon;
		
		private ImageIcon sIcon;
		
		private ImageIcon rIcon;
		
		public AnalysisCellRenderer(TreeCellRenderer parent) {
			super();
			
			this.parentRenderer = parent;
			
			final IconManager iconManager = IconManager.getInstance();
			oIcon = iconManager.createGlyphIcon(new Character('O'), UIManager.getFont("Label.font").deriveFont(Font.BOLD), Color.BLACK,
					new Color(255, 255, 255));
			sIcon = iconManager.createGlyphIcon(new Character('S'), UIManager.getFont("Label.font"), Color.black, Color.WHITE);
			rIcon =  iconManager.createGlyphIcon(new Character('R'), UIManager.getFont("Label.font"), Color.black, Color.WHITE);
		}
		
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			final WizardExtension ext = getWizardExtension();
			JLabel retVal = (JLabel) parentRenderer.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			
			if(value instanceof DefaultMutableTreeNode) {
				final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)value;
				
				if(treeNode.getUserObject() != null && treeNode.getUserObject() instanceof OpNode) {
					final OpNode node = (OpNode)treeNode.getUserObject();
					
					final List<ImageIcon> icons = new ArrayList<>();
					icons.add((ImageIcon)retVal.getIcon());
				
					if(ext.isNodeOptional(node)) {
						icons.add(oIcon);
					}
					
					if(ext.containsNode(node)) {
						if(ext.isNodeForced(node)) {
							icons.add(rIcon);
						} else {
							icons.add(sIcon);
						}
					}
					
					final ImageIcon icn = IconManager.getInstance().createIconStrip(icons.toArray(new ImageIcon[0]));
					retVal.setIcon(icn);
				}
			}
			
			return retVal;
		}
		
	}
	
}
