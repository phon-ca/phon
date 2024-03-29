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
package ca.phon.ui.text;

import ca.phon.ipa.features.*;
import ca.phon.phonex.*;
import org.antlr.runtime.*;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rsyntaxtextarea.modes.PlainTextTokenMaker;

import javax.swing.text.Segment;
import java.io.IOException;
import java.util.Properties;

public class PhonexTokenMaker extends AbstractTokenMaker {
	
	Properties antlrTokenMap = new Properties();
	
	public PhonexTokenMaker() {
		try {
			antlrTokenMap.load(getClass().getClassLoader().getResourceAsStream("PhonexTokenizer.tokens"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Token getDefaultTokenList(Segment text, int initialTokenType, int startOffset) {
		PlainTextTokenMaker plainTokenMaker = new PlainTextTokenMaker();
		return plainTokenMaker.getTokenList(text, initialTokenType, startOffset);
	}
	
	@Override
	public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
		resetTokenList();
		
		int offset = text.offset;
		int count = text.count;
		int end = offset + count;
		
		int currentTokenStart = offset;
		int newStartOffset = startOffset - offset;
		
		int currentTokenType = initialTokenType;
		
		if(text.toString().length() == 0) {
			addToken(text, offset, end-1, (currentTokenType == Token.NULL ? Token.IDENTIFIER : currentTokenType), newStartOffset+currentTokenStart);
			return firstToken;
		}
		
		CharStream exprStream = new ANTLRStringStream(text.toString());
		PhonexTokenizerLexer lexer = new PhonexTokenizerLexer(exprStream);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		
		try {
			tokenStream.fill();
		} catch (PhonexPatternException e) {
			// do nothing...
		}
		
		// if lexer failed...
//		if(tokenStream.size() <= 1) {
//			return getDefaultTokenList(text, initialTokenType, newStartOffset);
//		}
		
		boolean insideMultiLineComment = (currentTokenType == Token.COMMENT_MULTILINE);
		boolean insideFeatureList = (currentTokenType == Token.ERROR_IDENTIFIER);		
		for(int i = 0; i < tokenStream.size(); i++) {
			final org.antlr.runtime.Token antlrToken = tokenStream.get(i);
			
			int antlrTokenType = antlrToken.getType();
			if(antlrTokenType == org.antlr.runtime.Token.EOF) break;
			
			boolean shouldBreak = false;
			if(insideMultiLineComment) {
				if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("COMMENT_END"))) {
					insideMultiLineComment = false;
				}
			} else {
				if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("COMMENT_START"))) {
					currentTokenType = Token.COMMENT_MULTILINE;
					insideMultiLineComment = true;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("COMMENT_END"))) {
					currentTokenType = Token.COMMENT_MULTILINE;
					insideMultiLineComment = false;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("OPEN_BRACE"))) {
					currentTokenType = Token.SEPARATOR;
					insideFeatureList = true;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("CLOSE_BRACE"))) {
					currentTokenType = Token.SEPARATOR;
					insideFeatureList = false;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("WS"))) {
					currentTokenType = Token.WHITESPACE;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("EOL_COMMENT_START"))) {
					currentTokenType = Token.COMMENT_EOL;
					shouldBreak = true;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("OPEN_PAREN"))
						|| antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("CLOSE_PAREN"))
						|| antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("OPEN_BRACKET"))
						|| antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("CLOSE_BRACKET"))
						|| antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("BOUND_START"))
						|| antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("BOUND_END"))) {
					currentTokenType = Token.SEPARATOR;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("PIPE"))
						|| antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("EQUALS"))
						|| antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("COLON"))
						|| antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("EXC"))
						|| antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("AMP"))
						|| antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("COMMA"))) {
					currentTokenType = Token.OPERATOR;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("NON_CAPTURING_GROUP"))
						|| antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("LOOK_AHEAD_GROUP"))
						|| antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("LOOK_BEHIND_GROUP"))) {
					currentTokenType = Token.ANNOTATION;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("STRING"))) {
					currentTokenType = Token.LITERAL_STRING_DOUBLE_QUOTE;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("INT"))) {
					currentTokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("REGEX_STRING"))) {
					currentTokenType = Token.REGEX;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("ESCAPED_PHONE_CLASS"))) {
					currentTokenType = Token.RESERVED_WORD;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("ESCAPED_BOUNDARY"))) {
					currentTokenType = Token.RESERVED_WORD;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("BACKREF"))) {
					currentTokenType = Token.LITERAL_BACKQUOTE;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("HEX_CHAR"))) {
					currentTokenType = Token.LITERAL_NUMBER_HEXADECIMAL;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("SINGLE_QUANTIFIER"))) {
					currentTokenType = Token.OPERATOR;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("FLAGS"))) {
					currentTokenType = Token.ANNOTATION;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("PLUGIN"))) {
					currentTokenType = Token.FUNCTION;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("IDENTIFIER"))) {
					currentTokenType = Token.IDENTIFIER;
					
					if(insideFeatureList) {
						// use error identifier to indicate a feature name
						currentTokenType = Token.ERROR_IDENTIFIER;
					}
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("GROUP_NAME"))) {
					
					// add two tokens
					currentTokenType = Token.ANNOTATION;
					int tokenStart = offset+antlrToken.getCharPositionInLine();
					int tokenEnd = tokenStart + antlrToken.getText().length()-2;
					addToken(text, tokenStart, tokenEnd, currentTokenType, newStartOffset+currentTokenStart);
					currentTokenStart = tokenEnd+1;
					
					currentTokenType = Token.OPERATOR;
					tokenStart += antlrToken.getText().length()-1;
					tokenEnd = tokenStart;
					addToken(text, tokenStart, tokenEnd, currentTokenType, newStartOffset+currentTokenStart);
					currentTokenStart = tokenEnd+1;
					
					continue;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("SCTYPE"))
						|| antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("STRESS_TYPE"))) {
					currentTokenType = Token.FUNCTION;
				} else {
					switch(currentTokenType) {
					case Token.COMMENT_MULTILINE:
						break;
						
					case Token.ERROR_IDENTIFIER:
						break;
												
					default:
						currentTokenType = Token.IDENTIFIER;
						break;
					}
				}
			}
			
			int tokenStart = offset+antlrToken.getCharPositionInLine();
			int tokenEnd = tokenStart + antlrToken.getText().length()-1;
			addToken(text, tokenStart, tokenEnd, currentTokenType, newStartOffset+currentTokenStart);
			currentTokenStart = tokenEnd+1;
			
			if(shouldBreak) break;
		}
		
		if(currentTokenStart != end) {
			addToken(text, currentTokenStart, end-1, (currentTokenType != Token.NULL ? currentTokenType : Token.IDENTIFIER), newStartOffset+currentTokenStart);
		}
		
		if(!insideMultiLineComment && !insideFeatureList)
			addNullToken();
		
		if(insideFeatureList) {
			TokenImpl next = new TokenImpl();
			next.setOffset(0);
			next.textCount = 0;
			next.setType(Token.ERROR_IDENTIFIER);
			currentToken.setNextToken(next);
			previousToken = currentToken;
			currentToken = next;
		}
				
		return firstToken;
	}
	
	@Override
	public void addToken(Segment segment, int start, int end, int tokenType, int startOffset) {
		if(tokenType == Token.ERROR_IDENTIFIER && segment.count > 0) {
			int value = wordsToHighlight.get(segment, start, end);
			if(value != Token.FUNCTION && value >= 0) {
				tokenType = value;
			}
		}
		
		super.addToken(segment, start, end, tokenType, startOffset);
	}

	@Override
	public TokenMap getWordsToHighlight() {
		TokenMap map = new TokenMap();
		
//		PhonexPluginManager pluginManager = PhonexPluginManager.getSharedInstance();
//		for(PluginProvider provider:pluginManager.getPluginProviders()) {
//			final PhonexPlugin pluginInfo = provider.getClass().getAnnotation(PhonexPlugin.class);
//			if(pluginInfo != null) {
//				map.put(pluginInfo.name(), Token.FUNCTION);
//			}
//		}
		
		FeatureMatrix fm = FeatureMatrix.getInstance();
		for(Feature feature:fm.getFeatureData()) {
			map.put(feature.getName(), Token.RESERVED_WORD_2);
			for(String syn:feature.getSynonyms()) {
				map.put(syn, Token.RESERVED_WORD_2);
			}
		}
		
		return map;
	}

}
