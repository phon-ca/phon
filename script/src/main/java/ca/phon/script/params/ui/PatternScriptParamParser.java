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
package ca.phon.script.params.ui;

import ca.phon.script.params.PatternScriptParam;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rsyntaxtextarea.parser.*;

import java.net.URL;
import java.util.*;

public class PatternScriptParamParser implements Parser {
	
	private PatternScriptParam scriptParam;
	
	public PatternScriptParamParser(PatternScriptParam param) {
		super();
		this.scriptParam = param;
	}

	@Override
	public ExtendedHyperlinkListener getHyperlinkListener() {
		return (textArea, e) -> {};
	}

	@Override
	public URL getImageBase() {
		return null;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public ParseResult parse(RSyntaxDocument doc, String style) {
		if(!scriptParam.isValidate() && scriptParam.getErrLine() >= 0 && scriptParam.getErrChar() >= 0) {
			
			Token tokenListForLine = doc.getTokenListForLine((scriptParam.getErrLine() > 0 ? scriptParam.getErrLine()-1 : 0));
			Token myToken = RSyntaxUtilities.getTokenAtOffset(tokenListForLine, tokenListForLine.getOffset() + scriptParam.getErrChar());
			
			if(myToken == null)
				myToken = tokenListForLine;
			
			return new PatternParseResult((scriptParam.getErrLine() > 0 ? scriptParam.getErrLine() : 1), myToken.getOffset(), myToken.length(), scriptParam.getTooltipText());
		} else {
			return new NoResult();
		}
	}
	
	class NoResult implements ParseResult {
		
		long time = System.currentTimeMillis();

		@Override
		public Exception getError() {
			return null;
		}

		@Override
		public int getFirstLineParsed() {
			return 0;
		}

		@Override
		public int getLastLineParsed() {
			return 0;
		}

		@Override
		public List<ParserNotice> getNotices() {
			return new ArrayList<>();
		}

		@Override
		public Parser getParser() {
			return PatternScriptParamParser.this;
		}

		@Override
		public long getParseTime() {
			return time;
		}
		
	}
	
	class PatternParseResult implements ParseResult {
		
		long time = System.currentTimeMillis();
		
		int firstLineParsed = 0;
		
		int lastLineParsed = 0;
		
		int line = -1;
		
		int offset = -1;
		
		int len = 0;
		
		String message;
		
		private PatternParseResult(int errLine, int offset, int len, String message) {
			this.firstLineParsed = 0;
			this.lastLineParsed = errLine;
			this.line = errLine;
			this.offset = offset;
			this.len = len;
			this.message = message;
			
		}

		@Override
		public Exception getError() {
			return null;
		}

		@Override
		public List<ParserNotice> getNotices() {
			ParserNotice pn = new DefaultParserNotice(PatternScriptParamParser.this, message, line, offset, len);
			return List.of(pn);
		}

		@Override
		public Parser getParser() {
			return PatternScriptParamParser.this;
		}

		@Override
		public long getParseTime() {
			return time;
		}

		@Override
		public int getFirstLineParsed() {
			return firstLineParsed;
		}

		@Override
		public int getLastLineParsed() {
			return lastLineParsed;
		}
		
	}

}
