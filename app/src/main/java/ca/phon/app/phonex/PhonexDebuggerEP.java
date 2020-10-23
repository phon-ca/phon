package ca.phon.app.phonex;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import ca.phon.plugin.*;
import ca.phon.ui.*;
import ca.phon.ui.decorations.*;

@PhonPlugin(minPhonVersion = "3.1.1")
public class PhonexDebuggerEP implements IPluginEntryPoint {

	public static String EP_NAME = "PhonexDebugger";
	
	@Override
	public String getName() {
		return EP_NAME;
	}

	@Override
	public void pluginStart(Map<String, Object> args) {
		PhonexDebuggerWindow debugWindow = new PhonexDebuggerWindow();
		debugWindow.pack();
		debugWindow.setLocationByPlatform(true);
		debugWindow.setVisible(true);
	}
	
	private class PhonexDebuggerWindow extends CommonModuleFrame {
		
		PhonexDebugger debugger = new PhonexDebugger();
		
		public PhonexDebuggerWindow() {
			super();
			
			setTitle("Phonex Debugger");
			init();
		}
		
		private void init() {
			setLayout(new BorderLayout());
			
			DialogHeader header = new DialogHeader("Phonex Debugger", "Visualize and debug phonex expressions");
			add(header, BorderLayout.NORTH);
			add(debugger, BorderLayout.CENTER);
		}

		@Override
		public void setJMenuBar(JMenuBar menubar) {
			super.setJMenuBar(menubar);
		}
		
	}

}
