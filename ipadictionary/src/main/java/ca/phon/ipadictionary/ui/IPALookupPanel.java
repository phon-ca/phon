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
package ca.phon.ipadictionary.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.text.*;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.apache.commons.lang3.*;
import org.apache.logging.log4j.*;

import ca.phon.ipadictionary.cmd.*;
import ca.phon.ipadictionary.exceptions.*;
import ca.phon.util.*;
import ca.phon.worker.*;

/**
 * UI for IPA Lookups
 *
 */
public class IPALookupPanel extends JPanel {

	private static final long serialVersionUID = 2278689330995573469L;

	private final static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(IPALookupPanel.class.getName());

	/** The output console */
	private JTextPane console;
	
	/** Our lookup context */
	private IPALookupContext context;
		
	/** Input field */
	private JTextField inputField;
	
	/** Query task */
	private class QueryTask extends PhonTask {
		
		/** The query string */
		private String query;
		
		public QueryTask(String qSt) {
			this.query = qSt;
		}

		@Override
		public void performTask() {
			// output input line
			context.fireMessage(">" + query);

			long st = System.currentTimeMillis();
			// parse the line
			ByteArrayInputStream bin = new ByteArrayInputStream(query.getBytes());
			try {
				ANTLRInputStream ain = new ANTLRInputStream(bin);
				IPADictLexer lexer = new IPADictLexer(ain);
				TokenStream tokens = new CommonTokenStream(lexer);
				
				IPADictParser parser = new IPADictParser(tokens);
				parser.setLookupContext(getLookupContext());
				IPADictParser.expr_return r = parser.expr();
				
				CommonTree t = (CommonTree)r.getTree();
				CommonTreeNodeStream nodeStream = new CommonTreeNodeStream(t);
				IPADictTree walker = new IPADictTree(nodeStream);
				walker.setLookupContext(context);
				walker.expr();
				
			} catch (IOException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
				err = e;
				context.fireError(e.getLocalizedMessage());
				super.setStatus(TaskStatus.ERROR);
				return;
			} catch (RecognitionException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
				err = e;
				context.fireError(e.getLocalizedMessage());
				super.setStatus(TaskStatus.ERROR);
				return;
			} catch (IPADictionaryExecption e) {
				LOGGER.error( e.getLocalizedMessage(), e);
				err = e;
				context.fireError(e.getLocalizedMessage());
				super.setStatus(TaskStatus.ERROR);
				return;
			}
			long et = System.currentTimeMillis();

			String msg =
					"Query completed in " + MsFormatter.msToDisplayString(et-st);
			context.fireMessage(msg);
		}
		
	}
	
	private class QueryActionListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			String query = StringUtils.strip(inputField.getText());
			inputField.setText("");
			
			if(query.startsWith("\"") && query.endsWith("\""))
				query = "lookup " + query;
			
			if(
					!query.startsWith("help") &&
					!query.startsWith("list") &&
					!query.startsWith("lookup") &&
					!query.startsWith("import") &&
					!query.startsWith("export") &&
					!query.startsWith("use") &&
					!query.startsWith("add") &&
					!query.startsWith("remove") &&
					!query.startsWith("create") &&
					!query.startsWith("drop")) {
				query = "lookup \"" + query + "\"";
			}
			QueryTask task = new QueryTask(query);
			task.run();
		}
		
	}
	
	private class ContextListener implements IPALookupContextListener {

		@Override
		public void dictionaryAdded(String newDictionary) {
		}

		@Override
		public void dictionaryChanged(String newDictionary) {
		}

		@Override
		public void handleMessage(String msg) {
			final String message = msg + "\n";
			Runnable run = new Runnable() {
				@Override
				public void run() {
					// insert string in document
					IPALookupDocument doc =
							(IPALookupDocument)console.getStyledDocument();
					try {
						doc.insertString(doc.getLength(), message , null);
					} catch (BadLocationException e) {}
				}
			};
			SwingUtilities.invokeLater(run);
		}

		@Override
		public void errorOccured(String err) {
			final String message = err + "\n";
			Runnable run = new Runnable() {
				@Override
				public void run() {
					// insert string in document
					IPALookupDocument doc =
							(IPALookupDocument)console.getStyledDocument();
					try {
						doc.insertString(doc.getLength(), message, null);
					} catch (BadLocationException e) {}
				}
			};
			SwingUtilities.invokeLater(run);
		}

		@Override
		public void dictionaryRemoved(String dictName) {
			
		}
		
	}
	
	private class LanguageCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList arg0,
				Object arg1, int arg2, boolean arg3, boolean arg4) {
			JLabel retVal = 
				(JLabel)super.getListCellRendererComponent(arg0, arg1, arg2, arg3, arg4);
			
			String langId = arg1.toString();
			if(langId.indexOf('-') > 0) {
				langId = langId.split("-")[0];
			}
			LanguageEntry le = LanguageParser.getInstance().getEntryById(langId);
			if(le != null) {
				retVal.setText(le.getName() + " (" + arg1.toString() + ")");
			}
			
			return retVal;
		}
		
	}
	
	/** Constructor */
	public IPALookupPanel() {
		super();
		
		context = new IPALookupContext();
		context.addLookupContextListener(new ContextListener());
		
		init();
	}
	
	public IPALookupPanel(IPALookupContext ctx) {
		super();
		
		this.context = ctx;
		ctx.addLookupContextListener(new ContextListener());
		
		init();
	}
	
	public IPALookupContext getLookupContext() {
		return this.context;
	}
	
	public JTextPane getConsole() {
		return this.console;
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		console = new JTextPane();
		console.setEditable(false);
		console.setStyledDocument(new IPALookupDocument());
		
		inputField = new JTextField();
		inputField.addActionListener(new QueryActionListener());
		
		add(new JScrollPane(console), BorderLayout.CENTER);
		add(inputField, BorderLayout.SOUTH);
	}
}
