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
// $ANTLR 3.4 /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g 2013-01-10 12:31:28

package ca.phon.script.rewrite;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class ES3Lexer extends Lexer {
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

    private Token last;

    private final boolean areRegularExpressionsEnabled()
    {
    	if (last == null)
    	{
    		return true;
    	}
    	switch (last.getType())
    	{
    	// identifier
    		case Identifier:
    	// literals
    		case NULL:
    		case TRUE:
    		case FALSE:
    		case THIS:
    		case OctalIntegerLiteral:
    		case DecimalLiteral:
    		case HexIntegerLiteral:
    		case StringLiteral:
    	// member access ending 
    		case RBRACK:
    	// function call or nested expression ending
    		case RPAREN:
    			return false;
    	// otherwise OK
    		default:
    			return true;
    	}
    }
    	
    private final void consumeIdentifierUnicodeStart() throws RecognitionException, NoViableAltException
    {
    	int ch = input.LA(1);
    	if (isIdentifierStartUnicode(ch))
    	{
    		matchAny();
    		do
    		{
    			ch = input.LA(1);
    			if (ch == '$' || (ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'Z') || ch == '\\' || ch == '_' || (ch >= 'a' && ch <= 'z') || isIdentifierPartUnicode(ch))
    			{
    				mIdentifierPart();
    			}
    			else
    			{
    				return;
    			}
    		}
    		while (true);
    	}
    	else
    	{
    		throw new NoViableAltException();
    	}
    }
    	
    private final boolean isIdentifierPartUnicode(int ch)
    {
    	return Character.isJavaIdentifierPart(ch);
    }
    	
    private final boolean isIdentifierStartUnicode(int ch)
    {
    	return Character.isJavaIdentifierStart(ch);
    }

    public Token nextToken()
    {
    	Token result = super.nextToken();
    	if (result.getChannel() == Token.DEFAULT_CHANNEL)
    	{
    		last = result;
    	}
    	return result;		
    }


    // delegates
    // delegators
    public Lexer[] getDelegates() {
        return new Lexer[] {};
    }

    public ES3Lexer() {} 
    public ES3Lexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public ES3Lexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }
    public String getGrammarFileName() { return "/Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g"; }

    // $ANTLR start "ABSTRACT"
    public final void mABSTRACT() throws RecognitionException {
        try {
            int _type = ABSTRACT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:90:10: ( 'abstract' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:90:12: 'abstract'
            {
            match("abstract"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ABSTRACT"

    // $ANTLR start "ADD"
    public final void mADD() throws RecognitionException {
        try {
            int _type = ADD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:91:5: ( '+' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:91:7: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ADD"

    // $ANTLR start "ADDASS"
    public final void mADDASS() throws RecognitionException {
        try {
            int _type = ADDASS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:92:8: ( '+=' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:92:10: '+='
            {
            match("+="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ADDASS"

    // $ANTLR start "AND"
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:93:5: ( '&' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:93:7: '&'
            {
            match('&'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "ANDASS"
    public final void mANDASS() throws RecognitionException {
        try {
            int _type = ANDASS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:94:8: ( '&=' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:94:10: '&='
            {
            match("&="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ANDASS"

    // $ANTLR start "ASSIGN"
    public final void mASSIGN() throws RecognitionException {
        try {
            int _type = ASSIGN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:95:8: ( '=' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:95:10: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ASSIGN"

    // $ANTLR start "BOOLEAN"
    public final void mBOOLEAN() throws RecognitionException {
        try {
            int _type = BOOLEAN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:96:9: ( 'boolean' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:96:11: 'boolean'
            {
            match("boolean"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "BOOLEAN"

    // $ANTLR start "BREAK"
    public final void mBREAK() throws RecognitionException {
        try {
            int _type = BREAK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:97:7: ( 'break' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:97:9: 'break'
            {
            match("break"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "BREAK"

    // $ANTLR start "BYTE"
    public final void mBYTE() throws RecognitionException {
        try {
            int _type = BYTE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:98:6: ( 'byte' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:98:8: 'byte'
            {
            match("byte"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "BYTE"

    // $ANTLR start "CASE"
    public final void mCASE() throws RecognitionException {
        try {
            int _type = CASE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:99:6: ( 'case' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:99:8: 'case'
            {
            match("case"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CASE"

    // $ANTLR start "CATCH"
    public final void mCATCH() throws RecognitionException {
        try {
            int _type = CATCH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:100:7: ( 'catch' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:100:9: 'catch'
            {
            match("catch"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CATCH"

    // $ANTLR start "CHAR"
    public final void mCHAR() throws RecognitionException {
        try {
            int _type = CHAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:101:6: ( 'char' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:101:8: 'char'
            {
            match("char"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CHAR"

    // $ANTLR start "CLASS"
    public final void mCLASS() throws RecognitionException {
        try {
            int _type = CLASS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:102:7: ( 'class' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:102:9: 'class'
            {
            match("class"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CLASS"

    // $ANTLR start "COLON"
    public final void mCOLON() throws RecognitionException {
        try {
            int _type = COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:103:7: ( ':' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:103:9: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "COLON"

    // $ANTLR start "COMMA"
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:104:7: ( ',' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:104:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "COMMA"

    // $ANTLR start "CONST"
    public final void mCONST() throws RecognitionException {
        try {
            int _type = CONST;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:105:7: ( 'const' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:105:9: 'const'
            {
            match("const"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CONST"

    // $ANTLR start "CONTINUE"
    public final void mCONTINUE() throws RecognitionException {
        try {
            int _type = CONTINUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:106:10: ( 'continue' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:106:12: 'continue'
            {
            match("continue"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CONTINUE"

    // $ANTLR start "DEBUGGER"
    public final void mDEBUGGER() throws RecognitionException {
        try {
            int _type = DEBUGGER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:107:10: ( 'debugger' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:107:12: 'debugger'
            {
            match("debugger"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DEBUGGER"

    // $ANTLR start "DEC"
    public final void mDEC() throws RecognitionException {
        try {
            int _type = DEC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:108:5: ( '--' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:108:7: '--'
            {
            match("--"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DEC"

    // $ANTLR start "DEFAULT"
    public final void mDEFAULT() throws RecognitionException {
        try {
            int _type = DEFAULT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:109:9: ( 'default' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:109:11: 'default'
            {
            match("default"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DEFAULT"

    // $ANTLR start "DELETE"
    public final void mDELETE() throws RecognitionException {
        try {
            int _type = DELETE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:110:8: ( 'delete' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:110:10: 'delete'
            {
            match("delete"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DELETE"

    // $ANTLR start "DIV"
    public final void mDIV() throws RecognitionException {
        try {
            int _type = DIV;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:111:5: ( '/' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:111:7: '/'
            {
            match('/'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DIV"

    // $ANTLR start "DIVASS"
    public final void mDIVASS() throws RecognitionException {
        try {
            int _type = DIVASS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:112:8: ( '/=' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:112:10: '/='
            {
            match("/="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DIVASS"

    // $ANTLR start "DO"
    public final void mDO() throws RecognitionException {
        try {
            int _type = DO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:113:4: ( 'do' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:113:6: 'do'
            {
            match("do"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DO"

    // $ANTLR start "DOT"
    public final void mDOT() throws RecognitionException {
        try {
            int _type = DOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:114:5: ( '.' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:114:7: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DOT"

    // $ANTLR start "DOUBLE"
    public final void mDOUBLE() throws RecognitionException {
        try {
            int _type = DOUBLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:115:8: ( 'double' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:115:10: 'double'
            {
            match("double"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DOUBLE"

    // $ANTLR start "ELSE"
    public final void mELSE() throws RecognitionException {
        try {
            int _type = ELSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:116:6: ( 'else' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:116:8: 'else'
            {
            match("else"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ELSE"

    // $ANTLR start "ENUM"
    public final void mENUM() throws RecognitionException {
        try {
            int _type = ENUM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:117:6: ( 'enum' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:117:8: 'enum'
            {
            match("enum"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ENUM"

    // $ANTLR start "EQ"
    public final void mEQ() throws RecognitionException {
        try {
            int _type = EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:118:4: ( '==' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:118:6: '=='
            {
            match("=="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "EQ"

    // $ANTLR start "EXPORT"
    public final void mEXPORT() throws RecognitionException {
        try {
            int _type = EXPORT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:119:8: ( 'export' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:119:10: 'export'
            {
            match("export"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "EXPORT"

    // $ANTLR start "EXTENDS"
    public final void mEXTENDS() throws RecognitionException {
        try {
            int _type = EXTENDS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:120:9: ( 'extends' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:120:11: 'extends'
            {
            match("extends"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "EXTENDS"

    // $ANTLR start "FALSE"
    public final void mFALSE() throws RecognitionException {
        try {
            int _type = FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:121:7: ( 'false' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:121:9: 'false'
            {
            match("false"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "FALSE"

    // $ANTLR start "FINAL"
    public final void mFINAL() throws RecognitionException {
        try {
            int _type = FINAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:122:7: ( 'final' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:122:9: 'final'
            {
            match("final"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "FINAL"

    // $ANTLR start "FINALLY"
    public final void mFINALLY() throws RecognitionException {
        try {
            int _type = FINALLY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:123:9: ( 'finally' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:123:11: 'finally'
            {
            match("finally"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "FINALLY"

    // $ANTLR start "FLOAT"
    public final void mFLOAT() throws RecognitionException {
        try {
            int _type = FLOAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:124:7: ( 'float' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:124:9: 'float'
            {
            match("float"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "FLOAT"

    // $ANTLR start "FOR"
    public final void mFOR() throws RecognitionException {
        try {
            int _type = FOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:125:5: ( 'for' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:125:7: 'for'
            {
            match("for"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "FOR"

    // $ANTLR start "FSEND"
    public final void mFSEND() throws RecognitionException {
        try {
            int _type = FSEND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:126:7: ( '}^' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:126:9: '}^'
            {
            match("}^"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "FSEND"

    // $ANTLR start "FSSTART"
    public final void mFSSTART() throws RecognitionException {
        try {
            int _type = FSSTART;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:127:9: ( '^{' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:127:11: '^{'
            {
            match("^{"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "FSSTART"

    // $ANTLR start "FUNCTION"
    public final void mFUNCTION() throws RecognitionException {
        try {
            int _type = FUNCTION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:128:10: ( 'function' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:128:12: 'function'
            {
            match("function"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "FUNCTION"

    // $ANTLR start "GOTO"
    public final void mGOTO() throws RecognitionException {
        try {
            int _type = GOTO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:129:6: ( 'goto' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:129:8: 'goto'
            {
            match("goto"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "GOTO"

    // $ANTLR start "GT"
    public final void mGT() throws RecognitionException {
        try {
            int _type = GT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:130:4: ( '>' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:130:6: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "GT"

    // $ANTLR start "GTE"
    public final void mGTE() throws RecognitionException {
        try {
            int _type = GTE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:131:5: ( '>=' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:131:7: '>='
            {
            match(">="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "GTE"

    // $ANTLR start "IF"
    public final void mIF() throws RecognitionException {
        try {
            int _type = IF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:132:4: ( 'if' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:132:6: 'if'
            {
            match("if"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "IF"

    // $ANTLR start "IMPLEMENTS"
    public final void mIMPLEMENTS() throws RecognitionException {
        try {
            int _type = IMPLEMENTS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:133:12: ( 'implements' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:133:14: 'implements'
            {
            match("implements"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "IMPLEMENTS"

    // $ANTLR start "IMPORT"
    public final void mIMPORT() throws RecognitionException {
        try {
            int _type = IMPORT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:134:8: ( 'import' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:134:10: 'import'
            {
            match("import"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "IMPORT"

    // $ANTLR start "IN"
    public final void mIN() throws RecognitionException {
        try {
            int _type = IN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:135:4: ( 'in' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:135:6: 'in'
            {
            match("in"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "IN"

    // $ANTLR start "INC"
    public final void mINC() throws RecognitionException {
        try {
            int _type = INC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:136:5: ( '++' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:136:7: '++'
            {
            match("++"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "INC"

    // $ANTLR start "INSTANCEOF"
    public final void mINSTANCEOF() throws RecognitionException {
        try {
            int _type = INSTANCEOF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:137:12: ( 'instanceof' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:137:14: 'instanceof'
            {
            match("instanceof"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "INSTANCEOF"

    // $ANTLR start "INT"
    public final void mINT() throws RecognitionException {
        try {
            int _type = INT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:138:5: ( 'int' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:138:7: 'int'
            {
            match("int"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "INT"

    // $ANTLR start "INTERFACE"
    public final void mINTERFACE() throws RecognitionException {
        try {
            int _type = INTERFACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:139:11: ( 'interface' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:139:13: 'interface'
            {
            match("interface"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "INTERFACE"

    // $ANTLR start "INV"
    public final void mINV() throws RecognitionException {
        try {
            int _type = INV;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:140:5: ( '~' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:140:7: '~'
            {
            match('~'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "INV"

    // $ANTLR start "LAND"
    public final void mLAND() throws RecognitionException {
        try {
            int _type = LAND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:141:6: ( '&&' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:141:8: '&&'
            {
            match("&&"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LAND"

    // $ANTLR start "LBRACE"
    public final void mLBRACE() throws RecognitionException {
        try {
            int _type = LBRACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:142:8: ( '{' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:142:10: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LBRACE"

    // $ANTLR start "LBRACK"
    public final void mLBRACK() throws RecognitionException {
        try {
            int _type = LBRACK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:143:8: ( '[' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:143:10: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LBRACK"

    // $ANTLR start "LONG"
    public final void mLONG() throws RecognitionException {
        try {
            int _type = LONG;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:144:6: ( 'long' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:144:8: 'long'
            {
            match("long"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LONG"

    // $ANTLR start "LOR"
    public final void mLOR() throws RecognitionException {
        try {
            int _type = LOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:145:5: ( '||' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:145:7: '||'
            {
            match("||"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LOR"

    // $ANTLR start "LPAREN"
    public final void mLPAREN() throws RecognitionException {
        try {
            int _type = LPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:146:8: ( '(' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:146:10: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LPAREN"

    // $ANTLR start "LT"
    public final void mLT() throws RecognitionException {
        try {
            int _type = LT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:147:4: ( '<' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:147:6: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LT"

    // $ANTLR start "LTE"
    public final void mLTE() throws RecognitionException {
        try {
            int _type = LTE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:148:5: ( '<=' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:148:7: '<='
            {
            match("<="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LTE"

    // $ANTLR start "MOD"
    public final void mMOD() throws RecognitionException {
        try {
            int _type = MOD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:149:5: ( '%' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:149:7: '%'
            {
            match('%'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "MOD"

    // $ANTLR start "MODASS"
    public final void mMODASS() throws RecognitionException {
        try {
            int _type = MODASS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:150:8: ( '%=' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:150:10: '%='
            {
            match("%="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "MODASS"

    // $ANTLR start "MUL"
    public final void mMUL() throws RecognitionException {
        try {
            int _type = MUL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:151:5: ( '*' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:151:7: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "MUL"

    // $ANTLR start "MULASS"
    public final void mMULASS() throws RecognitionException {
        try {
            int _type = MULASS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:152:8: ( '*=' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:152:10: '*='
            {
            match("*="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "MULASS"

    // $ANTLR start "NATIVE"
    public final void mNATIVE() throws RecognitionException {
        try {
            int _type = NATIVE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:153:8: ( 'native' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:153:10: 'native'
            {
            match("native"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "NATIVE"

    // $ANTLR start "NEQ"
    public final void mNEQ() throws RecognitionException {
        try {
            int _type = NEQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:154:5: ( '!=' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:154:7: '!='
            {
            match("!="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "NEQ"

    // $ANTLR start "NEW"
    public final void mNEW() throws RecognitionException {
        try {
            int _type = NEW;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:155:5: ( 'new' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:155:7: 'new'
            {
            match("new"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "NEW"

    // $ANTLR start "NOT"
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:156:5: ( '!' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:156:7: '!'
            {
            match('!'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "NOT"

    // $ANTLR start "NSAME"
    public final void mNSAME() throws RecognitionException {
        try {
            int _type = NSAME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:157:7: ( '!==' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:157:9: '!=='
            {
            match("!=="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "NSAME"

    // $ANTLR start "NULL"
    public final void mNULL() throws RecognitionException {
        try {
            int _type = NULL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:158:6: ( 'null' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:158:8: 'null'
            {
            match("null"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "NULL"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:159:4: ( '|' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:159:6: '|'
            {
            match('|'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "ORASS"
    public final void mORASS() throws RecognitionException {
        try {
            int _type = ORASS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:160:7: ( '|=' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:160:9: '|='
            {
            match("|="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ORASS"

    // $ANTLR start "PACKAGE"
    public final void mPACKAGE() throws RecognitionException {
        try {
            int _type = PACKAGE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:161:9: ( 'package' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:161:11: 'package'
            {
            match("package"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "PACKAGE"

    // $ANTLR start "PRIVATE"
    public final void mPRIVATE() throws RecognitionException {
        try {
            int _type = PRIVATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:162:9: ( 'private' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:162:11: 'private'
            {
            match("private"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "PRIVATE"

    // $ANTLR start "PROTECTED"
    public final void mPROTECTED() throws RecognitionException {
        try {
            int _type = PROTECTED;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:163:11: ( 'protected' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:163:13: 'protected'
            {
            match("protected"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "PROTECTED"

    // $ANTLR start "PUBLIC"
    public final void mPUBLIC() throws RecognitionException {
        try {
            int _type = PUBLIC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:164:8: ( 'public' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:164:10: 'public'
            {
            match("public"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "PUBLIC"

    // $ANTLR start "QUE"
    public final void mQUE() throws RecognitionException {
        try {
            int _type = QUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:165:5: ( '?' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:165:7: '?'
            {
            match('?'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "QUE"

    // $ANTLR start "RBRACE"
    public final void mRBRACE() throws RecognitionException {
        try {
            int _type = RBRACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:166:8: ( '}' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:166:10: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "RBRACE"

    // $ANTLR start "RBRACK"
    public final void mRBRACK() throws RecognitionException {
        try {
            int _type = RBRACK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:167:8: ( ']' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:167:10: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "RBRACK"

    // $ANTLR start "RETURN"
    public final void mRETURN() throws RecognitionException {
        try {
            int _type = RETURN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:168:8: ( 'return' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:168:10: 'return'
            {
            match("return"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "RETURN"

    // $ANTLR start "RPAREN"
    public final void mRPAREN() throws RecognitionException {
        try {
            int _type = RPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:169:8: ( ')' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:169:10: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "RPAREN"

    // $ANTLR start "SAME"
    public final void mSAME() throws RecognitionException {
        try {
            int _type = SAME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:170:6: ( '===' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:170:8: '==='
            {
            match("==="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SAME"

    // $ANTLR start "SEMIC"
    public final void mSEMIC() throws RecognitionException {
        try {
            int _type = SEMIC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:171:7: ( ';' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:171:9: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SEMIC"

    // $ANTLR start "SHL"
    public final void mSHL() throws RecognitionException {
        try {
            int _type = SHL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:172:5: ( '<<' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:172:7: '<<'
            {
            match("<<"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SHL"

    // $ANTLR start "SHLASS"
    public final void mSHLASS() throws RecognitionException {
        try {
            int _type = SHLASS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:173:8: ( '<<=' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:173:10: '<<='
            {
            match("<<="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SHLASS"

    // $ANTLR start "SHORT"
    public final void mSHORT() throws RecognitionException {
        try {
            int _type = SHORT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:174:7: ( 'short' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:174:9: 'short'
            {
            match("short"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SHORT"

    // $ANTLR start "SHR"
    public final void mSHR() throws RecognitionException {
        try {
            int _type = SHR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:175:5: ( '>>' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:175:7: '>>'
            {
            match(">>"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SHR"

    // $ANTLR start "SHRASS"
    public final void mSHRASS() throws RecognitionException {
        try {
            int _type = SHRASS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:176:8: ( '>>=' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:176:10: '>>='
            {
            match(">>="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SHRASS"

    // $ANTLR start "SHU"
    public final void mSHU() throws RecognitionException {
        try {
            int _type = SHU;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:177:5: ( '>>>' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:177:7: '>>>'
            {
            match(">>>"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SHU"

    // $ANTLR start "SHUASS"
    public final void mSHUASS() throws RecognitionException {
        try {
            int _type = SHUASS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:178:8: ( '>>>=' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:178:10: '>>>='
            {
            match(">>>="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SHUASS"

    // $ANTLR start "STATIC"
    public final void mSTATIC() throws RecognitionException {
        try {
            int _type = STATIC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:179:8: ( 'static' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:179:10: 'static'
            {
            match("static"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "STATIC"

    // $ANTLR start "SUB"
    public final void mSUB() throws RecognitionException {
        try {
            int _type = SUB;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:180:5: ( '-' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:180:7: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SUB"

    // $ANTLR start "SUBASS"
    public final void mSUBASS() throws RecognitionException {
        try {
            int _type = SUBASS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:181:8: ( '-=' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:181:10: '-='
            {
            match("-="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SUBASS"

    // $ANTLR start "SUPER"
    public final void mSUPER() throws RecognitionException {
        try {
            int _type = SUPER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:182:7: ( 'super' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:182:9: 'super'
            {
            match("super"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SUPER"

    // $ANTLR start "SWITCH"
    public final void mSWITCH() throws RecognitionException {
        try {
            int _type = SWITCH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:183:8: ( 'switch' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:183:10: 'switch'
            {
            match("switch"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SWITCH"

    // $ANTLR start "SYNCHRONIZED"
    public final void mSYNCHRONIZED() throws RecognitionException {
        try {
            int _type = SYNCHRONIZED;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:184:14: ( 'synchronized' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:184:16: 'synchronized'
            {
            match("synchronized"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SYNCHRONIZED"

    // $ANTLR start "THIS"
    public final void mTHIS() throws RecognitionException {
        try {
            int _type = THIS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:185:6: ( 'this' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:185:8: 'this'
            {
            match("this"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "THIS"

    // $ANTLR start "THROW"
    public final void mTHROW() throws RecognitionException {
        try {
            int _type = THROW;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:186:7: ( 'throw' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:186:9: 'throw'
            {
            match("throw"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "THROW"

    // $ANTLR start "THROWS"
    public final void mTHROWS() throws RecognitionException {
        try {
            int _type = THROWS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:187:8: ( 'throws' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:187:10: 'throws'
            {
            match("throws"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "THROWS"

    // $ANTLR start "TRANSIENT"
    public final void mTRANSIENT() throws RecognitionException {
        try {
            int _type = TRANSIENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:188:11: ( 'transient' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:188:13: 'transient'
            {
            match("transient"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "TRANSIENT"

    // $ANTLR start "TRUE"
    public final void mTRUE() throws RecognitionException {
        try {
            int _type = TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:189:6: ( 'true' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:189:8: 'true'
            {
            match("true"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "TRUE"

    // $ANTLR start "TRY"
    public final void mTRY() throws RecognitionException {
        try {
            int _type = TRY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:190:5: ( 'try' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:190:7: 'try'
            {
            match("try"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "TRY"

    // $ANTLR start "TYPEOF"
    public final void mTYPEOF() throws RecognitionException {
        try {
            int _type = TYPEOF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:191:8: ( 'typeof' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:191:10: 'typeof'
            {
            match("typeof"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "TYPEOF"

    // $ANTLR start "VAR"
    public final void mVAR() throws RecognitionException {
        try {
            int _type = VAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:192:5: ( 'var' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:192:7: 'var'
            {
            match("var"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "VAR"

    // $ANTLR start "VOID"
    public final void mVOID() throws RecognitionException {
        try {
            int _type = VOID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:193:6: ( 'void' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:193:8: 'void'
            {
            match("void"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "VOID"

    // $ANTLR start "VOLATILE"
    public final void mVOLATILE() throws RecognitionException {
        try {
            int _type = VOLATILE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:194:10: ( 'volatile' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:194:12: 'volatile'
            {
            match("volatile"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "VOLATILE"

    // $ANTLR start "WHILE"
    public final void mWHILE() throws RecognitionException {
        try {
            int _type = WHILE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:195:7: ( 'while' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:195:9: 'while'
            {
            match("while"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "WHILE"

    // $ANTLR start "WITH"
    public final void mWITH() throws RecognitionException {
        try {
            int _type = WITH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:196:6: ( 'with' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:196:8: 'with'
            {
            match("with"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "WITH"

    // $ANTLR start "XOR"
    public final void mXOR() throws RecognitionException {
        try {
            int _type = XOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:197:5: ( '^' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:197:7: '^'
            {
            match('^'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "XOR"

    // $ANTLR start "XORASS"
    public final void mXORASS() throws RecognitionException {
        try {
            int _type = XORASS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:198:8: ( '^=' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:198:10: '^='
            {
            match("^="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "XORASS"

    // $ANTLR start "BSLASH"
    public final void mBSLASH() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:433:2: ( '\\\\' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:433:4: '\\\\'
            {
            match('\\'); 

            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "BSLASH"

    // $ANTLR start "DQUOTE"
    public final void mDQUOTE() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:437:2: ( '\"' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:437:4: '\"'
            {
            match('\"'); 

            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DQUOTE"

    // $ANTLR start "SQUOTE"
    public final void mSQUOTE() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:441:2: ( '\\'' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:441:4: '\\''
            {
            match('\''); 

            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SQUOTE"

    // $ANTLR start "TAB"
    public final void mTAB() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:447:2: ( '\\u0009' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:447:4: '\\u0009'
            {
            match('\t'); 

            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "TAB"

    // $ANTLR start "VT"
    public final void mVT() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:451:2: ( '\\u000b' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:451:4: '\\u000b'
            {
            match('\u000B'); 

            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "VT"

    // $ANTLR start "FF"
    public final void mFF() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:455:2: ( '\\u000c' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:455:4: '\\u000c'
            {
            match('\f'); 

            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "FF"

    // $ANTLR start "SP"
    public final void mSP() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:459:2: ( '\\u0020' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:459:4: '\\u0020'
            {
            match(' '); 

            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SP"

    // $ANTLR start "NBSP"
    public final void mNBSP() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:463:2: ( '\\u00a0' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:463:4: '\\u00a0'
            {
            match('\u00A0'); 

            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "NBSP"

    // $ANTLR start "USP"
    public final void mUSP() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:467:2: ( '\\u1680' | '\\u180E' | '\\u2000' | '\\u2001' | '\\u2002' | '\\u2003' | '\\u2004' | '\\u2005' | '\\u2006' | '\\u2007' | '\\u2008' | '\\u2009' | '\\u200A' | '\\u202F' | '\\u205F' | '\\u3000' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
            {
            if ( input.LA(1)=='\u1680'||input.LA(1)=='\u180E'||(input.LA(1) >= '\u2000' && input.LA(1) <= '\u200A')||input.LA(1)=='\u202F'||input.LA(1)=='\u205F'||input.LA(1)=='\u3000' ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "USP"

    // $ANTLR start "WhiteSpace"
    public final void mWhiteSpace() throws RecognitionException {
        try {
            int _type = WhiteSpace;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:486:2: ( ( TAB | VT | FF | SP | NBSP | USP )+ )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:486:4: ( TAB | VT | FF | SP | NBSP | USP )+
            {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:486:4: ( TAB | VT | FF | SP | NBSP | USP )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0=='\t'||(LA1_0 >= '\u000B' && LA1_0 <= '\f')||LA1_0==' '||LA1_0=='\u00A0'||LA1_0=='\u1680'||LA1_0=='\u180E'||(LA1_0 >= '\u2000' && LA1_0 <= '\u200A')||LA1_0=='\u202F'||LA1_0=='\u205F'||LA1_0=='\u3000') ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
            	    {
            	    if ( input.LA(1)=='\t'||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||input.LA(1)==' '||input.LA(1)=='\u00A0'||input.LA(1)=='\u1680'||input.LA(1)=='\u180E'||(input.LA(1) >= '\u2000' && input.LA(1) <= '\u200A')||input.LA(1)=='\u202F'||input.LA(1)=='\u205F'||input.LA(1)=='\u3000' ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);


             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "WhiteSpace"

    // $ANTLR start "LF"
    public final void mLF() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:494:2: ( '\\n' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:494:4: '\\n'
            {
            match('\n'); 

            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LF"

    // $ANTLR start "CR"
    public final void mCR() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:498:2: ( '\\r' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:498:4: '\\r'
            {
            match('\r'); 

            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CR"

    // $ANTLR start "LS"
    public final void mLS() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:502:2: ( '\\u2028' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:502:4: '\\u2028'
            {
            match('\u2028'); 

            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LS"

    // $ANTLR start "PS"
    public final void mPS() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:506:2: ( '\\u2029' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:506:4: '\\u2029'
            {
            match('\u2029'); 

            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "PS"

    // $ANTLR start "LineTerminator"
    public final void mLineTerminator() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:510:2: ( CR | LF | LS | PS )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
            {
            if ( input.LA(1)=='\n'||input.LA(1)=='\r'||(input.LA(1) >= '\u2028' && input.LA(1) <= '\u2029') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LineTerminator"

    // $ANTLR start "EOL"
    public final void mEOL() throws RecognitionException {
        try {
            int _type = EOL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:514:2: ( ( ( CR ( LF )? ) | LF | LS | PS ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:514:4: ( ( CR ( LF )? ) | LF | LS | PS )
            {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:514:4: ( ( CR ( LF )? ) | LF | LS | PS )
            int alt3=4;
            switch ( input.LA(1) ) {
            case '\r':
                {
                alt3=1;
                }
                break;
            case '\n':
                {
                alt3=2;
                }
                break;
            case '\u2028':
                {
                alt3=3;
                }
                break;
            case '\u2029':
                {
                alt3=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;

            }

            switch (alt3) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:514:6: ( CR ( LF )? )
                    {
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:514:6: ( CR ( LF )? )
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:514:8: CR ( LF )?
                    {
                    mCR(); 


                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:514:11: ( LF )?
                    int alt2=2;
                    int LA2_0 = input.LA(1);

                    if ( (LA2_0=='\n') ) {
                        alt2=1;
                    }
                    switch (alt2) {
                        case 1 :
                            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
                            {
                            if ( input.LA(1)=='\n' ) {
                                input.consume();
                            }
                            else {
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;
                            }


                            }
                            break;

                    }


                    }


                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:514:19: LF
                    {
                    mLF(); 


                    }
                    break;
                case 3 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:514:24: LS
                    {
                    mLS(); 


                    }
                    break;
                case 4 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:514:29: PS
                    {
                    mPS(); 


                    }
                    break;

            }


             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "EOL"

    // $ANTLR start "MultiLineComment"
    public final void mMultiLineComment() throws RecognitionException {
        try {
            int _type = MultiLineComment;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:521:2: ( '/*' ( options {greedy=false; } : . )* '*/' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:521:4: '/*' ( options {greedy=false; } : . )* '*/'
            {
            match("/*"); 



            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:521:9: ( options {greedy=false; } : . )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0=='*') ) {
                    int LA4_1 = input.LA(2);

                    if ( (LA4_1=='/') ) {
                        alt4=2;
                    }
                    else if ( ((LA4_1 >= '\u0000' && LA4_1 <= '.')||(LA4_1 >= '0' && LA4_1 <= '\uFFFF')) ) {
                        alt4=1;
                    }


                }
                else if ( ((LA4_0 >= '\u0000' && LA4_0 <= ')')||(LA4_0 >= '+' && LA4_0 <= '\uFFFF')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:521:41: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            match("*/"); 



             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "MultiLineComment"

    // $ANTLR start "SingleLineComment"
    public final void mSingleLineComment() throws RecognitionException {
        try {
            int _type = SingleLineComment;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:525:2: ( '//' (~ ( LineTerminator ) )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:525:4: '//' (~ ( LineTerminator ) )*
            {
            match("//"); 



            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:525:9: (~ ( LineTerminator ) )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0 >= '\u0000' && LA5_0 <= '\t')||(LA5_0 >= '\u000B' && LA5_0 <= '\f')||(LA5_0 >= '\u000E' && LA5_0 <= '\u2027')||(LA5_0 >= '\u202A' && LA5_0 <= '\uFFFF')) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
            	    {
            	    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '\u2027')||(input.LA(1) >= '\u202A' && input.LA(1) <= '\uFFFF') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);


             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SingleLineComment"

    // $ANTLR start "IdentifierStartASCII"
    public final void mIdentifierStartASCII() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:626:2: ( 'a' .. 'z' | 'A' .. 'Z' | '$' | '_' | BSLASH 'u' HexDigit HexDigit HexDigit HexDigit )
            int alt6=5;
            switch ( input.LA(1) ) {
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
                {
                alt6=1;
                }
                break;
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
                {
                alt6=2;
                }
                break;
            case '$':
                {
                alt6=3;
                }
                break;
            case '_':
                {
                alt6=4;
                }
                break;
            case '\\':
                {
                alt6=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;

            }

            switch (alt6) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:626:4: 'a' .. 'z'
                    {
                    matchRange('a','z'); 

                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:626:15: 'A' .. 'Z'
                    {
                    matchRange('A','Z'); 

                    }
                    break;
                case 3 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:627:4: '$'
                    {
                    match('$'); 

                    }
                    break;
                case 4 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:628:4: '_'
                    {
                    match('_'); 

                    }
                    break;
                case 5 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:629:4: BSLASH 'u' HexDigit HexDigit HexDigit HexDigit
                    {
                    mBSLASH(); 


                    match('u'); 

                    mHexDigit(); 


                    mHexDigit(); 


                    mHexDigit(); 


                    mHexDigit(); 


                    }
                    break;

            }

        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "IdentifierStartASCII"

    // $ANTLR start "IdentifierPart"
    public final void mIdentifierPart() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:637:2: ( DecimalDigit | IdentifierStartASCII |{...}?)
            int alt7=3;
            switch ( input.LA(1) ) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                {
                alt7=1;
                }
                break;
            case '$':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case '\\':
            case '_':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
                {
                alt7=2;
                }
                break;
            default:
                alt7=3;
            }

            switch (alt7) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:637:4: DecimalDigit
                    {
                    mDecimalDigit(); 


                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:638:4: IdentifierStartASCII
                    {
                    mIdentifierStartASCII(); 


                    }
                    break;
                case 3 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:639:4: {...}?
                    {
                    if ( !(( isIdentifierPartUnicode(input.LA(1)) )) ) {
                        throw new FailedPredicateException(input, "IdentifierPart", " isIdentifierPartUnicode(input.LA(1)) ");
                    }

                     matchAny(); 

                    }
                    break;

            }

        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "IdentifierPart"

    // $ANTLR start "IdentifierNameASCIIStart"
    public final void mIdentifierNameASCIIStart() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:643:2: ( IdentifierStartASCII ( IdentifierPart )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:643:4: IdentifierStartASCII ( IdentifierPart )*
            {
            mIdentifierStartASCII(); 


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:643:25: ( IdentifierPart )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0=='$'||(LA8_0 >= '0' && LA8_0 <= '9')||(LA8_0 >= 'A' && LA8_0 <= 'Z')||LA8_0=='\\'||LA8_0=='_'||(LA8_0 >= 'a' && LA8_0 <= 'z')) ) {
                    alt8=1;
                }
                else if ( (( isIdentifierPartUnicode(input.LA(1)) )) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:643:25: IdentifierPart
            	    {
            	    mIdentifierPart(); 


            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "IdentifierNameASCIIStart"

    // $ANTLR start "Identifier"
    public final void mIdentifier() throws RecognitionException {
        try {
            int _type = Identifier;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:655:2: ( IdentifierNameASCIIStart |)
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0=='$'||(LA9_0 >= 'A' && LA9_0 <= 'Z')||LA9_0=='\\'||LA9_0=='_'||(LA9_0 >= 'a' && LA9_0 <= 'z')) ) {
                alt9=1;
            }
            else {
                alt9=2;
            }
            switch (alt9) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:655:4: IdentifierNameASCIIStart
                    {
                    mIdentifierNameASCIIStart(); 


                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:656:4: 
                    {
                     consumeIdentifierUnicodeStart(); 

                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "Identifier"

    // $ANTLR start "DecimalDigit"
    public final void mDecimalDigit() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:740:2: ( '0' .. '9' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
            {
            if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DecimalDigit"

    // $ANTLR start "HexDigit"
    public final void mHexDigit() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:744:2: ( DecimalDigit | 'a' .. 'f' | 'A' .. 'F' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
            {
            if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "HexDigit"

    // $ANTLR start "OctalDigit"
    public final void mOctalDigit() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:748:2: ( '0' .. '7' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
            {
            if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "OctalDigit"

    // $ANTLR start "ExponentPart"
    public final void mExponentPart() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:752:2: ( ( 'e' | 'E' ) ( '+' | '-' )? ( DecimalDigit )+ )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:752:4: ( 'e' | 'E' ) ( '+' | '-' )? ( DecimalDigit )+
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:752:18: ( '+' | '-' )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0=='+'||LA10_0=='-') ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    }
                    break;

            }


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:752:33: ( DecimalDigit )+
            int cnt11=0;
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( ((LA11_0 >= '0' && LA11_0 <= '9')) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt11 >= 1 ) break loop11;
                        EarlyExitException eee =
                            new EarlyExitException(11, input);
                        throw eee;
                }
                cnt11++;
            } while (true);


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ExponentPart"

    // $ANTLR start "DecimalIntegerLiteral"
    public final void mDecimalIntegerLiteral() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:756:2: ( '0' | '1' .. '9' ( DecimalDigit )* )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0=='0') ) {
                alt13=1;
            }
            else if ( ((LA13_0 >= '1' && LA13_0 <= '9')) ) {
                alt13=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;

            }
            switch (alt13) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:756:4: '0'
                    {
                    match('0'); 

                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:757:4: '1' .. '9' ( DecimalDigit )*
                    {
                    matchRange('1','9'); 

                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:757:13: ( DecimalDigit )*
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);

                        if ( ((LA12_0 >= '0' && LA12_0 <= '9')) ) {
                            alt12=1;
                        }


                        switch (alt12) {
                    	case 1 :
                    	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
                    	    {
                    	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
                    	        input.consume();
                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;
                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop12;
                        }
                    } while (true);


                    }
                    break;

            }

        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DecimalIntegerLiteral"

    // $ANTLR start "DecimalLiteral"
    public final void mDecimalLiteral() throws RecognitionException {
        try {
            int _type = DecimalLiteral;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:761:2: ( DecimalIntegerLiteral '.' ( DecimalDigit )* ( ExponentPart )? | '.' ( DecimalDigit )+ ( ExponentPart )? | DecimalIntegerLiteral ( ExponentPart )? )
            int alt19=3;
            alt19 = dfa19.predict(input);
            switch (alt19) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:761:4: DecimalIntegerLiteral '.' ( DecimalDigit )* ( ExponentPart )?
                    {
                    mDecimalIntegerLiteral(); 


                    match('.'); 

                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:761:30: ( DecimalDigit )*
                    loop14:
                    do {
                        int alt14=2;
                        int LA14_0 = input.LA(1);

                        if ( ((LA14_0 >= '0' && LA14_0 <= '9')) ) {
                            alt14=1;
                        }


                        switch (alt14) {
                    	case 1 :
                    	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
                    	    {
                    	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
                    	        input.consume();
                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;
                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop14;
                        }
                    } while (true);


                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:761:44: ( ExponentPart )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0=='E'||LA15_0=='e') ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:761:44: ExponentPart
                            {
                            mExponentPart(); 


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:762:4: '.' ( DecimalDigit )+ ( ExponentPart )?
                    {
                    match('.'); 

                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:762:8: ( DecimalDigit )+
                    int cnt16=0;
                    loop16:
                    do {
                        int alt16=2;
                        int LA16_0 = input.LA(1);

                        if ( ((LA16_0 >= '0' && LA16_0 <= '9')) ) {
                            alt16=1;
                        }


                        switch (alt16) {
                    	case 1 :
                    	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
                    	    {
                    	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
                    	        input.consume();
                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;
                    	    }


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt16 >= 1 ) break loop16;
                                EarlyExitException eee =
                                    new EarlyExitException(16, input);
                                throw eee;
                        }
                        cnt16++;
                    } while (true);


                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:762:22: ( ExponentPart )?
                    int alt17=2;
                    int LA17_0 = input.LA(1);

                    if ( (LA17_0=='E'||LA17_0=='e') ) {
                        alt17=1;
                    }
                    switch (alt17) {
                        case 1 :
                            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:762:22: ExponentPart
                            {
                            mExponentPart(); 


                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:763:4: DecimalIntegerLiteral ( ExponentPart )?
                    {
                    mDecimalIntegerLiteral(); 


                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:763:26: ( ExponentPart )?
                    int alt18=2;
                    int LA18_0 = input.LA(1);

                    if ( (LA18_0=='E'||LA18_0=='e') ) {
                        alt18=1;
                    }
                    switch (alt18) {
                        case 1 :
                            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:763:26: ExponentPart
                            {
                            mExponentPart(); 


                            }
                            break;

                    }


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DecimalLiteral"

    // $ANTLR start "OctalIntegerLiteral"
    public final void mOctalIntegerLiteral() throws RecognitionException {
        try {
            int _type = OctalIntegerLiteral;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:767:2: ( '0' ( OctalDigit )+ )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:767:4: '0' ( OctalDigit )+
            {
            match('0'); 

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:767:8: ( OctalDigit )+
            int cnt20=0;
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( ((LA20_0 >= '0' && LA20_0 <= '7')) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt20 >= 1 ) break loop20;
                        EarlyExitException eee =
                            new EarlyExitException(20, input);
                        throw eee;
                }
                cnt20++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "OctalIntegerLiteral"

    // $ANTLR start "HexIntegerLiteral"
    public final void mHexIntegerLiteral() throws RecognitionException {
        try {
            int _type = HexIntegerLiteral;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:771:2: ( ( '0x' | '0X' ) ( HexDigit )+ )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:771:4: ( '0x' | '0X' ) ( HexDigit )+
            {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:771:4: ( '0x' | '0X' )
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0=='0') ) {
                int LA21_1 = input.LA(2);

                if ( (LA21_1=='x') ) {
                    alt21=1;
                }
                else if ( (LA21_1=='X') ) {
                    alt21=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 21, 1, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 21, 0, input);

                throw nvae;

            }
            switch (alt21) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:771:6: '0x'
                    {
                    match("0x"); 



                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:771:13: '0X'
                    {
                    match("0X"); 



                    }
                    break;

            }


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:771:20: ( HexDigit )+
            int cnt22=0;
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( ((LA22_0 >= '0' && LA22_0 <= '9')||(LA22_0 >= 'A' && LA22_0 <= 'F')||(LA22_0 >= 'a' && LA22_0 <= 'f')) ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt22 >= 1 ) break loop22;
                        EarlyExitException eee =
                            new EarlyExitException(22, input);
                        throw eee;
                }
                cnt22++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "HexIntegerLiteral"

    // $ANTLR start "CharacterEscapeSequence"
    public final void mCharacterEscapeSequence() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:790:2: (~ ( DecimalDigit | 'x' | 'u' | LineTerminator ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
            {
            if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '/')||(input.LA(1) >= ':' && input.LA(1) <= 't')||(input.LA(1) >= 'v' && input.LA(1) <= 'w')||(input.LA(1) >= 'y' && input.LA(1) <= '\u2027')||(input.LA(1) >= '\u202A' && input.LA(1) <= '\uFFFF') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "CharacterEscapeSequence"

    // $ANTLR start "ZeroToThree"
    public final void mZeroToThree() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:794:2: ( '0' .. '3' )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
            {
            if ( (input.LA(1) >= '0' && input.LA(1) <= '3') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ZeroToThree"

    // $ANTLR start "OctalEscapeSequence"
    public final void mOctalEscapeSequence() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:798:2: ( OctalDigit | ZeroToThree OctalDigit | '4' .. '7' OctalDigit | ZeroToThree OctalDigit OctalDigit )
            int alt23=4;
            int LA23_0 = input.LA(1);

            if ( ((LA23_0 >= '0' && LA23_0 <= '3')) ) {
                int LA23_1 = input.LA(2);

                if ( ((LA23_1 >= '0' && LA23_1 <= '7')) ) {
                    int LA23_4 = input.LA(3);

                    if ( ((LA23_4 >= '0' && LA23_4 <= '7')) ) {
                        alt23=4;
                    }
                    else {
                        alt23=2;
                    }
                }
                else {
                    alt23=1;
                }
            }
            else if ( ((LA23_0 >= '4' && LA23_0 <= '7')) ) {
                int LA23_2 = input.LA(2);

                if ( ((LA23_2 >= '0' && LA23_2 <= '7')) ) {
                    alt23=3;
                }
                else {
                    alt23=1;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 23, 0, input);

                throw nvae;

            }
            switch (alt23) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:798:4: OctalDigit
                    {
                    mOctalDigit(); 


                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:799:4: ZeroToThree OctalDigit
                    {
                    mZeroToThree(); 


                    mOctalDigit(); 


                    }
                    break;
                case 3 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:800:4: '4' .. '7' OctalDigit
                    {
                    matchRange('4','7'); 

                    mOctalDigit(); 


                    }
                    break;
                case 4 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:801:4: ZeroToThree OctalDigit OctalDigit
                    {
                    mZeroToThree(); 


                    mOctalDigit(); 


                    mOctalDigit(); 


                    }
                    break;

            }

        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "OctalEscapeSequence"

    // $ANTLR start "HexEscapeSequence"
    public final void mHexEscapeSequence() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:805:2: ( 'x' HexDigit HexDigit )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:805:4: 'x' HexDigit HexDigit
            {
            match('x'); 

            mHexDigit(); 


            mHexDigit(); 


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "HexEscapeSequence"

    // $ANTLR start "UnicodeEscapeSequence"
    public final void mUnicodeEscapeSequence() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:809:2: ( 'u' HexDigit HexDigit HexDigit HexDigit )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:809:4: 'u' HexDigit HexDigit HexDigit HexDigit
            {
            match('u'); 

            mHexDigit(); 


            mHexDigit(); 


            mHexDigit(); 


            mHexDigit(); 


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "UnicodeEscapeSequence"

    // $ANTLR start "EscapeSequence"
    public final void mEscapeSequence() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:813:2: ( BSLASH ( CharacterEscapeSequence | OctalEscapeSequence | HexEscapeSequence | UnicodeEscapeSequence ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:814:2: BSLASH ( CharacterEscapeSequence | OctalEscapeSequence | HexEscapeSequence | UnicodeEscapeSequence )
            {
            mBSLASH(); 


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:815:2: ( CharacterEscapeSequence | OctalEscapeSequence | HexEscapeSequence | UnicodeEscapeSequence )
            int alt24=4;
            int LA24_0 = input.LA(1);

            if ( ((LA24_0 >= '\u0000' && LA24_0 <= '\t')||(LA24_0 >= '\u000B' && LA24_0 <= '\f')||(LA24_0 >= '\u000E' && LA24_0 <= '/')||(LA24_0 >= ':' && LA24_0 <= 't')||(LA24_0 >= 'v' && LA24_0 <= 'w')||(LA24_0 >= 'y' && LA24_0 <= '\u2027')||(LA24_0 >= '\u202A' && LA24_0 <= '\uFFFF')) ) {
                alt24=1;
            }
            else if ( ((LA24_0 >= '0' && LA24_0 <= '7')) ) {
                alt24=2;
            }
            else if ( (LA24_0=='x') ) {
                alt24=3;
            }
            else if ( (LA24_0=='u') ) {
                alt24=4;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 24, 0, input);

                throw nvae;

            }
            switch (alt24) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:816:3: CharacterEscapeSequence
                    {
                    mCharacterEscapeSequence(); 


                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:817:5: OctalEscapeSequence
                    {
                    mOctalEscapeSequence(); 


                    }
                    break;
                case 3 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:818:5: HexEscapeSequence
                    {
                    mHexEscapeSequence(); 


                    }
                    break;
                case 4 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:819:5: UnicodeEscapeSequence
                    {
                    mUnicodeEscapeSequence(); 


                    }
                    break;

            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "EscapeSequence"

    // $ANTLR start "StringLiteral"
    public final void mStringLiteral() throws RecognitionException {
        try {
            int _type = StringLiteral;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:824:2: ( SQUOTE (~ ( SQUOTE | BSLASH | LineTerminator ) | EscapeSequence )* SQUOTE | DQUOTE (~ ( DQUOTE | BSLASH | LineTerminator ) | EscapeSequence )* DQUOTE )
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0=='\'') ) {
                alt27=1;
            }
            else if ( (LA27_0=='\"') ) {
                alt27=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 27, 0, input);

                throw nvae;

            }
            switch (alt27) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:824:4: SQUOTE (~ ( SQUOTE | BSLASH | LineTerminator ) | EscapeSequence )* SQUOTE
                    {
                    mSQUOTE(); 


                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:824:11: (~ ( SQUOTE | BSLASH | LineTerminator ) | EscapeSequence )*
                    loop25:
                    do {
                        int alt25=3;
                        int LA25_0 = input.LA(1);

                        if ( ((LA25_0 >= '\u0000' && LA25_0 <= '\t')||(LA25_0 >= '\u000B' && LA25_0 <= '\f')||(LA25_0 >= '\u000E' && LA25_0 <= '&')||(LA25_0 >= '(' && LA25_0 <= '[')||(LA25_0 >= ']' && LA25_0 <= '\u2027')||(LA25_0 >= '\u202A' && LA25_0 <= '\uFFFF')) ) {
                            alt25=1;
                        }
                        else if ( (LA25_0=='\\') ) {
                            alt25=2;
                        }


                        switch (alt25) {
                    	case 1 :
                    	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:824:13: ~ ( SQUOTE | BSLASH | LineTerminator )
                    	    {
                    	    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '&')||(input.LA(1) >= '(' && input.LA(1) <= '[')||(input.LA(1) >= ']' && input.LA(1) <= '\u2027')||(input.LA(1) >= '\u202A' && input.LA(1) <= '\uFFFF') ) {
                    	        input.consume();
                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;
                    	    }


                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:824:53: EscapeSequence
                    	    {
                    	    mEscapeSequence(); 


                    	    }
                    	    break;

                    	default :
                    	    break loop25;
                        }
                    } while (true);


                    mSQUOTE(); 


                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:825:4: DQUOTE (~ ( DQUOTE | BSLASH | LineTerminator ) | EscapeSequence )* DQUOTE
                    {
                    mDQUOTE(); 


                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:825:11: (~ ( DQUOTE | BSLASH | LineTerminator ) | EscapeSequence )*
                    loop26:
                    do {
                        int alt26=3;
                        int LA26_0 = input.LA(1);

                        if ( ((LA26_0 >= '\u0000' && LA26_0 <= '\t')||(LA26_0 >= '\u000B' && LA26_0 <= '\f')||(LA26_0 >= '\u000E' && LA26_0 <= '!')||(LA26_0 >= '#' && LA26_0 <= '[')||(LA26_0 >= ']' && LA26_0 <= '\u2027')||(LA26_0 >= '\u202A' && LA26_0 <= '\uFFFF')) ) {
                            alt26=1;
                        }
                        else if ( (LA26_0=='\\') ) {
                            alt26=2;
                        }


                        switch (alt26) {
                    	case 1 :
                    	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:825:13: ~ ( DQUOTE | BSLASH | LineTerminator )
                    	    {
                    	    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '!')||(input.LA(1) >= '#' && input.LA(1) <= '[')||(input.LA(1) >= ']' && input.LA(1) <= '\u2027')||(input.LA(1) >= '\u202A' && input.LA(1) <= '\uFFFF') ) {
                    	        input.consume();
                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;
                    	    }


                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:825:53: EscapeSequence
                    	    {
                    	    mEscapeSequence(); 


                    	    }
                    	    break;

                    	default :
                    	    break loop26;
                        }
                    } while (true);


                    mDQUOTE(); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "StringLiteral"

    // $ANTLR start "BackslashSequence"
    public final void mBackslashSequence() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:833:2: ( BSLASH ~ ( LineTerminator ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:833:4: BSLASH ~ ( LineTerminator )
            {
            mBSLASH(); 


            if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '\u2027')||(input.LA(1) >= '\u202A' && input.LA(1) <= '\uFFFF') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "BackslashSequence"

    // $ANTLR start "RegularExpressionFirstChar"
    public final void mRegularExpressionFirstChar() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:837:2: (~ ( LineTerminator | MUL | BSLASH | DIV ) | BackslashSequence )
            int alt28=2;
            int LA28_0 = input.LA(1);

            if ( ((LA28_0 >= '\u0000' && LA28_0 <= '\t')||(LA28_0 >= '\u000B' && LA28_0 <= '\f')||(LA28_0 >= '\u000E' && LA28_0 <= ')')||(LA28_0 >= '+' && LA28_0 <= '.')||(LA28_0 >= '0' && LA28_0 <= '[')||(LA28_0 >= ']' && LA28_0 <= '\u2027')||(LA28_0 >= '\u202A' && LA28_0 <= '\uFFFF')) ) {
                alt28=1;
            }
            else if ( (LA28_0=='\\') ) {
                alt28=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 28, 0, input);

                throw nvae;

            }
            switch (alt28) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:837:4: ~ ( LineTerminator | MUL | BSLASH | DIV )
                    {
                    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= ')')||(input.LA(1) >= '+' && input.LA(1) <= '.')||(input.LA(1) >= '0' && input.LA(1) <= '[')||(input.LA(1) >= ']' && input.LA(1) <= '\u2027')||(input.LA(1) >= '\u202A' && input.LA(1) <= '\uFFFF') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:838:4: BackslashSequence
                    {
                    mBackslashSequence(); 


                    }
                    break;

            }

        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "RegularExpressionFirstChar"

    // $ANTLR start "RegularExpressionChar"
    public final void mRegularExpressionChar() throws RecognitionException {
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:842:2: (~ ( LineTerminator | BSLASH | DIV ) | BackslashSequence )
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( ((LA29_0 >= '\u0000' && LA29_0 <= '\t')||(LA29_0 >= '\u000B' && LA29_0 <= '\f')||(LA29_0 >= '\u000E' && LA29_0 <= '.')||(LA29_0 >= '0' && LA29_0 <= '[')||(LA29_0 >= ']' && LA29_0 <= '\u2027')||(LA29_0 >= '\u202A' && LA29_0 <= '\uFFFF')) ) {
                alt29=1;
            }
            else if ( (LA29_0=='\\') ) {
                alt29=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 29, 0, input);

                throw nvae;

            }
            switch (alt29) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:842:4: ~ ( LineTerminator | BSLASH | DIV )
                    {
                    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '.')||(input.LA(1) >= '0' && input.LA(1) <= '[')||(input.LA(1) >= ']' && input.LA(1) <= '\u2027')||(input.LA(1) >= '\u202A' && input.LA(1) <= '\uFFFF') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:843:4: BackslashSequence
                    {
                    mBackslashSequence(); 


                    }
                    break;

            }

        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "RegularExpressionChar"

    // $ANTLR start "RegularExpressionLiteral"
    public final void mRegularExpressionLiteral() throws RecognitionException {
        try {
            int _type = RegularExpressionLiteral;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:847:2: ({...}? => DIV RegularExpressionFirstChar ( RegularExpressionChar )* DIV ( IdentifierPart )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:847:4: {...}? => DIV RegularExpressionFirstChar ( RegularExpressionChar )* DIV ( IdentifierPart )*
            {
            if ( !(( areRegularExpressionsEnabled() )) ) {
                throw new FailedPredicateException(input, "RegularExpressionLiteral", " areRegularExpressionsEnabled() ");
            }

            mDIV(); 


            mRegularExpressionFirstChar(); 


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:847:73: ( RegularExpressionChar )*
            loop30:
            do {
                int alt30=2;
                int LA30_0 = input.LA(1);

                if ( ((LA30_0 >= '\u0000' && LA30_0 <= '\t')||(LA30_0 >= '\u000B' && LA30_0 <= '\f')||(LA30_0 >= '\u000E' && LA30_0 <= '.')||(LA30_0 >= '0' && LA30_0 <= '\u2027')||(LA30_0 >= '\u202A' && LA30_0 <= '\uFFFF')) ) {
                    alt30=1;
                }


                switch (alt30) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:847:73: RegularExpressionChar
            	    {
            	    mRegularExpressionChar(); 


            	    }
            	    break;

            	default :
            	    break loop30;
                }
            } while (true);


            mDIV(); 


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:847:100: ( IdentifierPart )*
            loop31:
            do {
                int alt31=2;
                int LA31_0 = input.LA(1);

                if ( (LA31_0=='$'||(LA31_0 >= '0' && LA31_0 <= '9')||(LA31_0 >= 'A' && LA31_0 <= 'Z')||LA31_0=='\\'||LA31_0=='_'||(LA31_0 >= 'a' && LA31_0 <= 'z')) ) {
                    alt31=1;
                }
                else if ( (( isIdentifierPartUnicode(input.LA(1)) )) ) {
                    alt31=1;
                }


                switch (alt31) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:847:100: IdentifierPart
            	    {
            	    mIdentifierPart(); 


            	    }
            	    break;

            	default :
            	    break loop31;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "RegularExpressionLiteral"

    public void mTokens() throws RecognitionException {
        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:8: ( ABSTRACT | ADD | ADDASS | AND | ANDASS | ASSIGN | BOOLEAN | BREAK | BYTE | CASE | CATCH | CHAR | CLASS | COLON | COMMA | CONST | CONTINUE | DEBUGGER | DEC | DEFAULT | DELETE | DIV | DIVASS | DO | DOT | DOUBLE | ELSE | ENUM | EQ | EXPORT | EXTENDS | FALSE | FINAL | FINALLY | FLOAT | FOR | FSEND | FSSTART | FUNCTION | GOTO | GT | GTE | IF | IMPLEMENTS | IMPORT | IN | INC | INSTANCEOF | INT | INTERFACE | INV | LAND | LBRACE | LBRACK | LONG | LOR | LPAREN | LT | LTE | MOD | MODASS | MUL | MULASS | NATIVE | NEQ | NEW | NOT | NSAME | NULL | OR | ORASS | PACKAGE | PRIVATE | PROTECTED | PUBLIC | QUE | RBRACE | RBRACK | RETURN | RPAREN | SAME | SEMIC | SHL | SHLASS | SHORT | SHR | SHRASS | SHU | SHUASS | STATIC | SUB | SUBASS | SUPER | SWITCH | SYNCHRONIZED | THIS | THROW | THROWS | TRANSIENT | TRUE | TRY | TYPEOF | VAR | VOID | VOLATILE | WHILE | WITH | XOR | XORASS | WhiteSpace | EOL | MultiLineComment | SingleLineComment | Identifier | DecimalLiteral | OctalIntegerLiteral | HexIntegerLiteral | StringLiteral | RegularExpressionLiteral )
        int alt32=119;
        alt32 = dfa32.predict(input);
        switch (alt32) {
            case 1 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:10: ABSTRACT
                {
                mABSTRACT(); 


                }
                break;
            case 2 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:19: ADD
                {
                mADD(); 


                }
                break;
            case 3 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:23: ADDASS
                {
                mADDASS(); 


                }
                break;
            case 4 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:30: AND
                {
                mAND(); 


                }
                break;
            case 5 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:34: ANDASS
                {
                mANDASS(); 


                }
                break;
            case 6 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:41: ASSIGN
                {
                mASSIGN(); 


                }
                break;
            case 7 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:48: BOOLEAN
                {
                mBOOLEAN(); 


                }
                break;
            case 8 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:56: BREAK
                {
                mBREAK(); 


                }
                break;
            case 9 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:62: BYTE
                {
                mBYTE(); 


                }
                break;
            case 10 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:67: CASE
                {
                mCASE(); 


                }
                break;
            case 11 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:72: CATCH
                {
                mCATCH(); 


                }
                break;
            case 12 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:78: CHAR
                {
                mCHAR(); 


                }
                break;
            case 13 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:83: CLASS
                {
                mCLASS(); 


                }
                break;
            case 14 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:89: COLON
                {
                mCOLON(); 


                }
                break;
            case 15 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:95: COMMA
                {
                mCOMMA(); 


                }
                break;
            case 16 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:101: CONST
                {
                mCONST(); 


                }
                break;
            case 17 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:107: CONTINUE
                {
                mCONTINUE(); 


                }
                break;
            case 18 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:116: DEBUGGER
                {
                mDEBUGGER(); 


                }
                break;
            case 19 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:125: DEC
                {
                mDEC(); 


                }
                break;
            case 20 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:129: DEFAULT
                {
                mDEFAULT(); 


                }
                break;
            case 21 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:137: DELETE
                {
                mDELETE(); 


                }
                break;
            case 22 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:144: DIV
                {
                mDIV(); 


                }
                break;
            case 23 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:148: DIVASS
                {
                mDIVASS(); 


                }
                break;
            case 24 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:155: DO
                {
                mDO(); 


                }
                break;
            case 25 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:158: DOT
                {
                mDOT(); 


                }
                break;
            case 26 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:162: DOUBLE
                {
                mDOUBLE(); 


                }
                break;
            case 27 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:169: ELSE
                {
                mELSE(); 


                }
                break;
            case 28 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:174: ENUM
                {
                mENUM(); 


                }
                break;
            case 29 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:179: EQ
                {
                mEQ(); 


                }
                break;
            case 30 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:182: EXPORT
                {
                mEXPORT(); 


                }
                break;
            case 31 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:189: EXTENDS
                {
                mEXTENDS(); 


                }
                break;
            case 32 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:197: FALSE
                {
                mFALSE(); 


                }
                break;
            case 33 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:203: FINAL
                {
                mFINAL(); 


                }
                break;
            case 34 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:209: FINALLY
                {
                mFINALLY(); 


                }
                break;
            case 35 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:217: FLOAT
                {
                mFLOAT(); 


                }
                break;
            case 36 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:223: FOR
                {
                mFOR(); 


                }
                break;
            case 37 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:227: FSEND
                {
                mFSEND(); 


                }
                break;
            case 38 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:233: FSSTART
                {
                mFSSTART(); 


                }
                break;
            case 39 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:241: FUNCTION
                {
                mFUNCTION(); 


                }
                break;
            case 40 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:250: GOTO
                {
                mGOTO(); 


                }
                break;
            case 41 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:255: GT
                {
                mGT(); 


                }
                break;
            case 42 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:258: GTE
                {
                mGTE(); 


                }
                break;
            case 43 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:262: IF
                {
                mIF(); 


                }
                break;
            case 44 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:265: IMPLEMENTS
                {
                mIMPLEMENTS(); 


                }
                break;
            case 45 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:276: IMPORT
                {
                mIMPORT(); 


                }
                break;
            case 46 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:283: IN
                {
                mIN(); 


                }
                break;
            case 47 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:286: INC
                {
                mINC(); 


                }
                break;
            case 48 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:290: INSTANCEOF
                {
                mINSTANCEOF(); 


                }
                break;
            case 49 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:301: INT
                {
                mINT(); 


                }
                break;
            case 50 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:305: INTERFACE
                {
                mINTERFACE(); 


                }
                break;
            case 51 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:315: INV
                {
                mINV(); 


                }
                break;
            case 52 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:319: LAND
                {
                mLAND(); 


                }
                break;
            case 53 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:324: LBRACE
                {
                mLBRACE(); 


                }
                break;
            case 54 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:331: LBRACK
                {
                mLBRACK(); 


                }
                break;
            case 55 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:338: LONG
                {
                mLONG(); 


                }
                break;
            case 56 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:343: LOR
                {
                mLOR(); 


                }
                break;
            case 57 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:347: LPAREN
                {
                mLPAREN(); 


                }
                break;
            case 58 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:354: LT
                {
                mLT(); 


                }
                break;
            case 59 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:357: LTE
                {
                mLTE(); 


                }
                break;
            case 60 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:361: MOD
                {
                mMOD(); 


                }
                break;
            case 61 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:365: MODASS
                {
                mMODASS(); 


                }
                break;
            case 62 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:372: MUL
                {
                mMUL(); 


                }
                break;
            case 63 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:376: MULASS
                {
                mMULASS(); 


                }
                break;
            case 64 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:383: NATIVE
                {
                mNATIVE(); 


                }
                break;
            case 65 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:390: NEQ
                {
                mNEQ(); 


                }
                break;
            case 66 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:394: NEW
                {
                mNEW(); 


                }
                break;
            case 67 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:398: NOT
                {
                mNOT(); 


                }
                break;
            case 68 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:402: NSAME
                {
                mNSAME(); 


                }
                break;
            case 69 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:408: NULL
                {
                mNULL(); 


                }
                break;
            case 70 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:413: OR
                {
                mOR(); 


                }
                break;
            case 71 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:416: ORASS
                {
                mORASS(); 


                }
                break;
            case 72 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:422: PACKAGE
                {
                mPACKAGE(); 


                }
                break;
            case 73 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:430: PRIVATE
                {
                mPRIVATE(); 


                }
                break;
            case 74 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:438: PROTECTED
                {
                mPROTECTED(); 


                }
                break;
            case 75 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:448: PUBLIC
                {
                mPUBLIC(); 


                }
                break;
            case 76 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:455: QUE
                {
                mQUE(); 


                }
                break;
            case 77 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:459: RBRACE
                {
                mRBRACE(); 


                }
                break;
            case 78 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:466: RBRACK
                {
                mRBRACK(); 


                }
                break;
            case 79 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:473: RETURN
                {
                mRETURN(); 


                }
                break;
            case 80 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:480: RPAREN
                {
                mRPAREN(); 


                }
                break;
            case 81 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:487: SAME
                {
                mSAME(); 


                }
                break;
            case 82 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:492: SEMIC
                {
                mSEMIC(); 


                }
                break;
            case 83 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:498: SHL
                {
                mSHL(); 


                }
                break;
            case 84 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:502: SHLASS
                {
                mSHLASS(); 


                }
                break;
            case 85 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:509: SHORT
                {
                mSHORT(); 


                }
                break;
            case 86 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:515: SHR
                {
                mSHR(); 


                }
                break;
            case 87 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:519: SHRASS
                {
                mSHRASS(); 


                }
                break;
            case 88 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:526: SHU
                {
                mSHU(); 


                }
                break;
            case 89 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:530: SHUASS
                {
                mSHUASS(); 


                }
                break;
            case 90 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:537: STATIC
                {
                mSTATIC(); 


                }
                break;
            case 91 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:544: SUB
                {
                mSUB(); 


                }
                break;
            case 92 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:548: SUBASS
                {
                mSUBASS(); 


                }
                break;
            case 93 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:555: SUPER
                {
                mSUPER(); 


                }
                break;
            case 94 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:561: SWITCH
                {
                mSWITCH(); 


                }
                break;
            case 95 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:568: SYNCHRONIZED
                {
                mSYNCHRONIZED(); 


                }
                break;
            case 96 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:581: THIS
                {
                mTHIS(); 


                }
                break;
            case 97 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:586: THROW
                {
                mTHROW(); 


                }
                break;
            case 98 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:592: THROWS
                {
                mTHROWS(); 


                }
                break;
            case 99 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:599: TRANSIENT
                {
                mTRANSIENT(); 


                }
                break;
            case 100 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:609: TRUE
                {
                mTRUE(); 


                }
                break;
            case 101 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:614: TRY
                {
                mTRY(); 


                }
                break;
            case 102 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:618: TYPEOF
                {
                mTYPEOF(); 


                }
                break;
            case 103 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:625: VAR
                {
                mVAR(); 


                }
                break;
            case 104 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:629: VOID
                {
                mVOID(); 


                }
                break;
            case 105 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:634: VOLATILE
                {
                mVOLATILE(); 


                }
                break;
            case 106 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:643: WHILE
                {
                mWHILE(); 


                }
                break;
            case 107 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:649: WITH
                {
                mWITH(); 


                }
                break;
            case 108 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:654: XOR
                {
                mXOR(); 


                }
                break;
            case 109 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:658: XORASS
                {
                mXORASS(); 


                }
                break;
            case 110 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:665: WhiteSpace
                {
                mWhiteSpace(); 


                }
                break;
            case 111 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:676: EOL
                {
                mEOL(); 


                }
                break;
            case 112 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:680: MultiLineComment
                {
                mMultiLineComment(); 


                }
                break;
            case 113 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:697: SingleLineComment
                {
                mSingleLineComment(); 


                }
                break;
            case 114 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:715: Identifier
                {
                mIdentifier(); 


                }
                break;
            case 115 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:726: DecimalLiteral
                {
                mDecimalLiteral(); 


                }
                break;
            case 116 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:741: OctalIntegerLiteral
                {
                mOctalIntegerLiteral(); 


                }
                break;
            case 117 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:761: HexIntegerLiteral
                {
                mHexIntegerLiteral(); 


                }
                break;
            case 118 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:779: StringLiteral
                {
                mStringLiteral(); 


                }
                break;
            case 119 :
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1:793: RegularExpressionLiteral
                {
                mRegularExpressionLiteral(); 


                }
                break;

        }

    }


    protected DFA19 dfa19 = new DFA19(this);
    protected DFA32 dfa32 = new DFA32(this);
    static final String DFA19_eotS =
        "\1\uffff\2\4\3\uffff\1\4";
    static final String DFA19_eofS =
        "\7\uffff";
    static final String DFA19_minS =
        "\3\56\3\uffff\1\56";
    static final String DFA19_maxS =
        "\1\71\1\56\1\71\3\uffff\1\71";
    static final String DFA19_acceptS =
        "\3\uffff\1\2\1\3\1\1\1\uffff";
    static final String DFA19_specialS =
        "\7\uffff}>";
    static final String[] DFA19_transitionS = {
            "\1\3\1\uffff\1\1\11\2",
            "\1\5",
            "\1\5\1\uffff\12\6",
            "",
            "",
            "",
            "\1\5\1\uffff\12\6"
    };

    static final short[] DFA19_eot = DFA.unpackEncodedString(DFA19_eotS);
    static final short[] DFA19_eof = DFA.unpackEncodedString(DFA19_eofS);
    static final char[] DFA19_min = DFA.unpackEncodedStringToUnsignedChars(DFA19_minS);
    static final char[] DFA19_max = DFA.unpackEncodedStringToUnsignedChars(DFA19_maxS);
    static final short[] DFA19_accept = DFA.unpackEncodedString(DFA19_acceptS);
    static final short[] DFA19_special = DFA.unpackEncodedString(DFA19_specialS);
    static final short[][] DFA19_transition;

    static {
        int numStates = DFA19_transitionS.length;
        DFA19_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA19_transition[i] = DFA.unpackEncodedString(DFA19_transitionS[i]);
        }
    }

    class DFA19 extends DFA {

        public DFA19(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 19;
            this.eot = DFA19_eot;
            this.eof = DFA19_eof;
            this.min = DFA19_min;
            this.max = DFA19_max;
            this.accept = DFA19_accept;
            this.special = DFA19_special;
            this.transition = DFA19_transition;
        }
        public String getDescription() {
            return "760:1: DecimalLiteral : ( DecimalIntegerLiteral '.' ( DecimalDigit )* ( ExponentPart )? | '.' ( DecimalDigit )+ ( ExponentPart )? | DecimalIntegerLiteral ( ExponentPart )? );";
        }
    }
    static final String DFA32_eotS =
        "\2\53\1\62\1\65\1\67\2\53\2\uffff\1\53\1\103\1\107\1\111\2\53\1"+
        "\123\1\126\1\53\1\132\1\53\3\uffff\1\53\1\141\1\uffff\1\144\1\146"+
        "\1\150\1\53\1\155\1\53\2\uffff\1\53\2\uffff\4\53\3\uffff\1\55\2"+
        "\uffff\1\53\6\uffff\1\u0082\1\uffff\10\53\1\u008f\3\uffff\1\u0090"+
        "\5\uffff\10\53\5\uffff\1\53\1\uffff\1\u009d\1\uffff\1\u009e\1\53"+
        "\1\u00a2\1\53\4\uffff\1\u00a5\5\uffff\3\53\1\u00aa\1\uffff\20\53"+
        "\2\uffff\1\53\2\uffff\14\53\2\uffff\7\53\1\u00d5\2\53\1\uffff\1"+
        "\u00d9\2\uffff\2\53\1\u00de\1\uffff\1\53\2\uffff\1\53\1\u00e1\1"+
        "\53\2\uffff\16\53\1\u00f1\1\53\1\u00f3\7\53\1\u00fb\1\u00fc\1\53"+
        "\1\u00fe\7\53\1\u0106\1\u0107\5\53\1\uffff\1\53\1\u010e\2\uffff"+
        "\4\53\1\uffff\1\u0113\1\53\1\uffff\1\u0115\12\53\1\u0120\2\53\1"+
        "\u0123\1\uffff\1\53\1\uffff\1\u0125\2\53\1\u0128\2\53\1\u012b\2"+
        "\uffff\1\u012c\1\uffff\1\u012d\1\u012e\5\53\2\uffff\2\53\1\u0136"+
        "\1\u0138\1\u0139\1\53\1\uffff\4\53\1\uffff\1\53\1\uffff\5\53\1\u0145"+
        "\1\53\1\u0147\2\53\1\uffff\1\u014b\1\53\1\uffff\1\53\1\uffff\1\53"+
        "\1\u014f\1\uffff\2\53\4\uffff\3\53\1\u0155\1\u0156\1\u0157\1\53"+
        "\1\uffff\1\53\2\uffff\2\53\1\u015c\2\53\1\u015f\3\53\1\u0163\1\u0164"+
        "\1\uffff\1\u0165\1\uffff\1\u0166\1\53\1\u0168\1\uffff\1\53\1\u016a"+
        "\1\53\1\uffff\1\53\1\u016d\2\53\1\u0170\3\uffff\1\u0171\1\u0172"+
        "\2\53\1\uffff\2\53\1\uffff\1\u0177\1\u0178\1\53\4\uffff\1\53\1\uffff"+
        "\1\53\1\uffff\1\53\1\u017d\1\uffff\1\u017e\1\u017f\3\uffff\1\u0180"+
        "\3\53\2\uffff\3\53\1\u0187\4\uffff\2\53\1\u018a\1\u018b\1\53\1\u018d"+
        "\1\uffff\1\u018e\1\u018f\2\uffff\1\53\3\uffff\1\53\1\u0192\1\uffff";
    static final String DFA32_eofS =
        "\u0193\uffff";
    static final String DFA32_minS =
        "\1\11\1\142\1\53\1\46\1\75\1\157\1\141\2\uffff\1\145\1\55\1\0\1"+
        "\60\1\154\1\141\1\136\1\75\1\157\1\75\1\146\3\uffff\1\157\1\75\1"+
        "\uffff\1\74\2\75\1\141\1\75\1\141\2\uffff\1\145\2\uffff\2\150\1"+
        "\141\1\150\3\uffff\1\60\2\uffff\1\163\6\uffff\1\75\1\uffff\1\157"+
        "\1\145\1\164\1\163\2\141\1\156\1\142\1\44\3\uffff\1\0\5\uffff\1"+
        "\163\1\165\1\160\1\154\1\156\1\157\1\162\1\156\5\uffff\1\164\1\uffff"+
        "\1\75\1\uffff\1\44\1\160\1\44\1\156\4\uffff\1\75\5\uffff\1\164\1"+
        "\167\1\154\1\75\1\uffff\1\143\1\151\1\142\1\164\1\157\1\141\1\160"+
        "\1\151\1\156\1\151\1\141\1\160\1\162\2\151\1\164\2\uffff\1\164\2"+
        "\uffff\1\154\1\141\2\145\1\143\1\162\2\163\1\165\1\141\1\145\1\142"+
        "\2\uffff\1\145\1\155\1\157\1\145\1\163\2\141\1\44\1\143\1\157\1"+
        "\uffff\1\75\2\uffff\1\154\1\164\1\44\1\uffff\1\147\2\uffff\1\151"+
        "\1\44\1\154\2\uffff\1\153\1\166\1\164\1\154\1\165\1\162\1\164\1"+
        "\145\1\164\1\143\1\163\1\157\1\156\1\145\1\44\1\145\1\44\1\144\1"+
        "\141\1\154\1\150\1\162\1\145\1\153\2\44\1\150\1\44\1\163\1\164\1"+
        "\151\1\147\1\165\1\164\1\154\2\44\1\162\1\156\1\145\1\154\1\164"+
        "\1\uffff\1\164\1\44\2\uffff\1\145\1\162\1\141\1\162\1\uffff\1\44"+
        "\1\166\1\uffff\1\44\2\141\1\145\1\151\1\162\1\164\1\151\1\162\1"+
        "\143\1\150\1\44\1\167\1\163\1\44\1\uffff\1\157\1\uffff\1\44\1\164"+
        "\1\145\1\44\2\141\1\44\2\uffff\1\44\1\uffff\2\44\1\156\1\147\1\154"+
        "\2\145\2\uffff\1\164\1\144\3\44\1\151\1\uffff\1\155\1\164\1\156"+
        "\1\146\1\uffff\1\145\1\uffff\1\147\1\164\2\143\1\156\1\44\1\143"+
        "\1\44\1\150\1\162\1\uffff\1\44\1\151\1\uffff\1\146\1\uffff\1\151"+
        "\1\44\1\uffff\1\143\1\156\4\uffff\1\165\1\145\1\164\3\44\1\163\1"+
        "\uffff\1\171\2\uffff\1\157\1\145\1\44\1\143\1\141\1\44\2\145\1\164"+
        "\2\44\1\uffff\1\44\1\uffff\1\44\1\157\1\44\1\uffff\1\145\1\44\1"+
        "\154\1\uffff\1\164\1\44\1\145\1\162\1\44\3\uffff\2\44\2\156\1\uffff"+
        "\1\145\1\143\1\uffff\2\44\1\145\4\uffff\1\156\1\uffff\1\156\1\uffff"+
        "\1\145\1\44\1\uffff\2\44\3\uffff\1\44\1\164\1\157\1\145\2\uffff"+
        "\1\144\1\151\1\164\1\44\4\uffff\1\163\1\146\2\44\1\172\1\44\1\uffff"+
        "\2\44\2\uffff\1\145\3\uffff\1\144\1\44\1\uffff";
    static final String DFA32_maxS =
        "\1\u3000\1\142\3\75\1\171\1\157\2\uffff\1\157\1\75\1\uffff\1\71"+
        "\1\170\1\165\1\136\1\173\1\157\1\76\1\156\3\uffff\1\157\1\174\1"+
        "\uffff\3\75\1\165\1\75\1\165\2\uffff\1\145\2\uffff\2\171\1\157\1"+
        "\151\3\uffff\1\170\2\uffff\1\163\6\uffff\1\75\1\uffff\1\157\1\145"+
        "\2\164\2\141\1\156\1\154\1\172\3\uffff\1\uffff\5\uffff\1\163\1\165"+
        "\1\164\1\154\1\156\1\157\1\162\1\156\5\uffff\1\164\1\uffff\1\76"+
        "\1\uffff\1\172\1\160\1\172\1\156\4\uffff\1\75\5\uffff\1\164\1\167"+
        "\1\154\1\75\1\uffff\1\143\1\157\1\142\1\164\1\157\1\141\1\160\1"+
        "\151\1\156\1\162\1\171\1\160\1\162\1\154\1\151\1\164\2\uffff\1\164"+
        "\2\uffff\1\154\1\141\2\145\1\143\1\162\1\163\1\164\1\165\1\141\1"+
        "\145\1\142\2\uffff\1\145\1\155\1\157\1\145\1\163\2\141\1\172\1\143"+
        "\1\157\1\uffff\1\75\2\uffff\1\157\1\164\1\172\1\uffff\1\147\2\uffff"+
        "\1\151\1\172\1\154\2\uffff\1\153\1\166\1\164\1\154\1\165\1\162\1"+
        "\164\1\145\1\164\1\143\1\163\1\157\1\156\1\145\1\172\1\145\1\172"+
        "\1\144\1\141\1\154\1\150\1\162\1\145\1\153\2\172\1\150\1\172\1\163"+
        "\1\164\1\151\1\147\1\165\1\164\1\154\2\172\1\162\1\156\1\145\1\154"+
        "\1\164\1\uffff\1\164\1\172\2\uffff\1\145\1\162\1\141\1\162\1\uffff"+
        "\1\172\1\166\1\uffff\1\172\2\141\1\145\1\151\1\162\1\164\1\151\1"+
        "\162\1\143\1\150\1\172\1\167\1\163\1\172\1\uffff\1\157\1\uffff\1"+
        "\172\1\164\1\145\1\172\2\141\1\172\2\uffff\1\172\1\uffff\2\172\1"+
        "\156\1\147\1\154\2\145\2\uffff\1\164\1\144\3\172\1\151\1\uffff\1"+
        "\155\1\164\1\156\1\146\1\uffff\1\145\1\uffff\1\147\1\164\2\143\1"+
        "\156\1\172\1\143\1\172\1\150\1\162\1\uffff\1\172\1\151\1\uffff\1"+
        "\146\1\uffff\1\151\1\172\1\uffff\1\143\1\156\4\uffff\1\165\1\145"+
        "\1\164\3\172\1\163\1\uffff\1\171\2\uffff\1\157\1\145\1\172\1\143"+
        "\1\141\1\172\2\145\1\164\2\172\1\uffff\1\172\1\uffff\1\172\1\157"+
        "\1\172\1\uffff\1\145\1\172\1\154\1\uffff\1\164\1\172\1\145\1\162"+
        "\1\172\3\uffff\2\172\2\156\1\uffff\1\145\1\143\1\uffff\2\172\1\145"+
        "\4\uffff\1\156\1\uffff\1\156\1\uffff\1\145\1\172\1\uffff\2\172\3"+
        "\uffff\1\172\1\164\1\157\1\145\2\uffff\1\144\1\151\1\164\1\172\4"+
        "\uffff\1\163\1\146\4\172\1\uffff\2\172\2\uffff\1\145\3\uffff\1\144"+
        "\1\172\1\uffff";
    static final String DFA32_acceptS =
        "\7\uffff\1\16\1\17\13\uffff\1\63\1\65\1\66\2\uffff\1\71\6\uffff"+
        "\1\114\1\116\1\uffff\1\120\1\122\4\uffff\1\156\1\157\1\162\1\uffff"+
        "\1\163\1\166\1\uffff\1\3\1\57\1\2\1\5\1\64\1\4\1\uffff\1\6\11\uffff"+
        "\1\23\1\134\1\133\1\uffff\1\160\1\161\1\26\1\167\1\31\10\uffff\1"+
        "\45\1\115\1\46\1\155\1\154\1\uffff\1\52\1\uffff\1\51\4\uffff\1\70"+
        "\1\107\1\106\1\73\1\uffff\1\72\1\75\1\74\1\77\1\76\4\uffff\1\103"+
        "\20\uffff\1\165\1\164\1\uffff\1\121\1\35\14\uffff\1\30\1\27\12\uffff"+
        "\1\127\1\uffff\1\126\1\53\3\uffff\1\56\1\uffff\1\124\1\123\3\uffff"+
        "\1\104\1\101\52\uffff\1\44\2\uffff\1\131\1\130\4\uffff\1\61\2\uffff"+
        "\1\102\17\uffff\1\145\1\uffff\1\147\7\uffff\1\11\1\12\1\uffff\1"+
        "\14\7\uffff\1\33\1\34\6\uffff\1\50\4\uffff\1\67\1\uffff\1\105\12"+
        "\uffff\1\140\2\uffff\1\144\1\uffff\1\150\2\uffff\1\153\2\uffff\1"+
        "\10\1\13\1\15\1\20\7\uffff\1\40\1\uffff\1\41\1\43\13\uffff\1\125"+
        "\1\uffff\1\135\3\uffff\1\141\3\uffff\1\152\5\uffff\1\25\1\32\1\36"+
        "\4\uffff\1\55\2\uffff\1\100\3\uffff\1\113\1\117\1\132\1\136\1\uffff"+
        "\1\142\1\uffff\1\146\2\uffff\1\7\2\uffff\1\24\1\37\1\42\4\uffff"+
        "\1\110\1\111\4\uffff\1\1\1\21\1\22\1\47\6\uffff\1\151\2\uffff\1"+
        "\62\1\112\1\uffff\1\143\1\54\1\60\2\uffff\1\137";
    static final String DFA32_specialS =
        "\13\uffff\1\0\70\uffff\1\1\u014e\uffff}>";
    static final String[] DFA32_transitionS = {
            "\1\51\1\52\2\51\1\52\22\uffff\1\51\1\36\1\56\2\uffff\1\33\1"+
            "\3\1\56\1\31\1\43\1\34\1\2\1\10\1\12\1\14\1\13\1\54\11\55\1"+
            "\7\1\44\1\32\1\4\1\22\1\40\33\uffff\1\26\1\uffff\1\41\1\20\2"+
            "\uffff\1\1\1\5\1\6\1\11\1\15\1\16\1\21\1\uffff\1\23\2\uffff"+
            "\1\27\1\uffff\1\35\1\uffff\1\37\1\uffff\1\42\1\45\1\46\1\uffff"+
            "\1\47\1\50\3\uffff\1\25\1\30\1\17\1\24\41\uffff\1\51\u15df\uffff"+
            "\1\51\u018d\uffff\1\51\u07f1\uffff\13\51\35\uffff\2\52\5\uffff"+
            "\1\51\57\uffff\1\51\u0fa0\uffff\1\51",
            "\1\57",
            "\1\61\21\uffff\1\60",
            "\1\64\26\uffff\1\63",
            "\1\66",
            "\1\70\2\uffff\1\71\6\uffff\1\72",
            "\1\73\6\uffff\1\74\3\uffff\1\75\2\uffff\1\76",
            "",
            "",
            "\1\77\11\uffff\1\100",
            "\1\101\17\uffff\1\102",
            "\12\110\1\uffff\2\110\1\uffff\34\110\1\105\4\110\1\106\15\110"+
            "\1\104\u1fea\110\2\uffff\udfd6\110",
            "\12\55",
            "\1\112\1\uffff\1\113\11\uffff\1\114",
            "\1\115\7\uffff\1\116\2\uffff\1\117\2\uffff\1\120\5\uffff\1"+
            "\121",
            "\1\122",
            "\1\125\75\uffff\1\124",
            "\1\127",
            "\1\130\1\131",
            "\1\133\6\uffff\1\134\1\135",
            "",
            "",
            "",
            "\1\136",
            "\1\140\76\uffff\1\137",
            "",
            "\1\143\1\142",
            "\1\145",
            "\1\147",
            "\1\151\3\uffff\1\152\17\uffff\1\153",
            "\1\154",
            "\1\156\20\uffff\1\157\2\uffff\1\160",
            "",
            "",
            "\1\161",
            "",
            "",
            "\1\162\13\uffff\1\163\1\164\1\uffff\1\165\1\uffff\1\166",
            "\1\167\11\uffff\1\170\6\uffff\1\171",
            "\1\172\15\uffff\1\173",
            "\1\174\1\175",
            "",
            "",
            "",
            "\10\177\40\uffff\1\176\37\uffff\1\176",
            "",
            "",
            "\1\u0080",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u0081",
            "",
            "\1\u0083",
            "\1\u0084",
            "\1\u0085",
            "\1\u0086\1\u0087",
            "\1\u0088",
            "\1\u0089",
            "\1\u008a",
            "\1\u008b\3\uffff\1\u008c\5\uffff\1\u008d",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\24\53\1\u008e\5\53",
            "",
            "",
            "",
            "\12\110\1\uffff\2\110\1\uffff\u201a\110\2\uffff\udfd6\110",
            "",
            "",
            "",
            "",
            "",
            "\1\u0091",
            "\1\u0092",
            "\1\u0093\3\uffff\1\u0094",
            "\1\u0095",
            "\1\u0096",
            "\1\u0097",
            "\1\u0098",
            "\1\u0099",
            "",
            "",
            "",
            "",
            "",
            "\1\u009a",
            "",
            "\1\u009b\1\u009c",
            "",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u009f",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\22\53\1\u00a0\1\u00a1\6\53",
            "\1\u00a3",
            "",
            "",
            "",
            "",
            "\1\u00a4",
            "",
            "",
            "",
            "",
            "",
            "\1\u00a6",
            "\1\u00a7",
            "\1\u00a8",
            "\1\u00a9",
            "",
            "\1\u00ab",
            "\1\u00ac\5\uffff\1\u00ad",
            "\1\u00ae",
            "\1\u00af",
            "\1\u00b0",
            "\1\u00b1",
            "\1\u00b2",
            "\1\u00b3",
            "\1\u00b4",
            "\1\u00b5\10\uffff\1\u00b6",
            "\1\u00b7\23\uffff\1\u00b8\3\uffff\1\u00b9",
            "\1\u00ba",
            "\1\u00bb",
            "\1\u00bc\2\uffff\1\u00bd",
            "\1\u00be",
            "\1\u00bf",
            "",
            "",
            "\1\u00c0",
            "",
            "",
            "\1\u00c1",
            "\1\u00c2",
            "\1\u00c3",
            "\1\u00c4",
            "\1\u00c5",
            "\1\u00c6",
            "\1\u00c7",
            "\1\u00c8\1\u00c9",
            "\1\u00ca",
            "\1\u00cb",
            "\1\u00cc",
            "\1\u00cd",
            "",
            "",
            "\1\u00ce",
            "\1\u00cf",
            "\1\u00d0",
            "\1\u00d1",
            "\1\u00d2",
            "\1\u00d3",
            "\1\u00d4",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u00d6",
            "\1\u00d7",
            "",
            "\1\u00d8",
            "",
            "",
            "\1\u00da\2\uffff\1\u00db",
            "\1\u00dc",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\4\53\1\u00dd\25\53",
            "",
            "\1\u00df",
            "",
            "",
            "\1\u00e0",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u00e2",
            "",
            "",
            "\1\u00e3",
            "\1\u00e4",
            "\1\u00e5",
            "\1\u00e6",
            "\1\u00e7",
            "\1\u00e8",
            "\1\u00e9",
            "\1\u00ea",
            "\1\u00eb",
            "\1\u00ec",
            "\1\u00ed",
            "\1\u00ee",
            "\1\u00ef",
            "\1\u00f0",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u00f2",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u00f4",
            "\1\u00f5",
            "\1\u00f6",
            "\1\u00f7",
            "\1\u00f8",
            "\1\u00f9",
            "\1\u00fa",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u00fd",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u00ff",
            "\1\u0100",
            "\1\u0101",
            "\1\u0102",
            "\1\u0103",
            "\1\u0104",
            "\1\u0105",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u0108",
            "\1\u0109",
            "\1\u010a",
            "\1\u010b",
            "\1\u010c",
            "",
            "\1\u010d",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "",
            "",
            "\1\u010f",
            "\1\u0110",
            "\1\u0111",
            "\1\u0112",
            "",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u0114",
            "",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u0116",
            "\1\u0117",
            "\1\u0118",
            "\1\u0119",
            "\1\u011a",
            "\1\u011b",
            "\1\u011c",
            "\1\u011d",
            "\1\u011e",
            "\1\u011f",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u0121",
            "\1\u0122",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "",
            "\1\u0124",
            "",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u0126",
            "\1\u0127",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u0129",
            "\1\u012a",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "",
            "",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u012f",
            "\1\u0130",
            "\1\u0131",
            "\1\u0132",
            "\1\u0133",
            "",
            "",
            "\1\u0134",
            "\1\u0135",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\13\53\1\u0137\16\53",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u013a",
            "",
            "\1\u013b",
            "\1\u013c",
            "\1\u013d",
            "\1\u013e",
            "",
            "\1\u013f",
            "",
            "\1\u0140",
            "\1\u0141",
            "\1\u0142",
            "\1\u0143",
            "\1\u0144",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u0146",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u0148",
            "\1\u0149",
            "",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\22\53\1\u014a\7\53",
            "\1\u014c",
            "",
            "\1\u014d",
            "",
            "\1\u014e",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "",
            "\1\u0150",
            "\1\u0151",
            "",
            "",
            "",
            "",
            "\1\u0152",
            "\1\u0153",
            "\1\u0154",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u0158",
            "",
            "\1\u0159",
            "",
            "",
            "\1\u015a",
            "\1\u015b",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u015d",
            "\1\u015e",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u0160",
            "\1\u0161",
            "\1\u0162",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u0167",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "",
            "\1\u0169",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u016b",
            "",
            "\1\u016c",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u016e",
            "\1\u016f",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "",
            "",
            "",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u0173",
            "\1\u0174",
            "",
            "\1\u0175",
            "\1\u0176",
            "",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u0179",
            "",
            "",
            "",
            "",
            "\1\u017a",
            "",
            "\1\u017b",
            "",
            "\1\u017c",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "",
            "",
            "",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u0181",
            "\1\u0182",
            "\1\u0183",
            "",
            "",
            "\1\u0184",
            "\1\u0185",
            "\1\u0186",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "",
            "",
            "",
            "",
            "\1\u0188",
            "\1\u0189",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\u018c",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            "",
            "",
            "\1\u0190",
            "",
            "",
            "",
            "\1\u0191",
            "\1\53\13\uffff\12\53\7\uffff\32\53\1\uffff\1\53\2\uffff\1\53"+
            "\1\uffff\32\53",
            ""
    };

    static final short[] DFA32_eot = DFA.unpackEncodedString(DFA32_eotS);
    static final short[] DFA32_eof = DFA.unpackEncodedString(DFA32_eofS);
    static final char[] DFA32_min = DFA.unpackEncodedStringToUnsignedChars(DFA32_minS);
    static final char[] DFA32_max = DFA.unpackEncodedStringToUnsignedChars(DFA32_maxS);
    static final short[] DFA32_accept = DFA.unpackEncodedString(DFA32_acceptS);
    static final short[] DFA32_special = DFA.unpackEncodedString(DFA32_specialS);
    static final short[][] DFA32_transition;

    static {
        int numStates = DFA32_transitionS.length;
        DFA32_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA32_transition[i] = DFA.unpackEncodedString(DFA32_transitionS[i]);
        }
    }

    class DFA32 extends DFA {

        public DFA32(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 32;
            this.eot = DFA32_eot;
            this.eof = DFA32_eof;
            this.min = DFA32_min;
            this.max = DFA32_max;
            this.accept = DFA32_accept;
            this.special = DFA32_special;
            this.transition = DFA32_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( ABSTRACT | ADD | ADDASS | AND | ANDASS | ASSIGN | BOOLEAN | BREAK | BYTE | CASE | CATCH | CHAR | CLASS | COLON | COMMA | CONST | CONTINUE | DEBUGGER | DEC | DEFAULT | DELETE | DIV | DIVASS | DO | DOT | DOUBLE | ELSE | ENUM | EQ | EXPORT | EXTENDS | FALSE | FINAL | FINALLY | FLOAT | FOR | FSEND | FSSTART | FUNCTION | GOTO | GT | GTE | IF | IMPLEMENTS | IMPORT | IN | INC | INSTANCEOF | INT | INTERFACE | INV | LAND | LBRACE | LBRACK | LONG | LOR | LPAREN | LT | LTE | MOD | MODASS | MUL | MULASS | NATIVE | NEQ | NEW | NOT | NSAME | NULL | OR | ORASS | PACKAGE | PRIVATE | PROTECTED | PUBLIC | QUE | RBRACE | RBRACK | RETURN | RPAREN | SAME | SEMIC | SHL | SHLASS | SHORT | SHR | SHRASS | SHU | SHUASS | STATIC | SUB | SUBASS | SUPER | SWITCH | SYNCHRONIZED | THIS | THROW | THROWS | TRANSIENT | TRUE | TRY | TYPEOF | VAR | VOID | VOLATILE | WHILE | WITH | XOR | XORASS | WhiteSpace | EOL | MultiLineComment | SingleLineComment | Identifier | DecimalLiteral | OctalIntegerLiteral | HexIntegerLiteral | StringLiteral | RegularExpressionLiteral );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA32_11 = input.LA(1);

                         
                        int index32_11 = input.index();
                        input.rewind();

                        s = -1;
                        if ( (LA32_11=='=') ) {s = 68;}

                        else if ( (LA32_11=='*') ) {s = 69;}

                        else if ( (LA32_11=='/') ) {s = 70;}

                        else if ( ((LA32_11 >= '\u0000' && LA32_11 <= '\t')||(LA32_11 >= '\u000B' && LA32_11 <= '\f')||(LA32_11 >= '\u000E' && LA32_11 <= ')')||(LA32_11 >= '+' && LA32_11 <= '.')||(LA32_11 >= '0' && LA32_11 <= '<')||(LA32_11 >= '>' && LA32_11 <= '\u2027')||(LA32_11 >= '\u202A' && LA32_11 <= '\uFFFF')) && (( areRegularExpressionsEnabled() ))) {s = 72;}

                        else s = 71;

                         
                        input.seek(index32_11);

                        if ( s>=0 ) return s;
                        break;

                    case 1 : 
                        int LA32_68 = input.LA(1);

                         
                        int index32_68 = input.index();
                        input.rewind();

                        s = -1;
                        if ( ((LA32_68 >= '\u0000' && LA32_68 <= '\t')||(LA32_68 >= '\u000B' && LA32_68 <= '\f')||(LA32_68 >= '\u000E' && LA32_68 <= '\u2027')||(LA32_68 >= '\u202A' && LA32_68 <= '\uFFFF')) && (( areRegularExpressionsEnabled() ))) {s = 72;}

                        else s = 144;

                         
                        input.seek(index32_68);

                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 32, _s, input);
            error(nvae);
            throw nvae;
        }

    }
 

}