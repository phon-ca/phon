grammar Orthography;
options {
	output=AST;
	ASTLabelType=CommonTree;
}

tokens {
	WORD;
	WORDNET_MARKER;
	PUNCT;
	EVENT_TYPE;
	COMMENT_TYPE;
	EVENT;
	COMMENT;
	ERROR;
}

@header {
package ca.phon.orthography.parser;

import java.util.List;
import java.util.ArrayList;

import ca.phon.orthography.*;
import ca.phon.orthography.parser.*;
import ca.phon.orthography.parser.exceptions.*;
}

@members {

private final OrthographyBuilder builder = new OrthographyBuilder();

public Orthography getOrthography() {
	return builder.toOrthography();
}

public void reportError(RecognitionException e) {
	throw new OrthoParserException(e);
}

}

orthography returns [Orthography ortho]
@after {
	$ortho = getOrthography();
}
	:	(orthoElement|error)*
	;
	
error
	:	ERROR
	{
		RecognitionException re = new RecognitionException(input);
		re.token = $ERROR;
		re.charPositionInLine = $ERROR.getCharPositionInLine();
		re.line = -1;
		re.index = $ERROR.getTokenIndex();
		reportError(re);
	}
	;

word returns [OrthoWord word]
	:	WORD_PREFIX? WORD WORD_SUFFIX?
	{
		WordPrefix wp = null;
		if($WORD_PREFIX != null) {
			wp = WordPrefix.fromCode($WORD_PREFIX.text);
		}
		
		WordSuffix ws = null;
		if($WORD_SUFFIX != null) {
			ws = WordSuffix.fromCode($WORD_SUFFIX.text);
		}		
		
		final String data = $WORD.text;
		$word = new OrthoWord(data, wp, ws);
	}
	;
	
wordnet returns [OrthoWordnet wdnet]
scope {
	List<OrthoWord> words;
	List<OrthoWordnetMarker> markers;
}
@init {
	$wordnet::words = new ArrayList<OrthoWord>();
	$wordnet::markers = new ArrayList<OrthoWordnetMarker>();
}
	:	w1=word (mk=WORDNET_MARKER { $wordnet::markers.add(OrthoWordnetMarker.fromMarker($mk.text.charAt(0))); } w2=word { $wordnet::words.add($w2.word); } )+
	{
		final OrthoWord word1 = $w1.word;
		final OrthoWord word2 = $wordnet::words.get(0);
		final OrthoWordnetMarker marker = $wordnet::markers.get(0);
		
		$wdnet = new OrthoWordnet(word1, word2, marker); 
		for(int wordIdx = 1; wordIdx < $wordnet::words.size(); wordIdx++) {
			OrthoWord nextWord = $wordnet::words.get(wordIdx);
			OrthoWordnetMarker nextMarker = $wordnet::markers.get(wordIdx);
			
			$wdnet = new OrthoWordnet($wdnet, nextWord, nextMarker);
		}
	}
	;
	
comment returns [OrthoComment comment]
	:	COMMENT_TYPE? COMMENT
	{
		String type = null;
		if($COMMENT_TYPE != null) {
			type = $COMMENT_TYPE.text;
		}
		
		String data = $COMMENT.text;
		$comment = new OrthoComment(type, data);
	}
	;
	
event returns [OrthoEvent event]
	:	EVENT_TYPE? EVENT
	{
		String type = null;
		if($EVENT_TYPE != null) {
			type = $EVENT_TYPE.text;
		}
		
		String data = $EVENT.text;
		$event = new OrthoEvent(type, data);
	}
	;
	
punct returns [OrthoPunct punct]
	:	PUNCT
	{
		final OrthoPunctType type = OrthoPunctType.fromChar($PUNCT.text.charAt(0));
		$punct = new OrthoPunct(type);
	}
	;

orthoElement
	:	word
	{
		builder.append($word.word);
	}
	|	wordnet
	{
		builder.append($wordnet.wdnet);
	}
	|	comment
	{
		builder.append($comment.comment);
	}
	|	event
	{
		builder.append($event.event);
	}
	|	punct
	{
		builder.append($punct.punct);
	}
	;


