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
// $ANTLR 3.4 /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3Walker.g 2013-01-10 13:33:58

package ca.phon.script.rewrite;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.stringtemplate.*;
import org.antlr.stringtemplate.language.*;
import java.util.HashMap;
@SuppressWarnings({"all", "warnings", "unchecked"})
public class ES3Walker extends TreeParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ABSTRACT", "ADD", "ADDASS", "AND", "ANDASS", "ARGS", "ARRAY", "ASSIGN", "BLOCK", "BOOLEAN", "BREAK", "BSLASH", "BYFIELD", "BYINDEX", "BYTE", "BackslashSequence", "CALL", "CASE", "CATCH", "CEXPR", "CHAR", "CLASS", "COLON", "COMMA", "CONST", "CONTINUE", "CR", "CharacterEscapeSequence", "DEBUGGER", "DEC", "DEFAULT", "DELETE", "DIV", "DIVASS", "DO", "DOT", "DOUBLE", "DQUOTE", "DecimalDigit", "DecimalIntegerLiteral", "DecimalLiteral", "ELSE", "ENUM", "EOL", "EQ", "EXPORT", "EXPR", "EXTENDS", "EscapeSequence", "ExponentPart", "FALSE", "FEATURESET", "FF", "FINAL", "FINALLY", "FLOAT", "FOR", "FORITER", "FORSTEP", "FSELE", "FSEND", "FSSTART", "FUNCTION", "GOTO", "GT", "GTE", "HexDigit", "HexEscapeSequence", "HexIntegerLiteral", "IF", "IMPLEMENTS", "IMPORT", "IN", "INC", "INSTANCEOF", "INT", "INTERFACE", "INV", "ITEM", "Identifier", "IdentifierNameASCIIStart", "IdentifierPart", "IdentifierStartASCII", "LABELLED", "LAND", "LBRACE", "LBRACK", "LF", "LONG", "LOR", "LPAREN", "LS", "LT", "LTE", "LineTerminator", "MOD", "MODASS", "MUL", "MULASS", "MultiLineComment", "NAMEDVALUE", "NATIVE", "NBSP", "NEG", "NEQ", "NEW", "NOT", "NSAME", "NULL", "OBJECT", "OR", "ORASS", "OctalDigit", "OctalEscapeSequence", "OctalIntegerLiteral", "PACKAGE", "PAREXPR", "PDEC", "PINC", "POS", "PRIVATE", "PROTECTED", "PS", "PUBLIC", "QUE", "RBRACE", "RBRACK", "RETURN", "RPAREN", "RegularExpressionChar", "RegularExpressionFirstChar", "RegularExpressionLiteral", "SAME", "SEMIC", "SHL", "SHLASS", "SHORT", "SHR", "SHRASS", "SHU", "SHUASS", "SP", "SQUOTE", "STATIC", "SUB", "SUBASS", "SUPER", "SWITCH", "SYNCHRONIZED", "SingleLineComment", "StringLiteral", "TAB", "THIS", "THROW", "THROWS", "TRANSIENT", "TRUE", "TRY", "TYPEOF", "USP", "UnicodeEscapeSequence", "VAR", "VOID", "VOLATILE", "VT", "WHILE", "WITH", "WhiteSpace", "XOR", "XORASS", "ZeroToThree"
    };

    public static final int EOF=-1;
    public static final int ABSTRACT=4;
    public static final int ADD=5;
    public static final int ADDASS=6;
    public static final int AND=7;
    public static final int ANDASS=8;
    public static final int ARGS=9;
    public static final int ARRAY=10;
    public static final int ASSIGN=11;
    public static final int BLOCK=12;
    public static final int BOOLEAN=13;
    public static final int BREAK=14;
    public static final int BSLASH=15;
    public static final int BYFIELD=16;
    public static final int BYINDEX=17;
    public static final int BYTE=18;
    public static final int BackslashSequence=19;
    public static final int CALL=20;
    public static final int CASE=21;
    public static final int CATCH=22;
    public static final int CEXPR=23;
    public static final int CHAR=24;
    public static final int CLASS=25;
    public static final int COLON=26;
    public static final int COMMA=27;
    public static final int CONST=28;
    public static final int CONTINUE=29;
    public static final int CR=30;
    public static final int CharacterEscapeSequence=31;
    public static final int DEBUGGER=32;
    public static final int DEC=33;
    public static final int DEFAULT=34;
    public static final int DELETE=35;
    public static final int DIV=36;
    public static final int DIVASS=37;
    public static final int DO=38;
    public static final int DOT=39;
    public static final int DOUBLE=40;
    public static final int DQUOTE=41;
    public static final int DecimalDigit=42;
    public static final int DecimalIntegerLiteral=43;
    public static final int DecimalLiteral=44;
    public static final int ELSE=45;
    public static final int ENUM=46;
    public static final int EOL=47;
    public static final int EQ=48;
    public static final int EXPORT=49;
    public static final int EXPR=50;
    public static final int EXTENDS=51;
    public static final int EscapeSequence=52;
    public static final int ExponentPart=53;
    public static final int FALSE=54;
    public static final int FEATURESET=55;
    public static final int FF=56;
    public static final int FINAL=57;
    public static final int FINALLY=58;
    public static final int FLOAT=59;
    public static final int FOR=60;
    public static final int FORITER=61;
    public static final int FORSTEP=62;
    public static final int FSELE=63;
    public static final int FSEND=64;
    public static final int FSSTART=65;
    public static final int FUNCTION=66;
    public static final int GOTO=67;
    public static final int GT=68;
    public static final int GTE=69;
    public static final int HexDigit=70;
    public static final int HexEscapeSequence=71;
    public static final int HexIntegerLiteral=72;
    public static final int IF=73;
    public static final int IMPLEMENTS=74;
    public static final int IMPORT=75;
    public static final int IN=76;
    public static final int INC=77;
    public static final int INSTANCEOF=78;
    public static final int INT=79;
    public static final int INTERFACE=80;
    public static final int INV=81;
    public static final int ITEM=82;
    public static final int Identifier=83;
    public static final int IdentifierNameASCIIStart=84;
    public static final int IdentifierPart=85;
    public static final int IdentifierStartASCII=86;
    public static final int LABELLED=87;
    public static final int LAND=88;
    public static final int LBRACE=89;
    public static final int LBRACK=90;
    public static final int LF=91;
    public static final int LONG=92;
    public static final int LOR=93;
    public static final int LPAREN=94;
    public static final int LS=95;
    public static final int LT=96;
    public static final int LTE=97;
    public static final int LineTerminator=98;
    public static final int MOD=99;
    public static final int MODASS=100;
    public static final int MUL=101;
    public static final int MULASS=102;
    public static final int MultiLineComment=103;
    public static final int NAMEDVALUE=104;
    public static final int NATIVE=105;
    public static final int NBSP=106;
    public static final int NEG=107;
    public static final int NEQ=108;
    public static final int NEW=109;
    public static final int NOT=110;
    public static final int NSAME=111;
    public static final int NULL=112;
    public static final int OBJECT=113;
    public static final int OR=114;
    public static final int ORASS=115;
    public static final int OctalDigit=116;
    public static final int OctalEscapeSequence=117;
    public static final int OctalIntegerLiteral=118;
    public static final int PACKAGE=119;
    public static final int PAREXPR=120;
    public static final int PDEC=121;
    public static final int PINC=122;
    public static final int POS=123;
    public static final int PRIVATE=124;
    public static final int PROTECTED=125;
    public static final int PS=126;
    public static final int PUBLIC=127;
    public static final int QUE=128;
    public static final int RBRACE=129;
    public static final int RBRACK=130;
    public static final int RETURN=131;
    public static final int RPAREN=132;
    public static final int RegularExpressionChar=133;
    public static final int RegularExpressionFirstChar=134;
    public static final int RegularExpressionLiteral=135;
    public static final int SAME=136;
    public static final int SEMIC=137;
    public static final int SHL=138;
    public static final int SHLASS=139;
    public static final int SHORT=140;
    public static final int SHR=141;
    public static final int SHRASS=142;
    public static final int SHU=143;
    public static final int SHUASS=144;
    public static final int SP=145;
    public static final int SQUOTE=146;
    public static final int STATIC=147;
    public static final int SUB=148;
    public static final int SUBASS=149;
    public static final int SUPER=150;
    public static final int SWITCH=151;
    public static final int SYNCHRONIZED=152;
    public static final int SingleLineComment=153;
    public static final int StringLiteral=154;
    public static final int TAB=155;
    public static final int THIS=156;
    public static final int THROW=157;
    public static final int THROWS=158;
    public static final int TRANSIENT=159;
    public static final int TRUE=160;
    public static final int TRY=161;
    public static final int TYPEOF=162;
    public static final int USP=163;
    public static final int UnicodeEscapeSequence=164;
    public static final int VAR=165;
    public static final int VOID=166;
    public static final int VOLATILE=167;
    public static final int VT=168;
    public static final int WHILE=169;
    public static final int WITH=170;
    public static final int WhiteSpace=171;
    public static final int XOR=172;
    public static final int XORASS=173;
    public static final int ZeroToThree=174;

    // delegates
    public TreeParser[] getDelegates() {
        return new TreeParser[] {};
    }

    // delegators


    public ES3Walker(TreeNodeStream input) {
        this(input, new RecognizerSharedState());
    }
    public ES3Walker(TreeNodeStream input, RecognizerSharedState state) {
        super(input, state);
    }

