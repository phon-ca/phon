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
package ca.phon.script.rewrite;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.TokenRewriteStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

public class ScriptRewriter {
	
	/**
	 * Rewrite feature set literals in the given script.
	 * 
	 * @param script
	 * @return
	 * @throws Exception
	 */
	public static String rewriteScript(String script) throws Exception {
		String retVal = "";		
		
		ByteArrayInputStream bin = new ByteArrayInputStream(script.getBytes());
		ANTLRInputStream input = new ANTLRInputStream(bin);

		ES3Lexer lexer = new ES3Lexer(input);
		TokenRewriteStream tokenStream = new TokenRewriteStream(lexer);
		
		ES3Parser parser = new ES3Parser(tokenStream);
		ES3Parser.program_return r = parser.program();
		
		CommonTree tree = (CommonTree)r.getTree();
		if(tree != null) {
			List<CommonTree> featureTrees = findFeatureSets(tree);
			
			// use a tree walker to rewrite each tree
			for(CommonTree t:featureTrees) {
	//			printTree(t);
				
				CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
				nodes.setTokenStream(tokenStream);
				
				ES3Walker walker = new ES3Walker(nodes);
				walker.featureSetLiteral();
			}
			
			retVal = tokenStream.toString();
		}
		
		return retVal;
	}
	
	private static List<CommonTree> findFeatureSets(CommonTree tree) {
		List<CommonTree> retVal = new ArrayList<CommonTree>();
		
		if(tree.getType() == ES3Walker.FEATURESET) {
			retVal.add(tree);
		} else if(tree.getChildCount() > 0) {
			for(int cIndex = 0; cIndex < tree.getChildCount(); cIndex++) {
				retVal.addAll(findFeatureSets((CommonTree)tree.getChild(cIndex)));
			}
		}
		
		return retVal;
	}
	
	private static void printTree(CommonTree t) {
		printTree(t, 0);
	}
	
	private static void printTree(CommonTree tree, int tabIndex) {
		for(int i = 0; i < tabIndex; i++)
			System.out.print('\t');
		System.out.println(tree.toString());
		
		if(tree.getChildCount() > 0) {
			for(int cIndex = 0; cIndex < tree.getChildCount(); cIndex++) {
				printTree((CommonTree)tree.getChild(cIndex), tabIndex+1);
			}
		}
	}
}
