package ca.phon.script.params.ui;

import java.io.IOException;
import java.util.Properties;

import javax.swing.text.Segment;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

import ca.phon.phonex.PhonexLexer;
import ca.phon.phonex.PhonexTokenizerLexer;
import ca.phon.phonex.PhonexTokenizerParser;

public class PhonexTokenMaker extends AbstractTokenMaker {
	
	Properties antlrTokenMap = new Properties();
	
	public PhonexTokenMaker() {
		try {
			antlrTokenMap.load(getClass().getClassLoader().getResourceAsStream("PhonexTokenizer.tokens"));
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		
		tokenStream.fill();
		
		// if lexer failed...
		if(tokenStream.size() == 1) {
			// only EOF found
			if(initialTokenType == Token.COMMENT_MULTILINE) {
				addToken(text, offset, end-1, Token.COMMENT_MULTILINE, newStartOffset+currentTokenStart);
			} else if(initialTokenType == Token.NULL) {
				addToken(text, offset, end-1, Token.IDENTIFIER, newStartOffset+currentTokenStart);
				addNullToken();
			}
			return firstToken;
		}
		
		boolean insideMultiLineComment = (currentTokenType == Token.COMMENT_MULTILINE);
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
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("WS"))) {
					currentTokenType = Token.WHITESPACE;
				} else if(antlrTokenType == Integer.parseInt(antlrTokenMap.getProperty("EOL_COMMENT_START"))) {
					currentTokenType = Token.COMMENT_EOL;
					shouldBreak = true;
				} else {
					switch(currentTokenType) {
					case Token.COMMENT_MULTILINE:
						break;
						
					case Token.NULL:
						currentTokenType = Token.IDENTIFIER;
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
			addToken(text, currentTokenStart, end-1, currentTokenType, newStartOffset+currentTokenStart);
		}
		
		if(!insideMultiLineComment)
			addNullToken();
		
		return firstToken;
	}

	@Override
	public TokenMap getWordsToHighlight() {
		TokenMap map = new TokenMap();
		
		return map;
	}

}
