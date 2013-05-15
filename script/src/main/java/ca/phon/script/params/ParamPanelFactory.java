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
package ca.phon.script.params;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.Painter;

import ca.phon.ui.PhonGuiConstants;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ParamPanelFactory {
	
	/**
	 * Create a panel using the given script params.
	 * 
	 * @param scriptParams
	 * @return the generated panel
	 */
	public static JComponent buildScriptParamPanel2(ScriptParam[] params) {
		// very simple form layout
		String cols = "right:pref, 3dlu, fill:default:grow, 3dlu";
		String rows = "";
		FormLayout layout = new FormLayout(cols, rows);
		
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		
		JXTaskPane taskPane = null;
		JXTaskPaneContainer tpContainer = new JXTaskPaneContainer();
		
		for(ScriptParam param:params) {
			if(param.getParamType().equals("separator")) {
				if(taskPane != null) {
					JComponent panel = builder.getPanel();
					panel.setOpaque(false);
					taskPane.add(builder.getPanel());
					tpContainer.add(taskPane);
				}
				taskPane = new JXTaskPane();
				taskPane.setTitle(param.getParamDesc());
				taskPane.setCollapsed(((SeparatorScriptParam)param).isCollapsed());
				builder = new DefaultFormBuilder(new FormLayout(cols, rows));
			} else {
				JComponent comp = param.getEditorComponent();
//				comp.setOpaque(false);
				builder.append(new JLabel(param.getParamDesc()), comp);
			}
		}
		
		if(taskPane != null && builder.getPanel().getComponentCount() > 0)
		{
			JComponent panel = builder.getPanel();
//			panel.setOpaque(false);
			taskPane.add(panel);
			tpContainer.add(taskPane);
		}
		
		return tpContainer;
	}

	/**
	 * A different approach
	 */
	public static JComponent buildScriptParamPanel(ScriptParam[] params) {
		JPanel retVal = new JPanel(new VerticalLayout(2));
		retVal.setOpaque(true);
		
//		retVal.setBackground(Color.white);
		
		JPanel currentContainer = retVal;
		
		for(ScriptParam sp:params) {
			if(sp.getParamType().equals("separator")) {
				// create a new collapsible panel
				JXCollapsiblePane collasiblePane = new JXCollapsiblePane(Direction.DOWN);
//				collasiblePane.setOpaque(true);
//				collasiblePane.setBackground(Color.white);
				collasiblePane.setAnimated(false);
				collasiblePane.setLayout(new VerticalLayout());
				collasiblePane.setCollapsed(((SeparatorScriptParam)sp).isCollapsed());
				currentContainer = collasiblePane;
				
				JXButton btn = getToggleButton(sp.getParamDesc(), collasiblePane);
				retVal.add(btn);
				retVal.add(collasiblePane);
			} else {
				JComponent comp = sp.getEditorComponent();
				String cols = "20px, fill:pref:grow";
				String rows = "pref, pref";
				FormLayout layout = new FormLayout(cols, rows);
				JPanel compPanel = new JPanel(layout);
				CellConstraints cc = new CellConstraints();
				compPanel.add(new JLabel(sp.getParamDesc()), cc.xyw(1,1,2));
				compPanel.add(comp, cc.xy(2, 2));
//				compPanel.setOpaque(false);
				
				currentContainer.add(compPanel);
			}
		}
		
		return retVal;
	}
	
	private static JXButton getToggleButton(String name, JXCollapsiblePane cp) {
		Action toggleAction = cp.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION);
		
		// use the collapse/expand icons from the JTree UI
		toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON,
		                      UIManager.getIcon("Tree.expandedIcon"));
		toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON,
		                      UIManager.getIcon("Tree.collapsedIcon"));
		toggleAction.putValue(Action.NAME, name);

		JXButton btn = new JXButton(toggleAction) {
			@Override
			public Insets getInsets() {
				Insets retVal = super.getInsets();
				
				retVal.top = 0;
				retVal.bottom = 0;
				
				return retVal;
			}
		};
		
		btn.setHorizontalAlignment(SwingConstants.LEFT);
		
		btn.setBackgroundPainter(new Painter<JXButton>() {
			
			@Override
			public void paint(Graphics2D g, JXButton object, int width, int height) {
				MattePainter mp = new MattePainter(PhonGuiConstants.PHON_SELECTED);
				mp.paint(g, object, width, height);
			}
		});
		
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		return btn;
	}
	
}