protected StringTemplateGroup templateLib =
  new StringTemplateGroup("ES3WalkerTemplates", AngleBracketTemplateLexer.class);

public void setTemplateLib(StringTemplateGroup templateLib) {
  this.templateLib = templateLib;
}
public StringTemplateGroup getTemplateLib() {
  return templateLib;
}
/** allows convenient multi-value initialization:
 *  "new STAttrMap().put(...).put(...)"
 */
public static class STAttrMap extends HashMap {
  public STAttrMap put(String attrName, Object value) {
    super.put(attrName, value);
    return this;
  }
  public STAttrMap put(String attrName, int value) {
    super.put(attrName, new Integer(value));
    return this;
  }
}
    public String[] getTokenNames() { return ES3Walker.tokenNames; }
    public String getGrammarFileName() { return "/Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3Walker.g"; }


    public String fsElementsToString(List vals)
    {
    	String retVal = "";
    	
    	for(int i = 0; i < vals.size(); i++) {
    		Object obj = vals.get(i);
    		retVal += (i > 0 ? "," : "") + (obj != null ? obj.toString() : "");
    	}
    	
    	return retVal;
    }


    public static class featureSetElement_return extends TreeRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };


    // $ANTLR start "featureSetElement"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3Walker.g:29:1: featureSetElement : ( StringLiteral | Identifier );
    public final ES3Walker.featureSetElement_return featureSetElement() throws RecognitionException {
        ES3Walker.featureSetElement_return retval = new ES3Walker.featureSetElement_return();
        retval.start = input.LT(1);


        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3Walker.g:30:2: ( StringLiteral | Identifier )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3Walker.g:
            {
            if ( input.LA(1)==Identifier||input.LA(1)==StringLiteral ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "featureSetElement"


    public static class featureSetItem_return extends TreeRuleReturnScope {
        public String fsEleVal;
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };


    // $ANTLR start "featureSetItem"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3Walker.g:33:1: featureSetItem returns [String fsEleVal] : ^( FSELE fsVal= featureSetElement ) ;
    public final ES3Walker.featureSetItem_return featureSetItem() throws RecognitionException {
        ES3Walker.featureSetItem_return retval = new ES3Walker.featureSetItem_return();
        retval.start = input.LT(1);


        ES3Walker.featureSetElement_return fsVal =null;


        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3Walker.g:34:2: ( ^( FSELE fsVal= featureSetElement ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3Walker.g:34:4: ^( FSELE fsVal= featureSetElement )
            {
            match(input,FSELE,FOLLOW_FSELE_in_featureSetItem84); 

            match(input, Token.DOWN, null); 
            pushFollow(FOLLOW_featureSetElement_in_featureSetItem88);
            fsVal=featureSetElement();

            state._fsp--;


            match(input, Token.UP, null); 



            	retval.fsEleVal = (fsVal!=null?(input.getTokenStream().toString(input.getTreeAdaptor().getTokenStartIndex(fsVal.start),input.getTreeAdaptor().getTokenStopIndex(fsVal.start))):null);
            	

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "featureSetItem"


    protected static class featureSetLiteral_scope {
        String fsString;
    }
    protected Stack featureSetLiteral_stack = new Stack();


    public static class featureSetLiteral_return extends TreeRuleReturnScope {
        public StringTemplate st;
        public Object getTemplate() { return st; }
        public String toString() { return st==null?null:st.toString(); }
    };


    // $ANTLR start "featureSetLiteral"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3Walker.g:40:1: featureSetLiteral : ^( FEATURESET (fsItem= featureSetItem )* ) -> template(e=$featureSetLiteral::fsString) \"FeatureSet.fromArray( [ <e> ] )\";
    public final ES3Walker.featureSetLiteral_return featureSetLiteral() throws RecognitionException {
        featureSetLiteral_stack.push(new featureSetLiteral_scope());
        ES3Walker.featureSetLiteral_return retval = new ES3Walker.featureSetLiteral_return();
        retval.start = input.LT(1);


        ES3Walker.featureSetItem_return fsItem =null;



        	((featureSetLiteral_scope)featureSetLiteral_stack.peek()).fsString = "";

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3Walker.g:47:2: ( ^( FEATURESET (fsItem= featureSetItem )* ) -> template(e=$featureSetLiteral::fsString) \"FeatureSet.fromArray( [ <e> ] )\")
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3Walker.g:47:4: ^( FEATURESET (fsItem= featureSetItem )* )
            {
            match(input,FEATURESET,FOLLOW_FEATURESET_in_featureSetLiteral115); 

            if ( input.LA(1)==Token.DOWN ) {
                match(input, Token.DOWN, null); 
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3Walker.g:47:18: (fsItem= featureSetItem )*
                loop1:
                do {
                    int alt1=2;
                    int LA1_0 = input.LA(1);

                    if ( (LA1_0==FSELE) ) {
                        alt1=1;
                    }


                    switch (alt1) {
                	case 1 :
                	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3Walker.g:47:19: fsItem= featureSetItem
                	    {
                	    pushFollow(FOLLOW_featureSetItem_in_featureSetLiteral120);
                	    fsItem=featureSetItem();

                	    state._fsp--;


                	    ((featureSetLiteral_scope)featureSetLiteral_stack.peek()).fsString += (((featureSetLiteral_scope)featureSetLiteral_stack.peek()).fsString.length() > 0 ? "," : "") + (fsItem!=null?fsItem.fsEleVal:null);

                	    }
                	    break;

                	default :
                	    break loop1;
                    }
                } while (true);


                match(input, Token.UP, null); 
            }


            // TEMPLATE REWRITE
            // 48:2: -> template(e=$featureSetLiteral::fsString) \"FeatureSet.fromArray( [ <e> ] )\"
            {
                retval.st = new StringTemplate(templateLib, "FeatureSet.fromArray( [ <e> ] )",new STAttrMap().put("e", ((featureSetLiteral_scope)featureSetLiteral_stack.peek()).fsString));
            }


            ((TokenRewriteStream)input.getTokenStream()).replace(
              input.getTreeAdaptor().getTokenStartIndex(retval.start),
              input.getTreeAdaptor().getTokenStopIndex(retval.start),
              retval.st);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
            featureSetLiteral_stack.pop();
        }
        return retval;
    }
    // $ANTLR end "featureSetLiteral"

    // Delegated rules


 

    public static final BitSet FOLLOW_FSELE_in_featureSetItem84 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_featureSetElement_in_featureSetItem88 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_FEATURESET_in_featureSetLiteral115 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_featureSetItem_in_featureSetLiteral120 = new BitSet(new long[]{0x8000000000000008L});

}