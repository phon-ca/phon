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
package ca.phon.ipadictionary.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.text.BadLocationException;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.apache.commons.lang3.StringUtils;

import ca.phon.ipadictionary.cmd.*;
import ca.phon.ipadictionary.exceptions.IPADictionaryExecption;
import ca.phon.util.*;
import ca.phon.worker.PhonTask;

/**
 * UI for IPA Lookups
 *
 */
public class IPALookupPanel extends JPanel {

	private static final long serialVersionUID = 2278689330995573469L;

	private final static Logger LOGGER = Logger
			.getLogger(IPALookupPanel.class.getName());

	/** The output console */
	private JTextPane console;
	
	/** Our lookup context */
	private IPALookupContext context;
	
//	/** The execution thread */
//	private PhonWorker worker;
	
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
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				err = e;
				context.fireError(e.getLocalizedMessage());
				super.setStatus(TaskStatus.ERROR);
				return;
			} catch (RecognitionException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				err = e;
				context.fireError(e.getLocalizedMessage());
				super.setStatus(TaskStatus.ERROR);
				return;
			} catch (IPADictionaryExecption e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
//			worker.invokeLater(task);
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
		
//		// start our worker thread
//		worker = PhonWorker.createWorker();
//		worker.start();
		
		console = new JTextPane();
		console.setEditable(false);
		console.setStyledDocument(new IPALookupDocument());
		
		inputField = new JTextField();
		inputField.addActionListener(new QueryActionListener());
		
		add(new JScrollPane(console), BorderLayout.CENTER);
		add(inputField, BorderLayout.SOUTH);
	}
}
