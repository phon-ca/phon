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
}

@header {
package ca.phon.orthography;

import java.util.List;
import java.util.ArrayList;
}

@members {

private Orthography orthography = new Orthography();

public void setOrthography(Orthography orthography) {
	this.orthography = orthography;
}

public Orthography getOrthography() {
	return this.orthography;
}

}

orthography returns [Orthography ortho]
@after {
	$ortho = getOrthography();
}
	:	orthoElement*
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
	
wordnet returns [OrthoWordnet wordnet]
	:	w1=word WORDNET_MARKER w2=word
	{
		final OrthoWord word1 = $w1.word;
		final OrthoWord word2 = $w2.word;
		final OrthoWordnetMarker marker = OrthoWordnetMarker.fromMarker($WORDNET_MARKER.text.charAt(0));
		
		$wordnet = new OrthoWordnet(word1, word2, marker); 
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
		getOrthography().add($word.word);
	}
	|	wordnet
	{
		getOrthography().add($wordnet.wordnet);
	}
	|	comment
	{
		getOrthography().add($comment.comment);
	}
	|	event
	{
		getOrthography().add($event.event);
	}
	|	punct
	{
		getOrthography().add($punct.punct);
	}
	;


