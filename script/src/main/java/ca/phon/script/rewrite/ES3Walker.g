tree grammar ES3Walker ;

options
{
	ASTLabelType = CommonTree ;
	tokenVocab = ES3 ;
	rewrite = true;
	output = template;
}
@header {
package ca.phon.engines.search.script.rewrite;
}

@members 
{
public String fsElementsToString(List vals)
{
	String retVal = "";
	
	for(int i = 0; i < vals.size(); i++) {
		Object obj = vals.get(i);
		retVal += (i > 0 ? "," : "") + (obj != null ? obj.toString() : "");
	}
	
	return retVal;
}
}

featureSetElement
	: StringLiteral
	| Identifier;

featureSetItem returns [String fsEleVal]
	: ^(FSELE fsVal=featureSetElement)
	{
	$fsEleVal = $fsVal.text;
	}
	;
	
featureSetLiteral
scope {
	String fsString;
}
@init {
	$featureSetLiteral::fsString = "";
}
	: ^( FEATURESET (fsItem=featureSetItem {$featureSetLiteral::fsString += ($featureSetLiteral::fsString.length() > 0 ? "," : "") + $fsItem.fsEleVal;})* )
	->template(e={$featureSetLiteral::fsString})
	"FeatureSet.fromArray( [ <e> ] )"
	;
