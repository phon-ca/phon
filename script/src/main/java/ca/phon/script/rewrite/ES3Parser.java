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

import org.antlr.runtime.tree.*;


@SuppressWarnings({"all", "warnings", "unchecked"})
public class ES3Parser extends Parser {
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
    public Parser[] getDelegates() {
        return new Parser[] {};
    }

    // delegators


    public ES3Parser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }
    public ES3Parser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

protected TreeAdaptor adaptor = new CommonTreeAdaptor();

public void setTreeAdaptor(TreeAdaptor adaptor) {
    this.adaptor = adaptor;
}
public TreeAdaptor getTreeAdaptor() {
    return adaptor;
}
    public String[] getTokenNames() { return ES3Parser.tokenNames; }
    public String getGrammarFileName() { return "/Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g"; }


    private final boolean isLeftHandSideAssign(RuleReturnScope lhs, Object[] cached)
    {
    	if (cached[0] != null)
    	{
    		return ((Boolean)cached[0]).booleanValue();
    	}
    	
    	boolean result;
    	if (isLeftHandSideExpression(lhs))
    	{
    		switch (input.LA(1))
    		{
    			case ASSIGN:
    			case MULASS:
    			case DIVASS:
    			case MODASS:
    			case ADDASS:
    			case SUBASS:
    			case SHLASS:
    			case SHRASS:
    			case SHUASS:
    			case ANDASS:
    			case XORASS:
    			case ORASS:
    				result = true;
    				break;
    			default:
    				result = false;
    				break;
    		}
    	}
    	else
    	{
    		result = false;
    	}
    	
    	cached[0] = new Boolean(result);
    	return result;
    }

    private final static boolean isLeftHandSideExpression(RuleReturnScope lhs)
    {
    	if (lhs.getTree() == null) // e.g. during backtracking
    	{
    		return true;
    	}
    	else
    	{
    		switch (((Tree)lhs.getTree()).getType())
    		{
    		// primaryExpression
    			case THIS:
    			case Identifier:
    			case NULL:
    			case TRUE:
    			case FALSE:
    			case DecimalLiteral:
    			case OctalIntegerLiteral:
    			case HexIntegerLiteral:
    			case StringLiteral:
    			case RegularExpressionLiteral:
    			case ARRAY:
    			case OBJECT:
    			case PAREXPR:
    		// functionExpression
    			case FUNCTION:
    		// newExpression
    			case NEW:
    		// leftHandSideExpression
    			case CALL:
    			case BYFIELD:
    			case BYINDEX:
    				return true;
    			
    			default:
    				return false;
    		}
    	}
    }
    	
    private final boolean isLeftHandSideIn(RuleReturnScope lhs, Object[] cached)
    {
    	if (cached[0] != null)
    	{
    		return ((Boolean)cached[0]).booleanValue();
    	}
    	
    	boolean result = isLeftHandSideExpression(lhs) && (input.LA(1) == IN);
    	cached[0] = new Boolean(result);
    	return result;
    }

    private final void promoteEOL(ParserRuleReturnScope rule)
    {
    	// Get current token and its type (the possibly offending token).
    	Token lt = input.LT(1);
    	int la = lt.getType();
    	
    	// We only need to promote an EOL when the current token is offending (not a SEMIC, EOF, RBRACE, EOL or MultiLineComment).
    	// EOL and MultiLineComment are not offending as they're already promoted in a previous call to this method.
    	// Promoting an EOL means switching it from off channel to on channel.
    	// A MultiLineComment gets promoted when it contains an EOL.
    	if (!(la == SEMIC || la == EOF || la == RBRACE || la == EOL || la == MultiLineComment))
    	{
    		// Start on the possition before the current token and scan backwards off channel tokens until the previous on channel token.
    		for (int ix = lt.getTokenIndex() - 1; ix > 0; ix--)
    		{
    			lt = input.get(ix);
    			if (lt.getChannel() == Token.DEFAULT_CHANNEL)
    			{
    				// On channel token found: stop scanning.
    				break;
    			}
    			else if (lt.getType() == EOL || (lt.getType() == MultiLineComment && lt.getText().matches("/.*\r\n|\r|\n")))
    			{
    				// We found our EOL: promote the token to on channel, position the input on it and reset the rule start.
    				lt.setChannel(Token.DEFAULT_CHANNEL);
    				input.seek(lt.getTokenIndex());
    				if (rule != null)
    				{
    					rule.start = lt;
    				}
    				break;
    			}
    		}
    	}
    }	


    public static class token_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "token"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:532:1: token : ( reservedWord | Identifier | punctuator | numericLiteral | StringLiteral );
    public final ES3Parser.token_return token() throws RecognitionException {
        ES3Parser.token_return retval = new ES3Parser.token_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token Identifier2=null;
        Token StringLiteral5=null;
        ES3Parser.reservedWord_return reservedWord1 =null;

        ES3Parser.punctuator_return punctuator3 =null;

        ES3Parser.numericLiteral_return numericLiteral4 =null;


        Object Identifier2_tree=null;
        Object StringLiteral5_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:533:2: ( reservedWord | Identifier | punctuator | numericLiteral | StringLiteral )
            int alt1=5;
            switch ( input.LA(1) ) {
            case ABSTRACT:
            case BOOLEAN:
            case BREAK:
            case BYTE:
            case CASE:
            case CATCH:
            case CHAR:
            case CLASS:
            case CONST:
            case CONTINUE:
            case DEBUGGER:
            case DEFAULT:
            case DELETE:
            case DO:
            case DOUBLE:
            case ELSE:
            case ENUM:
            case EXPORT:
            case EXTENDS:
            case FALSE:
            case FINAL:
            case FINALLY:
            case FLOAT:
            case FOR:
            case FUNCTION:
            case GOTO:
            case IF:
            case IMPLEMENTS:
            case IMPORT:
            case IN:
            case INSTANCEOF:
            case INT:
            case INTERFACE:
            case LONG:
            case NATIVE:
            case NEW:
            case NULL:
            case PACKAGE:
            case PRIVATE:
            case PROTECTED:
            case PUBLIC:
            case RETURN:
            case SHORT:
            case STATIC:
            case SUPER:
            case SWITCH:
            case SYNCHRONIZED:
            case THIS:
            case THROW:
            case THROWS:
            case TRANSIENT:
            case TRUE:
            case TRY:
            case TYPEOF:
            case VAR:
            case VOID:
            case VOLATILE:
            case WHILE:
            case WITH:
                {
                alt1=1;
                }
                break;
            case Identifier:
                {
                alt1=2;
                }
                break;
            case ADD:
            case ADDASS:
            case AND:
            case ANDASS:
            case ASSIGN:
            case COLON:
            case COMMA:
            case DEC:
            case DIV:
            case DIVASS:
            case DOT:
            case EQ:
            case GT:
            case GTE:
            case INC:
            case INV:
            case LAND:
            case LBRACE:
            case LBRACK:
            case LOR:
            case LPAREN:
            case LT:
            case LTE:
            case MOD:
            case MODASS:
            case MUL:
            case MULASS:
            case NEQ:
            case NOT:
            case NSAME:
            case OR:
            case ORASS:
            case QUE:
            case RBRACE:
            case RBRACK:
            case RPAREN:
            case SAME:
            case SEMIC:
            case SHL:
            case SHLASS:
            case SHR:
            case SHRASS:
            case SHU:
            case SHUASS:
            case SUB:
            case SUBASS:
            case XOR:
            case XORASS:
                {
                alt1=3;
                }
                break;
            case DecimalLiteral:
            case HexIntegerLiteral:
            case OctalIntegerLiteral:
                {
                alt1=4;
                }
                break;
            case StringLiteral:
                {
                alt1=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;

            }

            switch (alt1) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:533:4: reservedWord
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_reservedWord_in_token1789);
                    reservedWord1=reservedWord();

                    state._fsp--;

                    adaptor.addChild(root_0, reservedWord1.getTree());

                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:534:4: Identifier
                    {
                    root_0 = (Object)adaptor.nil();


                    Identifier2=(Token)match(input,Identifier,FOLLOW_Identifier_in_token1794); 
                    Identifier2_tree = 
                    (Object)adaptor.create(Identifier2)
                    ;
                    adaptor.addChild(root_0, Identifier2_tree);


                    }
                    break;
                case 3 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:535:4: punctuator
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_punctuator_in_token1799);
                    punctuator3=punctuator();

                    state._fsp--;

                    adaptor.addChild(root_0, punctuator3.getTree());

                    }
                    break;
                case 4 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:536:4: numericLiteral
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_numericLiteral_in_token1804);
                    numericLiteral4=numericLiteral();

                    state._fsp--;

                    adaptor.addChild(root_0, numericLiteral4.getTree());

                    }
                    break;
                case 5 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:537:4: StringLiteral
                    {
                    root_0 = (Object)adaptor.nil();


                    StringLiteral5=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_token1809); 
                    StringLiteral5_tree = 
                    (Object)adaptor.create(StringLiteral5)
                    ;
                    adaptor.addChild(root_0, StringLiteral5_tree);


                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "token"


    public static class reservedWord_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "reservedWord"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:542:1: reservedWord : ( keyword | futureReservedWord | NULL | booleanLiteral );
    public final ES3Parser.reservedWord_return reservedWord() throws RecognitionException {
        ES3Parser.reservedWord_return retval = new ES3Parser.reservedWord_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token NULL8=null;
        ES3Parser.keyword_return keyword6 =null;

        ES3Parser.futureReservedWord_return futureReservedWord7 =null;

        ES3Parser.booleanLiteral_return booleanLiteral9 =null;


        Object NULL8_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:543:2: ( keyword | futureReservedWord | NULL | booleanLiteral )
            int alt2=4;
            switch ( input.LA(1) ) {
            case BREAK:
            case CASE:
            case CATCH:
            case CONTINUE:
            case DEFAULT:
            case DELETE:
            case DO:
            case ELSE:
            case FINALLY:
            case FOR:
            case FUNCTION:
            case IF:
            case IN:
            case INSTANCEOF:
            case NEW:
            case RETURN:
            case SWITCH:
            case THIS:
            case THROW:
            case TRY:
            case TYPEOF:
            case VAR:
            case VOID:
            case WHILE:
            case WITH:
                {
                alt2=1;
                }
                break;
            case ABSTRACT:
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case CLASS:
            case CONST:
            case DEBUGGER:
            case DOUBLE:
            case ENUM:
            case EXPORT:
            case EXTENDS:
            case FINAL:
            case FLOAT:
            case GOTO:
            case IMPLEMENTS:
            case IMPORT:
            case INT:
            case INTERFACE:
            case LONG:
            case NATIVE:
            case PACKAGE:
            case PRIVATE:
            case PROTECTED:
            case PUBLIC:
            case SHORT:
            case STATIC:
            case SUPER:
            case SYNCHRONIZED:
            case THROWS:
            case TRANSIENT:
            case VOLATILE:
                {
                alt2=2;
                }
                break;
            case NULL:
                {
                alt2=3;
                }
                break;
            case FALSE:
            case TRUE:
                {
                alt2=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;

            }

            switch (alt2) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:543:4: keyword
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_keyword_in_reservedWord1822);
                    keyword6=keyword();

                    state._fsp--;

                    adaptor.addChild(root_0, keyword6.getTree());

                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:544:4: futureReservedWord
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_futureReservedWord_in_reservedWord1827);
                    futureReservedWord7=futureReservedWord();

                    state._fsp--;

                    adaptor.addChild(root_0, futureReservedWord7.getTree());

                    }
                    break;
                case 3 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:545:4: NULL
                    {
                    root_0 = (Object)adaptor.nil();


                    NULL8=(Token)match(input,NULL,FOLLOW_NULL_in_reservedWord1832); 
                    NULL8_tree = 
                    (Object)adaptor.create(NULL8)
                    ;
                    adaptor.addChild(root_0, NULL8_tree);


                    }
                    break;
                case 4 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:546:4: booleanLiteral
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_booleanLiteral_in_reservedWord1837);
                    booleanLiteral9=booleanLiteral();

                    state._fsp--;

                    adaptor.addChild(root_0, booleanLiteral9.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "reservedWord"


    public static class keyword_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "keyword"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:553:1: keyword : ( BREAK | CASE | CATCH | CONTINUE | DEFAULT | DELETE | DO | ELSE | FINALLY | FOR | FUNCTION | IF | IN | INSTANCEOF | NEW | RETURN | SWITCH | THIS | THROW | TRY | TYPEOF | VAR | VOID | WHILE | WITH );
    public final ES3Parser.keyword_return keyword() throws RecognitionException {
        ES3Parser.keyword_return retval = new ES3Parser.keyword_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set10=null;

        Object set10_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:554:2: ( BREAK | CASE | CATCH | CONTINUE | DEFAULT | DELETE | DO | ELSE | FINALLY | FOR | FUNCTION | IF | IN | INSTANCEOF | NEW | RETURN | SWITCH | THIS | THROW | TRY | TYPEOF | VAR | VOID | WHILE | WITH )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
            {
            root_0 = (Object)adaptor.nil();


            set10=(Token)input.LT(1);

            if ( input.LA(1)==BREAK||(input.LA(1) >= CASE && input.LA(1) <= CATCH)||input.LA(1)==CONTINUE||(input.LA(1) >= DEFAULT && input.LA(1) <= DELETE)||input.LA(1)==DO||input.LA(1)==ELSE||input.LA(1)==FINALLY||input.LA(1)==FOR||input.LA(1)==FUNCTION||input.LA(1)==IF||input.LA(1)==IN||input.LA(1)==INSTANCEOF||input.LA(1)==NEW||input.LA(1)==RETURN||input.LA(1)==SWITCH||(input.LA(1) >= THIS && input.LA(1) <= THROW)||(input.LA(1) >= TRY && input.LA(1) <= TYPEOF)||(input.LA(1) >= VAR && input.LA(1) <= VOID)||(input.LA(1) >= WHILE && input.LA(1) <= WITH) ) {
                input.consume();
                adaptor.addChild(root_0, 
                (Object)adaptor.create(set10)
                );
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "keyword"


    public static class futureReservedWord_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "futureReservedWord"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:585:1: futureReservedWord : ( ABSTRACT | BOOLEAN | BYTE | CHAR | CLASS | CONST | DEBUGGER | DOUBLE | ENUM | EXPORT | EXTENDS | FINAL | FLOAT | GOTO | IMPLEMENTS | IMPORT | INT | INTERFACE | LONG | NATIVE | PACKAGE | PRIVATE | PROTECTED | PUBLIC | SHORT | STATIC | SUPER | SYNCHRONIZED | THROWS | TRANSIENT | VOLATILE );
    public final ES3Parser.futureReservedWord_return futureReservedWord() throws RecognitionException {
        ES3Parser.futureReservedWord_return retval = new ES3Parser.futureReservedWord_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set11=null;

        Object set11_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:586:2: ( ABSTRACT | BOOLEAN | BYTE | CHAR | CLASS | CONST | DEBUGGER | DOUBLE | ENUM | EXPORT | EXTENDS | FINAL | FLOAT | GOTO | IMPLEMENTS | IMPORT | INT | INTERFACE | LONG | NATIVE | PACKAGE | PRIVATE | PROTECTED | PUBLIC | SHORT | STATIC | SUPER | SYNCHRONIZED | THROWS | TRANSIENT | VOLATILE )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
            {
            root_0 = (Object)adaptor.nil();


            set11=(Token)input.LT(1);

            if ( input.LA(1)==ABSTRACT||input.LA(1)==BOOLEAN||input.LA(1)==BYTE||(input.LA(1) >= CHAR && input.LA(1) <= CLASS)||input.LA(1)==CONST||input.LA(1)==DEBUGGER||input.LA(1)==DOUBLE||input.LA(1)==ENUM||input.LA(1)==EXPORT||input.LA(1)==EXTENDS||input.LA(1)==FINAL||input.LA(1)==FLOAT||input.LA(1)==GOTO||(input.LA(1) >= IMPLEMENTS && input.LA(1) <= IMPORT)||(input.LA(1) >= INT && input.LA(1) <= INTERFACE)||input.LA(1)==LONG||input.LA(1)==NATIVE||input.LA(1)==PACKAGE||(input.LA(1) >= PRIVATE && input.LA(1) <= PROTECTED)||input.LA(1)==PUBLIC||input.LA(1)==SHORT||input.LA(1)==STATIC||input.LA(1)==SUPER||input.LA(1)==SYNCHRONIZED||(input.LA(1) >= THROWS && input.LA(1) <= TRANSIENT)||input.LA(1)==VOLATILE ) {
                input.consume();
                adaptor.addChild(root_0, 
                (Object)adaptor.create(set11)
                );
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "futureReservedWord"


    public static class punctuator_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "punctuator"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:663:1: punctuator : ( LBRACE | RBRACE | LPAREN | RPAREN | LBRACK | RBRACK | DOT | SEMIC | COMMA | LT | GT | LTE | GTE | EQ | NEQ | SAME | NSAME | ADD | SUB | MUL | MOD | INC | DEC | SHL | SHR | SHU | AND | OR | XOR | NOT | INV | LAND | LOR | QUE | COLON | ASSIGN | ADDASS | SUBASS | MULASS | MODASS | SHLASS | SHRASS | SHUASS | ANDASS | ORASS | XORASS | DIV | DIVASS );
    public final ES3Parser.punctuator_return punctuator() throws RecognitionException {
        ES3Parser.punctuator_return retval = new ES3Parser.punctuator_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set12=null;

        Object set12_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:664:2: ( LBRACE | RBRACE | LPAREN | RPAREN | LBRACK | RBRACK | DOT | SEMIC | COMMA | LT | GT | LTE | GTE | EQ | NEQ | SAME | NSAME | ADD | SUB | MUL | MOD | INC | DEC | SHL | SHR | SHU | AND | OR | XOR | NOT | INV | LAND | LOR | QUE | COLON | ASSIGN | ADDASS | SUBASS | MULASS | MODASS | SHLASS | SHRASS | SHUASS | ANDASS | ORASS | XORASS | DIV | DIVASS )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
            {
            root_0 = (Object)adaptor.nil();


            set12=(Token)input.LT(1);

            if ( (input.LA(1) >= ADD && input.LA(1) <= ANDASS)||input.LA(1)==ASSIGN||(input.LA(1) >= COLON && input.LA(1) <= COMMA)||input.LA(1)==DEC||(input.LA(1) >= DIV && input.LA(1) <= DIVASS)||input.LA(1)==DOT||input.LA(1)==EQ||(input.LA(1) >= GT && input.LA(1) <= GTE)||input.LA(1)==INC||input.LA(1)==INV||(input.LA(1) >= LAND && input.LA(1) <= LBRACK)||(input.LA(1) >= LOR && input.LA(1) <= LPAREN)||(input.LA(1) >= LT && input.LA(1) <= LTE)||(input.LA(1) >= MOD && input.LA(1) <= MULASS)||input.LA(1)==NEQ||(input.LA(1) >= NOT && input.LA(1) <= NSAME)||(input.LA(1) >= OR && input.LA(1) <= ORASS)||(input.LA(1) >= QUE && input.LA(1) <= RBRACK)||input.LA(1)==RPAREN||(input.LA(1) >= SAME && input.LA(1) <= SHLASS)||(input.LA(1) >= SHR && input.LA(1) <= SHUASS)||(input.LA(1) >= SUB && input.LA(1) <= SUBASS)||(input.LA(1) >= XOR && input.LA(1) <= XORASS) ) {
                input.consume();
                adaptor.addChild(root_0, 
                (Object)adaptor.create(set12)
                );
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "punctuator"


    public static class literal_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "literal"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:718:1: literal : ( NULL | booleanLiteral | numericLiteral | StringLiteral | RegularExpressionLiteral | featureSetLiteral );
    public final ES3Parser.literal_return literal() throws RecognitionException {
        ES3Parser.literal_return retval = new ES3Parser.literal_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token NULL13=null;
        Token StringLiteral16=null;
        Token RegularExpressionLiteral17=null;
        ES3Parser.booleanLiteral_return booleanLiteral14 =null;

        ES3Parser.numericLiteral_return numericLiteral15 =null;

        ES3Parser.featureSetLiteral_return featureSetLiteral18 =null;


        Object NULL13_tree=null;
        Object StringLiteral16_tree=null;
        Object RegularExpressionLiteral17_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:719:2: ( NULL | booleanLiteral | numericLiteral | StringLiteral | RegularExpressionLiteral | featureSetLiteral )
            int alt3=6;
            switch ( input.LA(1) ) {
            case NULL:
                {
                alt3=1;
                }
                break;
            case FALSE:
            case TRUE:
                {
                alt3=2;
                }
                break;
            case DecimalLiteral:
            case HexIntegerLiteral:
            case OctalIntegerLiteral:
                {
                alt3=3;
                }
                break;
            case StringLiteral:
                {
                alt3=4;
                }
                break;
            case RegularExpressionLiteral:
                {
                alt3=5;
                }
                break;
            case FSSTART:
                {
                alt3=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;

            }

            switch (alt3) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:719:4: NULL
                    {
                    root_0 = (Object)adaptor.nil();


                    NULL13=(Token)match(input,NULL,FOLLOW_NULL_in_literal2518); 
                    NULL13_tree = 
                    (Object)adaptor.create(NULL13)
                    ;
                    adaptor.addChild(root_0, NULL13_tree);


                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:720:4: booleanLiteral
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_booleanLiteral_in_literal2523);
                    booleanLiteral14=booleanLiteral();

                    state._fsp--;

                    adaptor.addChild(root_0, booleanLiteral14.getTree());

                    }
                    break;
                case 3 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:721:4: numericLiteral
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_numericLiteral_in_literal2528);
                    numericLiteral15=numericLiteral();

                    state._fsp--;

                    adaptor.addChild(root_0, numericLiteral15.getTree());

                    }
                    break;
                case 4 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:722:4: StringLiteral
                    {
                    root_0 = (Object)adaptor.nil();


                    StringLiteral16=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_literal2533); 
                    StringLiteral16_tree = 
                    (Object)adaptor.create(StringLiteral16)
                    ;
                    adaptor.addChild(root_0, StringLiteral16_tree);


                    }
                    break;
                case 5 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:723:4: RegularExpressionLiteral
                    {
                    root_0 = (Object)adaptor.nil();


                    RegularExpressionLiteral17=(Token)match(input,RegularExpressionLiteral,FOLLOW_RegularExpressionLiteral_in_literal2538); 
                    RegularExpressionLiteral17_tree = 
                    (Object)adaptor.create(RegularExpressionLiteral17)
                    ;
                    adaptor.addChild(root_0, RegularExpressionLiteral17_tree);


                    }
                    break;
                case 6 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:724:4: featureSetLiteral
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_featureSetLiteral_in_literal2543);
                    featureSetLiteral18=featureSetLiteral();

                    state._fsp--;

                    adaptor.addChild(root_0, featureSetLiteral18.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "literal"


    public static class booleanLiteral_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "booleanLiteral"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:727:1: booleanLiteral : ( TRUE | FALSE );
    public final ES3Parser.booleanLiteral_return booleanLiteral() throws RecognitionException {
        ES3Parser.booleanLiteral_return retval = new ES3Parser.booleanLiteral_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set19=null;

        Object set19_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:728:2: ( TRUE | FALSE )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
            {
            root_0 = (Object)adaptor.nil();


            set19=(Token)input.LT(1);

            if ( input.LA(1)==FALSE||input.LA(1)==TRUE ) {
                input.consume();
                adaptor.addChild(root_0, 
                (Object)adaptor.create(set19)
                );
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "booleanLiteral"


    public static class numericLiteral_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "numericLiteral"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:774:1: numericLiteral : ( DecimalLiteral | OctalIntegerLiteral | HexIntegerLiteral );
    public final ES3Parser.numericLiteral_return numericLiteral() throws RecognitionException {
        ES3Parser.numericLiteral_return retval = new ES3Parser.numericLiteral_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set20=null;

        Object set20_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:775:2: ( DecimalLiteral | OctalIntegerLiteral | HexIntegerLiteral )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
            {
            root_0 = (Object)adaptor.nil();


            set20=(Token)input.LT(1);

            if ( input.LA(1)==DecimalLiteral||input.LA(1)==HexIntegerLiteral||input.LA(1)==OctalIntegerLiteral ) {
                input.consume();
                adaptor.addChild(root_0, 
                (Object)adaptor.create(set20)
                );
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "numericLiteral"


    public static class featureSetElement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "featureSetElement"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:853:1: featureSetElement : ( StringLiteral | Identifier );
    public final ES3Parser.featureSetElement_return featureSetElement() throws RecognitionException {
        ES3Parser.featureSetElement_return retval = new ES3Parser.featureSetElement_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set21=null;

        Object set21_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:854:2: ( StringLiteral | Identifier )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
            {
            root_0 = (Object)adaptor.nil();


            set21=(Token)input.LT(1);

            if ( input.LA(1)==Identifier||input.LA(1)==StringLiteral ) {
                input.consume();
                adaptor.addChild(root_0, 
                (Object)adaptor.create(set21)
                );
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "featureSetElement"


    public static class featureSetItem_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "featureSetItem"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:858:1: featureSetItem : (fsele= featureSetElement |{...}?) -> ^( FSELE ( $fsele)? ) ;
    public final ES3Parser.featureSetItem_return featureSetItem() throws RecognitionException {
        ES3Parser.featureSetItem_return retval = new ES3Parser.featureSetItem_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        ES3Parser.featureSetElement_return fsele =null;


        RewriteRuleSubtreeStream stream_featureSetElement=new RewriteRuleSubtreeStream(adaptor,"rule featureSetElement");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:859:2: ( (fsele= featureSetElement |{...}?) -> ^( FSELE ( $fsele)? ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:859:4: (fsele= featureSetElement |{...}?)
            {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:859:4: (fsele= featureSetElement |{...}?)
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==Identifier||LA4_0==StringLiteral) ) {
                alt4=1;
            }
            else if ( (LA4_0==COMMA||LA4_0==FSEND) ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;

            }
            switch (alt4) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:859:6: fsele= featureSetElement
                    {
                    pushFollow(FOLLOW_featureSetElement_in_featureSetItem3168);
                    fsele=featureSetElement();

                    state._fsp--;

                    stream_featureSetElement.add(fsele.getTree());

                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:859:32: {...}?
                    {
                    if ( !(( input.LA(1) == COMMA )) ) {
                        throw new FailedPredicateException(input, "featureSetItem", " input.LA(1) == COMMA ");
                    }

                    }
                    break;

            }


            // AST REWRITE
            // elements: fsele
            // token labels: 
            // rule labels: fsele, retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_fsele=new RewriteRuleSubtreeStream(adaptor,"rule fsele",fsele!=null?fsele.tree:null);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 860:2: -> ^( FSELE ( $fsele)? )
            {
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:860:5: ^( FSELE ( $fsele)? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(FSELE, "FSELE")
                , root_1);

                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:860:14: ( $fsele)?
                if ( stream_fsele.hasNext() ) {
                    adaptor.addChild(root_1, stream_fsele.nextTree());

                }
                stream_fsele.reset();

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "featureSetItem"


    public static class featureSetLiteral_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "featureSetLiteral"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:863:1: featureSetLiteral : fsstart= FSSTART ( featureSetItem ( COMMA featureSetItem )* )? FSEND -> ^( FEATURESET[$fsstart, \"FEATURESET\"] ( featureSetItem )* ) ;
    public final ES3Parser.featureSetLiteral_return featureSetLiteral() throws RecognitionException {
        ES3Parser.featureSetLiteral_return retval = new ES3Parser.featureSetLiteral_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token fsstart=null;
        Token COMMA23=null;
        Token FSEND25=null;
        ES3Parser.featureSetItem_return featureSetItem22 =null;

        ES3Parser.featureSetItem_return featureSetItem24 =null;


        Object fsstart_tree=null;
        Object COMMA23_tree=null;
        Object FSEND25_tree=null;
        RewriteRuleTokenStream stream_FSSTART=new RewriteRuleTokenStream(adaptor,"token FSSTART");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_FSEND=new RewriteRuleTokenStream(adaptor,"token FSEND");
        RewriteRuleSubtreeStream stream_featureSetItem=new RewriteRuleSubtreeStream(adaptor,"rule featureSetItem");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:864:2: (fsstart= FSSTART ( featureSetItem ( COMMA featureSetItem )* )? FSEND -> ^( FEATURESET[$fsstart, \"FEATURESET\"] ( featureSetItem )* ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:864:4: fsstart= FSSTART ( featureSetItem ( COMMA featureSetItem )* )? FSEND
            {
            fsstart=(Token)match(input,FSSTART,FOLLOW_FSSTART_in_featureSetLiteral3199);  
            stream_FSSTART.add(fsstart);


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:864:20: ( featureSetItem ( COMMA featureSetItem )* )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==COMMA||LA6_0==Identifier||LA6_0==StringLiteral) ) {
                alt6=1;
            }
            else if ( (LA6_0==FSEND) ) {
                int LA6_2 = input.LA(2);

                if ( (( input.LA(1) == COMMA )) ) {
                    alt6=1;
                }
            }
            switch (alt6) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:864:22: featureSetItem ( COMMA featureSetItem )*
                    {
                    pushFollow(FOLLOW_featureSetItem_in_featureSetLiteral3203);
                    featureSetItem22=featureSetItem();

                    state._fsp--;

                    stream_featureSetItem.add(featureSetItem22.getTree());

                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:864:37: ( COMMA featureSetItem )*
                    loop5:
                    do {
                        int alt5=2;
                        int LA5_0 = input.LA(1);

                        if ( (LA5_0==COMMA) ) {
                            alt5=1;
                        }


                        switch (alt5) {
                    	case 1 :
                    	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:864:39: COMMA featureSetItem
                    	    {
                    	    COMMA23=(Token)match(input,COMMA,FOLLOW_COMMA_in_featureSetLiteral3207);  
                    	    stream_COMMA.add(COMMA23);


                    	    pushFollow(FOLLOW_featureSetItem_in_featureSetLiteral3209);
                    	    featureSetItem24=featureSetItem();

                    	    state._fsp--;

                    	    stream_featureSetItem.add(featureSetItem24.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop5;
                        }
                    } while (true);


                    }
                    break;

            }


            FSEND25=(Token)match(input,FSEND,FOLLOW_FSEND_in_featureSetLiteral3217);  
            stream_FSEND.add(FSEND25);


            // AST REWRITE
            // elements: featureSetItem
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 865:2: -> ^( FEATURESET[$fsstart, \"FEATURESET\"] ( featureSetItem )* )
            {
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:865:5: ^( FEATURESET[$fsstart, \"FEATURESET\"] ( featureSetItem )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(FEATURESET, fsstart, "FEATURESET")
                , root_1);

                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:865:43: ( featureSetItem )*
                while ( stream_featureSetItem.hasNext() ) {
                    adaptor.addChild(root_1, stream_featureSetItem.nextTree());

                }
                stream_featureSetItem.reset();

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "featureSetLiteral"


    public static class primaryExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "primaryExpression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:880:1: primaryExpression : ( THIS | Identifier | literal | arrayLiteral | objectLiteral |lpar= LPAREN expression RPAREN -> ^( PAREXPR[$lpar, \"PAREXPR\"] expression ) );
    public final ES3Parser.primaryExpression_return primaryExpression() throws RecognitionException {
        ES3Parser.primaryExpression_return retval = new ES3Parser.primaryExpression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token lpar=null;
        Token THIS26=null;
        Token Identifier27=null;
        Token RPAREN32=null;
        ES3Parser.literal_return literal28 =null;

        ES3Parser.arrayLiteral_return arrayLiteral29 =null;

        ES3Parser.objectLiteral_return objectLiteral30 =null;

        ES3Parser.expression_return expression31 =null;


        Object lpar_tree=null;
        Object THIS26_tree=null;
        Object Identifier27_tree=null;
        Object RPAREN32_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:881:2: ( THIS | Identifier | literal | arrayLiteral | objectLiteral |lpar= LPAREN expression RPAREN -> ^( PAREXPR[$lpar, \"PAREXPR\"] expression ) )
            int alt7=6;
            switch ( input.LA(1) ) {
            case THIS:
                {
                alt7=1;
                }
                break;
            case Identifier:
                {
                alt7=2;
                }
                break;
            case DecimalLiteral:
            case FALSE:
            case FSSTART:
            case HexIntegerLiteral:
            case NULL:
            case OctalIntegerLiteral:
            case RegularExpressionLiteral:
            case StringLiteral:
            case TRUE:
                {
                alt7=3;
                }
                break;
            case LBRACK:
                {
                alt7=4;
                }
                break;
            case LBRACE:
                {
                alt7=5;
                }
                break;
            case LPAREN:
                {
                alt7=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;

            }

            switch (alt7) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:881:4: THIS
                    {
                    root_0 = (Object)adaptor.nil();


                    THIS26=(Token)match(input,THIS,FOLLOW_THIS_in_primaryExpression3254); 
                    THIS26_tree = 
                    (Object)adaptor.create(THIS26)
                    ;
                    adaptor.addChild(root_0, THIS26_tree);


                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:882:4: Identifier
                    {
                    root_0 = (Object)adaptor.nil();


                    Identifier27=(Token)match(input,Identifier,FOLLOW_Identifier_in_primaryExpression3259); 
                    Identifier27_tree = 
                    (Object)adaptor.create(Identifier27)
                    ;
                    adaptor.addChild(root_0, Identifier27_tree);


                    }
                    break;
                case 3 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:883:4: literal
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_literal_in_primaryExpression3264);
                    literal28=literal();

                    state._fsp--;

                    adaptor.addChild(root_0, literal28.getTree());

                    }
                    break;
                case 4 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:884:4: arrayLiteral
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_arrayLiteral_in_primaryExpression3269);
                    arrayLiteral29=arrayLiteral();

                    state._fsp--;

                    adaptor.addChild(root_0, arrayLiteral29.getTree());

                    }
                    break;
                case 5 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:885:4: objectLiteral
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_objectLiteral_in_primaryExpression3274);
                    objectLiteral30=objectLiteral();

                    state._fsp--;

                    adaptor.addChild(root_0, objectLiteral30.getTree());

                    }
                    break;
                case 6 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:886:4: lpar= LPAREN expression RPAREN
                    {
                    lpar=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_primaryExpression3281);  
                    stream_LPAREN.add(lpar);


                    pushFollow(FOLLOW_expression_in_primaryExpression3283);
                    expression31=expression();

                    state._fsp--;

                    stream_expression.add(expression31.getTree());

                    RPAREN32=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_primaryExpression3285);  
                    stream_RPAREN.add(RPAREN32);


                    // AST REWRITE
                    // elements: expression
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 886:34: -> ^( PAREXPR[$lpar, \"PAREXPR\"] expression )
                    {
                        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:886:37: ^( PAREXPR[$lpar, \"PAREXPR\"] expression )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(PAREXPR, lpar, "PAREXPR")
                        , root_1);

                        adaptor.addChild(root_1, stream_expression.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "primaryExpression"


    public static class arrayLiteral_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "arrayLiteral"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:889:1: arrayLiteral : lb= LBRACK ( arrayItem ( COMMA arrayItem )* )? RBRACK -> ^( ARRAY[$lb, \"ARRAY\"] ( arrayItem )* ) ;
    public final ES3Parser.arrayLiteral_return arrayLiteral() throws RecognitionException {
        ES3Parser.arrayLiteral_return retval = new ES3Parser.arrayLiteral_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token lb=null;
        Token COMMA34=null;
        Token RBRACK36=null;
        ES3Parser.arrayItem_return arrayItem33 =null;

        ES3Parser.arrayItem_return arrayItem35 =null;


        Object lb_tree=null;
        Object COMMA34_tree=null;
        Object RBRACK36_tree=null;
        RewriteRuleTokenStream stream_RBRACK=new RewriteRuleTokenStream(adaptor,"token RBRACK");
        RewriteRuleTokenStream stream_LBRACK=new RewriteRuleTokenStream(adaptor,"token LBRACK");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_arrayItem=new RewriteRuleSubtreeStream(adaptor,"rule arrayItem");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:890:2: (lb= LBRACK ( arrayItem ( COMMA arrayItem )* )? RBRACK -> ^( ARRAY[$lb, \"ARRAY\"] ( arrayItem )* ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:890:4: lb= LBRACK ( arrayItem ( COMMA arrayItem )* )? RBRACK
            {
            lb=(Token)match(input,LBRACK,FOLLOW_LBRACK_in_arrayLiteral3309);  
            stream_LBRACK.add(lb);


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:890:14: ( arrayItem ( COMMA arrayItem )* )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==ADD||LA9_0==COMMA||LA9_0==DEC||LA9_0==DELETE||LA9_0==DecimalLiteral||LA9_0==FALSE||(LA9_0 >= FSSTART && LA9_0 <= FUNCTION)||LA9_0==HexIntegerLiteral||LA9_0==INC||LA9_0==INV||LA9_0==Identifier||(LA9_0 >= LBRACE && LA9_0 <= LBRACK)||LA9_0==LPAREN||(LA9_0 >= NEW && LA9_0 <= NOT)||LA9_0==NULL||LA9_0==OctalIntegerLiteral||LA9_0==RegularExpressionLiteral||LA9_0==SUB||LA9_0==StringLiteral||LA9_0==THIS||LA9_0==TRUE||LA9_0==TYPEOF||LA9_0==VOID) ) {
                alt9=1;
            }
            else if ( (LA9_0==RBRACK) ) {
                int LA9_2 = input.LA(2);

                if ( (( input.LA(1) == COMMA )) ) {
                    alt9=1;
                }
            }
            switch (alt9) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:890:16: arrayItem ( COMMA arrayItem )*
                    {
                    pushFollow(FOLLOW_arrayItem_in_arrayLiteral3313);
                    arrayItem33=arrayItem();

                    state._fsp--;

                    stream_arrayItem.add(arrayItem33.getTree());

                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:890:26: ( COMMA arrayItem )*
                    loop8:
                    do {
                        int alt8=2;
                        int LA8_0 = input.LA(1);

                        if ( (LA8_0==COMMA) ) {
                            alt8=1;
                        }


                        switch (alt8) {
                    	case 1 :
                    	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:890:28: COMMA arrayItem
                    	    {
                    	    COMMA34=(Token)match(input,COMMA,FOLLOW_COMMA_in_arrayLiteral3317);  
                    	    stream_COMMA.add(COMMA34);


                    	    pushFollow(FOLLOW_arrayItem_in_arrayLiteral3319);
                    	    arrayItem35=arrayItem();

                    	    state._fsp--;

                    	    stream_arrayItem.add(arrayItem35.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop8;
                        }
                    } while (true);


                    }
                    break;

            }


            RBRACK36=(Token)match(input,RBRACK,FOLLOW_RBRACK_in_arrayLiteral3327);  
            stream_RBRACK.add(RBRACK36);


            // AST REWRITE
            // elements: arrayItem
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 891:2: -> ^( ARRAY[$lb, \"ARRAY\"] ( arrayItem )* )
            {
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:891:5: ^( ARRAY[$lb, \"ARRAY\"] ( arrayItem )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(ARRAY, lb, "ARRAY")
                , root_1);

                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:891:28: ( arrayItem )*
                while ( stream_arrayItem.hasNext() ) {
                    adaptor.addChild(root_1, stream_arrayItem.nextTree());

                }
                stream_arrayItem.reset();

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "arrayLiteral"


    public static class arrayItem_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "arrayItem"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:894:1: arrayItem : (expr= assignmentExpression |{...}?) -> ^( ITEM ( $expr)? ) ;
    public final ES3Parser.arrayItem_return arrayItem() throws RecognitionException {
        ES3Parser.arrayItem_return retval = new ES3Parser.arrayItem_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        ES3Parser.assignmentExpression_return expr =null;


        RewriteRuleSubtreeStream stream_assignmentExpression=new RewriteRuleSubtreeStream(adaptor,"rule assignmentExpression");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:895:2: ( (expr= assignmentExpression |{...}?) -> ^( ITEM ( $expr)? ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:895:4: (expr= assignmentExpression |{...}?)
            {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:895:4: (expr= assignmentExpression |{...}?)
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==ADD||LA10_0==DEC||LA10_0==DELETE||LA10_0==DecimalLiteral||LA10_0==FALSE||(LA10_0 >= FSSTART && LA10_0 <= FUNCTION)||LA10_0==HexIntegerLiteral||LA10_0==INC||LA10_0==INV||LA10_0==Identifier||(LA10_0 >= LBRACE && LA10_0 <= LBRACK)||LA10_0==LPAREN||(LA10_0 >= NEW && LA10_0 <= NOT)||LA10_0==NULL||LA10_0==OctalIntegerLiteral||LA10_0==RegularExpressionLiteral||LA10_0==SUB||LA10_0==StringLiteral||LA10_0==THIS||LA10_0==TRUE||LA10_0==TYPEOF||LA10_0==VOID) ) {
                alt10=1;
            }
            else if ( (LA10_0==COMMA||LA10_0==RBRACK) ) {
                alt10=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;

            }
            switch (alt10) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:895:6: expr= assignmentExpression
                    {
                    pushFollow(FOLLOW_assignmentExpression_in_arrayItem3355);
                    expr=assignmentExpression();

                    state._fsp--;

                    stream_assignmentExpression.add(expr.getTree());

                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:895:34: {...}?
                    {
                    if ( !(( input.LA(1) == COMMA )) ) {
                        throw new FailedPredicateException(input, "arrayItem", " input.LA(1) == COMMA ");
                    }

                    }
                    break;

            }


            // AST REWRITE
            // elements: expr
            // token labels: 
            // rule labels: retval, expr
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_expr=new RewriteRuleSubtreeStream(adaptor,"rule expr",expr!=null?expr.tree:null);

            root_0 = (Object)adaptor.nil();
            // 896:2: -> ^( ITEM ( $expr)? )
            {
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:896:5: ^( ITEM ( $expr)? )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(ITEM, "ITEM")
                , root_1);

                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:896:14: ( $expr)?
                if ( stream_expr.hasNext() ) {
                    adaptor.addChild(root_1, stream_expr.nextTree());

                }
                stream_expr.reset();

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "arrayItem"


    public static class objectLiteral_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "objectLiteral"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:899:1: objectLiteral : lb= LBRACE ( nameValuePair ( COMMA nameValuePair )* )? RBRACE -> ^( OBJECT[$lb, \"OBJECT\"] ( nameValuePair )* ) ;
    public final ES3Parser.objectLiteral_return objectLiteral() throws RecognitionException {
        ES3Parser.objectLiteral_return retval = new ES3Parser.objectLiteral_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token lb=null;
        Token COMMA38=null;
        Token RBRACE40=null;
        ES3Parser.nameValuePair_return nameValuePair37 =null;

        ES3Parser.nameValuePair_return nameValuePair39 =null;


        Object lb_tree=null;
        Object COMMA38_tree=null;
        Object RBRACE40_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_nameValuePair=new RewriteRuleSubtreeStream(adaptor,"rule nameValuePair");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:900:2: (lb= LBRACE ( nameValuePair ( COMMA nameValuePair )* )? RBRACE -> ^( OBJECT[$lb, \"OBJECT\"] ( nameValuePair )* ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:900:4: lb= LBRACE ( nameValuePair ( COMMA nameValuePair )* )? RBRACE
            {
            lb=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_objectLiteral3387);  
            stream_LBRACE.add(lb);


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:900:14: ( nameValuePair ( COMMA nameValuePair )* )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==DecimalLiteral||LA12_0==HexIntegerLiteral||LA12_0==Identifier||LA12_0==OctalIntegerLiteral||LA12_0==StringLiteral) ) {
                alt12=1;
            }
            switch (alt12) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:900:16: nameValuePair ( COMMA nameValuePair )*
                    {
                    pushFollow(FOLLOW_nameValuePair_in_objectLiteral3391);
                    nameValuePair37=nameValuePair();

                    state._fsp--;

                    stream_nameValuePair.add(nameValuePair37.getTree());

                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:900:30: ( COMMA nameValuePair )*
                    loop11:
                    do {
                        int alt11=2;
                        int LA11_0 = input.LA(1);

                        if ( (LA11_0==COMMA) ) {
                            alt11=1;
                        }


                        switch (alt11) {
                    	case 1 :
                    	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:900:32: COMMA nameValuePair
                    	    {
                    	    COMMA38=(Token)match(input,COMMA,FOLLOW_COMMA_in_objectLiteral3395);  
                    	    stream_COMMA.add(COMMA38);


                    	    pushFollow(FOLLOW_nameValuePair_in_objectLiteral3397);
                    	    nameValuePair39=nameValuePair();

                    	    state._fsp--;

                    	    stream_nameValuePair.add(nameValuePair39.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop11;
                        }
                    } while (true);


                    }
                    break;

            }


            RBRACE40=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_objectLiteral3405);  
            stream_RBRACE.add(RBRACE40);


            // AST REWRITE
            // elements: nameValuePair
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 901:2: -> ^( OBJECT[$lb, \"OBJECT\"] ( nameValuePair )* )
            {
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:901:5: ^( OBJECT[$lb, \"OBJECT\"] ( nameValuePair )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(OBJECT, lb, "OBJECT")
                , root_1);

                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:901:30: ( nameValuePair )*
                while ( stream_nameValuePair.hasNext() ) {
                    adaptor.addChild(root_1, stream_nameValuePair.nextTree());

                }
                stream_nameValuePair.reset();

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "objectLiteral"


    public static class nameValuePair_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "nameValuePair"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:904:1: nameValuePair : propertyName COLON assignmentExpression -> ^( NAMEDVALUE propertyName assignmentExpression ) ;
    public final ES3Parser.nameValuePair_return nameValuePair() throws RecognitionException {
        ES3Parser.nameValuePair_return retval = new ES3Parser.nameValuePair_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token COLON42=null;
        ES3Parser.propertyName_return propertyName41 =null;

        ES3Parser.assignmentExpression_return assignmentExpression43 =null;


        Object COLON42_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleSubtreeStream stream_propertyName=new RewriteRuleSubtreeStream(adaptor,"rule propertyName");
        RewriteRuleSubtreeStream stream_assignmentExpression=new RewriteRuleSubtreeStream(adaptor,"rule assignmentExpression");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:905:2: ( propertyName COLON assignmentExpression -> ^( NAMEDVALUE propertyName assignmentExpression ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:905:4: propertyName COLON assignmentExpression
            {
            pushFollow(FOLLOW_propertyName_in_nameValuePair3430);
            propertyName41=propertyName();

            state._fsp--;

            stream_propertyName.add(propertyName41.getTree());

            COLON42=(Token)match(input,COLON,FOLLOW_COLON_in_nameValuePair3432);  
            stream_COLON.add(COLON42);


            pushFollow(FOLLOW_assignmentExpression_in_nameValuePair3434);
            assignmentExpression43=assignmentExpression();

            state._fsp--;

            stream_assignmentExpression.add(assignmentExpression43.getTree());

            // AST REWRITE
            // elements: propertyName, assignmentExpression
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 906:2: -> ^( NAMEDVALUE propertyName assignmentExpression )
            {
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:906:5: ^( NAMEDVALUE propertyName assignmentExpression )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(NAMEDVALUE, "NAMEDVALUE")
                , root_1);

                adaptor.addChild(root_1, stream_propertyName.nextTree());

                adaptor.addChild(root_1, stream_assignmentExpression.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "nameValuePair"


    public static class propertyName_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "propertyName"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:909:1: propertyName : ( Identifier | StringLiteral | numericLiteral );
    public final ES3Parser.propertyName_return propertyName() throws RecognitionException {
        ES3Parser.propertyName_return retval = new ES3Parser.propertyName_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token Identifier44=null;
        Token StringLiteral45=null;
        ES3Parser.numericLiteral_return numericLiteral46 =null;


        Object Identifier44_tree=null;
        Object StringLiteral45_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:910:2: ( Identifier | StringLiteral | numericLiteral )
            int alt13=3;
            switch ( input.LA(1) ) {
            case Identifier:
                {
                alt13=1;
                }
                break;
            case StringLiteral:
                {
                alt13=2;
                }
                break;
            case DecimalLiteral:
            case HexIntegerLiteral:
            case OctalIntegerLiteral:
                {
                alt13=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;

            }

            switch (alt13) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:910:4: Identifier
                    {
                    root_0 = (Object)adaptor.nil();


                    Identifier44=(Token)match(input,Identifier,FOLLOW_Identifier_in_propertyName3458); 
                    Identifier44_tree = 
                    (Object)adaptor.create(Identifier44)
                    ;
                    adaptor.addChild(root_0, Identifier44_tree);


                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:911:4: StringLiteral
                    {
                    root_0 = (Object)adaptor.nil();


                    StringLiteral45=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_propertyName3463); 
                    StringLiteral45_tree = 
                    (Object)adaptor.create(StringLiteral45)
                    ;
                    adaptor.addChild(root_0, StringLiteral45_tree);


                    }
                    break;
                case 3 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:912:4: numericLiteral
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_numericLiteral_in_propertyName3468);
                    numericLiteral46=numericLiteral();

                    state._fsp--;

                    adaptor.addChild(root_0, numericLiteral46.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "propertyName"


    public static class memberExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "memberExpression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:924:1: memberExpression : ( primaryExpression | functionExpression | newExpression );
    public final ES3Parser.memberExpression_return memberExpression() throws RecognitionException {
        ES3Parser.memberExpression_return retval = new ES3Parser.memberExpression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        ES3Parser.primaryExpression_return primaryExpression47 =null;

        ES3Parser.functionExpression_return functionExpression48 =null;

        ES3Parser.newExpression_return newExpression49 =null;



        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:925:2: ( primaryExpression | functionExpression | newExpression )
            int alt14=3;
            switch ( input.LA(1) ) {
            case DecimalLiteral:
            case FALSE:
            case FSSTART:
            case HexIntegerLiteral:
            case Identifier:
            case LBRACE:
            case LBRACK:
            case LPAREN:
            case NULL:
            case OctalIntegerLiteral:
            case RegularExpressionLiteral:
            case StringLiteral:
            case THIS:
            case TRUE:
                {
                alt14=1;
                }
                break;
            case FUNCTION:
                {
                alt14=2;
                }
                break;
            case NEW:
                {
                alt14=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;

            }

            switch (alt14) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:925:4: primaryExpression
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_primaryExpression_in_memberExpression3486);
                    primaryExpression47=primaryExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, primaryExpression47.getTree());

                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:926:4: functionExpression
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_functionExpression_in_memberExpression3491);
                    functionExpression48=functionExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, functionExpression48.getTree());

                    }
                    break;
                case 3 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:927:4: newExpression
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_newExpression_in_memberExpression3496);
                    newExpression49=newExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, newExpression49.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "memberExpression"


    public static class newExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "newExpression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:930:1: newExpression : NEW ^ primaryExpression ;
    public final ES3Parser.newExpression_return newExpression() throws RecognitionException {
        ES3Parser.newExpression_return retval = new ES3Parser.newExpression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token NEW50=null;
        ES3Parser.primaryExpression_return primaryExpression51 =null;


        Object NEW50_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:931:2: ( NEW ^ primaryExpression )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:931:4: NEW ^ primaryExpression
            {
            root_0 = (Object)adaptor.nil();


            NEW50=(Token)match(input,NEW,FOLLOW_NEW_in_newExpression3507); 
            NEW50_tree = 
            (Object)adaptor.create(NEW50)
            ;
            root_0 = (Object)adaptor.becomeRoot(NEW50_tree, root_0);


            pushFollow(FOLLOW_primaryExpression_in_newExpression3510);
            primaryExpression51=primaryExpression();

            state._fsp--;

            adaptor.addChild(root_0, primaryExpression51.getTree());

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "newExpression"


    public static class arguments_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "arguments"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:935:1: arguments : LPAREN ( assignmentExpression ( COMMA assignmentExpression )* )? RPAREN -> ^( ARGS ( assignmentExpression )* ) ;
    public final ES3Parser.arguments_return arguments() throws RecognitionException {
        ES3Parser.arguments_return retval = new ES3Parser.arguments_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token LPAREN52=null;
        Token COMMA54=null;
        Token RPAREN56=null;
        ES3Parser.assignmentExpression_return assignmentExpression53 =null;

        ES3Parser.assignmentExpression_return assignmentExpression55 =null;


        Object LPAREN52_tree=null;
        Object COMMA54_tree=null;
        Object RPAREN56_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_assignmentExpression=new RewriteRuleSubtreeStream(adaptor,"rule assignmentExpression");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:936:2: ( LPAREN ( assignmentExpression ( COMMA assignmentExpression )* )? RPAREN -> ^( ARGS ( assignmentExpression )* ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:936:4: LPAREN ( assignmentExpression ( COMMA assignmentExpression )* )? RPAREN
            {
            LPAREN52=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_arguments3523);  
            stream_LPAREN.add(LPAREN52);


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:936:11: ( assignmentExpression ( COMMA assignmentExpression )* )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==ADD||LA16_0==DEC||LA16_0==DELETE||LA16_0==DecimalLiteral||LA16_0==FALSE||(LA16_0 >= FSSTART && LA16_0 <= FUNCTION)||LA16_0==HexIntegerLiteral||LA16_0==INC||LA16_0==INV||LA16_0==Identifier||(LA16_0 >= LBRACE && LA16_0 <= LBRACK)||LA16_0==LPAREN||(LA16_0 >= NEW && LA16_0 <= NOT)||LA16_0==NULL||LA16_0==OctalIntegerLiteral||LA16_0==RegularExpressionLiteral||LA16_0==SUB||LA16_0==StringLiteral||LA16_0==THIS||LA16_0==TRUE||LA16_0==TYPEOF||LA16_0==VOID) ) {
                alt16=1;
            }
            switch (alt16) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:936:13: assignmentExpression ( COMMA assignmentExpression )*
                    {
                    pushFollow(FOLLOW_assignmentExpression_in_arguments3527);
                    assignmentExpression53=assignmentExpression();

                    state._fsp--;

                    stream_assignmentExpression.add(assignmentExpression53.getTree());

                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:936:34: ( COMMA assignmentExpression )*
                    loop15:
                    do {
                        int alt15=2;
                        int LA15_0 = input.LA(1);

                        if ( (LA15_0==COMMA) ) {
                            alt15=1;
                        }


                        switch (alt15) {
                    	case 1 :
                    	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:936:36: COMMA assignmentExpression
                    	    {
                    	    COMMA54=(Token)match(input,COMMA,FOLLOW_COMMA_in_arguments3531);  
                    	    stream_COMMA.add(COMMA54);


                    	    pushFollow(FOLLOW_assignmentExpression_in_arguments3533);
                    	    assignmentExpression55=assignmentExpression();

                    	    state._fsp--;

                    	    stream_assignmentExpression.add(assignmentExpression55.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop15;
                        }
                    } while (true);


                    }
                    break;

            }


            RPAREN56=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_arguments3541);  
            stream_RPAREN.add(RPAREN56);


            // AST REWRITE
            // elements: assignmentExpression
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 937:2: -> ^( ARGS ( assignmentExpression )* )
            {
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:937:5: ^( ARGS ( assignmentExpression )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(ARGS, "ARGS")
                , root_1);

                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:937:13: ( assignmentExpression )*
                while ( stream_assignmentExpression.hasNext() ) {
                    adaptor.addChild(root_1, stream_assignmentExpression.nextTree());

                }
                stream_assignmentExpression.reset();

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "arguments"


    public static class leftHandSideExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "leftHandSideExpression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:940:1: leftHandSideExpression : ( memberExpression -> memberExpression ) ( arguments -> ^( CALL $leftHandSideExpression arguments ) | LBRACK expression RBRACK -> ^( BYINDEX $leftHandSideExpression expression ) | DOT Identifier -> ^( BYFIELD $leftHandSideExpression Identifier ) )* ;
    public final ES3Parser.leftHandSideExpression_return leftHandSideExpression() throws RecognitionException {
        ES3Parser.leftHandSideExpression_return retval = new ES3Parser.leftHandSideExpression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token LBRACK59=null;
        Token RBRACK61=null;
        Token DOT62=null;
        Token Identifier63=null;
        ES3Parser.memberExpression_return memberExpression57 =null;

        ES3Parser.arguments_return arguments58 =null;

        ES3Parser.expression_return expression60 =null;


        Object LBRACK59_tree=null;
        Object RBRACK61_tree=null;
        Object DOT62_tree=null;
        Object Identifier63_tree=null;
        RewriteRuleTokenStream stream_RBRACK=new RewriteRuleTokenStream(adaptor,"token RBRACK");
        RewriteRuleTokenStream stream_LBRACK=new RewriteRuleTokenStream(adaptor,"token LBRACK");
        RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
        RewriteRuleTokenStream stream_Identifier=new RewriteRuleTokenStream(adaptor,"token Identifier");
        RewriteRuleSubtreeStream stream_memberExpression=new RewriteRuleSubtreeStream(adaptor,"rule memberExpression");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        RewriteRuleSubtreeStream stream_arguments=new RewriteRuleSubtreeStream(adaptor,"rule arguments");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:941:2: ( ( memberExpression -> memberExpression ) ( arguments -> ^( CALL $leftHandSideExpression arguments ) | LBRACK expression RBRACK -> ^( BYINDEX $leftHandSideExpression expression ) | DOT Identifier -> ^( BYFIELD $leftHandSideExpression Identifier ) )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:942:2: ( memberExpression -> memberExpression ) ( arguments -> ^( CALL $leftHandSideExpression arguments ) | LBRACK expression RBRACK -> ^( BYINDEX $leftHandSideExpression expression ) | DOT Identifier -> ^( BYFIELD $leftHandSideExpression Identifier ) )*
            {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:942:2: ( memberExpression -> memberExpression )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:943:3: memberExpression
            {
            pushFollow(FOLLOW_memberExpression_in_leftHandSideExpression3570);
            memberExpression57=memberExpression();

            state._fsp--;

            stream_memberExpression.add(memberExpression57.getTree());

            // AST REWRITE
            // elements: memberExpression
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 943:22: -> memberExpression
            {
                adaptor.addChild(root_0, stream_memberExpression.nextTree());

            }


            retval.tree = root_0;

            }


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:945:2: ( arguments -> ^( CALL $leftHandSideExpression arguments ) | LBRACK expression RBRACK -> ^( BYINDEX $leftHandSideExpression expression ) | DOT Identifier -> ^( BYFIELD $leftHandSideExpression Identifier ) )*
            loop17:
            do {
                int alt17=4;
                switch ( input.LA(1) ) {
                case LPAREN:
                    {
                    alt17=1;
                    }
                    break;
                case LBRACK:
                    {
                    alt17=2;
                    }
                    break;
                case DOT:
                    {
                    alt17=3;
                    }
                    break;

                }

                switch (alt17) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:946:3: arguments
            	    {
            	    pushFollow(FOLLOW_arguments_in_leftHandSideExpression3586);
            	    arguments58=arguments();

            	    state._fsp--;

            	    stream_arguments.add(arguments58.getTree());

            	    // AST REWRITE
            	    // elements: leftHandSideExpression, arguments
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (Object)adaptor.nil();
            	    // 946:15: -> ^( CALL $leftHandSideExpression arguments )
            	    {
            	        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:946:18: ^( CALL $leftHandSideExpression arguments )
            	        {
            	        Object root_1 = (Object)adaptor.nil();
            	        root_1 = (Object)adaptor.becomeRoot(
            	        (Object)adaptor.create(CALL, "CALL")
            	        , root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());

            	        adaptor.addChild(root_1, stream_arguments.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }


            	    retval.tree = root_0;

            	    }
            	    break;
            	case 2 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:947:5: LBRACK expression RBRACK
            	    {
            	    LBRACK59=(Token)match(input,LBRACK,FOLLOW_LBRACK_in_leftHandSideExpression3607);  
            	    stream_LBRACK.add(LBRACK59);


            	    pushFollow(FOLLOW_expression_in_leftHandSideExpression3609);
            	    expression60=expression();

            	    state._fsp--;

            	    stream_expression.add(expression60.getTree());

            	    RBRACK61=(Token)match(input,RBRACK,FOLLOW_RBRACK_in_leftHandSideExpression3611);  
            	    stream_RBRACK.add(RBRACK61);


            	    // AST REWRITE
            	    // elements: expression, leftHandSideExpression
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (Object)adaptor.nil();
            	    // 947:30: -> ^( BYINDEX $leftHandSideExpression expression )
            	    {
            	        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:947:33: ^( BYINDEX $leftHandSideExpression expression )
            	        {
            	        Object root_1 = (Object)adaptor.nil();
            	        root_1 = (Object)adaptor.becomeRoot(
            	        (Object)adaptor.create(BYINDEX, "BYINDEX")
            	        , root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());

            	        adaptor.addChild(root_1, stream_expression.nextTree());

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }


            	    retval.tree = root_0;

            	    }
            	    break;
            	case 3 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:948:5: DOT Identifier
            	    {
            	    DOT62=(Token)match(input,DOT,FOLLOW_DOT_in_leftHandSideExpression3630);  
            	    stream_DOT.add(DOT62);


            	    Identifier63=(Token)match(input,Identifier,FOLLOW_Identifier_in_leftHandSideExpression3632);  
            	    stream_Identifier.add(Identifier63);


            	    // AST REWRITE
            	    // elements: Identifier, leftHandSideExpression
            	    // token labels: 
            	    // rule labels: retval
            	    // token list labels: 
            	    // rule list labels: 
            	    // wildcard labels: 
            	    retval.tree = root_0;
            	    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            	    root_0 = (Object)adaptor.nil();
            	    // 948:21: -> ^( BYFIELD $leftHandSideExpression Identifier )
            	    {
            	        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:948:24: ^( BYFIELD $leftHandSideExpression Identifier )
            	        {
            	        Object root_1 = (Object)adaptor.nil();
            	        root_1 = (Object)adaptor.becomeRoot(
            	        (Object)adaptor.create(BYFIELD, "BYFIELD")
            	        , root_1);

            	        adaptor.addChild(root_1, stream_retval.nextTree());

            	        adaptor.addChild(root_1, 
            	        stream_Identifier.nextNode()
            	        );

            	        adaptor.addChild(root_0, root_1);
            	        }

            	    }


            	    retval.tree = root_0;

            	    }
            	    break;

            	default :
            	    break loop17;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "leftHandSideExpression"


    public static class postfixExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "postfixExpression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:962:1: postfixExpression : leftHandSideExpression ( postfixOperator ^)? ;
    public final ES3Parser.postfixExpression_return postfixExpression() throws RecognitionException {
        ES3Parser.postfixExpression_return retval = new ES3Parser.postfixExpression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        ES3Parser.leftHandSideExpression_return leftHandSideExpression64 =null;

        ES3Parser.postfixOperator_return postfixOperator65 =null;



        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:963:2: ( leftHandSideExpression ( postfixOperator ^)? )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:963:4: leftHandSideExpression ( postfixOperator ^)?
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_leftHandSideExpression_in_postfixExpression3667);
            leftHandSideExpression64=leftHandSideExpression();

            state._fsp--;

            adaptor.addChild(root_0, leftHandSideExpression64.getTree());

             if (input.LA(1) == INC || input.LA(1) == DEC) promoteEOL(null); 

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:963:95: ( postfixOperator ^)?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==DEC||LA18_0==INC) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:963:97: postfixOperator ^
                    {
                    pushFollow(FOLLOW_postfixOperator_in_postfixExpression3673);
                    postfixOperator65=postfixOperator();

                    state._fsp--;

                    root_0 = (Object)adaptor.becomeRoot(postfixOperator65.getTree(), root_0);

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "postfixExpression"


    public static class postfixOperator_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "postfixOperator"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:966:1: postfixOperator : (op= INC |op= DEC );
    public final ES3Parser.postfixOperator_return postfixOperator() throws RecognitionException {
        ES3Parser.postfixOperator_return retval = new ES3Parser.postfixOperator_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token op=null;

        Object op_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:967:2: (op= INC |op= DEC )
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==INC) ) {
                alt19=1;
            }
            else if ( (LA19_0==DEC) ) {
                alt19=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;

            }
            switch (alt19) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:967:4: op= INC
                    {
                    root_0 = (Object)adaptor.nil();


                    op=(Token)match(input,INC,FOLLOW_INC_in_postfixOperator3691); 
                    op_tree = 
                    (Object)adaptor.create(op)
                    ;
                    adaptor.addChild(root_0, op_tree);


                     op.setType(PINC); 

                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:968:4: op= DEC
                    {
                    root_0 = (Object)adaptor.nil();


                    op=(Token)match(input,DEC,FOLLOW_DEC_in_postfixOperator3700); 
                    op_tree = 
                    (Object)adaptor.create(op)
                    ;
                    adaptor.addChild(root_0, op_tree);


                     op.setType(PDEC); 

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "postfixOperator"


    public static class unaryExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "unaryExpression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:975:1: unaryExpression : ( postfixExpression | unaryOperator ^ unaryExpression );
    public final ES3Parser.unaryExpression_return unaryExpression() throws RecognitionException {
        ES3Parser.unaryExpression_return retval = new ES3Parser.unaryExpression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        ES3Parser.postfixExpression_return postfixExpression66 =null;

        ES3Parser.unaryOperator_return unaryOperator67 =null;

        ES3Parser.unaryExpression_return unaryExpression68 =null;



        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:976:2: ( postfixExpression | unaryOperator ^ unaryExpression )
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==DecimalLiteral||LA20_0==FALSE||(LA20_0 >= FSSTART && LA20_0 <= FUNCTION)||LA20_0==HexIntegerLiteral||LA20_0==Identifier||(LA20_0 >= LBRACE && LA20_0 <= LBRACK)||LA20_0==LPAREN||LA20_0==NEW||LA20_0==NULL||LA20_0==OctalIntegerLiteral||LA20_0==RegularExpressionLiteral||LA20_0==StringLiteral||LA20_0==THIS||LA20_0==TRUE) ) {
                alt20=1;
            }
            else if ( (LA20_0==ADD||LA20_0==DEC||LA20_0==DELETE||LA20_0==INC||LA20_0==INV||LA20_0==NOT||LA20_0==SUB||LA20_0==TYPEOF||LA20_0==VOID) ) {
                alt20=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;

            }
            switch (alt20) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:976:4: postfixExpression
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_postfixExpression_in_unaryExpression3717);
                    postfixExpression66=postfixExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, postfixExpression66.getTree());

                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:977:4: unaryOperator ^ unaryExpression
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_unaryOperator_in_unaryExpression3722);
                    unaryOperator67=unaryOperator();

                    state._fsp--;

                    root_0 = (Object)adaptor.becomeRoot(unaryOperator67.getTree(), root_0);

                    pushFollow(FOLLOW_unaryExpression_in_unaryExpression3725);
                    unaryExpression68=unaryExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, unaryExpression68.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "unaryExpression"


    public static class unaryOperator_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "unaryOperator"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:980:1: unaryOperator : ( DELETE | VOID | TYPEOF | INC | DEC |op= ADD |op= SUB | INV | NOT );
    public final ES3Parser.unaryOperator_return unaryOperator() throws RecognitionException {
        ES3Parser.unaryOperator_return retval = new ES3Parser.unaryOperator_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token op=null;
        Token DELETE69=null;
        Token VOID70=null;
        Token TYPEOF71=null;
        Token INC72=null;
        Token DEC73=null;
        Token INV74=null;
        Token NOT75=null;

        Object op_tree=null;
        Object DELETE69_tree=null;
        Object VOID70_tree=null;
        Object TYPEOF71_tree=null;
        Object INC72_tree=null;
        Object DEC73_tree=null;
        Object INV74_tree=null;
        Object NOT75_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:981:2: ( DELETE | VOID | TYPEOF | INC | DEC |op= ADD |op= SUB | INV | NOT )
            int alt21=9;
            switch ( input.LA(1) ) {
            case DELETE:
                {
                alt21=1;
                }
                break;
            case VOID:
                {
                alt21=2;
                }
                break;
            case TYPEOF:
                {
                alt21=3;
                }
                break;
            case INC:
                {
                alt21=4;
                }
                break;
            case DEC:
                {
                alt21=5;
                }
                break;
            case ADD:
                {
                alt21=6;
                }
                break;
            case SUB:
                {
                alt21=7;
                }
                break;
            case INV:
                {
                alt21=8;
                }
                break;
            case NOT:
                {
                alt21=9;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 21, 0, input);

                throw nvae;

            }

            switch (alt21) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:981:4: DELETE
                    {
                    root_0 = (Object)adaptor.nil();


                    DELETE69=(Token)match(input,DELETE,FOLLOW_DELETE_in_unaryOperator3737); 
                    DELETE69_tree = 
                    (Object)adaptor.create(DELETE69)
                    ;
                    adaptor.addChild(root_0, DELETE69_tree);


                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:982:4: VOID
                    {
                    root_0 = (Object)adaptor.nil();


                    VOID70=(Token)match(input,VOID,FOLLOW_VOID_in_unaryOperator3742); 
                    VOID70_tree = 
                    (Object)adaptor.create(VOID70)
                    ;
                    adaptor.addChild(root_0, VOID70_tree);


                    }
                    break;
                case 3 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:983:4: TYPEOF
                    {
                    root_0 = (Object)adaptor.nil();


                    TYPEOF71=(Token)match(input,TYPEOF,FOLLOW_TYPEOF_in_unaryOperator3747); 
                    TYPEOF71_tree = 
                    (Object)adaptor.create(TYPEOF71)
                    ;
                    adaptor.addChild(root_0, TYPEOF71_tree);


                    }
                    break;
                case 4 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:984:4: INC
                    {
                    root_0 = (Object)adaptor.nil();


                    INC72=(Token)match(input,INC,FOLLOW_INC_in_unaryOperator3752); 
                    INC72_tree = 
                    (Object)adaptor.create(INC72)
                    ;
                    adaptor.addChild(root_0, INC72_tree);


                    }
                    break;
                case 5 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:985:4: DEC
                    {
                    root_0 = (Object)adaptor.nil();


                    DEC73=(Token)match(input,DEC,FOLLOW_DEC_in_unaryOperator3757); 
                    DEC73_tree = 
                    (Object)adaptor.create(DEC73)
                    ;
                    adaptor.addChild(root_0, DEC73_tree);


                    }
                    break;
                case 6 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:986:4: op= ADD
                    {
                    root_0 = (Object)adaptor.nil();


                    op=(Token)match(input,ADD,FOLLOW_ADD_in_unaryOperator3764); 
                    op_tree = 
                    (Object)adaptor.create(op)
                    ;
                    adaptor.addChild(root_0, op_tree);


                     op.setType(POS); 

                    }
                    break;
                case 7 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:987:4: op= SUB
                    {
                    root_0 = (Object)adaptor.nil();


                    op=(Token)match(input,SUB,FOLLOW_SUB_in_unaryOperator3773); 
                    op_tree = 
                    (Object)adaptor.create(op)
                    ;
                    adaptor.addChild(root_0, op_tree);


                     op.setType(NEG); 

                    }
                    break;
                case 8 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:988:4: INV
                    {
                    root_0 = (Object)adaptor.nil();


                    INV74=(Token)match(input,INV,FOLLOW_INV_in_unaryOperator3780); 
                    INV74_tree = 
                    (Object)adaptor.create(INV74)
                    ;
                    adaptor.addChild(root_0, INV74_tree);


                    }
                    break;
                case 9 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:989:4: NOT
                    {
                    root_0 = (Object)adaptor.nil();


                    NOT75=(Token)match(input,NOT,FOLLOW_NOT_in_unaryOperator3785); 
                    NOT75_tree = 
                    (Object)adaptor.create(NOT75)
                    ;
                    adaptor.addChild(root_0, NOT75_tree);


                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "unaryOperator"


    public static class multiplicativeExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "multiplicativeExpression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:996:1: multiplicativeExpression : unaryExpression ( ( MUL | DIV | MOD ) ^ unaryExpression )* ;
    public final ES3Parser.multiplicativeExpression_return multiplicativeExpression() throws RecognitionException {
        ES3Parser.multiplicativeExpression_return retval = new ES3Parser.multiplicativeExpression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set77=null;
        ES3Parser.unaryExpression_return unaryExpression76 =null;

        ES3Parser.unaryExpression_return unaryExpression78 =null;


        Object set77_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:997:2: ( unaryExpression ( ( MUL | DIV | MOD ) ^ unaryExpression )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:997:4: unaryExpression ( ( MUL | DIV | MOD ) ^ unaryExpression )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_unaryExpression_in_multiplicativeExpression3800);
            unaryExpression76=unaryExpression();

            state._fsp--;

            adaptor.addChild(root_0, unaryExpression76.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:997:20: ( ( MUL | DIV | MOD ) ^ unaryExpression )*
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( (LA22_0==DIV||LA22_0==MOD||LA22_0==MUL) ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:997:22: ( MUL | DIV | MOD ) ^ unaryExpression
            	    {
            	    set77=(Token)input.LT(1);

            	    set77=(Token)input.LT(1);

            	    if ( input.LA(1)==DIV||input.LA(1)==MOD||input.LA(1)==MUL ) {
            	        input.consume();
            	        root_0 = (Object)adaptor.becomeRoot(
            	        (Object)adaptor.create(set77)
            	        , root_0);
            	        state.errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }


            	    pushFollow(FOLLOW_unaryExpression_in_multiplicativeExpression3819);
            	    unaryExpression78=unaryExpression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, unaryExpression78.getTree());

            	    }
            	    break;

            	default :
            	    break loop22;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "multiplicativeExpression"


    public static class additiveExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "additiveExpression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1004:1: additiveExpression : multiplicativeExpression ( ( ADD | SUB ) ^ multiplicativeExpression )* ;
    public final ES3Parser.additiveExpression_return additiveExpression() throws RecognitionException {
        ES3Parser.additiveExpression_return retval = new ES3Parser.additiveExpression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set80=null;
        ES3Parser.multiplicativeExpression_return multiplicativeExpression79 =null;

        ES3Parser.multiplicativeExpression_return multiplicativeExpression81 =null;


        Object set80_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1005:2: ( multiplicativeExpression ( ( ADD | SUB ) ^ multiplicativeExpression )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1005:4: multiplicativeExpression ( ( ADD | SUB ) ^ multiplicativeExpression )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_multiplicativeExpression_in_additiveExpression3837);
            multiplicativeExpression79=multiplicativeExpression();

            state._fsp--;

            adaptor.addChild(root_0, multiplicativeExpression79.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1005:29: ( ( ADD | SUB ) ^ multiplicativeExpression )*
            loop23:
            do {
                int alt23=2;
                int LA23_0 = input.LA(1);

                if ( (LA23_0==ADD||LA23_0==SUB) ) {
                    alt23=1;
                }


                switch (alt23) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1005:31: ( ADD | SUB ) ^ multiplicativeExpression
            	    {
            	    set80=(Token)input.LT(1);

            	    set80=(Token)input.LT(1);

            	    if ( input.LA(1)==ADD||input.LA(1)==SUB ) {
            	        input.consume();
            	        root_0 = (Object)adaptor.becomeRoot(
            	        (Object)adaptor.create(set80)
            	        , root_0);
            	        state.errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }


            	    pushFollow(FOLLOW_multiplicativeExpression_in_additiveExpression3852);
            	    multiplicativeExpression81=multiplicativeExpression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, multiplicativeExpression81.getTree());

            	    }
            	    break;

            	default :
            	    break loop23;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "additiveExpression"


    public static class shiftExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "shiftExpression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1012:1: shiftExpression : additiveExpression ( ( SHL | SHR | SHU ) ^ additiveExpression )* ;
    public final ES3Parser.shiftExpression_return shiftExpression() throws RecognitionException {
        ES3Parser.shiftExpression_return retval = new ES3Parser.shiftExpression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set83=null;
        ES3Parser.additiveExpression_return additiveExpression82 =null;

        ES3Parser.additiveExpression_return additiveExpression84 =null;


        Object set83_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1013:2: ( additiveExpression ( ( SHL | SHR | SHU ) ^ additiveExpression )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1013:4: additiveExpression ( ( SHL | SHR | SHU ) ^ additiveExpression )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_additiveExpression_in_shiftExpression3871);
            additiveExpression82=additiveExpression();

            state._fsp--;

            adaptor.addChild(root_0, additiveExpression82.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1013:23: ( ( SHL | SHR | SHU ) ^ additiveExpression )*
            loop24:
            do {
                int alt24=2;
                int LA24_0 = input.LA(1);

                if ( (LA24_0==SHL||LA24_0==SHR||LA24_0==SHU) ) {
                    alt24=1;
                }


                switch (alt24) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1013:25: ( SHL | SHR | SHU ) ^ additiveExpression
            	    {
            	    set83=(Token)input.LT(1);

            	    set83=(Token)input.LT(1);

            	    if ( input.LA(1)==SHL||input.LA(1)==SHR||input.LA(1)==SHU ) {
            	        input.consume();
            	        root_0 = (Object)adaptor.becomeRoot(
            	        (Object)adaptor.create(set83)
            	        , root_0);
            	        state.errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }


            	    pushFollow(FOLLOW_additiveExpression_in_shiftExpression3890);
            	    additiveExpression84=additiveExpression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, additiveExpression84.getTree());

            	    }
            	    break;

            	default :
            	    break loop24;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "shiftExpression"


    public static class relationalExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "relationalExpression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1020:1: relationalExpression : shiftExpression ( ( LT | GT | LTE | GTE | INSTANCEOF | IN ) ^ shiftExpression )* ;
    public final ES3Parser.relationalExpression_return relationalExpression() throws RecognitionException {
        ES3Parser.relationalExpression_return retval = new ES3Parser.relationalExpression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set86=null;
        ES3Parser.shiftExpression_return shiftExpression85 =null;

        ES3Parser.shiftExpression_return shiftExpression87 =null;


        Object set86_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1021:2: ( shiftExpression ( ( LT | GT | LTE | GTE | INSTANCEOF | IN ) ^ shiftExpression )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1021:4: shiftExpression ( ( LT | GT | LTE | GTE | INSTANCEOF | IN ) ^ shiftExpression )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_shiftExpression_in_relationalExpression3909);
            shiftExpression85=shiftExpression();

            state._fsp--;

            adaptor.addChild(root_0, shiftExpression85.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1021:20: ( ( LT | GT | LTE | GTE | INSTANCEOF | IN ) ^ shiftExpression )*
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( ((LA25_0 >= GT && LA25_0 <= GTE)||LA25_0==IN||LA25_0==INSTANCEOF||(LA25_0 >= LT && LA25_0 <= LTE)) ) {
                    alt25=1;
                }


                switch (alt25) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1021:22: ( LT | GT | LTE | GTE | INSTANCEOF | IN ) ^ shiftExpression
            	    {
            	    set86=(Token)input.LT(1);

            	    set86=(Token)input.LT(1);

            	    if ( (input.LA(1) >= GT && input.LA(1) <= GTE)||input.LA(1)==IN||input.LA(1)==INSTANCEOF||(input.LA(1) >= LT && input.LA(1) <= LTE) ) {
            	        input.consume();
            	        root_0 = (Object)adaptor.becomeRoot(
            	        (Object)adaptor.create(set86)
            	        , root_0);
            	        state.errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }


            	    pushFollow(FOLLOW_shiftExpression_in_relationalExpression3940);
            	    shiftExpression87=shiftExpression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, shiftExpression87.getTree());

            	    }
            	    break;

            	default :
            	    break loop25;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "relationalExpression"


    public static class relationalExpressionNoIn_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "relationalExpressionNoIn"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1024:1: relationalExpressionNoIn : shiftExpression ( ( LT | GT | LTE | GTE | INSTANCEOF ) ^ shiftExpression )* ;
    public final ES3Parser.relationalExpressionNoIn_return relationalExpressionNoIn() throws RecognitionException {
        ES3Parser.relationalExpressionNoIn_return retval = new ES3Parser.relationalExpressionNoIn_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set89=null;
        ES3Parser.shiftExpression_return shiftExpression88 =null;

        ES3Parser.shiftExpression_return shiftExpression90 =null;


        Object set89_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1025:2: ( shiftExpression ( ( LT | GT | LTE | GTE | INSTANCEOF ) ^ shiftExpression )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1025:4: shiftExpression ( ( LT | GT | LTE | GTE | INSTANCEOF ) ^ shiftExpression )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_shiftExpression_in_relationalExpressionNoIn3954);
            shiftExpression88=shiftExpression();

            state._fsp--;

            adaptor.addChild(root_0, shiftExpression88.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1025:20: ( ( LT | GT | LTE | GTE | INSTANCEOF ) ^ shiftExpression )*
            loop26:
            do {
                int alt26=2;
                int LA26_0 = input.LA(1);

                if ( ((LA26_0 >= GT && LA26_0 <= GTE)||LA26_0==INSTANCEOF||(LA26_0 >= LT && LA26_0 <= LTE)) ) {
                    alt26=1;
                }


                switch (alt26) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1025:22: ( LT | GT | LTE | GTE | INSTANCEOF ) ^ shiftExpression
            	    {
            	    set89=(Token)input.LT(1);

            	    set89=(Token)input.LT(1);

            	    if ( (input.LA(1) >= GT && input.LA(1) <= GTE)||input.LA(1)==INSTANCEOF||(input.LA(1) >= LT && input.LA(1) <= LTE) ) {
            	        input.consume();
            	        root_0 = (Object)adaptor.becomeRoot(
            	        (Object)adaptor.create(set89)
            	        , root_0);
            	        state.errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }


            	    pushFollow(FOLLOW_shiftExpression_in_relationalExpressionNoIn3981);
            	    shiftExpression90=shiftExpression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, shiftExpression90.getTree());

            	    }
            	    break;

            	default :
            	    break loop26;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "relationalExpressionNoIn"


    public static class equalityExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "equalityExpression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1032:1: equalityExpression : relationalExpression ( ( EQ | NEQ | SAME | NSAME ) ^ relationalExpression )* ;
    public final ES3Parser.equalityExpression_return equalityExpression() throws RecognitionException {
        ES3Parser.equalityExpression_return retval = new ES3Parser.equalityExpression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set92=null;
        ES3Parser.relationalExpression_return relationalExpression91 =null;

        ES3Parser.relationalExpression_return relationalExpression93 =null;


        Object set92_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1033:2: ( relationalExpression ( ( EQ | NEQ | SAME | NSAME ) ^ relationalExpression )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1033:4: relationalExpression ( ( EQ | NEQ | SAME | NSAME ) ^ relationalExpression )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_relationalExpression_in_equalityExpression4000);
            relationalExpression91=relationalExpression();

            state._fsp--;

            adaptor.addChild(root_0, relationalExpression91.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1033:25: ( ( EQ | NEQ | SAME | NSAME ) ^ relationalExpression )*
            loop27:
            do {
                int alt27=2;
                int LA27_0 = input.LA(1);

                if ( (LA27_0==EQ||LA27_0==NEQ||LA27_0==NSAME||LA27_0==SAME) ) {
                    alt27=1;
                }


                switch (alt27) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1033:27: ( EQ | NEQ | SAME | NSAME ) ^ relationalExpression
            	    {
            	    set92=(Token)input.LT(1);

            	    set92=(Token)input.LT(1);

            	    if ( input.LA(1)==EQ||input.LA(1)==NEQ||input.LA(1)==NSAME||input.LA(1)==SAME ) {
            	        input.consume();
            	        root_0 = (Object)adaptor.becomeRoot(
            	        (Object)adaptor.create(set92)
            	        , root_0);
            	        state.errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }


            	    pushFollow(FOLLOW_relationalExpression_in_equalityExpression4023);
            	    relationalExpression93=relationalExpression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, relationalExpression93.getTree());

            	    }
            	    break;

            	default :
            	    break loop27;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "equalityExpression"


    public static class equalityExpressionNoIn_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "equalityExpressionNoIn"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1036:1: equalityExpressionNoIn : relationalExpressionNoIn ( ( EQ | NEQ | SAME | NSAME ) ^ relationalExpressionNoIn )* ;
    public final ES3Parser.equalityExpressionNoIn_return equalityExpressionNoIn() throws RecognitionException {
        ES3Parser.equalityExpressionNoIn_return retval = new ES3Parser.equalityExpressionNoIn_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set95=null;
        ES3Parser.relationalExpressionNoIn_return relationalExpressionNoIn94 =null;

        ES3Parser.relationalExpressionNoIn_return relationalExpressionNoIn96 =null;


        Object set95_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1037:2: ( relationalExpressionNoIn ( ( EQ | NEQ | SAME | NSAME ) ^ relationalExpressionNoIn )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1037:4: relationalExpressionNoIn ( ( EQ | NEQ | SAME | NSAME ) ^ relationalExpressionNoIn )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_relationalExpressionNoIn_in_equalityExpressionNoIn4037);
            relationalExpressionNoIn94=relationalExpressionNoIn();

            state._fsp--;

            adaptor.addChild(root_0, relationalExpressionNoIn94.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1037:29: ( ( EQ | NEQ | SAME | NSAME ) ^ relationalExpressionNoIn )*
            loop28:
            do {
                int alt28=2;
                int LA28_0 = input.LA(1);

                if ( (LA28_0==EQ||LA28_0==NEQ||LA28_0==NSAME||LA28_0==SAME) ) {
                    alt28=1;
                }


                switch (alt28) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1037:31: ( EQ | NEQ | SAME | NSAME ) ^ relationalExpressionNoIn
            	    {
            	    set95=(Token)input.LT(1);

            	    set95=(Token)input.LT(1);

            	    if ( input.LA(1)==EQ||input.LA(1)==NEQ||input.LA(1)==NSAME||input.LA(1)==SAME ) {
            	        input.consume();
            	        root_0 = (Object)adaptor.becomeRoot(
            	        (Object)adaptor.create(set95)
            	        , root_0);
            	        state.errorRecovery=false;
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }


            	    pushFollow(FOLLOW_relationalExpressionNoIn_in_equalityExpressionNoIn4060);
            	    relationalExpressionNoIn96=relationalExpressionNoIn();

            	    state._fsp--;

            	    adaptor.addChild(root_0, relationalExpressionNoIn96.getTree());

            	    }
            	    break;

            	default :
            	    break loop28;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "equalityExpressionNoIn"


    public static class bitwiseANDExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "bitwiseANDExpression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1044:1: bitwiseANDExpression : equalityExpression ( AND ^ equalityExpression )* ;
    public final ES3Parser.bitwiseANDExpression_return bitwiseANDExpression() throws RecognitionException {
        ES3Parser.bitwiseANDExpression_return retval = new ES3Parser.bitwiseANDExpression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token AND98=null;
        ES3Parser.equalityExpression_return equalityExpression97 =null;

        ES3Parser.equalityExpression_return equalityExpression99 =null;


        Object AND98_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1045:2: ( equalityExpression ( AND ^ equalityExpression )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1045:4: equalityExpression ( AND ^ equalityExpression )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_equalityExpression_in_bitwiseANDExpression4080);
            equalityExpression97=equalityExpression();

            state._fsp--;

            adaptor.addChild(root_0, equalityExpression97.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1045:23: ( AND ^ equalityExpression )*
            loop29:
            do {
                int alt29=2;
                int LA29_0 = input.LA(1);

                if ( (LA29_0==AND) ) {
                    alt29=1;
                }


                switch (alt29) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1045:25: AND ^ equalityExpression
            	    {
            	    AND98=(Token)match(input,AND,FOLLOW_AND_in_bitwiseANDExpression4084); 
            	    AND98_tree = 
            	    (Object)adaptor.create(AND98)
            	    ;
            	    root_0 = (Object)adaptor.becomeRoot(AND98_tree, root_0);


            	    pushFollow(FOLLOW_equalityExpression_in_bitwiseANDExpression4087);
            	    equalityExpression99=equalityExpression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, equalityExpression99.getTree());

            	    }
            	    break;

            	default :
            	    break loop29;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "bitwiseANDExpression"


    public static class bitwiseANDExpressionNoIn_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "bitwiseANDExpressionNoIn"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1048:1: bitwiseANDExpressionNoIn : equalityExpressionNoIn ( AND ^ equalityExpressionNoIn )* ;
    public final ES3Parser.bitwiseANDExpressionNoIn_return bitwiseANDExpressionNoIn() throws RecognitionException {
        ES3Parser.bitwiseANDExpressionNoIn_return retval = new ES3Parser.bitwiseANDExpressionNoIn_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token AND101=null;
        ES3Parser.equalityExpressionNoIn_return equalityExpressionNoIn100 =null;

        ES3Parser.equalityExpressionNoIn_return equalityExpressionNoIn102 =null;


        Object AND101_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1049:2: ( equalityExpressionNoIn ( AND ^ equalityExpressionNoIn )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1049:4: equalityExpressionNoIn ( AND ^ equalityExpressionNoIn )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_equalityExpressionNoIn_in_bitwiseANDExpressionNoIn4101);
            equalityExpressionNoIn100=equalityExpressionNoIn();

            state._fsp--;

            adaptor.addChild(root_0, equalityExpressionNoIn100.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1049:27: ( AND ^ equalityExpressionNoIn )*
            loop30:
            do {
                int alt30=2;
                int LA30_0 = input.LA(1);

                if ( (LA30_0==AND) ) {
                    alt30=1;
                }


                switch (alt30) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1049:29: AND ^ equalityExpressionNoIn
            	    {
            	    AND101=(Token)match(input,AND,FOLLOW_AND_in_bitwiseANDExpressionNoIn4105); 
            	    AND101_tree = 
            	    (Object)adaptor.create(AND101)
            	    ;
            	    root_0 = (Object)adaptor.becomeRoot(AND101_tree, root_0);


            	    pushFollow(FOLLOW_equalityExpressionNoIn_in_bitwiseANDExpressionNoIn4108);
            	    equalityExpressionNoIn102=equalityExpressionNoIn();

            	    state._fsp--;

            	    adaptor.addChild(root_0, equalityExpressionNoIn102.getTree());

            	    }
            	    break;

            	default :
            	    break loop30;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "bitwiseANDExpressionNoIn"


    public static class bitwiseXORExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "bitwiseXORExpression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1052:1: bitwiseXORExpression : bitwiseANDExpression ( XOR ^ bitwiseANDExpression )* ;
    public final ES3Parser.bitwiseXORExpression_return bitwiseXORExpression() throws RecognitionException {
        ES3Parser.bitwiseXORExpression_return retval = new ES3Parser.bitwiseXORExpression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token XOR104=null;
        ES3Parser.bitwiseANDExpression_return bitwiseANDExpression103 =null;

        ES3Parser.bitwiseANDExpression_return bitwiseANDExpression105 =null;


        Object XOR104_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1053:2: ( bitwiseANDExpression ( XOR ^ bitwiseANDExpression )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1053:4: bitwiseANDExpression ( XOR ^ bitwiseANDExpression )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_bitwiseANDExpression_in_bitwiseXORExpression4124);
            bitwiseANDExpression103=bitwiseANDExpression();

            state._fsp--;

            adaptor.addChild(root_0, bitwiseANDExpression103.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1053:25: ( XOR ^ bitwiseANDExpression )*
            loop31:
            do {
                int alt31=2;
                int LA31_0 = input.LA(1);

                if ( (LA31_0==XOR) ) {
                    alt31=1;
                }


                switch (alt31) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1053:27: XOR ^ bitwiseANDExpression
            	    {
            	    XOR104=(Token)match(input,XOR,FOLLOW_XOR_in_bitwiseXORExpression4128); 
            	    XOR104_tree = 
            	    (Object)adaptor.create(XOR104)
            	    ;
            	    root_0 = (Object)adaptor.becomeRoot(XOR104_tree, root_0);


            	    pushFollow(FOLLOW_bitwiseANDExpression_in_bitwiseXORExpression4131);
            	    bitwiseANDExpression105=bitwiseANDExpression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, bitwiseANDExpression105.getTree());

            	    }
            	    break;

            	default :
            	    break loop31;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "bitwiseXORExpression"


    public static class bitwiseXORExpressionNoIn_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "bitwiseXORExpressionNoIn"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1056:1: bitwiseXORExpressionNoIn : bitwiseANDExpressionNoIn ( XOR ^ bitwiseANDExpressionNoIn )* ;
    public final ES3Parser.bitwiseXORExpressionNoIn_return bitwiseXORExpressionNoIn() throws RecognitionException {
        ES3Parser.bitwiseXORExpressionNoIn_return retval = new ES3Parser.bitwiseXORExpressionNoIn_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token XOR107=null;
        ES3Parser.bitwiseANDExpressionNoIn_return bitwiseANDExpressionNoIn106 =null;

        ES3Parser.bitwiseANDExpressionNoIn_return bitwiseANDExpressionNoIn108 =null;


        Object XOR107_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1057:2: ( bitwiseANDExpressionNoIn ( XOR ^ bitwiseANDExpressionNoIn )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1057:4: bitwiseANDExpressionNoIn ( XOR ^ bitwiseANDExpressionNoIn )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_bitwiseANDExpressionNoIn_in_bitwiseXORExpressionNoIn4147);
            bitwiseANDExpressionNoIn106=bitwiseANDExpressionNoIn();

            state._fsp--;

            adaptor.addChild(root_0, bitwiseANDExpressionNoIn106.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1057:29: ( XOR ^ bitwiseANDExpressionNoIn )*
            loop32:
            do {
                int alt32=2;
                int LA32_0 = input.LA(1);

                if ( (LA32_0==XOR) ) {
                    alt32=1;
                }


                switch (alt32) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1057:31: XOR ^ bitwiseANDExpressionNoIn
            	    {
            	    XOR107=(Token)match(input,XOR,FOLLOW_XOR_in_bitwiseXORExpressionNoIn4151); 
            	    XOR107_tree = 
            	    (Object)adaptor.create(XOR107)
            	    ;
            	    root_0 = (Object)adaptor.becomeRoot(XOR107_tree, root_0);


            	    pushFollow(FOLLOW_bitwiseANDExpressionNoIn_in_bitwiseXORExpressionNoIn4154);
            	    bitwiseANDExpressionNoIn108=bitwiseANDExpressionNoIn();

            	    state._fsp--;

            	    adaptor.addChild(root_0, bitwiseANDExpressionNoIn108.getTree());

            	    }
            	    break;

            	default :
            	    break loop32;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "bitwiseXORExpressionNoIn"


    public static class bitwiseORExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "bitwiseORExpression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1060:1: bitwiseORExpression : bitwiseXORExpression ( OR ^ bitwiseXORExpression )* ;
    public final ES3Parser.bitwiseORExpression_return bitwiseORExpression() throws RecognitionException {
        ES3Parser.bitwiseORExpression_return retval = new ES3Parser.bitwiseORExpression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token OR110=null;
        ES3Parser.bitwiseXORExpression_return bitwiseXORExpression109 =null;

        ES3Parser.bitwiseXORExpression_return bitwiseXORExpression111 =null;


        Object OR110_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1061:2: ( bitwiseXORExpression ( OR ^ bitwiseXORExpression )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1061:4: bitwiseXORExpression ( OR ^ bitwiseXORExpression )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_bitwiseXORExpression_in_bitwiseORExpression4169);
            bitwiseXORExpression109=bitwiseXORExpression();

            state._fsp--;

            adaptor.addChild(root_0, bitwiseXORExpression109.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1061:25: ( OR ^ bitwiseXORExpression )*
            loop33:
            do {
                int alt33=2;
                int LA33_0 = input.LA(1);

                if ( (LA33_0==OR) ) {
                    alt33=1;
                }


                switch (alt33) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1061:27: OR ^ bitwiseXORExpression
            	    {
            	    OR110=(Token)match(input,OR,FOLLOW_OR_in_bitwiseORExpression4173); 
            	    OR110_tree = 
            	    (Object)adaptor.create(OR110)
            	    ;
            	    root_0 = (Object)adaptor.becomeRoot(OR110_tree, root_0);


            	    pushFollow(FOLLOW_bitwiseXORExpression_in_bitwiseORExpression4176);
            	    bitwiseXORExpression111=bitwiseXORExpression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, bitwiseXORExpression111.getTree());

            	    }
            	    break;

            	default :
            	    break loop33;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "bitwiseORExpression"


    public static class bitwiseORExpressionNoIn_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "bitwiseORExpressionNoIn"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1064:1: bitwiseORExpressionNoIn : bitwiseXORExpressionNoIn ( OR ^ bitwiseXORExpressionNoIn )* ;
    public final ES3Parser.bitwiseORExpressionNoIn_return bitwiseORExpressionNoIn() throws RecognitionException {
        ES3Parser.bitwiseORExpressionNoIn_return retval = new ES3Parser.bitwiseORExpressionNoIn_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token OR113=null;
        ES3Parser.bitwiseXORExpressionNoIn_return bitwiseXORExpressionNoIn112 =null;

        ES3Parser.bitwiseXORExpressionNoIn_return bitwiseXORExpressionNoIn114 =null;


        Object OR113_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1065:2: ( bitwiseXORExpressionNoIn ( OR ^ bitwiseXORExpressionNoIn )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1065:4: bitwiseXORExpressionNoIn ( OR ^ bitwiseXORExpressionNoIn )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_bitwiseXORExpressionNoIn_in_bitwiseORExpressionNoIn4191);
            bitwiseXORExpressionNoIn112=bitwiseXORExpressionNoIn();

            state._fsp--;

            adaptor.addChild(root_0, bitwiseXORExpressionNoIn112.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1065:29: ( OR ^ bitwiseXORExpressionNoIn )*
            loop34:
            do {
                int alt34=2;
                int LA34_0 = input.LA(1);

                if ( (LA34_0==OR) ) {
                    alt34=1;
                }


                switch (alt34) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1065:31: OR ^ bitwiseXORExpressionNoIn
            	    {
            	    OR113=(Token)match(input,OR,FOLLOW_OR_in_bitwiseORExpressionNoIn4195); 
            	    OR113_tree = 
            	    (Object)adaptor.create(OR113)
            	    ;
            	    root_0 = (Object)adaptor.becomeRoot(OR113_tree, root_0);


            	    pushFollow(FOLLOW_bitwiseXORExpressionNoIn_in_bitwiseORExpressionNoIn4198);
            	    bitwiseXORExpressionNoIn114=bitwiseXORExpressionNoIn();

            	    state._fsp--;

            	    adaptor.addChild(root_0, bitwiseXORExpressionNoIn114.getTree());

            	    }
            	    break;

            	default :
            	    break loop34;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "bitwiseORExpressionNoIn"


    public static class logicalANDExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "logicalANDExpression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1072:1: logicalANDExpression : bitwiseORExpression ( LAND ^ bitwiseORExpression )* ;
    public final ES3Parser.logicalANDExpression_return logicalANDExpression() throws RecognitionException {
        ES3Parser.logicalANDExpression_return retval = new ES3Parser.logicalANDExpression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token LAND116=null;
        ES3Parser.bitwiseORExpression_return bitwiseORExpression115 =null;

        ES3Parser.bitwiseORExpression_return bitwiseORExpression117 =null;


        Object LAND116_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1073:2: ( bitwiseORExpression ( LAND ^ bitwiseORExpression )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1073:4: bitwiseORExpression ( LAND ^ bitwiseORExpression )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_bitwiseORExpression_in_logicalANDExpression4217);
            bitwiseORExpression115=bitwiseORExpression();

            state._fsp--;

            adaptor.addChild(root_0, bitwiseORExpression115.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1073:24: ( LAND ^ bitwiseORExpression )*
            loop35:
            do {
                int alt35=2;
                int LA35_0 = input.LA(1);

                if ( (LA35_0==LAND) ) {
                    alt35=1;
                }


                switch (alt35) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1073:26: LAND ^ bitwiseORExpression
            	    {
            	    LAND116=(Token)match(input,LAND,FOLLOW_LAND_in_logicalANDExpression4221); 
            	    LAND116_tree = 
            	    (Object)adaptor.create(LAND116)
            	    ;
            	    root_0 = (Object)adaptor.becomeRoot(LAND116_tree, root_0);


            	    pushFollow(FOLLOW_bitwiseORExpression_in_logicalANDExpression4224);
            	    bitwiseORExpression117=bitwiseORExpression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, bitwiseORExpression117.getTree());

            	    }
            	    break;

            	default :
            	    break loop35;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "logicalANDExpression"


    public static class logicalANDExpressionNoIn_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "logicalANDExpressionNoIn"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1076:1: logicalANDExpressionNoIn : bitwiseORExpressionNoIn ( LAND ^ bitwiseORExpressionNoIn )* ;
    public final ES3Parser.logicalANDExpressionNoIn_return logicalANDExpressionNoIn() throws RecognitionException {
        ES3Parser.logicalANDExpressionNoIn_return retval = new ES3Parser.logicalANDExpressionNoIn_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token LAND119=null;
        ES3Parser.bitwiseORExpressionNoIn_return bitwiseORExpressionNoIn118 =null;

        ES3Parser.bitwiseORExpressionNoIn_return bitwiseORExpressionNoIn120 =null;


        Object LAND119_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1077:2: ( bitwiseORExpressionNoIn ( LAND ^ bitwiseORExpressionNoIn )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1077:4: bitwiseORExpressionNoIn ( LAND ^ bitwiseORExpressionNoIn )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_bitwiseORExpressionNoIn_in_logicalANDExpressionNoIn4238);
            bitwiseORExpressionNoIn118=bitwiseORExpressionNoIn();

            state._fsp--;

            adaptor.addChild(root_0, bitwiseORExpressionNoIn118.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1077:28: ( LAND ^ bitwiseORExpressionNoIn )*
            loop36:
            do {
                int alt36=2;
                int LA36_0 = input.LA(1);

                if ( (LA36_0==LAND) ) {
                    alt36=1;
                }


                switch (alt36) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1077:30: LAND ^ bitwiseORExpressionNoIn
            	    {
            	    LAND119=(Token)match(input,LAND,FOLLOW_LAND_in_logicalANDExpressionNoIn4242); 
            	    LAND119_tree = 
            	    (Object)adaptor.create(LAND119)
            	    ;
            	    root_0 = (Object)adaptor.becomeRoot(LAND119_tree, root_0);


            	    pushFollow(FOLLOW_bitwiseORExpressionNoIn_in_logicalANDExpressionNoIn4245);
            	    bitwiseORExpressionNoIn120=bitwiseORExpressionNoIn();

            	    state._fsp--;

            	    adaptor.addChild(root_0, bitwiseORExpressionNoIn120.getTree());

            	    }
            	    break;

            	default :
            	    break loop36;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "logicalANDExpressionNoIn"


    public static class logicalORExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "logicalORExpression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1080:1: logicalORExpression : logicalANDExpression ( LOR ^ logicalANDExpression )* ;
    public final ES3Parser.logicalORExpression_return logicalORExpression() throws RecognitionException {
        ES3Parser.logicalORExpression_return retval = new ES3Parser.logicalORExpression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token LOR122=null;
        ES3Parser.logicalANDExpression_return logicalANDExpression121 =null;

        ES3Parser.logicalANDExpression_return logicalANDExpression123 =null;


        Object LOR122_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1081:2: ( logicalANDExpression ( LOR ^ logicalANDExpression )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1081:4: logicalANDExpression ( LOR ^ logicalANDExpression )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_logicalANDExpression_in_logicalORExpression4260);
            logicalANDExpression121=logicalANDExpression();

            state._fsp--;

            adaptor.addChild(root_0, logicalANDExpression121.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1081:25: ( LOR ^ logicalANDExpression )*
            loop37:
            do {
                int alt37=2;
                int LA37_0 = input.LA(1);

                if ( (LA37_0==LOR) ) {
                    alt37=1;
                }


                switch (alt37) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1081:27: LOR ^ logicalANDExpression
            	    {
            	    LOR122=(Token)match(input,LOR,FOLLOW_LOR_in_logicalORExpression4264); 
            	    LOR122_tree = 
            	    (Object)adaptor.create(LOR122)
            	    ;
            	    root_0 = (Object)adaptor.becomeRoot(LOR122_tree, root_0);


            	    pushFollow(FOLLOW_logicalANDExpression_in_logicalORExpression4267);
            	    logicalANDExpression123=logicalANDExpression();

            	    state._fsp--;

            	    adaptor.addChild(root_0, logicalANDExpression123.getTree());

            	    }
            	    break;

            	default :
            	    break loop37;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "logicalORExpression"


    public static class logicalORExpressionNoIn_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "logicalORExpressionNoIn"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1084:1: logicalORExpressionNoIn : logicalANDExpressionNoIn ( LOR ^ logicalANDExpressionNoIn )* ;
    public final ES3Parser.logicalORExpressionNoIn_return logicalORExpressionNoIn() throws RecognitionException {
        ES3Parser.logicalORExpressionNoIn_return retval = new ES3Parser.logicalORExpressionNoIn_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token LOR125=null;
        ES3Parser.logicalANDExpressionNoIn_return logicalANDExpressionNoIn124 =null;

        ES3Parser.logicalANDExpressionNoIn_return logicalANDExpressionNoIn126 =null;


        Object LOR125_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1085:2: ( logicalANDExpressionNoIn ( LOR ^ logicalANDExpressionNoIn )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1085:4: logicalANDExpressionNoIn ( LOR ^ logicalANDExpressionNoIn )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_logicalANDExpressionNoIn_in_logicalORExpressionNoIn4282);
            logicalANDExpressionNoIn124=logicalANDExpressionNoIn();

            state._fsp--;

            adaptor.addChild(root_0, logicalANDExpressionNoIn124.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1085:29: ( LOR ^ logicalANDExpressionNoIn )*
            loop38:
            do {
                int alt38=2;
                int LA38_0 = input.LA(1);

                if ( (LA38_0==LOR) ) {
                    alt38=1;
                }


                switch (alt38) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1085:31: LOR ^ logicalANDExpressionNoIn
            	    {
            	    LOR125=(Token)match(input,LOR,FOLLOW_LOR_in_logicalORExpressionNoIn4286); 
            	    LOR125_tree = 
            	    (Object)adaptor.create(LOR125)
            	    ;
            	    root_0 = (Object)adaptor.becomeRoot(LOR125_tree, root_0);


            	    pushFollow(FOLLOW_logicalANDExpressionNoIn_in_logicalORExpressionNoIn4289);
            	    logicalANDExpressionNoIn126=logicalANDExpressionNoIn();

            	    state._fsp--;

            	    adaptor.addChild(root_0, logicalANDExpressionNoIn126.getTree());

            	    }
            	    break;

            	default :
            	    break loop38;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "logicalORExpressionNoIn"


    public static class conditionalExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "conditionalExpression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1092:1: conditionalExpression : logicalORExpression ( QUE ^ assignmentExpression COLON ! assignmentExpression )? ;
    public final ES3Parser.conditionalExpression_return conditionalExpression() throws RecognitionException {
        ES3Parser.conditionalExpression_return retval = new ES3Parser.conditionalExpression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token QUE128=null;
        Token COLON130=null;
        ES3Parser.logicalORExpression_return logicalORExpression127 =null;

        ES3Parser.assignmentExpression_return assignmentExpression129 =null;

        ES3Parser.assignmentExpression_return assignmentExpression131 =null;


        Object QUE128_tree=null;
        Object COLON130_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1093:2: ( logicalORExpression ( QUE ^ assignmentExpression COLON ! assignmentExpression )? )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1093:4: logicalORExpression ( QUE ^ assignmentExpression COLON ! assignmentExpression )?
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_logicalORExpression_in_conditionalExpression4308);
            logicalORExpression127=logicalORExpression();

            state._fsp--;

            adaptor.addChild(root_0, logicalORExpression127.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1093:24: ( QUE ^ assignmentExpression COLON ! assignmentExpression )?
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==QUE) ) {
                alt39=1;
            }
            switch (alt39) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1093:26: QUE ^ assignmentExpression COLON ! assignmentExpression
                    {
                    QUE128=(Token)match(input,QUE,FOLLOW_QUE_in_conditionalExpression4312); 
                    QUE128_tree = 
                    (Object)adaptor.create(QUE128)
                    ;
                    root_0 = (Object)adaptor.becomeRoot(QUE128_tree, root_0);


                    pushFollow(FOLLOW_assignmentExpression_in_conditionalExpression4315);
                    assignmentExpression129=assignmentExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, assignmentExpression129.getTree());

                    COLON130=(Token)match(input,COLON,FOLLOW_COLON_in_conditionalExpression4317); 

                    pushFollow(FOLLOW_assignmentExpression_in_conditionalExpression4320);
                    assignmentExpression131=assignmentExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, assignmentExpression131.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "conditionalExpression"


    public static class conditionalExpressionNoIn_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "conditionalExpressionNoIn"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1096:1: conditionalExpressionNoIn : logicalORExpressionNoIn ( QUE ^ assignmentExpressionNoIn COLON ! assignmentExpressionNoIn )? ;
    public final ES3Parser.conditionalExpressionNoIn_return conditionalExpressionNoIn() throws RecognitionException {
        ES3Parser.conditionalExpressionNoIn_return retval = new ES3Parser.conditionalExpressionNoIn_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token QUE133=null;
        Token COLON135=null;
        ES3Parser.logicalORExpressionNoIn_return logicalORExpressionNoIn132 =null;

        ES3Parser.assignmentExpressionNoIn_return assignmentExpressionNoIn134 =null;

        ES3Parser.assignmentExpressionNoIn_return assignmentExpressionNoIn136 =null;


        Object QUE133_tree=null;
        Object COLON135_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1097:2: ( logicalORExpressionNoIn ( QUE ^ assignmentExpressionNoIn COLON ! assignmentExpressionNoIn )? )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1097:4: logicalORExpressionNoIn ( QUE ^ assignmentExpressionNoIn COLON ! assignmentExpressionNoIn )?
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_logicalORExpressionNoIn_in_conditionalExpressionNoIn4334);
            logicalORExpressionNoIn132=logicalORExpressionNoIn();

            state._fsp--;

            adaptor.addChild(root_0, logicalORExpressionNoIn132.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1097:28: ( QUE ^ assignmentExpressionNoIn COLON ! assignmentExpressionNoIn )?
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( (LA40_0==QUE) ) {
                alt40=1;
            }
            switch (alt40) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1097:30: QUE ^ assignmentExpressionNoIn COLON ! assignmentExpressionNoIn
                    {
                    QUE133=(Token)match(input,QUE,FOLLOW_QUE_in_conditionalExpressionNoIn4338); 
                    QUE133_tree = 
                    (Object)adaptor.create(QUE133)
                    ;
                    root_0 = (Object)adaptor.becomeRoot(QUE133_tree, root_0);


                    pushFollow(FOLLOW_assignmentExpressionNoIn_in_conditionalExpressionNoIn4341);
                    assignmentExpressionNoIn134=assignmentExpressionNoIn();

                    state._fsp--;

                    adaptor.addChild(root_0, assignmentExpressionNoIn134.getTree());

                    COLON135=(Token)match(input,COLON,FOLLOW_COLON_in_conditionalExpressionNoIn4343); 

                    pushFollow(FOLLOW_assignmentExpressionNoIn_in_conditionalExpressionNoIn4346);
                    assignmentExpressionNoIn136=assignmentExpressionNoIn();

                    state._fsp--;

                    adaptor.addChild(root_0, assignmentExpressionNoIn136.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "conditionalExpressionNoIn"


    public static class assignmentExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "assignmentExpression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1126:1: assignmentExpression : lhs= conditionalExpression ({...}? assignmentOperator ^ assignmentExpression )? ;
    public final ES3Parser.assignmentExpression_return assignmentExpression() throws RecognitionException {
        ES3Parser.assignmentExpression_return retval = new ES3Parser.assignmentExpression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        ES3Parser.conditionalExpression_return lhs =null;

        ES3Parser.assignmentOperator_return assignmentOperator137 =null;

        ES3Parser.assignmentExpression_return assignmentExpression138 =null;




        	Object[] isLhs = new Object[1];

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1131:2: (lhs= conditionalExpression ({...}? assignmentOperator ^ assignmentExpression )? )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1131:4: lhs= conditionalExpression ({...}? assignmentOperator ^ assignmentExpression )?
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_conditionalExpression_in_assignmentExpression4374);
            lhs=conditionalExpression();

            state._fsp--;

            adaptor.addChild(root_0, lhs.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1132:2: ({...}? assignmentOperator ^ assignmentExpression )?
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==ADDASS||LA41_0==ANDASS||LA41_0==ASSIGN||LA41_0==DIVASS||LA41_0==MODASS||LA41_0==MULASS||LA41_0==ORASS||LA41_0==SHLASS||LA41_0==SHRASS||LA41_0==SHUASS||LA41_0==SUBASS||LA41_0==XORASS) ) {
                int LA41_1 = input.LA(2);

                if ( (( isLeftHandSideAssign(lhs, isLhs) )) ) {
                    alt41=1;
                }
            }
            switch (alt41) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1132:4: {...}? assignmentOperator ^ assignmentExpression
                    {
                    if ( !(( isLeftHandSideAssign(lhs, isLhs) )) ) {
                        throw new FailedPredicateException(input, "assignmentExpression", " isLeftHandSideAssign(lhs, isLhs) ");
                    }

                    pushFollow(FOLLOW_assignmentOperator_in_assignmentExpression4381);
                    assignmentOperator137=assignmentOperator();

                    state._fsp--;

                    root_0 = (Object)adaptor.becomeRoot(assignmentOperator137.getTree(), root_0);

                    pushFollow(FOLLOW_assignmentExpression_in_assignmentExpression4384);
                    assignmentExpression138=assignmentExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, assignmentExpression138.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "assignmentExpression"


    public static class assignmentOperator_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "assignmentOperator"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1135:1: assignmentOperator : ( ASSIGN | MULASS | DIVASS | MODASS | ADDASS | SUBASS | SHLASS | SHRASS | SHUASS | ANDASS | XORASS | ORASS );
    public final ES3Parser.assignmentOperator_return assignmentOperator() throws RecognitionException {
        ES3Parser.assignmentOperator_return retval = new ES3Parser.assignmentOperator_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set139=null;

        Object set139_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1136:2: ( ASSIGN | MULASS | DIVASS | MODASS | ADDASS | SUBASS | SHLASS | SHRASS | SHUASS | ANDASS | XORASS | ORASS )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:
            {
            root_0 = (Object)adaptor.nil();


            set139=(Token)input.LT(1);

            if ( input.LA(1)==ADDASS||input.LA(1)==ANDASS||input.LA(1)==ASSIGN||input.LA(1)==DIVASS||input.LA(1)==MODASS||input.LA(1)==MULASS||input.LA(1)==ORASS||input.LA(1)==SHLASS||input.LA(1)==SHRASS||input.LA(1)==SHUASS||input.LA(1)==SUBASS||input.LA(1)==XORASS ) {
                input.consume();
                adaptor.addChild(root_0, 
                (Object)adaptor.create(set139)
                );
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "assignmentOperator"


    public static class assignmentExpressionNoIn_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "assignmentExpressionNoIn"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1139:1: assignmentExpressionNoIn : lhs= conditionalExpressionNoIn ({...}? assignmentOperator ^ assignmentExpressionNoIn )? ;
    public final ES3Parser.assignmentExpressionNoIn_return assignmentExpressionNoIn() throws RecognitionException {
        ES3Parser.assignmentExpressionNoIn_return retval = new ES3Parser.assignmentExpressionNoIn_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        ES3Parser.conditionalExpressionNoIn_return lhs =null;

        ES3Parser.assignmentOperator_return assignmentOperator140 =null;

        ES3Parser.assignmentExpressionNoIn_return assignmentExpressionNoIn141 =null;




        	Object[] isLhs = new Object[1];

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1144:2: (lhs= conditionalExpressionNoIn ({...}? assignmentOperator ^ assignmentExpressionNoIn )? )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1144:4: lhs= conditionalExpressionNoIn ({...}? assignmentOperator ^ assignmentExpressionNoIn )?
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_conditionalExpressionNoIn_in_assignmentExpressionNoIn4461);
            lhs=conditionalExpressionNoIn();

            state._fsp--;

            adaptor.addChild(root_0, lhs.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1145:2: ({...}? assignmentOperator ^ assignmentExpressionNoIn )?
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( (LA42_0==ADDASS||LA42_0==ANDASS||LA42_0==ASSIGN||LA42_0==DIVASS||LA42_0==MODASS||LA42_0==MULASS||LA42_0==ORASS||LA42_0==SHLASS||LA42_0==SHRASS||LA42_0==SHUASS||LA42_0==SUBASS||LA42_0==XORASS) ) {
                int LA42_1 = input.LA(2);

                if ( (( isLeftHandSideAssign(lhs, isLhs) )) ) {
                    alt42=1;
                }
            }
            switch (alt42) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1145:4: {...}? assignmentOperator ^ assignmentExpressionNoIn
                    {
                    if ( !(( isLeftHandSideAssign(lhs, isLhs) )) ) {
                        throw new FailedPredicateException(input, "assignmentExpressionNoIn", " isLeftHandSideAssign(lhs, isLhs) ");
                    }

                    pushFollow(FOLLOW_assignmentOperator_in_assignmentExpressionNoIn4468);
                    assignmentOperator140=assignmentOperator();

                    state._fsp--;

                    root_0 = (Object)adaptor.becomeRoot(assignmentOperator140.getTree(), root_0);

                    pushFollow(FOLLOW_assignmentExpressionNoIn_in_assignmentExpressionNoIn4471);
                    assignmentExpressionNoIn141=assignmentExpressionNoIn();

                    state._fsp--;

                    adaptor.addChild(root_0, assignmentExpressionNoIn141.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "assignmentExpressionNoIn"


    public static class expression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "expression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1152:1: expression :exprs+= assignmentExpression ( COMMA exprs+= assignmentExpression )* -> { $exprs.size() > 1 }? ^( CEXPR ( $exprs)+ ) -> $exprs;
    public final ES3Parser.expression_return expression() throws RecognitionException {
        ES3Parser.expression_return retval = new ES3Parser.expression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token COMMA142=null;
        List list_exprs=null;
        RuleReturnScope exprs = null;
        Object COMMA142_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_assignmentExpression=new RewriteRuleSubtreeStream(adaptor,"rule assignmentExpression");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1153:2: (exprs+= assignmentExpression ( COMMA exprs+= assignmentExpression )* -> { $exprs.size() > 1 }? ^( CEXPR ( $exprs)+ ) -> $exprs)
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1153:4: exprs+= assignmentExpression ( COMMA exprs+= assignmentExpression )*
            {
            pushFollow(FOLLOW_assignmentExpression_in_expression4493);
            exprs=assignmentExpression();

            state._fsp--;

            stream_assignmentExpression.add(exprs.getTree());
            if (list_exprs==null) list_exprs=new ArrayList();
            list_exprs.add(exprs.getTree());


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1153:32: ( COMMA exprs+= assignmentExpression )*
            loop43:
            do {
                int alt43=2;
                int LA43_0 = input.LA(1);

                if ( (LA43_0==COMMA) ) {
                    alt43=1;
                }


                switch (alt43) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1153:34: COMMA exprs+= assignmentExpression
            	    {
            	    COMMA142=(Token)match(input,COMMA,FOLLOW_COMMA_in_expression4497);  
            	    stream_COMMA.add(COMMA142);


            	    pushFollow(FOLLOW_assignmentExpression_in_expression4501);
            	    exprs=assignmentExpression();

            	    state._fsp--;

            	    stream_assignmentExpression.add(exprs.getTree());
            	    if (list_exprs==null) list_exprs=new ArrayList();
            	    list_exprs.add(exprs.getTree());


            	    }
            	    break;

            	default :
            	    break loop43;
                }
            } while (true);


            // AST REWRITE
            // elements: exprs, exprs
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: exprs
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_exprs=new RewriteRuleSubtreeStream(adaptor,"token exprs",list_exprs);
            root_0 = (Object)adaptor.nil();
            // 1154:2: -> { $exprs.size() > 1 }? ^( CEXPR ( $exprs)+ )
            if ( list_exprs.size() > 1 ) {
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1154:28: ^( CEXPR ( $exprs)+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(CEXPR, "CEXPR")
                , root_1);

                if ( !(stream_exprs.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_exprs.hasNext() ) {
                    adaptor.addChild(root_1, stream_exprs.nextTree());

                }
                stream_exprs.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            else // 1155:2: -> $exprs
            {
                adaptor.addChild(root_0, stream_exprs.nextTree());

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "expression"


    public static class expressionNoIn_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "expressionNoIn"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1158:1: expressionNoIn :exprs+= assignmentExpressionNoIn ( COMMA exprs+= assignmentExpressionNoIn )* -> { $exprs.size() > 1 }? ^( CEXPR ( $exprs)+ ) -> $exprs;
    public final ES3Parser.expressionNoIn_return expressionNoIn() throws RecognitionException {
        ES3Parser.expressionNoIn_return retval = new ES3Parser.expressionNoIn_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token COMMA143=null;
        List list_exprs=null;
        RuleReturnScope exprs = null;
        Object COMMA143_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_assignmentExpressionNoIn=new RewriteRuleSubtreeStream(adaptor,"rule assignmentExpressionNoIn");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1159:2: (exprs+= assignmentExpressionNoIn ( COMMA exprs+= assignmentExpressionNoIn )* -> { $exprs.size() > 1 }? ^( CEXPR ( $exprs)+ ) -> $exprs)
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1159:4: exprs+= assignmentExpressionNoIn ( COMMA exprs+= assignmentExpressionNoIn )*
            {
            pushFollow(FOLLOW_assignmentExpressionNoIn_in_expressionNoIn4538);
            exprs=assignmentExpressionNoIn();

            state._fsp--;

            stream_assignmentExpressionNoIn.add(exprs.getTree());
            if (list_exprs==null) list_exprs=new ArrayList();
            list_exprs.add(exprs.getTree());


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1159:36: ( COMMA exprs+= assignmentExpressionNoIn )*
            loop44:
            do {
                int alt44=2;
                int LA44_0 = input.LA(1);

                if ( (LA44_0==COMMA) ) {
                    alt44=1;
                }


                switch (alt44) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1159:38: COMMA exprs+= assignmentExpressionNoIn
            	    {
            	    COMMA143=(Token)match(input,COMMA,FOLLOW_COMMA_in_expressionNoIn4542);  
            	    stream_COMMA.add(COMMA143);


            	    pushFollow(FOLLOW_assignmentExpressionNoIn_in_expressionNoIn4546);
            	    exprs=assignmentExpressionNoIn();

            	    state._fsp--;

            	    stream_assignmentExpressionNoIn.add(exprs.getTree());
            	    if (list_exprs==null) list_exprs=new ArrayList();
            	    list_exprs.add(exprs.getTree());


            	    }
            	    break;

            	default :
            	    break loop44;
                }
            } while (true);


            // AST REWRITE
            // elements: exprs, exprs
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: exprs
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_exprs=new RewriteRuleSubtreeStream(adaptor,"token exprs",list_exprs);
            root_0 = (Object)adaptor.nil();
            // 1160:2: -> { $exprs.size() > 1 }? ^( CEXPR ( $exprs)+ )
            if ( list_exprs.size() > 1 ) {
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1160:28: ^( CEXPR ( $exprs)+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(CEXPR, "CEXPR")
                , root_1);

                if ( !(stream_exprs.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_exprs.hasNext() ) {
                    adaptor.addChild(root_1, stream_exprs.nextTree());

                }
                stream_exprs.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            else // 1161:2: -> $exprs
            {
                adaptor.addChild(root_0, stream_exprs.nextTree());

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "expressionNoIn"


    public static class semic_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "semic"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1186:1: semic : ( SEMIC | EOF | RBRACE | EOL | MultiLineComment );
    public final ES3Parser.semic_return semic() throws RecognitionException {
        ES3Parser.semic_return retval = new ES3Parser.semic_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token SEMIC144=null;
        Token EOF145=null;
        Token RBRACE146=null;
        Token EOL147=null;
        Token MultiLineComment148=null;

        Object SEMIC144_tree=null;
        Object EOF145_tree=null;
        Object RBRACE146_tree=null;
        Object EOL147_tree=null;
        Object MultiLineComment148_tree=null;


        	// Mark current position so we can unconsume a RBRACE.
        	int marker = input.mark();
        	// Promote EOL if appropriate	
        	promoteEOL(retval);

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1194:2: ( SEMIC | EOF | RBRACE | EOL | MultiLineComment )
            int alt45=5;
            switch ( input.LA(1) ) {
            case SEMIC:
                {
                alt45=1;
                }
                break;
            case EOF:
                {
                alt45=2;
                }
                break;
            case RBRACE:
                {
                alt45=3;
                }
                break;
            case EOL:
                {
                alt45=4;
                }
                break;
            case MultiLineComment:
                {
                alt45=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 45, 0, input);

                throw nvae;

            }

            switch (alt45) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1194:4: SEMIC
                    {
                    root_0 = (Object)adaptor.nil();


                    SEMIC144=(Token)match(input,SEMIC,FOLLOW_SEMIC_in_semic4597); 
                    SEMIC144_tree = 
                    (Object)adaptor.create(SEMIC144)
                    ;
                    adaptor.addChild(root_0, SEMIC144_tree);


                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1195:4: EOF
                    {
                    root_0 = (Object)adaptor.nil();


                    EOF145=(Token)match(input,EOF,FOLLOW_EOF_in_semic4602); 
                    EOF145_tree = 
                    (Object)adaptor.create(EOF145)
                    ;
                    adaptor.addChild(root_0, EOF145_tree);


                    }
                    break;
                case 3 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1196:4: RBRACE
                    {
                    root_0 = (Object)adaptor.nil();


                    RBRACE146=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_semic4607); 
                    RBRACE146_tree = 
                    (Object)adaptor.create(RBRACE146)
                    ;
                    adaptor.addChild(root_0, RBRACE146_tree);


                     input.rewind(marker); 

                    }
                    break;
                case 4 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1197:4: EOL
                    {
                    root_0 = (Object)adaptor.nil();


                    EOL147=(Token)match(input,EOL,FOLLOW_EOL_in_semic4614); 
                    EOL147_tree = 
                    (Object)adaptor.create(EOL147)
                    ;
                    adaptor.addChild(root_0, EOL147_tree);


                    }
                    break;
                case 5 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1197:10: MultiLineComment
                    {
                    root_0 = (Object)adaptor.nil();


                    MultiLineComment148=(Token)match(input,MultiLineComment,FOLLOW_MultiLineComment_in_semic4618); 
                    MultiLineComment148_tree = 
                    (Object)adaptor.create(MultiLineComment148)
                    ;
                    adaptor.addChild(root_0, MultiLineComment148_tree);


                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "semic"


    public static class statement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "statement"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1205:1: statement options {k=1; } : ({...}? block | statementTail );
    public final ES3Parser.statement_return statement() throws RecognitionException {
        ES3Parser.statement_return retval = new ES3Parser.statement_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        ES3Parser.block_return block149 =null;

        ES3Parser.statementTail_return statementTail150 =null;



        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1210:2: ({...}? block | statementTail )
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==LBRACE) ) {
                int LA46_1 = input.LA(2);

                if ( (( input.LA(1) == LBRACE )) ) {
                    alt46=1;
                }
                else if ( (true) ) {
                    alt46=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 46, 1, input);

                    throw nvae;

                }
            }
            else if ( (LA46_0==ADD||LA46_0==BREAK||LA46_0==CONTINUE||LA46_0==DEC||LA46_0==DELETE||LA46_0==DO||LA46_0==DecimalLiteral||LA46_0==FALSE||LA46_0==FOR||(LA46_0 >= FSSTART && LA46_0 <= FUNCTION)||(LA46_0 >= HexIntegerLiteral && LA46_0 <= IF)||LA46_0==INC||LA46_0==INV||LA46_0==Identifier||LA46_0==LBRACK||LA46_0==LPAREN||(LA46_0 >= NEW && LA46_0 <= NOT)||LA46_0==NULL||LA46_0==OctalIntegerLiteral||LA46_0==RETURN||LA46_0==RegularExpressionLiteral||LA46_0==SEMIC||LA46_0==SUB||LA46_0==SWITCH||LA46_0==StringLiteral||(LA46_0 >= THIS && LA46_0 <= THROW)||(LA46_0 >= TRUE && LA46_0 <= TYPEOF)||(LA46_0 >= VAR && LA46_0 <= VOID)||(LA46_0 >= WHILE && LA46_0 <= WITH)) ) {
                alt46=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 46, 0, input);

                throw nvae;

            }
            switch (alt46) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1210:4: {...}? block
                    {
                    root_0 = (Object)adaptor.nil();


                    if ( !(( input.LA(1) == LBRACE )) ) {
                        throw new FailedPredicateException(input, "statement", " input.LA(1) == LBRACE ");
                    }

                    pushFollow(FOLLOW_block_in_statement4647);
                    block149=block();

                    state._fsp--;

                    adaptor.addChild(root_0, block149.getTree());

                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1211:4: statementTail
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_statementTail_in_statement4652);
                    statementTail150=statementTail();

                    state._fsp--;

                    adaptor.addChild(root_0, statementTail150.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "statement"


    public static class statementTail_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "statementTail"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1214:1: statementTail : ( variableStatement | emptyStatement | expressionStatement | ifStatement | iterationStatement | continueStatement | breakStatement | returnStatement | withStatement | labelledStatement | switchStatement | throwStatement | tryStatement );
    public final ES3Parser.statementTail_return statementTail() throws RecognitionException {
        ES3Parser.statementTail_return retval = new ES3Parser.statementTail_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        ES3Parser.variableStatement_return variableStatement151 =null;

        ES3Parser.emptyStatement_return emptyStatement152 =null;

        ES3Parser.expressionStatement_return expressionStatement153 =null;

        ES3Parser.ifStatement_return ifStatement154 =null;

        ES3Parser.iterationStatement_return iterationStatement155 =null;

        ES3Parser.continueStatement_return continueStatement156 =null;

        ES3Parser.breakStatement_return breakStatement157 =null;

        ES3Parser.returnStatement_return returnStatement158 =null;

        ES3Parser.withStatement_return withStatement159 =null;

        ES3Parser.labelledStatement_return labelledStatement160 =null;

        ES3Parser.switchStatement_return switchStatement161 =null;

        ES3Parser.throwStatement_return throwStatement162 =null;

        ES3Parser.tryStatement_return tryStatement163 =null;



        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1215:2: ( variableStatement | emptyStatement | expressionStatement | ifStatement | iterationStatement | continueStatement | breakStatement | returnStatement | withStatement | labelledStatement | switchStatement | throwStatement | tryStatement )
            int alt47=13;
            switch ( input.LA(1) ) {
            case VAR:
                {
                alt47=1;
                }
                break;
            case SEMIC:
                {
                alt47=2;
                }
                break;
            case ADD:
            case DEC:
            case DELETE:
            case DecimalLiteral:
            case FALSE:
            case FSSTART:
            case FUNCTION:
            case HexIntegerLiteral:
            case INC:
            case INV:
            case LBRACE:
            case LBRACK:
            case LPAREN:
            case NEW:
            case NOT:
            case NULL:
            case OctalIntegerLiteral:
            case RegularExpressionLiteral:
            case SUB:
            case StringLiteral:
            case THIS:
            case TRUE:
            case TYPEOF:
            case VOID:
                {
                alt47=3;
                }
                break;
            case Identifier:
                {
                int LA47_4 = input.LA(2);

                if ( (LA47_4==COLON) ) {
                    alt47=10;
                }
                else if ( (LA47_4==EOF||(LA47_4 >= ADD && LA47_4 <= ANDASS)||LA47_4==ASSIGN||LA47_4==COMMA||LA47_4==DEC||(LA47_4 >= DIV && LA47_4 <= DIVASS)||LA47_4==DOT||(LA47_4 >= EOL && LA47_4 <= EQ)||(LA47_4 >= GT && LA47_4 <= GTE)||(LA47_4 >= IN && LA47_4 <= INSTANCEOF)||LA47_4==LAND||LA47_4==LBRACK||(LA47_4 >= LOR && LA47_4 <= LPAREN)||(LA47_4 >= LT && LA47_4 <= LTE)||(LA47_4 >= MOD && LA47_4 <= MultiLineComment)||LA47_4==NEQ||LA47_4==NSAME||(LA47_4 >= OR && LA47_4 <= ORASS)||(LA47_4 >= QUE && LA47_4 <= RBRACE)||(LA47_4 >= SAME && LA47_4 <= SHLASS)||(LA47_4 >= SHR && LA47_4 <= SHUASS)||(LA47_4 >= SUB && LA47_4 <= SUBASS)||(LA47_4 >= XOR && LA47_4 <= XORASS)) ) {
                    alt47=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 47, 4, input);

                    throw nvae;

                }
                }
                break;
            case IF:
                {
                alt47=4;
                }
                break;
            case DO:
            case FOR:
            case WHILE:
                {
                alt47=5;
                }
                break;
            case CONTINUE:
                {
                alt47=6;
                }
                break;
            case BREAK:
                {
                alt47=7;
                }
                break;
            case RETURN:
                {
                alt47=8;
                }
                break;
            case WITH:
                {
                alt47=9;
                }
                break;
            case SWITCH:
                {
                alt47=11;
                }
                break;
            case THROW:
                {
                alt47=12;
                }
                break;
            case TRY:
                {
                alt47=13;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 47, 0, input);

                throw nvae;

            }

            switch (alt47) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1215:4: variableStatement
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_variableStatement_in_statementTail4664);
                    variableStatement151=variableStatement();

                    state._fsp--;

                    adaptor.addChild(root_0, variableStatement151.getTree());

                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1216:4: emptyStatement
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_emptyStatement_in_statementTail4669);
                    emptyStatement152=emptyStatement();

                    state._fsp--;

                    adaptor.addChild(root_0, emptyStatement152.getTree());

                    }
                    break;
                case 3 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1217:4: expressionStatement
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_expressionStatement_in_statementTail4674);
                    expressionStatement153=expressionStatement();

                    state._fsp--;

                    adaptor.addChild(root_0, expressionStatement153.getTree());

                    }
                    break;
                case 4 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1218:4: ifStatement
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_ifStatement_in_statementTail4679);
                    ifStatement154=ifStatement();

                    state._fsp--;

                    adaptor.addChild(root_0, ifStatement154.getTree());

                    }
                    break;
                case 5 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1219:4: iterationStatement
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_iterationStatement_in_statementTail4684);
                    iterationStatement155=iterationStatement();

                    state._fsp--;

                    adaptor.addChild(root_0, iterationStatement155.getTree());

                    }
                    break;
                case 6 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1220:4: continueStatement
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_continueStatement_in_statementTail4689);
                    continueStatement156=continueStatement();

                    state._fsp--;

                    adaptor.addChild(root_0, continueStatement156.getTree());

                    }
                    break;
                case 7 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1221:4: breakStatement
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_breakStatement_in_statementTail4694);
                    breakStatement157=breakStatement();

                    state._fsp--;

                    adaptor.addChild(root_0, breakStatement157.getTree());

                    }
                    break;
                case 8 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1222:4: returnStatement
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_returnStatement_in_statementTail4699);
                    returnStatement158=returnStatement();

                    state._fsp--;

                    adaptor.addChild(root_0, returnStatement158.getTree());

                    }
                    break;
                case 9 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1223:4: withStatement
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_withStatement_in_statementTail4704);
                    withStatement159=withStatement();

                    state._fsp--;

                    adaptor.addChild(root_0, withStatement159.getTree());

                    }
                    break;
                case 10 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1224:4: labelledStatement
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_labelledStatement_in_statementTail4709);
                    labelledStatement160=labelledStatement();

                    state._fsp--;

                    adaptor.addChild(root_0, labelledStatement160.getTree());

                    }
                    break;
                case 11 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1225:4: switchStatement
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_switchStatement_in_statementTail4714);
                    switchStatement161=switchStatement();

                    state._fsp--;

                    adaptor.addChild(root_0, switchStatement161.getTree());

                    }
                    break;
                case 12 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1226:4: throwStatement
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_throwStatement_in_statementTail4719);
                    throwStatement162=throwStatement();

                    state._fsp--;

                    adaptor.addChild(root_0, throwStatement162.getTree());

                    }
                    break;
                case 13 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1227:4: tryStatement
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_tryStatement_in_statementTail4724);
                    tryStatement163=tryStatement();

                    state._fsp--;

                    adaptor.addChild(root_0, tryStatement163.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "statementTail"


    public static class block_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "block"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1232:1: block : lb= LBRACE ( statement )* RBRACE -> ^( BLOCK[$lb, \"BLOCK\"] ( statement )* ) ;
    public final ES3Parser.block_return block() throws RecognitionException {
        ES3Parser.block_return retval = new ES3Parser.block_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token lb=null;
        Token RBRACE165=null;
        ES3Parser.statement_return statement164 =null;


        Object lb_tree=null;
        Object RBRACE165_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_statement=new RewriteRuleSubtreeStream(adaptor,"rule statement");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1233:2: (lb= LBRACE ( statement )* RBRACE -> ^( BLOCK[$lb, \"BLOCK\"] ( statement )* ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1233:4: lb= LBRACE ( statement )* RBRACE
            {
            lb=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_block4739);  
            stream_LBRACE.add(lb);


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1233:14: ( statement )*
            loop48:
            do {
                int alt48=2;
                int LA48_0 = input.LA(1);

                if ( (LA48_0==ADD||LA48_0==BREAK||LA48_0==CONTINUE||LA48_0==DEC||LA48_0==DELETE||LA48_0==DO||LA48_0==DecimalLiteral||LA48_0==FALSE||LA48_0==FOR||(LA48_0 >= FSSTART && LA48_0 <= FUNCTION)||(LA48_0 >= HexIntegerLiteral && LA48_0 <= IF)||LA48_0==INC||LA48_0==INV||LA48_0==Identifier||(LA48_0 >= LBRACE && LA48_0 <= LBRACK)||LA48_0==LPAREN||(LA48_0 >= NEW && LA48_0 <= NOT)||LA48_0==NULL||LA48_0==OctalIntegerLiteral||LA48_0==RETURN||LA48_0==RegularExpressionLiteral||LA48_0==SEMIC||LA48_0==SUB||LA48_0==SWITCH||LA48_0==StringLiteral||(LA48_0 >= THIS && LA48_0 <= THROW)||(LA48_0 >= TRUE && LA48_0 <= TYPEOF)||(LA48_0 >= VAR && LA48_0 <= VOID)||(LA48_0 >= WHILE && LA48_0 <= WITH)) ) {
                    alt48=1;
                }


                switch (alt48) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1233:14: statement
            	    {
            	    pushFollow(FOLLOW_statement_in_block4741);
            	    statement164=statement();

            	    state._fsp--;

            	    stream_statement.add(statement164.getTree());

            	    }
            	    break;

            	default :
            	    break loop48;
                }
            } while (true);


            RBRACE165=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_block4744);  
            stream_RBRACE.add(RBRACE165);


            // AST REWRITE
            // elements: statement
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 1234:2: -> ^( BLOCK[$lb, \"BLOCK\"] ( statement )* )
            {
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1234:5: ^( BLOCK[$lb, \"BLOCK\"] ( statement )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(BLOCK, lb, "BLOCK")
                , root_1);

                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1234:28: ( statement )*
                while ( stream_statement.hasNext() ) {
                    adaptor.addChild(root_1, stream_statement.nextTree());

                }
                stream_statement.reset();

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "block"


    public static class variableStatement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "variableStatement"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1241:1: variableStatement : VAR variableDeclaration ( COMMA variableDeclaration )* semic -> ^( VAR ( variableDeclaration )+ ) ;
    public final ES3Parser.variableStatement_return variableStatement() throws RecognitionException {
        ES3Parser.variableStatement_return retval = new ES3Parser.variableStatement_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token VAR166=null;
        Token COMMA168=null;
        ES3Parser.variableDeclaration_return variableDeclaration167 =null;

        ES3Parser.variableDeclaration_return variableDeclaration169 =null;

        ES3Parser.semic_return semic170 =null;


        Object VAR166_tree=null;
        Object COMMA168_tree=null;
        RewriteRuleTokenStream stream_VAR=new RewriteRuleTokenStream(adaptor,"token VAR");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_variableDeclaration=new RewriteRuleSubtreeStream(adaptor,"rule variableDeclaration");
        RewriteRuleSubtreeStream stream_semic=new RewriteRuleSubtreeStream(adaptor,"rule semic");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1242:2: ( VAR variableDeclaration ( COMMA variableDeclaration )* semic -> ^( VAR ( variableDeclaration )+ ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1242:4: VAR variableDeclaration ( COMMA variableDeclaration )* semic
            {
            VAR166=(Token)match(input,VAR,FOLLOW_VAR_in_variableStatement4773);  
            stream_VAR.add(VAR166);


            pushFollow(FOLLOW_variableDeclaration_in_variableStatement4775);
            variableDeclaration167=variableDeclaration();

            state._fsp--;

            stream_variableDeclaration.add(variableDeclaration167.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1242:28: ( COMMA variableDeclaration )*
            loop49:
            do {
                int alt49=2;
                int LA49_0 = input.LA(1);

                if ( (LA49_0==COMMA) ) {
                    alt49=1;
                }


                switch (alt49) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1242:30: COMMA variableDeclaration
            	    {
            	    COMMA168=(Token)match(input,COMMA,FOLLOW_COMMA_in_variableStatement4779);  
            	    stream_COMMA.add(COMMA168);


            	    pushFollow(FOLLOW_variableDeclaration_in_variableStatement4781);
            	    variableDeclaration169=variableDeclaration();

            	    state._fsp--;

            	    stream_variableDeclaration.add(variableDeclaration169.getTree());

            	    }
            	    break;

            	default :
            	    break loop49;
                }
            } while (true);


            pushFollow(FOLLOW_semic_in_variableStatement4786);
            semic170=semic();

            state._fsp--;

            stream_semic.add(semic170.getTree());

            // AST REWRITE
            // elements: VAR, variableDeclaration
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 1243:2: -> ^( VAR ( variableDeclaration )+ )
            {
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1243:5: ^( VAR ( variableDeclaration )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                stream_VAR.nextNode()
                , root_1);

                if ( !(stream_variableDeclaration.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_variableDeclaration.hasNext() ) {
                    adaptor.addChild(root_1, stream_variableDeclaration.nextTree());

                }
                stream_variableDeclaration.reset();

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "variableStatement"


    public static class variableDeclaration_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "variableDeclaration"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1246:1: variableDeclaration : Identifier ( ASSIGN ^ assignmentExpression )? ;
    public final ES3Parser.variableDeclaration_return variableDeclaration() throws RecognitionException {
        ES3Parser.variableDeclaration_return retval = new ES3Parser.variableDeclaration_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token Identifier171=null;
        Token ASSIGN172=null;
        ES3Parser.assignmentExpression_return assignmentExpression173 =null;


        Object Identifier171_tree=null;
        Object ASSIGN172_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1247:2: ( Identifier ( ASSIGN ^ assignmentExpression )? )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1247:4: Identifier ( ASSIGN ^ assignmentExpression )?
            {
            root_0 = (Object)adaptor.nil();


            Identifier171=(Token)match(input,Identifier,FOLLOW_Identifier_in_variableDeclaration4809); 
            Identifier171_tree = 
            (Object)adaptor.create(Identifier171)
            ;
            adaptor.addChild(root_0, Identifier171_tree);


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1247:15: ( ASSIGN ^ assignmentExpression )?
            int alt50=2;
            int LA50_0 = input.LA(1);

            if ( (LA50_0==ASSIGN) ) {
                alt50=1;
            }
            switch (alt50) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1247:17: ASSIGN ^ assignmentExpression
                    {
                    ASSIGN172=(Token)match(input,ASSIGN,FOLLOW_ASSIGN_in_variableDeclaration4813); 
                    ASSIGN172_tree = 
                    (Object)adaptor.create(ASSIGN172)
                    ;
                    root_0 = (Object)adaptor.becomeRoot(ASSIGN172_tree, root_0);


                    pushFollow(FOLLOW_assignmentExpression_in_variableDeclaration4816);
                    assignmentExpression173=assignmentExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, assignmentExpression173.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "variableDeclaration"


    public static class variableDeclarationNoIn_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "variableDeclarationNoIn"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1250:1: variableDeclarationNoIn : Identifier ( ASSIGN ^ assignmentExpressionNoIn )? ;
    public final ES3Parser.variableDeclarationNoIn_return variableDeclarationNoIn() throws RecognitionException {
        ES3Parser.variableDeclarationNoIn_return retval = new ES3Parser.variableDeclarationNoIn_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token Identifier174=null;
        Token ASSIGN175=null;
        ES3Parser.assignmentExpressionNoIn_return assignmentExpressionNoIn176 =null;


        Object Identifier174_tree=null;
        Object ASSIGN175_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1251:2: ( Identifier ( ASSIGN ^ assignmentExpressionNoIn )? )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1251:4: Identifier ( ASSIGN ^ assignmentExpressionNoIn )?
            {
            root_0 = (Object)adaptor.nil();


            Identifier174=(Token)match(input,Identifier,FOLLOW_Identifier_in_variableDeclarationNoIn4831); 
            Identifier174_tree = 
            (Object)adaptor.create(Identifier174)
            ;
            adaptor.addChild(root_0, Identifier174_tree);


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1251:15: ( ASSIGN ^ assignmentExpressionNoIn )?
            int alt51=2;
            int LA51_0 = input.LA(1);

            if ( (LA51_0==ASSIGN) ) {
                alt51=1;
            }
            switch (alt51) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1251:17: ASSIGN ^ assignmentExpressionNoIn
                    {
                    ASSIGN175=(Token)match(input,ASSIGN,FOLLOW_ASSIGN_in_variableDeclarationNoIn4835); 
                    ASSIGN175_tree = 
                    (Object)adaptor.create(ASSIGN175)
                    ;
                    root_0 = (Object)adaptor.becomeRoot(ASSIGN175_tree, root_0);


                    pushFollow(FOLLOW_assignmentExpressionNoIn_in_variableDeclarationNoIn4838);
                    assignmentExpressionNoIn176=assignmentExpressionNoIn();

                    state._fsp--;

                    adaptor.addChild(root_0, assignmentExpressionNoIn176.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "variableDeclarationNoIn"


    public static class emptyStatement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "emptyStatement"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1258:1: emptyStatement : SEMIC !;
    public final ES3Parser.emptyStatement_return emptyStatement() throws RecognitionException {
        ES3Parser.emptyStatement_return retval = new ES3Parser.emptyStatement_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token SEMIC177=null;

        Object SEMIC177_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1259:2: ( SEMIC !)
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1259:4: SEMIC !
            {
            root_0 = (Object)adaptor.nil();


            SEMIC177=(Token)match(input,SEMIC,FOLLOW_SEMIC_in_emptyStatement4857); 

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "emptyStatement"


    public static class expressionStatement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "expressionStatement"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1272:1: expressionStatement : expression semic !;
    public final ES3Parser.expressionStatement_return expressionStatement() throws RecognitionException {
        ES3Parser.expressionStatement_return retval = new ES3Parser.expressionStatement_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        ES3Parser.expression_return expression178 =null;

        ES3Parser.semic_return semic179 =null;



        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1273:2: ( expression semic !)
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1273:4: expression semic !
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_expression_in_expressionStatement4876);
            expression178=expression();

            state._fsp--;

            adaptor.addChild(root_0, expression178.getTree());

            pushFollow(FOLLOW_semic_in_expressionStatement4878);
            semic179=semic();

            state._fsp--;


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "expressionStatement"


    public static class ifStatement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "ifStatement"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1280:1: ifStatement : IF LPAREN expression RPAREN statement ({...}? ELSE statement )? -> ^( IF expression ( statement )+ ) ;
    public final ES3Parser.ifStatement_return ifStatement() throws RecognitionException {
        ES3Parser.ifStatement_return retval = new ES3Parser.ifStatement_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token IF180=null;
        Token LPAREN181=null;
        Token RPAREN183=null;
        Token ELSE185=null;
        ES3Parser.expression_return expression182 =null;

        ES3Parser.statement_return statement184 =null;

        ES3Parser.statement_return statement186 =null;


        Object IF180_tree=null;
        Object LPAREN181_tree=null;
        Object RPAREN183_tree=null;
        Object ELSE185_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_IF=new RewriteRuleTokenStream(adaptor,"token IF");
        RewriteRuleTokenStream stream_ELSE=new RewriteRuleTokenStream(adaptor,"token ELSE");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        RewriteRuleSubtreeStream stream_statement=new RewriteRuleSubtreeStream(adaptor,"rule statement");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1282:2: ( IF LPAREN expression RPAREN statement ({...}? ELSE statement )? -> ^( IF expression ( statement )+ ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1282:4: IF LPAREN expression RPAREN statement ({...}? ELSE statement )?
            {
            IF180=(Token)match(input,IF,FOLLOW_IF_in_ifStatement4896);  
            stream_IF.add(IF180);


            LPAREN181=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_ifStatement4898);  
            stream_LPAREN.add(LPAREN181);


            pushFollow(FOLLOW_expression_in_ifStatement4900);
            expression182=expression();

            state._fsp--;

            stream_expression.add(expression182.getTree());

            RPAREN183=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_ifStatement4902);  
            stream_RPAREN.add(RPAREN183);


            pushFollow(FOLLOW_statement_in_ifStatement4904);
            statement184=statement();

            state._fsp--;

            stream_statement.add(statement184.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1282:42: ({...}? ELSE statement )?
            int alt52=2;
            int LA52_0 = input.LA(1);

            if ( (LA52_0==ELSE) ) {
                int LA52_1 = input.LA(2);

                if ( (( input.LA(1) == ELSE )) ) {
                    alt52=1;
                }
            }
            switch (alt52) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1282:44: {...}? ELSE statement
                    {
                    if ( !(( input.LA(1) == ELSE )) ) {
                        throw new FailedPredicateException(input, "ifStatement", " input.LA(1) == ELSE ");
                    }

                    ELSE185=(Token)match(input,ELSE,FOLLOW_ELSE_in_ifStatement4910);  
                    stream_ELSE.add(ELSE185);


                    pushFollow(FOLLOW_statement_in_ifStatement4912);
                    statement186=statement();

                    state._fsp--;

                    stream_statement.add(statement186.getTree());

                    }
                    break;

            }


            // AST REWRITE
            // elements: statement, expression, IF
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 1283:2: -> ^( IF expression ( statement )+ )
            {
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1283:5: ^( IF expression ( statement )+ )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                stream_IF.nextNode()
                , root_1);

                adaptor.addChild(root_1, stream_expression.nextTree());

                if ( !(stream_statement.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_statement.hasNext() ) {
                    adaptor.addChild(root_1, stream_statement.nextTree());

                }
                stream_statement.reset();

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "ifStatement"


    public static class iterationStatement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "iterationStatement"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1290:1: iterationStatement : ( doStatement | whileStatement | forStatement );
    public final ES3Parser.iterationStatement_return iterationStatement() throws RecognitionException {
        ES3Parser.iterationStatement_return retval = new ES3Parser.iterationStatement_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        ES3Parser.doStatement_return doStatement187 =null;

        ES3Parser.whileStatement_return whileStatement188 =null;

        ES3Parser.forStatement_return forStatement189 =null;



        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1291:2: ( doStatement | whileStatement | forStatement )
            int alt53=3;
            switch ( input.LA(1) ) {
            case DO:
                {
                alt53=1;
                }
                break;
            case WHILE:
                {
                alt53=2;
                }
                break;
            case FOR:
                {
                alt53=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 53, 0, input);

                throw nvae;

            }

            switch (alt53) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1291:4: doStatement
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_doStatement_in_iterationStatement4945);
                    doStatement187=doStatement();

                    state._fsp--;

                    adaptor.addChild(root_0, doStatement187.getTree());

                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1292:4: whileStatement
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_whileStatement_in_iterationStatement4950);
                    whileStatement188=whileStatement();

                    state._fsp--;

                    adaptor.addChild(root_0, whileStatement188.getTree());

                    }
                    break;
                case 3 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1293:4: forStatement
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_forStatement_in_iterationStatement4955);
                    forStatement189=forStatement();

                    state._fsp--;

                    adaptor.addChild(root_0, forStatement189.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "iterationStatement"


    public static class doStatement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "doStatement"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1296:1: doStatement : DO statement WHILE LPAREN expression RPAREN semic -> ^( DO statement expression ) ;
    public final ES3Parser.doStatement_return doStatement() throws RecognitionException {
        ES3Parser.doStatement_return retval = new ES3Parser.doStatement_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token DO190=null;
        Token WHILE192=null;
        Token LPAREN193=null;
        Token RPAREN195=null;
        ES3Parser.statement_return statement191 =null;

        ES3Parser.expression_return expression194 =null;

        ES3Parser.semic_return semic196 =null;


        Object DO190_tree=null;
        Object WHILE192_tree=null;
        Object LPAREN193_tree=null;
        Object RPAREN195_tree=null;
        RewriteRuleTokenStream stream_DO=new RewriteRuleTokenStream(adaptor,"token DO");
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_WHILE=new RewriteRuleTokenStream(adaptor,"token WHILE");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_statement=new RewriteRuleSubtreeStream(adaptor,"rule statement");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        RewriteRuleSubtreeStream stream_semic=new RewriteRuleSubtreeStream(adaptor,"rule semic");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1297:2: ( DO statement WHILE LPAREN expression RPAREN semic -> ^( DO statement expression ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1297:4: DO statement WHILE LPAREN expression RPAREN semic
            {
            DO190=(Token)match(input,DO,FOLLOW_DO_in_doStatement4967);  
            stream_DO.add(DO190);


            pushFollow(FOLLOW_statement_in_doStatement4969);
            statement191=statement();

            state._fsp--;

            stream_statement.add(statement191.getTree());

            WHILE192=(Token)match(input,WHILE,FOLLOW_WHILE_in_doStatement4971);  
            stream_WHILE.add(WHILE192);


            LPAREN193=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_doStatement4973);  
            stream_LPAREN.add(LPAREN193);


            pushFollow(FOLLOW_expression_in_doStatement4975);
            expression194=expression();

            state._fsp--;

            stream_expression.add(expression194.getTree());

            RPAREN195=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_doStatement4977);  
            stream_RPAREN.add(RPAREN195);


            pushFollow(FOLLOW_semic_in_doStatement4979);
            semic196=semic();

            state._fsp--;

            stream_semic.add(semic196.getTree());

            // AST REWRITE
            // elements: DO, expression, statement
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 1298:2: -> ^( DO statement expression )
            {
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1298:5: ^( DO statement expression )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                stream_DO.nextNode()
                , root_1);

                adaptor.addChild(root_1, stream_statement.nextTree());

                adaptor.addChild(root_1, stream_expression.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "doStatement"


    public static class whileStatement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "whileStatement"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1301:1: whileStatement : WHILE ^ LPAREN ! expression RPAREN ! statement ;
    public final ES3Parser.whileStatement_return whileStatement() throws RecognitionException {
        ES3Parser.whileStatement_return retval = new ES3Parser.whileStatement_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token WHILE197=null;
        Token LPAREN198=null;
        Token RPAREN200=null;
        ES3Parser.expression_return expression199 =null;

        ES3Parser.statement_return statement201 =null;


        Object WHILE197_tree=null;
        Object LPAREN198_tree=null;
        Object RPAREN200_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1302:2: ( WHILE ^ LPAREN ! expression RPAREN ! statement )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1302:4: WHILE ^ LPAREN ! expression RPAREN ! statement
            {
            root_0 = (Object)adaptor.nil();


            WHILE197=(Token)match(input,WHILE,FOLLOW_WHILE_in_whileStatement5004); 
            WHILE197_tree = 
            (Object)adaptor.create(WHILE197)
            ;
            root_0 = (Object)adaptor.becomeRoot(WHILE197_tree, root_0);


            LPAREN198=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_whileStatement5007); 

            pushFollow(FOLLOW_expression_in_whileStatement5010);
            expression199=expression();

            state._fsp--;

            adaptor.addChild(root_0, expression199.getTree());

            RPAREN200=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_whileStatement5012); 

            pushFollow(FOLLOW_statement_in_whileStatement5015);
            statement201=statement();

            state._fsp--;

            adaptor.addChild(root_0, statement201.getTree());

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "whileStatement"


    public static class forStatement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "forStatement"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1346:1: forStatement : FOR ^ LPAREN ! forControl RPAREN ! statement ;
    public final ES3Parser.forStatement_return forStatement() throws RecognitionException {
        ES3Parser.forStatement_return retval = new ES3Parser.forStatement_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token FOR202=null;
        Token LPAREN203=null;
        Token RPAREN205=null;
        ES3Parser.forControl_return forControl204 =null;

        ES3Parser.statement_return statement206 =null;


        Object FOR202_tree=null;
        Object LPAREN203_tree=null;
        Object RPAREN205_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1347:2: ( FOR ^ LPAREN ! forControl RPAREN ! statement )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1347:4: FOR ^ LPAREN ! forControl RPAREN ! statement
            {
            root_0 = (Object)adaptor.nil();


            FOR202=(Token)match(input,FOR,FOLLOW_FOR_in_forStatement5028); 
            FOR202_tree = 
            (Object)adaptor.create(FOR202)
            ;
            root_0 = (Object)adaptor.becomeRoot(FOR202_tree, root_0);


            LPAREN203=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_forStatement5031); 

            pushFollow(FOLLOW_forControl_in_forStatement5034);
            forControl204=forControl();

            state._fsp--;

            adaptor.addChild(root_0, forControl204.getTree());

            RPAREN205=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_forStatement5036); 

            pushFollow(FOLLOW_statement_in_forStatement5039);
            statement206=statement();

            state._fsp--;

            adaptor.addChild(root_0, statement206.getTree());

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "forStatement"


    public static class forControl_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "forControl"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1350:1: forControl : ( forControlVar | forControlExpression | forControlSemic );
    public final ES3Parser.forControl_return forControl() throws RecognitionException {
        ES3Parser.forControl_return retval = new ES3Parser.forControl_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        ES3Parser.forControlVar_return forControlVar207 =null;

        ES3Parser.forControlExpression_return forControlExpression208 =null;

        ES3Parser.forControlSemic_return forControlSemic209 =null;



        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1351:2: ( forControlVar | forControlExpression | forControlSemic )
            int alt54=3;
            switch ( input.LA(1) ) {
            case VAR:
                {
                alt54=1;
                }
                break;
            case ADD:
            case DEC:
            case DELETE:
            case DecimalLiteral:
            case FALSE:
            case FSSTART:
            case FUNCTION:
            case HexIntegerLiteral:
            case INC:
            case INV:
            case Identifier:
            case LBRACE:
            case LBRACK:
            case LPAREN:
            case NEW:
            case NOT:
            case NULL:
            case OctalIntegerLiteral:
            case RegularExpressionLiteral:
            case SUB:
            case StringLiteral:
            case THIS:
            case TRUE:
            case TYPEOF:
            case VOID:
                {
                alt54=2;
                }
                break;
            case SEMIC:
                {
                alt54=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 54, 0, input);

                throw nvae;

            }

            switch (alt54) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1351:4: forControlVar
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_forControlVar_in_forControl5050);
                    forControlVar207=forControlVar();

                    state._fsp--;

                    adaptor.addChild(root_0, forControlVar207.getTree());

                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1352:4: forControlExpression
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_forControlExpression_in_forControl5055);
                    forControlExpression208=forControlExpression();

                    state._fsp--;

                    adaptor.addChild(root_0, forControlExpression208.getTree());

                    }
                    break;
                case 3 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1353:4: forControlSemic
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_forControlSemic_in_forControl5060);
                    forControlSemic209=forControlSemic();

                    state._fsp--;

                    adaptor.addChild(root_0, forControlSemic209.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "forControl"


    public static class forControlVar_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "forControlVar"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1356:1: forControlVar : VAR variableDeclarationNoIn ( ( IN expression -> ^( FORITER ^( VAR variableDeclarationNoIn ) ^( EXPR expression ) ) ) | ( ( COMMA variableDeclarationNoIn )* SEMIC (ex1= expression )? SEMIC (ex2= expression )? -> ^( FORSTEP ^( VAR ( variableDeclarationNoIn )+ ) ^( EXPR ( $ex1)? ) ^( EXPR ( $ex2)? ) ) ) ) ;
    public final ES3Parser.forControlVar_return forControlVar() throws RecognitionException {
        ES3Parser.forControlVar_return retval = new ES3Parser.forControlVar_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token VAR210=null;
        Token IN212=null;
        Token COMMA214=null;
        Token SEMIC216=null;
        Token SEMIC217=null;
        ES3Parser.expression_return ex1 =null;

        ES3Parser.expression_return ex2 =null;

        ES3Parser.variableDeclarationNoIn_return variableDeclarationNoIn211 =null;

        ES3Parser.expression_return expression213 =null;

        ES3Parser.variableDeclarationNoIn_return variableDeclarationNoIn215 =null;


        Object VAR210_tree=null;
        Object IN212_tree=null;
        Object COMMA214_tree=null;
        Object SEMIC216_tree=null;
        Object SEMIC217_tree=null;
        RewriteRuleTokenStream stream_VAR=new RewriteRuleTokenStream(adaptor,"token VAR");
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleTokenStream stream_SEMIC=new RewriteRuleTokenStream(adaptor,"token SEMIC");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        RewriteRuleSubtreeStream stream_variableDeclarationNoIn=new RewriteRuleSubtreeStream(adaptor,"rule variableDeclarationNoIn");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1357:2: ( VAR variableDeclarationNoIn ( ( IN expression -> ^( FORITER ^( VAR variableDeclarationNoIn ) ^( EXPR expression ) ) ) | ( ( COMMA variableDeclarationNoIn )* SEMIC (ex1= expression )? SEMIC (ex2= expression )? -> ^( FORSTEP ^( VAR ( variableDeclarationNoIn )+ ) ^( EXPR ( $ex1)? ) ^( EXPR ( $ex2)? ) ) ) ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1357:4: VAR variableDeclarationNoIn ( ( IN expression -> ^( FORITER ^( VAR variableDeclarationNoIn ) ^( EXPR expression ) ) ) | ( ( COMMA variableDeclarationNoIn )* SEMIC (ex1= expression )? SEMIC (ex2= expression )? -> ^( FORSTEP ^( VAR ( variableDeclarationNoIn )+ ) ^( EXPR ( $ex1)? ) ^( EXPR ( $ex2)? ) ) ) )
            {
            VAR210=(Token)match(input,VAR,FOLLOW_VAR_in_forControlVar5071);  
            stream_VAR.add(VAR210);


            pushFollow(FOLLOW_variableDeclarationNoIn_in_forControlVar5073);
            variableDeclarationNoIn211=variableDeclarationNoIn();

            state._fsp--;

            stream_variableDeclarationNoIn.add(variableDeclarationNoIn211.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1358:2: ( ( IN expression -> ^( FORITER ^( VAR variableDeclarationNoIn ) ^( EXPR expression ) ) ) | ( ( COMMA variableDeclarationNoIn )* SEMIC (ex1= expression )? SEMIC (ex2= expression )? -> ^( FORSTEP ^( VAR ( variableDeclarationNoIn )+ ) ^( EXPR ( $ex1)? ) ^( EXPR ( $ex2)? ) ) ) )
            int alt58=2;
            int LA58_0 = input.LA(1);

            if ( (LA58_0==IN) ) {
                alt58=1;
            }
            else if ( (LA58_0==COMMA||LA58_0==SEMIC) ) {
                alt58=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 58, 0, input);

                throw nvae;

            }
            switch (alt58) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1359:3: ( IN expression -> ^( FORITER ^( VAR variableDeclarationNoIn ) ^( EXPR expression ) ) )
                    {
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1359:3: ( IN expression -> ^( FORITER ^( VAR variableDeclarationNoIn ) ^( EXPR expression ) ) )
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1360:4: IN expression
                    {
                    IN212=(Token)match(input,IN,FOLLOW_IN_in_forControlVar5085);  
                    stream_IN.add(IN212);


                    pushFollow(FOLLOW_expression_in_forControlVar5087);
                    expression213=expression();

                    state._fsp--;

                    stream_expression.add(expression213.getTree());

                    // AST REWRITE
                    // elements: variableDeclarationNoIn, VAR, expression
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 1361:4: -> ^( FORITER ^( VAR variableDeclarationNoIn ) ^( EXPR expression ) )
                    {
                        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1361:7: ^( FORITER ^( VAR variableDeclarationNoIn ) ^( EXPR expression ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(FORITER, "FORITER")
                        , root_1);

                        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1361:18: ^( VAR variableDeclarationNoIn )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        stream_VAR.nextNode()
                        , root_2);

                        adaptor.addChild(root_2, stream_variableDeclarationNoIn.nextTree());

                        adaptor.addChild(root_1, root_2);
                        }

                        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1361:51: ^( EXPR expression )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(EXPR, "EXPR")
                        , root_2);

                        adaptor.addChild(root_2, stream_expression.nextTree());

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;

                    }


                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1364:3: ( ( COMMA variableDeclarationNoIn )* SEMIC (ex1= expression )? SEMIC (ex2= expression )? -> ^( FORSTEP ^( VAR ( variableDeclarationNoIn )+ ) ^( EXPR ( $ex1)? ) ^( EXPR ( $ex2)? ) ) )
                    {
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1364:3: ( ( COMMA variableDeclarationNoIn )* SEMIC (ex1= expression )? SEMIC (ex2= expression )? -> ^( FORSTEP ^( VAR ( variableDeclarationNoIn )+ ) ^( EXPR ( $ex1)? ) ^( EXPR ( $ex2)? ) ) )
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1365:4: ( COMMA variableDeclarationNoIn )* SEMIC (ex1= expression )? SEMIC (ex2= expression )?
                    {
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1365:4: ( COMMA variableDeclarationNoIn )*
                    loop55:
                    do {
                        int alt55=2;
                        int LA55_0 = input.LA(1);

                        if ( (LA55_0==COMMA) ) {
                            alt55=1;
                        }


                        switch (alt55) {
                    	case 1 :
                    	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1365:6: COMMA variableDeclarationNoIn
                    	    {
                    	    COMMA214=(Token)match(input,COMMA,FOLLOW_COMMA_in_forControlVar5133);  
                    	    stream_COMMA.add(COMMA214);


                    	    pushFollow(FOLLOW_variableDeclarationNoIn_in_forControlVar5135);
                    	    variableDeclarationNoIn215=variableDeclarationNoIn();

                    	    state._fsp--;

                    	    stream_variableDeclarationNoIn.add(variableDeclarationNoIn215.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop55;
                        }
                    } while (true);


                    SEMIC216=(Token)match(input,SEMIC,FOLLOW_SEMIC_in_forControlVar5140);  
                    stream_SEMIC.add(SEMIC216);


                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1365:48: (ex1= expression )?
                    int alt56=2;
                    int LA56_0 = input.LA(1);

                    if ( (LA56_0==ADD||LA56_0==DEC||LA56_0==DELETE||LA56_0==DecimalLiteral||LA56_0==FALSE||(LA56_0 >= FSSTART && LA56_0 <= FUNCTION)||LA56_0==HexIntegerLiteral||LA56_0==INC||LA56_0==INV||LA56_0==Identifier||(LA56_0 >= LBRACE && LA56_0 <= LBRACK)||LA56_0==LPAREN||(LA56_0 >= NEW && LA56_0 <= NOT)||LA56_0==NULL||LA56_0==OctalIntegerLiteral||LA56_0==RegularExpressionLiteral||LA56_0==SUB||LA56_0==StringLiteral||LA56_0==THIS||LA56_0==TRUE||LA56_0==TYPEOF||LA56_0==VOID) ) {
                        alt56=1;
                    }
                    switch (alt56) {
                        case 1 :
                            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1365:48: ex1= expression
                            {
                            pushFollow(FOLLOW_expression_in_forControlVar5144);
                            ex1=expression();

                            state._fsp--;

                            stream_expression.add(ex1.getTree());

                            }
                            break;

                    }


                    SEMIC217=(Token)match(input,SEMIC,FOLLOW_SEMIC_in_forControlVar5147);  
                    stream_SEMIC.add(SEMIC217);


                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1365:70: (ex2= expression )?
                    int alt57=2;
                    int LA57_0 = input.LA(1);

                    if ( (LA57_0==ADD||LA57_0==DEC||LA57_0==DELETE||LA57_0==DecimalLiteral||LA57_0==FALSE||(LA57_0 >= FSSTART && LA57_0 <= FUNCTION)||LA57_0==HexIntegerLiteral||LA57_0==INC||LA57_0==INV||LA57_0==Identifier||(LA57_0 >= LBRACE && LA57_0 <= LBRACK)||LA57_0==LPAREN||(LA57_0 >= NEW && LA57_0 <= NOT)||LA57_0==NULL||LA57_0==OctalIntegerLiteral||LA57_0==RegularExpressionLiteral||LA57_0==SUB||LA57_0==StringLiteral||LA57_0==THIS||LA57_0==TRUE||LA57_0==TYPEOF||LA57_0==VOID) ) {
                        alt57=1;
                    }
                    switch (alt57) {
                        case 1 :
                            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1365:70: ex2= expression
                            {
                            pushFollow(FOLLOW_expression_in_forControlVar5151);
                            ex2=expression();

                            state._fsp--;

                            stream_expression.add(ex2.getTree());

                            }
                            break;

                    }


                    // AST REWRITE
                    // elements: VAR, ex1, variableDeclarationNoIn, ex2
                    // token labels: 
                    // rule labels: retval, ex2, ex1
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_ex2=new RewriteRuleSubtreeStream(adaptor,"rule ex2",ex2!=null?ex2.tree:null);
                    RewriteRuleSubtreeStream stream_ex1=new RewriteRuleSubtreeStream(adaptor,"rule ex1",ex1!=null?ex1.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 1366:4: -> ^( FORSTEP ^( VAR ( variableDeclarationNoIn )+ ) ^( EXPR ( $ex1)? ) ^( EXPR ( $ex2)? ) )
                    {
                        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1366:7: ^( FORSTEP ^( VAR ( variableDeclarationNoIn )+ ) ^( EXPR ( $ex1)? ) ^( EXPR ( $ex2)? ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(FORSTEP, "FORSTEP")
                        , root_1);

                        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1366:18: ^( VAR ( variableDeclarationNoIn )+ )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        stream_VAR.nextNode()
                        , root_2);

                        if ( !(stream_variableDeclarationNoIn.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_variableDeclarationNoIn.hasNext() ) {
                            adaptor.addChild(root_2, stream_variableDeclarationNoIn.nextTree());

                        }
                        stream_variableDeclarationNoIn.reset();

                        adaptor.addChild(root_1, root_2);
                        }

                        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1366:52: ^( EXPR ( $ex1)? )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(EXPR, "EXPR")
                        , root_2);

                        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1366:61: ( $ex1)?
                        if ( stream_ex1.hasNext() ) {
                            adaptor.addChild(root_2, stream_ex1.nextTree());

                        }
                        stream_ex1.reset();

                        adaptor.addChild(root_1, root_2);
                        }

                        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1366:68: ^( EXPR ( $ex2)? )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(EXPR, "EXPR")
                        , root_2);

                        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1366:77: ( $ex2)?
                        if ( stream_ex2.hasNext() ) {
                            adaptor.addChild(root_2, stream_ex2.nextTree());

                        }
                        stream_ex2.reset();

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;

                    }


                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "forControlVar"


    public static class forControlExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "forControlExpression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1371:1: forControlExpression : ex1= expressionNoIn ({...}? ( IN ex2= expression -> ^( FORITER ^( EXPR $ex1) ^( EXPR $ex2) ) ) | ( SEMIC (ex2= expression )? SEMIC (ex3= expression )? -> ^( FORSTEP ^( EXPR $ex1) ^( EXPR ( $ex2)? ) ^( EXPR ( $ex3)? ) ) ) ) ;
    public final ES3Parser.forControlExpression_return forControlExpression() throws RecognitionException {
        ES3Parser.forControlExpression_return retval = new ES3Parser.forControlExpression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token IN218=null;
        Token SEMIC219=null;
        Token SEMIC220=null;
        ES3Parser.expressionNoIn_return ex1 =null;

        ES3Parser.expression_return ex2 =null;

        ES3Parser.expression_return ex3 =null;


        Object IN218_tree=null;
        Object SEMIC219_tree=null;
        Object SEMIC220_tree=null;
        RewriteRuleTokenStream stream_IN=new RewriteRuleTokenStream(adaptor,"token IN");
        RewriteRuleTokenStream stream_SEMIC=new RewriteRuleTokenStream(adaptor,"token SEMIC");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        RewriteRuleSubtreeStream stream_expressionNoIn=new RewriteRuleSubtreeStream(adaptor,"rule expressionNoIn");

        	Object[] isLhs = new Object[1];

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1376:2: (ex1= expressionNoIn ({...}? ( IN ex2= expression -> ^( FORITER ^( EXPR $ex1) ^( EXPR $ex2) ) ) | ( SEMIC (ex2= expression )? SEMIC (ex3= expression )? -> ^( FORSTEP ^( EXPR $ex1) ^( EXPR ( $ex2)? ) ^( EXPR ( $ex3)? ) ) ) ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1376:4: ex1= expressionNoIn ({...}? ( IN ex2= expression -> ^( FORITER ^( EXPR $ex1) ^( EXPR $ex2) ) ) | ( SEMIC (ex2= expression )? SEMIC (ex3= expression )? -> ^( FORSTEP ^( EXPR $ex1) ^( EXPR ( $ex2)? ) ^( EXPR ( $ex3)? ) ) ) )
            {
            pushFollow(FOLLOW_expressionNoIn_in_forControlExpression5217);
            ex1=expressionNoIn();

            state._fsp--;

            stream_expressionNoIn.add(ex1.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1377:2: ({...}? ( IN ex2= expression -> ^( FORITER ^( EXPR $ex1) ^( EXPR $ex2) ) ) | ( SEMIC (ex2= expression )? SEMIC (ex3= expression )? -> ^( FORSTEP ^( EXPR $ex1) ^( EXPR ( $ex2)? ) ^( EXPR ( $ex3)? ) ) ) )
            int alt61=2;
            int LA61_0 = input.LA(1);

            if ( (LA61_0==IN) ) {
                alt61=1;
            }
            else if ( (LA61_0==SEMIC) ) {
                alt61=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 61, 0, input);

                throw nvae;

            }
            switch (alt61) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1378:3: {...}? ( IN ex2= expression -> ^( FORITER ^( EXPR $ex1) ^( EXPR $ex2) ) )
                    {
                    if ( !(( isLeftHandSideIn(ex1, isLhs) )) ) {
                        throw new FailedPredicateException(input, "forControlExpression", " isLeftHandSideIn(ex1, isLhs) ");
                    }

                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1378:37: ( IN ex2= expression -> ^( FORITER ^( EXPR $ex1) ^( EXPR $ex2) ) )
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1379:4: IN ex2= expression
                    {
                    IN218=(Token)match(input,IN,FOLLOW_IN_in_forControlExpression5232);  
                    stream_IN.add(IN218);


                    pushFollow(FOLLOW_expression_in_forControlExpression5236);
                    ex2=expression();

                    state._fsp--;

                    stream_expression.add(ex2.getTree());

                    // AST REWRITE
                    // elements: ex2, ex1
                    // token labels: 
                    // rule labels: retval, ex2, ex1
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_ex2=new RewriteRuleSubtreeStream(adaptor,"rule ex2",ex2!=null?ex2.tree:null);
                    RewriteRuleSubtreeStream stream_ex1=new RewriteRuleSubtreeStream(adaptor,"rule ex1",ex1!=null?ex1.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 1380:4: -> ^( FORITER ^( EXPR $ex1) ^( EXPR $ex2) )
                    {
                        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1380:7: ^( FORITER ^( EXPR $ex1) ^( EXPR $ex2) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(FORITER, "FORITER")
                        , root_1);

                        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1380:18: ^( EXPR $ex1)
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(EXPR, "EXPR")
                        , root_2);

                        adaptor.addChild(root_2, stream_ex1.nextTree());

                        adaptor.addChild(root_1, root_2);
                        }

                        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1380:33: ^( EXPR $ex2)
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(EXPR, "EXPR")
                        , root_2);

                        adaptor.addChild(root_2, stream_ex2.nextTree());

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;

                    }


                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1383:3: ( SEMIC (ex2= expression )? SEMIC (ex3= expression )? -> ^( FORSTEP ^( EXPR $ex1) ^( EXPR ( $ex2)? ) ^( EXPR ( $ex3)? ) ) )
                    {
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1383:3: ( SEMIC (ex2= expression )? SEMIC (ex3= expression )? -> ^( FORSTEP ^( EXPR $ex1) ^( EXPR ( $ex2)? ) ^( EXPR ( $ex3)? ) ) )
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1384:4: SEMIC (ex2= expression )? SEMIC (ex3= expression )?
                    {
                    SEMIC219=(Token)match(input,SEMIC,FOLLOW_SEMIC_in_forControlExpression5282);  
                    stream_SEMIC.add(SEMIC219);


                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1384:13: (ex2= expression )?
                    int alt59=2;
                    int LA59_0 = input.LA(1);

                    if ( (LA59_0==ADD||LA59_0==DEC||LA59_0==DELETE||LA59_0==DecimalLiteral||LA59_0==FALSE||(LA59_0 >= FSSTART && LA59_0 <= FUNCTION)||LA59_0==HexIntegerLiteral||LA59_0==INC||LA59_0==INV||LA59_0==Identifier||(LA59_0 >= LBRACE && LA59_0 <= LBRACK)||LA59_0==LPAREN||(LA59_0 >= NEW && LA59_0 <= NOT)||LA59_0==NULL||LA59_0==OctalIntegerLiteral||LA59_0==RegularExpressionLiteral||LA59_0==SUB||LA59_0==StringLiteral||LA59_0==THIS||LA59_0==TRUE||LA59_0==TYPEOF||LA59_0==VOID) ) {
                        alt59=1;
                    }
                    switch (alt59) {
                        case 1 :
                            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1384:13: ex2= expression
                            {
                            pushFollow(FOLLOW_expression_in_forControlExpression5286);
                            ex2=expression();

                            state._fsp--;

                            stream_expression.add(ex2.getTree());

                            }
                            break;

                    }


                    SEMIC220=(Token)match(input,SEMIC,FOLLOW_SEMIC_in_forControlExpression5289);  
                    stream_SEMIC.add(SEMIC220);


                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1384:35: (ex3= expression )?
                    int alt60=2;
                    int LA60_0 = input.LA(1);

                    if ( (LA60_0==ADD||LA60_0==DEC||LA60_0==DELETE||LA60_0==DecimalLiteral||LA60_0==FALSE||(LA60_0 >= FSSTART && LA60_0 <= FUNCTION)||LA60_0==HexIntegerLiteral||LA60_0==INC||LA60_0==INV||LA60_0==Identifier||(LA60_0 >= LBRACE && LA60_0 <= LBRACK)||LA60_0==LPAREN||(LA60_0 >= NEW && LA60_0 <= NOT)||LA60_0==NULL||LA60_0==OctalIntegerLiteral||LA60_0==RegularExpressionLiteral||LA60_0==SUB||LA60_0==StringLiteral||LA60_0==THIS||LA60_0==TRUE||LA60_0==TYPEOF||LA60_0==VOID) ) {
                        alt60=1;
                    }
                    switch (alt60) {
                        case 1 :
                            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1384:35: ex3= expression
                            {
                            pushFollow(FOLLOW_expression_in_forControlExpression5293);
                            ex3=expression();

                            state._fsp--;

                            stream_expression.add(ex3.getTree());

                            }
                            break;

                    }


                    // AST REWRITE
                    // elements: ex2, ex3, ex1
                    // token labels: 
                    // rule labels: retval, ex3, ex2, ex1
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_ex3=new RewriteRuleSubtreeStream(adaptor,"rule ex3",ex3!=null?ex3.tree:null);
                    RewriteRuleSubtreeStream stream_ex2=new RewriteRuleSubtreeStream(adaptor,"rule ex2",ex2!=null?ex2.tree:null);
                    RewriteRuleSubtreeStream stream_ex1=new RewriteRuleSubtreeStream(adaptor,"rule ex1",ex1!=null?ex1.tree:null);

                    root_0 = (Object)adaptor.nil();
                    // 1385:4: -> ^( FORSTEP ^( EXPR $ex1) ^( EXPR ( $ex2)? ) ^( EXPR ( $ex3)? ) )
                    {
                        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1385:7: ^( FORSTEP ^( EXPR $ex1) ^( EXPR ( $ex2)? ) ^( EXPR ( $ex3)? ) )
                        {
                        Object root_1 = (Object)adaptor.nil();
                        root_1 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(FORSTEP, "FORSTEP")
                        , root_1);

                        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1385:18: ^( EXPR $ex1)
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(EXPR, "EXPR")
                        , root_2);

                        adaptor.addChild(root_2, stream_ex1.nextTree());

                        adaptor.addChild(root_1, root_2);
                        }

                        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1385:33: ^( EXPR ( $ex2)? )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(EXPR, "EXPR")
                        , root_2);

                        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1385:42: ( $ex2)?
                        if ( stream_ex2.hasNext() ) {
                            adaptor.addChild(root_2, stream_ex2.nextTree());

                        }
                        stream_ex2.reset();

                        adaptor.addChild(root_1, root_2);
                        }

                        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1385:49: ^( EXPR ( $ex3)? )
                        {
                        Object root_2 = (Object)adaptor.nil();
                        root_2 = (Object)adaptor.becomeRoot(
                        (Object)adaptor.create(EXPR, "EXPR")
                        , root_2);

                        // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1385:58: ( $ex3)?
                        if ( stream_ex3.hasNext() ) {
                            adaptor.addChild(root_2, stream_ex3.nextTree());

                        }
                        stream_ex3.reset();

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;

                    }


                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "forControlExpression"


    public static class forControlSemic_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "forControlSemic"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1390:1: forControlSemic : SEMIC (ex1= expression )? SEMIC (ex2= expression )? -> ^( FORSTEP ^( EXPR ) ^( EXPR ( $ex1)? ) ^( EXPR ( $ex2)? ) ) ;
    public final ES3Parser.forControlSemic_return forControlSemic() throws RecognitionException {
        ES3Parser.forControlSemic_return retval = new ES3Parser.forControlSemic_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token SEMIC221=null;
        Token SEMIC222=null;
        ES3Parser.expression_return ex1 =null;

        ES3Parser.expression_return ex2 =null;


        Object SEMIC221_tree=null;
        Object SEMIC222_tree=null;
        RewriteRuleTokenStream stream_SEMIC=new RewriteRuleTokenStream(adaptor,"token SEMIC");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1391:2: ( SEMIC (ex1= expression )? SEMIC (ex2= expression )? -> ^( FORSTEP ^( EXPR ) ^( EXPR ( $ex1)? ) ^( EXPR ( $ex2)? ) ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1391:4: SEMIC (ex1= expression )? SEMIC (ex2= expression )?
            {
            SEMIC221=(Token)match(input,SEMIC,FOLLOW_SEMIC_in_forControlSemic5352);  
            stream_SEMIC.add(SEMIC221);


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1391:13: (ex1= expression )?
            int alt62=2;
            int LA62_0 = input.LA(1);

            if ( (LA62_0==ADD||LA62_0==DEC||LA62_0==DELETE||LA62_0==DecimalLiteral||LA62_0==FALSE||(LA62_0 >= FSSTART && LA62_0 <= FUNCTION)||LA62_0==HexIntegerLiteral||LA62_0==INC||LA62_0==INV||LA62_0==Identifier||(LA62_0 >= LBRACE && LA62_0 <= LBRACK)||LA62_0==LPAREN||(LA62_0 >= NEW && LA62_0 <= NOT)||LA62_0==NULL||LA62_0==OctalIntegerLiteral||LA62_0==RegularExpressionLiteral||LA62_0==SUB||LA62_0==StringLiteral||LA62_0==THIS||LA62_0==TRUE||LA62_0==TYPEOF||LA62_0==VOID) ) {
                alt62=1;
            }
            switch (alt62) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1391:13: ex1= expression
                    {
                    pushFollow(FOLLOW_expression_in_forControlSemic5356);
                    ex1=expression();

                    state._fsp--;

                    stream_expression.add(ex1.getTree());

                    }
                    break;

            }


            SEMIC222=(Token)match(input,SEMIC,FOLLOW_SEMIC_in_forControlSemic5359);  
            stream_SEMIC.add(SEMIC222);


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1391:35: (ex2= expression )?
            int alt63=2;
            int LA63_0 = input.LA(1);

            if ( (LA63_0==ADD||LA63_0==DEC||LA63_0==DELETE||LA63_0==DecimalLiteral||LA63_0==FALSE||(LA63_0 >= FSSTART && LA63_0 <= FUNCTION)||LA63_0==HexIntegerLiteral||LA63_0==INC||LA63_0==INV||LA63_0==Identifier||(LA63_0 >= LBRACE && LA63_0 <= LBRACK)||LA63_0==LPAREN||(LA63_0 >= NEW && LA63_0 <= NOT)||LA63_0==NULL||LA63_0==OctalIntegerLiteral||LA63_0==RegularExpressionLiteral||LA63_0==SUB||LA63_0==StringLiteral||LA63_0==THIS||LA63_0==TRUE||LA63_0==TYPEOF||LA63_0==VOID) ) {
                alt63=1;
            }
            switch (alt63) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1391:35: ex2= expression
                    {
                    pushFollow(FOLLOW_expression_in_forControlSemic5363);
                    ex2=expression();

                    state._fsp--;

                    stream_expression.add(ex2.getTree());

                    }
                    break;

            }


            // AST REWRITE
            // elements: ex1, ex2
            // token labels: 
            // rule labels: retval, ex2, ex1
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_ex2=new RewriteRuleSubtreeStream(adaptor,"rule ex2",ex2!=null?ex2.tree:null);
            RewriteRuleSubtreeStream stream_ex1=new RewriteRuleSubtreeStream(adaptor,"rule ex1",ex1!=null?ex1.tree:null);

            root_0 = (Object)adaptor.nil();
            // 1392:2: -> ^( FORSTEP ^( EXPR ) ^( EXPR ( $ex1)? ) ^( EXPR ( $ex2)? ) )
            {
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1392:5: ^( FORSTEP ^( EXPR ) ^( EXPR ( $ex1)? ) ^( EXPR ( $ex2)? ) )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(FORSTEP, "FORSTEP")
                , root_1);

                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1392:16: ^( EXPR )
                {
                Object root_2 = (Object)adaptor.nil();
                root_2 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(EXPR, "EXPR")
                , root_2);

                adaptor.addChild(root_1, root_2);
                }

                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1392:26: ^( EXPR ( $ex1)? )
                {
                Object root_2 = (Object)adaptor.nil();
                root_2 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(EXPR, "EXPR")
                , root_2);

                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1392:35: ( $ex1)?
                if ( stream_ex1.hasNext() ) {
                    adaptor.addChild(root_2, stream_ex1.nextTree());

                }
                stream_ex1.reset();

                adaptor.addChild(root_1, root_2);
                }

                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1392:42: ^( EXPR ( $ex2)? )
                {
                Object root_2 = (Object)adaptor.nil();
                root_2 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(EXPR, "EXPR")
                , root_2);

                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1392:51: ( $ex2)?
                if ( stream_ex2.hasNext() ) {
                    adaptor.addChild(root_2, stream_ex2.nextTree());

                }
                stream_ex2.reset();

                adaptor.addChild(root_1, root_2);
                }

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "forControlSemic"


    public static class continueStatement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "continueStatement"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1404:1: continueStatement : CONTINUE ^ ( Identifier )? semic !;
    public final ES3Parser.continueStatement_return continueStatement() throws RecognitionException {
        ES3Parser.continueStatement_return retval = new ES3Parser.continueStatement_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token CONTINUE223=null;
        Token Identifier224=null;
        ES3Parser.semic_return semic225 =null;


        Object CONTINUE223_tree=null;
        Object Identifier224_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1405:2: ( CONTINUE ^ ( Identifier )? semic !)
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1405:4: CONTINUE ^ ( Identifier )? semic !
            {
            root_0 = (Object)adaptor.nil();


            CONTINUE223=(Token)match(input,CONTINUE,FOLLOW_CONTINUE_in_continueStatement5417); 
            CONTINUE223_tree = 
            (Object)adaptor.create(CONTINUE223)
            ;
            root_0 = (Object)adaptor.becomeRoot(CONTINUE223_tree, root_0);


             if (input.LA(1) == Identifier) promoteEOL(null); 

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1405:67: ( Identifier )?
            int alt64=2;
            int LA64_0 = input.LA(1);

            if ( (LA64_0==Identifier) ) {
                alt64=1;
            }
            switch (alt64) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1405:67: Identifier
                    {
                    Identifier224=(Token)match(input,Identifier,FOLLOW_Identifier_in_continueStatement5422); 
                    Identifier224_tree = 
                    (Object)adaptor.create(Identifier224)
                    ;
                    adaptor.addChild(root_0, Identifier224_tree);


                    }
                    break;

            }


            pushFollow(FOLLOW_semic_in_continueStatement5425);
            semic225=semic();

            state._fsp--;


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "continueStatement"


    public static class breakStatement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "breakStatement"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1417:1: breakStatement : BREAK ^ ( Identifier )? semic !;
    public final ES3Parser.breakStatement_return breakStatement() throws RecognitionException {
        ES3Parser.breakStatement_return retval = new ES3Parser.breakStatement_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token BREAK226=null;
        Token Identifier227=null;
        ES3Parser.semic_return semic228 =null;


        Object BREAK226_tree=null;
        Object Identifier227_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1418:2: ( BREAK ^ ( Identifier )? semic !)
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1418:4: BREAK ^ ( Identifier )? semic !
            {
            root_0 = (Object)adaptor.nil();


            BREAK226=(Token)match(input,BREAK,FOLLOW_BREAK_in_breakStatement5444); 
            BREAK226_tree = 
            (Object)adaptor.create(BREAK226)
            ;
            root_0 = (Object)adaptor.becomeRoot(BREAK226_tree, root_0);


             if (input.LA(1) == Identifier) promoteEOL(null); 

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1418:64: ( Identifier )?
            int alt65=2;
            int LA65_0 = input.LA(1);

            if ( (LA65_0==Identifier) ) {
                alt65=1;
            }
            switch (alt65) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1418:64: Identifier
                    {
                    Identifier227=(Token)match(input,Identifier,FOLLOW_Identifier_in_breakStatement5449); 
                    Identifier227_tree = 
                    (Object)adaptor.create(Identifier227)
                    ;
                    adaptor.addChild(root_0, Identifier227_tree);


                    }
                    break;

            }


            pushFollow(FOLLOW_semic_in_breakStatement5452);
            semic228=semic();

            state._fsp--;


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "breakStatement"


    public static class returnStatement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "returnStatement"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1438:1: returnStatement : RETURN ^ ( expression )? semic !;
    public final ES3Parser.returnStatement_return returnStatement() throws RecognitionException {
        ES3Parser.returnStatement_return retval = new ES3Parser.returnStatement_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token RETURN229=null;
        ES3Parser.expression_return expression230 =null;

        ES3Parser.semic_return semic231 =null;


        Object RETURN229_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1439:2: ( RETURN ^ ( expression )? semic !)
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1439:4: RETURN ^ ( expression )? semic !
            {
            root_0 = (Object)adaptor.nil();


            RETURN229=(Token)match(input,RETURN,FOLLOW_RETURN_in_returnStatement5471); 
            RETURN229_tree = 
            (Object)adaptor.create(RETURN229)
            ;
            root_0 = (Object)adaptor.becomeRoot(RETURN229_tree, root_0);


             promoteEOL(null); 

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1439:34: ( expression )?
            int alt66=2;
            int LA66_0 = input.LA(1);

            if ( (LA66_0==ADD||LA66_0==DEC||LA66_0==DELETE||LA66_0==DecimalLiteral||LA66_0==FALSE||(LA66_0 >= FSSTART && LA66_0 <= FUNCTION)||LA66_0==HexIntegerLiteral||LA66_0==INC||LA66_0==INV||LA66_0==Identifier||(LA66_0 >= LBRACE && LA66_0 <= LBRACK)||LA66_0==LPAREN||(LA66_0 >= NEW && LA66_0 <= NOT)||LA66_0==NULL||LA66_0==OctalIntegerLiteral||LA66_0==RegularExpressionLiteral||LA66_0==SUB||LA66_0==StringLiteral||LA66_0==THIS||LA66_0==TRUE||LA66_0==TYPEOF||LA66_0==VOID) ) {
                alt66=1;
            }
            switch (alt66) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1439:34: expression
                    {
                    pushFollow(FOLLOW_expression_in_returnStatement5476);
                    expression230=expression();

                    state._fsp--;

                    adaptor.addChild(root_0, expression230.getTree());

                    }
                    break;

            }


            pushFollow(FOLLOW_semic_in_returnStatement5479);
            semic231=semic();

            state._fsp--;


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "returnStatement"


    public static class withStatement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "withStatement"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1446:1: withStatement : WITH ^ LPAREN ! expression RPAREN ! statement ;
    public final ES3Parser.withStatement_return withStatement() throws RecognitionException {
        ES3Parser.withStatement_return retval = new ES3Parser.withStatement_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token WITH232=null;
        Token LPAREN233=null;
        Token RPAREN235=null;
        ES3Parser.expression_return expression234 =null;

        ES3Parser.statement_return statement236 =null;


        Object WITH232_tree=null;
        Object LPAREN233_tree=null;
        Object RPAREN235_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1447:2: ( WITH ^ LPAREN ! expression RPAREN ! statement )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1447:4: WITH ^ LPAREN ! expression RPAREN ! statement
            {
            root_0 = (Object)adaptor.nil();


            WITH232=(Token)match(input,WITH,FOLLOW_WITH_in_withStatement5496); 
            WITH232_tree = 
            (Object)adaptor.create(WITH232)
            ;
            root_0 = (Object)adaptor.becomeRoot(WITH232_tree, root_0);


            LPAREN233=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_withStatement5499); 

            pushFollow(FOLLOW_expression_in_withStatement5502);
            expression234=expression();

            state._fsp--;

            adaptor.addChild(root_0, expression234.getTree());

            RPAREN235=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_withStatement5504); 

            pushFollow(FOLLOW_statement_in_withStatement5507);
            statement236=statement();

            state._fsp--;

            adaptor.addChild(root_0, statement236.getTree());

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "withStatement"


    public static class switchStatement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "switchStatement"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1454:1: switchStatement : SWITCH LPAREN expression RPAREN LBRACE ({...}? => defaultClause | caseClause )* RBRACE -> ^( SWITCH expression ( defaultClause )? ( caseClause )* ) ;
    public final ES3Parser.switchStatement_return switchStatement() throws RecognitionException {
        ES3Parser.switchStatement_return retval = new ES3Parser.switchStatement_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token SWITCH237=null;
        Token LPAREN238=null;
        Token RPAREN240=null;
        Token LBRACE241=null;
        Token RBRACE244=null;
        ES3Parser.expression_return expression239 =null;

        ES3Parser.defaultClause_return defaultClause242 =null;

        ES3Parser.caseClause_return caseClause243 =null;


        Object SWITCH237_tree=null;
        Object LPAREN238_tree=null;
        Object RPAREN240_tree=null;
        Object LBRACE241_tree=null;
        Object RBRACE244_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_SWITCH=new RewriteRuleTokenStream(adaptor,"token SWITCH");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_expression=new RewriteRuleSubtreeStream(adaptor,"rule expression");
        RewriteRuleSubtreeStream stream_caseClause=new RewriteRuleSubtreeStream(adaptor,"rule caseClause");
        RewriteRuleSubtreeStream stream_defaultClause=new RewriteRuleSubtreeStream(adaptor,"rule defaultClause");

        	int defaultClauseCount = 0;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1459:2: ( SWITCH LPAREN expression RPAREN LBRACE ({...}? => defaultClause | caseClause )* RBRACE -> ^( SWITCH expression ( defaultClause )? ( caseClause )* ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1459:4: SWITCH LPAREN expression RPAREN LBRACE ({...}? => defaultClause | caseClause )* RBRACE
            {
            SWITCH237=(Token)match(input,SWITCH,FOLLOW_SWITCH_in_switchStatement5528);  
            stream_SWITCH.add(SWITCH237);


            LPAREN238=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_switchStatement5530);  
            stream_LPAREN.add(LPAREN238);


            pushFollow(FOLLOW_expression_in_switchStatement5532);
            expression239=expression();

            state._fsp--;

            stream_expression.add(expression239.getTree());

            RPAREN240=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_switchStatement5534);  
            stream_RPAREN.add(RPAREN240);


            LBRACE241=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_switchStatement5536);  
            stream_LBRACE.add(LBRACE241);


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1459:43: ({...}? => defaultClause | caseClause )*
            loop67:
            do {
                int alt67=3;
                int LA67_0 = input.LA(1);

                if ( (LA67_0==DEFAULT) && (( defaultClauseCount == 0 ))) {
                    alt67=1;
                }
                else if ( (LA67_0==CASE) ) {
                    alt67=2;
                }


                switch (alt67) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1459:45: {...}? => defaultClause
            	    {
            	    if ( !(( defaultClauseCount == 0 )) ) {
            	        throw new FailedPredicateException(input, "switchStatement", " defaultClauseCount == 0 ");
            	    }

            	    pushFollow(FOLLOW_defaultClause_in_switchStatement5543);
            	    defaultClause242=defaultClause();

            	    state._fsp--;

            	    stream_defaultClause.add(defaultClause242.getTree());

            	     defaultClauseCount++; 

            	    }
            	    break;
            	case 2 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1459:118: caseClause
            	    {
            	    pushFollow(FOLLOW_caseClause_in_switchStatement5549);
            	    caseClause243=caseClause();

            	    state._fsp--;

            	    stream_caseClause.add(caseClause243.getTree());

            	    }
            	    break;

            	default :
            	    break loop67;
                }
            } while (true);


            RBRACE244=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_switchStatement5554);  
            stream_RBRACE.add(RBRACE244);


            // AST REWRITE
            // elements: defaultClause, expression, SWITCH, caseClause
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 1460:2: -> ^( SWITCH expression ( defaultClause )? ( caseClause )* )
            {
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1460:5: ^( SWITCH expression ( defaultClause )? ( caseClause )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                stream_SWITCH.nextNode()
                , root_1);

                adaptor.addChild(root_1, stream_expression.nextTree());

                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1460:26: ( defaultClause )?
                if ( stream_defaultClause.hasNext() ) {
                    adaptor.addChild(root_1, stream_defaultClause.nextTree());

                }
                stream_defaultClause.reset();

                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1460:41: ( caseClause )*
                while ( stream_caseClause.hasNext() ) {
                    adaptor.addChild(root_1, stream_caseClause.nextTree());

                }
                stream_caseClause.reset();

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "switchStatement"


    public static class caseClause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "caseClause"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1463:1: caseClause : CASE ^ expression COLON ! ( statement )* ;
    public final ES3Parser.caseClause_return caseClause() throws RecognitionException {
        ES3Parser.caseClause_return retval = new ES3Parser.caseClause_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token CASE245=null;
        Token COLON247=null;
        ES3Parser.expression_return expression246 =null;

        ES3Parser.statement_return statement248 =null;


        Object CASE245_tree=null;
        Object COLON247_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1464:2: ( CASE ^ expression COLON ! ( statement )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1464:4: CASE ^ expression COLON ! ( statement )*
            {
            root_0 = (Object)adaptor.nil();


            CASE245=(Token)match(input,CASE,FOLLOW_CASE_in_caseClause5582); 
            CASE245_tree = 
            (Object)adaptor.create(CASE245)
            ;
            root_0 = (Object)adaptor.becomeRoot(CASE245_tree, root_0);


            pushFollow(FOLLOW_expression_in_caseClause5585);
            expression246=expression();

            state._fsp--;

            adaptor.addChild(root_0, expression246.getTree());

            COLON247=(Token)match(input,COLON,FOLLOW_COLON_in_caseClause5587); 

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1464:28: ( statement )*
            loop68:
            do {
                int alt68=2;
                int LA68_0 = input.LA(1);

                if ( (LA68_0==ADD||LA68_0==BREAK||LA68_0==CONTINUE||LA68_0==DEC||LA68_0==DELETE||LA68_0==DO||LA68_0==DecimalLiteral||LA68_0==FALSE||LA68_0==FOR||(LA68_0 >= FSSTART && LA68_0 <= FUNCTION)||(LA68_0 >= HexIntegerLiteral && LA68_0 <= IF)||LA68_0==INC||LA68_0==INV||LA68_0==Identifier||(LA68_0 >= LBRACE && LA68_0 <= LBRACK)||LA68_0==LPAREN||(LA68_0 >= NEW && LA68_0 <= NOT)||LA68_0==NULL||LA68_0==OctalIntegerLiteral||LA68_0==RETURN||LA68_0==RegularExpressionLiteral||LA68_0==SEMIC||LA68_0==SUB||LA68_0==SWITCH||LA68_0==StringLiteral||(LA68_0 >= THIS && LA68_0 <= THROW)||(LA68_0 >= TRUE && LA68_0 <= TYPEOF)||(LA68_0 >= VAR && LA68_0 <= VOID)||(LA68_0 >= WHILE && LA68_0 <= WITH)) ) {
                    alt68=1;
                }


                switch (alt68) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1464:28: statement
            	    {
            	    pushFollow(FOLLOW_statement_in_caseClause5590);
            	    statement248=statement();

            	    state._fsp--;

            	    adaptor.addChild(root_0, statement248.getTree());

            	    }
            	    break;

            	default :
            	    break loop68;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "caseClause"


    public static class defaultClause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "defaultClause"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1467:1: defaultClause : DEFAULT ^ COLON ! ( statement )* ;
    public final ES3Parser.defaultClause_return defaultClause() throws RecognitionException {
        ES3Parser.defaultClause_return retval = new ES3Parser.defaultClause_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token DEFAULT249=null;
        Token COLON250=null;
        ES3Parser.statement_return statement251 =null;


        Object DEFAULT249_tree=null;
        Object COLON250_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1468:2: ( DEFAULT ^ COLON ! ( statement )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1468:4: DEFAULT ^ COLON ! ( statement )*
            {
            root_0 = (Object)adaptor.nil();


            DEFAULT249=(Token)match(input,DEFAULT,FOLLOW_DEFAULT_in_defaultClause5603); 
            DEFAULT249_tree = 
            (Object)adaptor.create(DEFAULT249)
            ;
            root_0 = (Object)adaptor.becomeRoot(DEFAULT249_tree, root_0);


            COLON250=(Token)match(input,COLON,FOLLOW_COLON_in_defaultClause5606); 

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1468:20: ( statement )*
            loop69:
            do {
                int alt69=2;
                int LA69_0 = input.LA(1);

                if ( (LA69_0==ADD||LA69_0==BREAK||LA69_0==CONTINUE||LA69_0==DEC||LA69_0==DELETE||LA69_0==DO||LA69_0==DecimalLiteral||LA69_0==FALSE||LA69_0==FOR||(LA69_0 >= FSSTART && LA69_0 <= FUNCTION)||(LA69_0 >= HexIntegerLiteral && LA69_0 <= IF)||LA69_0==INC||LA69_0==INV||LA69_0==Identifier||(LA69_0 >= LBRACE && LA69_0 <= LBRACK)||LA69_0==LPAREN||(LA69_0 >= NEW && LA69_0 <= NOT)||LA69_0==NULL||LA69_0==OctalIntegerLiteral||LA69_0==RETURN||LA69_0==RegularExpressionLiteral||LA69_0==SEMIC||LA69_0==SUB||LA69_0==SWITCH||LA69_0==StringLiteral||(LA69_0 >= THIS && LA69_0 <= THROW)||(LA69_0 >= TRUE && LA69_0 <= TYPEOF)||(LA69_0 >= VAR && LA69_0 <= VOID)||(LA69_0 >= WHILE && LA69_0 <= WITH)) ) {
                    alt69=1;
                }


                switch (alt69) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1468:20: statement
            	    {
            	    pushFollow(FOLLOW_statement_in_defaultClause5609);
            	    statement251=statement();

            	    state._fsp--;

            	    adaptor.addChild(root_0, statement251.getTree());

            	    }
            	    break;

            	default :
            	    break loop69;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "defaultClause"


    public static class labelledStatement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "labelledStatement"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1475:1: labelledStatement : Identifier COLON statement -> ^( LABELLED Identifier statement ) ;
    public final ES3Parser.labelledStatement_return labelledStatement() throws RecognitionException {
        ES3Parser.labelledStatement_return retval = new ES3Parser.labelledStatement_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token Identifier252=null;
        Token COLON253=null;
        ES3Parser.statement_return statement254 =null;


        Object Identifier252_tree=null;
        Object COLON253_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_Identifier=new RewriteRuleTokenStream(adaptor,"token Identifier");
        RewriteRuleSubtreeStream stream_statement=new RewriteRuleSubtreeStream(adaptor,"rule statement");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1476:2: ( Identifier COLON statement -> ^( LABELLED Identifier statement ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1476:4: Identifier COLON statement
            {
            Identifier252=(Token)match(input,Identifier,FOLLOW_Identifier_in_labelledStatement5626);  
            stream_Identifier.add(Identifier252);


            COLON253=(Token)match(input,COLON,FOLLOW_COLON_in_labelledStatement5628);  
            stream_COLON.add(COLON253);


            pushFollow(FOLLOW_statement_in_labelledStatement5630);
            statement254=statement();

            state._fsp--;

            stream_statement.add(statement254.getTree());

            // AST REWRITE
            // elements: Identifier, statement
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 1477:2: -> ^( LABELLED Identifier statement )
            {
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1477:5: ^( LABELLED Identifier statement )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(LABELLED, "LABELLED")
                , root_1);

                adaptor.addChild(root_1, 
                stream_Identifier.nextNode()
                );

                adaptor.addChild(root_1, stream_statement.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "labelledStatement"


    public static class throwStatement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "throwStatement"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1499:1: throwStatement : THROW ^ expression semic !;
    public final ES3Parser.throwStatement_return throwStatement() throws RecognitionException {
        ES3Parser.throwStatement_return retval = new ES3Parser.throwStatement_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token THROW255=null;
        ES3Parser.expression_return expression256 =null;

        ES3Parser.semic_return semic257 =null;


        Object THROW255_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1500:2: ( THROW ^ expression semic !)
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1500:4: THROW ^ expression semic !
            {
            root_0 = (Object)adaptor.nil();


            THROW255=(Token)match(input,THROW,FOLLOW_THROW_in_throwStatement5661); 
            THROW255_tree = 
            (Object)adaptor.create(THROW255)
            ;
            root_0 = (Object)adaptor.becomeRoot(THROW255_tree, root_0);


             promoteEOL(null); 

            pushFollow(FOLLOW_expression_in_throwStatement5666);
            expression256=expression();

            state._fsp--;

            adaptor.addChild(root_0, expression256.getTree());

            pushFollow(FOLLOW_semic_in_throwStatement5668);
            semic257=semic();

            state._fsp--;


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "throwStatement"


    public static class tryStatement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "tryStatement"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1507:1: tryStatement : TRY ^ block ( catchClause ( finallyClause )? | finallyClause ) ;
    public final ES3Parser.tryStatement_return tryStatement() throws RecognitionException {
        ES3Parser.tryStatement_return retval = new ES3Parser.tryStatement_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token TRY258=null;
        ES3Parser.block_return block259 =null;

        ES3Parser.catchClause_return catchClause260 =null;

        ES3Parser.finallyClause_return finallyClause261 =null;

        ES3Parser.finallyClause_return finallyClause262 =null;


        Object TRY258_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1508:2: ( TRY ^ block ( catchClause ( finallyClause )? | finallyClause ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1508:4: TRY ^ block ( catchClause ( finallyClause )? | finallyClause )
            {
            root_0 = (Object)adaptor.nil();


            TRY258=(Token)match(input,TRY,FOLLOW_TRY_in_tryStatement5685); 
            TRY258_tree = 
            (Object)adaptor.create(TRY258)
            ;
            root_0 = (Object)adaptor.becomeRoot(TRY258_tree, root_0);


            pushFollow(FOLLOW_block_in_tryStatement5688);
            block259=block();

            state._fsp--;

            adaptor.addChild(root_0, block259.getTree());

            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1508:15: ( catchClause ( finallyClause )? | finallyClause )
            int alt71=2;
            int LA71_0 = input.LA(1);

            if ( (LA71_0==CATCH) ) {
                alt71=1;
            }
            else if ( (LA71_0==FINALLY) ) {
                alt71=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 71, 0, input);

                throw nvae;

            }
            switch (alt71) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1508:17: catchClause ( finallyClause )?
                    {
                    pushFollow(FOLLOW_catchClause_in_tryStatement5692);
                    catchClause260=catchClause();

                    state._fsp--;

                    adaptor.addChild(root_0, catchClause260.getTree());

                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1508:29: ( finallyClause )?
                    int alt70=2;
                    int LA70_0 = input.LA(1);

                    if ( (LA70_0==FINALLY) ) {
                        alt70=1;
                    }
                    switch (alt70) {
                        case 1 :
                            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1508:29: finallyClause
                            {
                            pushFollow(FOLLOW_finallyClause_in_tryStatement5694);
                            finallyClause261=finallyClause();

                            state._fsp--;

                            adaptor.addChild(root_0, finallyClause261.getTree());

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1508:46: finallyClause
                    {
                    pushFollow(FOLLOW_finallyClause_in_tryStatement5699);
                    finallyClause262=finallyClause();

                    state._fsp--;

                    adaptor.addChild(root_0, finallyClause262.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "tryStatement"


    public static class catchClause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "catchClause"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1511:1: catchClause : CATCH ^ LPAREN ! Identifier RPAREN ! block ;
    public final ES3Parser.catchClause_return catchClause() throws RecognitionException {
        ES3Parser.catchClause_return retval = new ES3Parser.catchClause_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token CATCH263=null;
        Token LPAREN264=null;
        Token Identifier265=null;
        Token RPAREN266=null;
        ES3Parser.block_return block267 =null;


        Object CATCH263_tree=null;
        Object LPAREN264_tree=null;
        Object Identifier265_tree=null;
        Object RPAREN266_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1512:2: ( CATCH ^ LPAREN ! Identifier RPAREN ! block )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1512:4: CATCH ^ LPAREN ! Identifier RPAREN ! block
            {
            root_0 = (Object)adaptor.nil();


            CATCH263=(Token)match(input,CATCH,FOLLOW_CATCH_in_catchClause5713); 
            CATCH263_tree = 
            (Object)adaptor.create(CATCH263)
            ;
            root_0 = (Object)adaptor.becomeRoot(CATCH263_tree, root_0);


            LPAREN264=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_catchClause5716); 

            Identifier265=(Token)match(input,Identifier,FOLLOW_Identifier_in_catchClause5719); 
            Identifier265_tree = 
            (Object)adaptor.create(Identifier265)
            ;
            adaptor.addChild(root_0, Identifier265_tree);


            RPAREN266=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_catchClause5721); 

            pushFollow(FOLLOW_block_in_catchClause5724);
            block267=block();

            state._fsp--;

            adaptor.addChild(root_0, block267.getTree());

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "catchClause"


    public static class finallyClause_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "finallyClause"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1515:1: finallyClause : FINALLY ^ block ;
    public final ES3Parser.finallyClause_return finallyClause() throws RecognitionException {
        ES3Parser.finallyClause_return retval = new ES3Parser.finallyClause_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token FINALLY268=null;
        ES3Parser.block_return block269 =null;


        Object FINALLY268_tree=null;

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1516:2: ( FINALLY ^ block )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1516:4: FINALLY ^ block
            {
            root_0 = (Object)adaptor.nil();


            FINALLY268=(Token)match(input,FINALLY,FOLLOW_FINALLY_in_finallyClause5736); 
            FINALLY268_tree = 
            (Object)adaptor.create(FINALLY268)
            ;
            root_0 = (Object)adaptor.becomeRoot(FINALLY268_tree, root_0);


            pushFollow(FOLLOW_block_in_finallyClause5739);
            block269=block();

            state._fsp--;

            adaptor.addChild(root_0, block269.getTree());

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "finallyClause"


    public static class functionDeclaration_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "functionDeclaration"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1529:1: functionDeclaration : FUNCTION name= Identifier formalParameterList functionBody -> ^( FUNCTION $name formalParameterList functionBody ) ;
    public final ES3Parser.functionDeclaration_return functionDeclaration() throws RecognitionException {
        ES3Parser.functionDeclaration_return retval = new ES3Parser.functionDeclaration_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token name=null;
        Token FUNCTION270=null;
        ES3Parser.formalParameterList_return formalParameterList271 =null;

        ES3Parser.functionBody_return functionBody272 =null;


        Object name_tree=null;
        Object FUNCTION270_tree=null;
        RewriteRuleTokenStream stream_FUNCTION=new RewriteRuleTokenStream(adaptor,"token FUNCTION");
        RewriteRuleTokenStream stream_Identifier=new RewriteRuleTokenStream(adaptor,"token Identifier");
        RewriteRuleSubtreeStream stream_functionBody=new RewriteRuleSubtreeStream(adaptor,"rule functionBody");
        RewriteRuleSubtreeStream stream_formalParameterList=new RewriteRuleSubtreeStream(adaptor,"rule formalParameterList");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1530:2: ( FUNCTION name= Identifier formalParameterList functionBody -> ^( FUNCTION $name formalParameterList functionBody ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1530:4: FUNCTION name= Identifier formalParameterList functionBody
            {
            FUNCTION270=(Token)match(input,FUNCTION,FOLLOW_FUNCTION_in_functionDeclaration5760);  
            stream_FUNCTION.add(FUNCTION270);


            name=(Token)match(input,Identifier,FOLLOW_Identifier_in_functionDeclaration5764);  
            stream_Identifier.add(name);


            pushFollow(FOLLOW_formalParameterList_in_functionDeclaration5766);
            formalParameterList271=formalParameterList();

            state._fsp--;

            stream_formalParameterList.add(formalParameterList271.getTree());

            pushFollow(FOLLOW_functionBody_in_functionDeclaration5768);
            functionBody272=functionBody();

            state._fsp--;

            stream_functionBody.add(functionBody272.getTree());

            // AST REWRITE
            // elements: formalParameterList, FUNCTION, functionBody, name
            // token labels: name
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleTokenStream stream_name=new RewriteRuleTokenStream(adaptor,"token name",name);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 1531:2: -> ^( FUNCTION $name formalParameterList functionBody )
            {
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1531:5: ^( FUNCTION $name formalParameterList functionBody )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                stream_FUNCTION.nextNode()
                , root_1);

                adaptor.addChild(root_1, stream_name.nextNode());

                adaptor.addChild(root_1, stream_formalParameterList.nextTree());

                adaptor.addChild(root_1, stream_functionBody.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "functionDeclaration"


    public static class functionExpression_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "functionExpression"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1534:1: functionExpression : FUNCTION (name= Identifier )? formalParameterList functionBody -> ^( FUNCTION ( $name)? formalParameterList functionBody ) ;
    public final ES3Parser.functionExpression_return functionExpression() throws RecognitionException {
        ES3Parser.functionExpression_return retval = new ES3Parser.functionExpression_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token name=null;
        Token FUNCTION273=null;
        ES3Parser.formalParameterList_return formalParameterList274 =null;

        ES3Parser.functionBody_return functionBody275 =null;


        Object name_tree=null;
        Object FUNCTION273_tree=null;
        RewriteRuleTokenStream stream_FUNCTION=new RewriteRuleTokenStream(adaptor,"token FUNCTION");
        RewriteRuleTokenStream stream_Identifier=new RewriteRuleTokenStream(adaptor,"token Identifier");
        RewriteRuleSubtreeStream stream_functionBody=new RewriteRuleSubtreeStream(adaptor,"rule functionBody");
        RewriteRuleSubtreeStream stream_formalParameterList=new RewriteRuleSubtreeStream(adaptor,"rule formalParameterList");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1535:2: ( FUNCTION (name= Identifier )? formalParameterList functionBody -> ^( FUNCTION ( $name)? formalParameterList functionBody ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1535:4: FUNCTION (name= Identifier )? formalParameterList functionBody
            {
            FUNCTION273=(Token)match(input,FUNCTION,FOLLOW_FUNCTION_in_functionExpression5795);  
            stream_FUNCTION.add(FUNCTION273);


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1535:17: (name= Identifier )?
            int alt72=2;
            int LA72_0 = input.LA(1);

            if ( (LA72_0==Identifier) ) {
                alt72=1;
            }
            switch (alt72) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1535:17: name= Identifier
                    {
                    name=(Token)match(input,Identifier,FOLLOW_Identifier_in_functionExpression5799);  
                    stream_Identifier.add(name);


                    }
                    break;

            }


            pushFollow(FOLLOW_formalParameterList_in_functionExpression5802);
            formalParameterList274=formalParameterList();

            state._fsp--;

            stream_formalParameterList.add(formalParameterList274.getTree());

            pushFollow(FOLLOW_functionBody_in_functionExpression5804);
            functionBody275=functionBody();

            state._fsp--;

            stream_functionBody.add(functionBody275.getTree());

            // AST REWRITE
            // elements: functionBody, formalParameterList, FUNCTION, name
            // token labels: name
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleTokenStream stream_name=new RewriteRuleTokenStream(adaptor,"token name",name);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 1536:2: -> ^( FUNCTION ( $name)? formalParameterList functionBody )
            {
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1536:5: ^( FUNCTION ( $name)? formalParameterList functionBody )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                stream_FUNCTION.nextNode()
                , root_1);

                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1536:18: ( $name)?
                if ( stream_name.hasNext() ) {
                    adaptor.addChild(root_1, stream_name.nextNode());

                }
                stream_name.reset();

                adaptor.addChild(root_1, stream_formalParameterList.nextTree());

                adaptor.addChild(root_1, stream_functionBody.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "functionExpression"


    public static class formalParameterList_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "formalParameterList"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1539:1: formalParameterList : LPAREN ( Identifier ( COMMA Identifier )* )? RPAREN -> ^( ARGS ( Identifier )* ) ;
    public final ES3Parser.formalParameterList_return formalParameterList() throws RecognitionException {
        ES3Parser.formalParameterList_return retval = new ES3Parser.formalParameterList_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token LPAREN276=null;
        Token Identifier277=null;
        Token COMMA278=null;
        Token Identifier279=null;
        Token RPAREN280=null;

        Object LPAREN276_tree=null;
        Object Identifier277_tree=null;
        Object COMMA278_tree=null;
        Object Identifier279_tree=null;
        Object RPAREN280_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_Identifier=new RewriteRuleTokenStream(adaptor,"token Identifier");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");

        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1540:2: ( LPAREN ( Identifier ( COMMA Identifier )* )? RPAREN -> ^( ARGS ( Identifier )* ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1540:4: LPAREN ( Identifier ( COMMA Identifier )* )? RPAREN
            {
            LPAREN276=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_formalParameterList5832);  
            stream_LPAREN.add(LPAREN276);


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1540:11: ( Identifier ( COMMA Identifier )* )?
            int alt74=2;
            int LA74_0 = input.LA(1);

            if ( (LA74_0==Identifier) ) {
                alt74=1;
            }
            switch (alt74) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1540:13: Identifier ( COMMA Identifier )*
                    {
                    Identifier277=(Token)match(input,Identifier,FOLLOW_Identifier_in_formalParameterList5836);  
                    stream_Identifier.add(Identifier277);


                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1540:24: ( COMMA Identifier )*
                    loop73:
                    do {
                        int alt73=2;
                        int LA73_0 = input.LA(1);

                        if ( (LA73_0==COMMA) ) {
                            alt73=1;
                        }


                        switch (alt73) {
                    	case 1 :
                    	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1540:26: COMMA Identifier
                    	    {
                    	    COMMA278=(Token)match(input,COMMA,FOLLOW_COMMA_in_formalParameterList5840);  
                    	    stream_COMMA.add(COMMA278);


                    	    Identifier279=(Token)match(input,Identifier,FOLLOW_Identifier_in_formalParameterList5842);  
                    	    stream_Identifier.add(Identifier279);


                    	    }
                    	    break;

                    	default :
                    	    break loop73;
                        }
                    } while (true);


                    }
                    break;

            }


            RPAREN280=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_formalParameterList5850);  
            stream_RPAREN.add(RPAREN280);


            // AST REWRITE
            // elements: Identifier
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 1541:2: -> ^( ARGS ( Identifier )* )
            {
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1541:5: ^( ARGS ( Identifier )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(ARGS, "ARGS")
                , root_1);

                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1541:13: ( Identifier )*
                while ( stream_Identifier.hasNext() ) {
                    adaptor.addChild(root_1, 
                    stream_Identifier.nextNode()
                    );

                }
                stream_Identifier.reset();

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "formalParameterList"


    public static class functionBody_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "functionBody"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1544:1: functionBody : lb= LBRACE ( sourceElement )* RBRACE -> ^( BLOCK[$lb, \"BLOCK\"] ( sourceElement )* ) ;
    public final ES3Parser.functionBody_return functionBody() throws RecognitionException {
        ES3Parser.functionBody_return retval = new ES3Parser.functionBody_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token lb=null;
        Token RBRACE282=null;
        ES3Parser.sourceElement_return sourceElement281 =null;


        Object lb_tree=null;
        Object RBRACE282_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_LBRACE=new RewriteRuleTokenStream(adaptor,"token LBRACE");
        RewriteRuleSubtreeStream stream_sourceElement=new RewriteRuleSubtreeStream(adaptor,"rule sourceElement");
        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1545:2: (lb= LBRACE ( sourceElement )* RBRACE -> ^( BLOCK[$lb, \"BLOCK\"] ( sourceElement )* ) )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1545:4: lb= LBRACE ( sourceElement )* RBRACE
            {
            lb=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_functionBody5875);  
            stream_LBRACE.add(lb);


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1545:14: ( sourceElement )*
            loop75:
            do {
                int alt75=2;
                int LA75_0 = input.LA(1);

                if ( (LA75_0==ADD||LA75_0==BREAK||LA75_0==CONTINUE||LA75_0==DEC||LA75_0==DELETE||LA75_0==DO||LA75_0==DecimalLiteral||LA75_0==FALSE||LA75_0==FOR||(LA75_0 >= FSSTART && LA75_0 <= FUNCTION)||(LA75_0 >= HexIntegerLiteral && LA75_0 <= IF)||LA75_0==INC||LA75_0==INV||LA75_0==Identifier||(LA75_0 >= LBRACE && LA75_0 <= LBRACK)||LA75_0==LPAREN||(LA75_0 >= NEW && LA75_0 <= NOT)||LA75_0==NULL||LA75_0==OctalIntegerLiteral||LA75_0==RETURN||LA75_0==RegularExpressionLiteral||LA75_0==SEMIC||LA75_0==SUB||LA75_0==SWITCH||LA75_0==StringLiteral||(LA75_0 >= THIS && LA75_0 <= THROW)||(LA75_0 >= TRUE && LA75_0 <= TYPEOF)||(LA75_0 >= VAR && LA75_0 <= VOID)||(LA75_0 >= WHILE && LA75_0 <= WITH)) ) {
                    alt75=1;
                }


                switch (alt75) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1545:14: sourceElement
            	    {
            	    pushFollow(FOLLOW_sourceElement_in_functionBody5877);
            	    sourceElement281=sourceElement();

            	    state._fsp--;

            	    stream_sourceElement.add(sourceElement281.getTree());

            	    }
            	    break;

            	default :
            	    break loop75;
                }
            } while (true);


            RBRACE282=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_functionBody5880);  
            stream_RBRACE.add(RBRACE282);


            // AST REWRITE
            // elements: sourceElement
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 1546:2: -> ^( BLOCK[$lb, \"BLOCK\"] ( sourceElement )* )
            {
                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1546:5: ^( BLOCK[$lb, \"BLOCK\"] ( sourceElement )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot(
                (Object)adaptor.create(BLOCK, lb, "BLOCK")
                , root_1);

                // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1546:28: ( sourceElement )*
                while ( stream_sourceElement.hasNext() ) {
                    adaptor.addChild(root_1, stream_sourceElement.nextTree());

                }
                stream_sourceElement.reset();

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "functionBody"


    public static class program_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "program"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1553:1: program : ( sourceElement )* ;
    public final ES3Parser.program_return program() throws RecognitionException {
        ES3Parser.program_return retval = new ES3Parser.program_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        ES3Parser.sourceElement_return sourceElement283 =null;



        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1554:2: ( ( sourceElement )* )
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1554:4: ( sourceElement )*
            {
            root_0 = (Object)adaptor.nil();


            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1554:4: ( sourceElement )*
            loop76:
            do {
                int alt76=2;
                int LA76_0 = input.LA(1);

                if ( (LA76_0==ADD||LA76_0==BREAK||LA76_0==CONTINUE||LA76_0==DEC||LA76_0==DELETE||LA76_0==DO||LA76_0==DecimalLiteral||LA76_0==FALSE||LA76_0==FOR||(LA76_0 >= FSSTART && LA76_0 <= FUNCTION)||(LA76_0 >= HexIntegerLiteral && LA76_0 <= IF)||LA76_0==INC||LA76_0==INV||LA76_0==Identifier||(LA76_0 >= LBRACE && LA76_0 <= LBRACK)||LA76_0==LPAREN||(LA76_0 >= NEW && LA76_0 <= NOT)||LA76_0==NULL||LA76_0==OctalIntegerLiteral||LA76_0==RETURN||LA76_0==RegularExpressionLiteral||LA76_0==SEMIC||LA76_0==SUB||LA76_0==SWITCH||LA76_0==StringLiteral||(LA76_0 >= THIS && LA76_0 <= THROW)||(LA76_0 >= TRUE && LA76_0 <= TYPEOF)||(LA76_0 >= VAR && LA76_0 <= VOID)||(LA76_0 >= WHILE && LA76_0 <= WITH)) ) {
                    alt76=1;
                }


                switch (alt76) {
            	case 1 :
            	    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1554:4: sourceElement
            	    {
            	    pushFollow(FOLLOW_sourceElement_in_program5909);
            	    sourceElement283=sourceElement();

            	    state._fsp--;

            	    adaptor.addChild(root_0, sourceElement283.getTree());

            	    }
            	    break;

            	default :
            	    break loop76;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "program"


    public static class sourceElement_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "sourceElement"
    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1562:1: sourceElement options {k=1; } : ({...}? functionDeclaration | statement );
    public final ES3Parser.sourceElement_return sourceElement() throws RecognitionException {
        ES3Parser.sourceElement_return retval = new ES3Parser.sourceElement_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        ES3Parser.functionDeclaration_return functionDeclaration284 =null;

        ES3Parser.statement_return statement285 =null;



        try {
            // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1567:2: ({...}? functionDeclaration | statement )
            int alt77=2;
            int LA77_0 = input.LA(1);

            if ( (LA77_0==FUNCTION) ) {
                int LA77_1 = input.LA(2);

                if ( (( input.LA(1) == FUNCTION )) ) {
                    alt77=1;
                }
                else if ( (true) ) {
                    alt77=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 77, 1, input);

                    throw nvae;

                }
            }
            else if ( (LA77_0==ADD||LA77_0==BREAK||LA77_0==CONTINUE||LA77_0==DEC||LA77_0==DELETE||LA77_0==DO||LA77_0==DecimalLiteral||LA77_0==FALSE||LA77_0==FOR||LA77_0==FSSTART||(LA77_0 >= HexIntegerLiteral && LA77_0 <= IF)||LA77_0==INC||LA77_0==INV||LA77_0==Identifier||(LA77_0 >= LBRACE && LA77_0 <= LBRACK)||LA77_0==LPAREN||(LA77_0 >= NEW && LA77_0 <= NOT)||LA77_0==NULL||LA77_0==OctalIntegerLiteral||LA77_0==RETURN||LA77_0==RegularExpressionLiteral||LA77_0==SEMIC||LA77_0==SUB||LA77_0==SWITCH||LA77_0==StringLiteral||(LA77_0 >= THIS && LA77_0 <= THROW)||(LA77_0 >= TRUE && LA77_0 <= TYPEOF)||(LA77_0 >= VAR && LA77_0 <= VOID)||(LA77_0 >= WHILE && LA77_0 <= WITH)) ) {
                alt77=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 77, 0, input);

                throw nvae;

            }
            switch (alt77) {
                case 1 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1567:4: {...}? functionDeclaration
                    {
                    root_0 = (Object)adaptor.nil();


                    if ( !(( input.LA(1) == FUNCTION )) ) {
                        throw new FailedPredicateException(input, "sourceElement", " input.LA(1) == FUNCTION ");
                    }

                    pushFollow(FOLLOW_functionDeclaration_in_sourceElement5938);
                    functionDeclaration284=functionDeclaration();

                    state._fsp--;

                    adaptor.addChild(root_0, functionDeclaration284.getTree());

                    }
                    break;
                case 2 :
                    // /Users/ghedlund/Documents/phon/gitprojects/phon-old/src/ca/phon/engines/search/script/rewrite/ES3.g:1568:4: statement
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_statement_in_sourceElement5943);
                    statement285=statement();

                    state._fsp--;

                    adaptor.addChild(root_0, statement285.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "sourceElement"

    // Delegated rules


 

    public static final BitSet FOLLOW_reservedWord_in_token1789 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_token1794 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_punctuator_in_token1799 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericLiteral_in_token1804 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_StringLiteral_in_token1809 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_keyword_in_reservedWord1822 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_futureReservedWord_in_reservedWord1827 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NULL_in_reservedWord1832 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanLiteral_in_reservedWord1837 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NULL_in_literal2518 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanLiteral_in_literal2523 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericLiteral_in_literal2528 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_StringLiteral_in_literal2533 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RegularExpressionLiteral_in_literal2538 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_featureSetLiteral_in_literal2543 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_featureSetElement_in_featureSetItem3168 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FSSTART_in_featureSetLiteral3199 = new BitSet(new long[]{0x0000000008000000L,0x0000000000080001L,0x0000000004000000L});
    public static final BitSet FOLLOW_featureSetItem_in_featureSetLiteral3203 = new BitSet(new long[]{0x0000000008000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_COMMA_in_featureSetLiteral3207 = new BitSet(new long[]{0x0000000008000000L,0x0000000000080001L,0x0000000004000000L});
    public static final BitSet FOLLOW_featureSetItem_in_featureSetLiteral3209 = new BitSet(new long[]{0x0000000008000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_FSEND_in_featureSetLiteral3217 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_THIS_in_primaryExpression3254 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_primaryExpression3259 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_primaryExpression3264 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_arrayLiteral_in_primaryExpression3269 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_objectLiteral_in_primaryExpression3274 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_primaryExpression3281 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_expression_in_primaryExpression3283 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_primaryExpression3285 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACK_in_arrayLiteral3309 = new BitSet(new long[]{0x0040100A08000020L,0x00416000460A2106L,0x0000004514100084L});
    public static final BitSet FOLLOW_arrayItem_in_arrayLiteral3313 = new BitSet(new long[]{0x0000000008000000L,0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_COMMA_in_arrayLiteral3317 = new BitSet(new long[]{0x0040100A08000020L,0x00416000460A2106L,0x0000004514100084L});
    public static final BitSet FOLLOW_arrayItem_in_arrayLiteral3319 = new BitSet(new long[]{0x0000000008000000L,0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_RBRACK_in_arrayLiteral3327 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignmentExpression_in_arrayItem3355 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_objectLiteral3387 = new BitSet(new long[]{0x0000100000000000L,0x0040000000080100L,0x0000000004000002L});
    public static final BitSet FOLLOW_nameValuePair_in_objectLiteral3391 = new BitSet(new long[]{0x0000000008000000L,0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_COMMA_in_objectLiteral3395 = new BitSet(new long[]{0x0000100000000000L,0x0040000000080100L,0x0000000004000000L});
    public static final BitSet FOLLOW_nameValuePair_in_objectLiteral3397 = new BitSet(new long[]{0x0000000008000000L,0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_RBRACE_in_objectLiteral3405 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_propertyName_in_nameValuePair3430 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_COLON_in_nameValuePair3432 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_assignmentExpression_in_nameValuePair3434 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_propertyName3458 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_StringLiteral_in_propertyName3463 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericLiteral_in_propertyName3468 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primaryExpression_in_memberExpression3486 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionExpression_in_memberExpression3491 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_newExpression_in_memberExpression3496 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NEW_in_newExpression3507 = new BitSet(new long[]{0x0040100000000000L,0x0041000046080102L,0x0000000114000080L});
    public static final BitSet FOLLOW_primaryExpression_in_newExpression3510 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_arguments3523 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100090L});
    public static final BitSet FOLLOW_assignmentExpression_in_arguments3527 = new BitSet(new long[]{0x0000000008000000L,0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_COMMA_in_arguments3531 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_assignmentExpression_in_arguments3533 = new BitSet(new long[]{0x0000000008000000L,0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_arguments3541 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_memberExpression_in_leftHandSideExpression3570 = new BitSet(new long[]{0x0000008000000002L,0x0000000044000000L});
    public static final BitSet FOLLOW_arguments_in_leftHandSideExpression3586 = new BitSet(new long[]{0x0000008000000002L,0x0000000044000000L});
    public static final BitSet FOLLOW_LBRACK_in_leftHandSideExpression3607 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_expression_in_leftHandSideExpression3609 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_RBRACK_in_leftHandSideExpression3611 = new BitSet(new long[]{0x0000008000000002L,0x0000000044000000L});
    public static final BitSet FOLLOW_DOT_in_leftHandSideExpression3630 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_Identifier_in_leftHandSideExpression3632 = new BitSet(new long[]{0x0000008000000002L,0x0000000044000000L});
    public static final BitSet FOLLOW_leftHandSideExpression_in_postfixExpression3667 = new BitSet(new long[]{0x0000000200000002L,0x0000000000002000L});
    public static final BitSet FOLLOW_postfixOperator_in_postfixExpression3673 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INC_in_postfixOperator3691 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DEC_in_postfixOperator3700 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_postfixExpression_in_unaryExpression3717 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unaryOperator_in_unaryExpression3722 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_unaryExpression_in_unaryExpression3725 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DELETE_in_unaryOperator3737 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VOID_in_unaryOperator3742 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TYPEOF_in_unaryOperator3747 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INC_in_unaryOperator3752 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DEC_in_unaryOperator3757 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ADD_in_unaryOperator3764 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUB_in_unaryOperator3773 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INV_in_unaryOperator3780 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_unaryOperator3785 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unaryExpression_in_multiplicativeExpression3800 = new BitSet(new long[]{0x0000001000000002L,0x0000002800000000L});
    public static final BitSet FOLLOW_set_in_multiplicativeExpression3804 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_unaryExpression_in_multiplicativeExpression3819 = new BitSet(new long[]{0x0000001000000002L,0x0000002800000000L});
    public static final BitSet FOLLOW_multiplicativeExpression_in_additiveExpression3837 = new BitSet(new long[]{0x0000000000000022L,0x0000000000000000L,0x0000000000100000L});
    public static final BitSet FOLLOW_set_in_additiveExpression3841 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_multiplicativeExpression_in_additiveExpression3852 = new BitSet(new long[]{0x0000000000000022L,0x0000000000000000L,0x0000000000100000L});
    public static final BitSet FOLLOW_additiveExpression_in_shiftExpression3871 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x000000000000A400L});
    public static final BitSet FOLLOW_set_in_shiftExpression3875 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_additiveExpression_in_shiftExpression3890 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x000000000000A400L});
    public static final BitSet FOLLOW_shiftExpression_in_relationalExpression3909 = new BitSet(new long[]{0x0000000000000002L,0x0000000300005030L});
    public static final BitSet FOLLOW_set_in_relationalExpression3913 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_shiftExpression_in_relationalExpression3940 = new BitSet(new long[]{0x0000000000000002L,0x0000000300005030L});
    public static final BitSet FOLLOW_shiftExpression_in_relationalExpressionNoIn3954 = new BitSet(new long[]{0x0000000000000002L,0x0000000300004030L});
    public static final BitSet FOLLOW_set_in_relationalExpressionNoIn3958 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_shiftExpression_in_relationalExpressionNoIn3981 = new BitSet(new long[]{0x0000000000000002L,0x0000000300004030L});
    public static final BitSet FOLLOW_relationalExpression_in_equalityExpression4000 = new BitSet(new long[]{0x0001000000000002L,0x0000900000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_set_in_equalityExpression4004 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_relationalExpression_in_equalityExpression4023 = new BitSet(new long[]{0x0001000000000002L,0x0000900000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_relationalExpressionNoIn_in_equalityExpressionNoIn4037 = new BitSet(new long[]{0x0001000000000002L,0x0000900000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_set_in_equalityExpressionNoIn4041 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_relationalExpressionNoIn_in_equalityExpressionNoIn4060 = new BitSet(new long[]{0x0001000000000002L,0x0000900000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_equalityExpression_in_bitwiseANDExpression4080 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_AND_in_bitwiseANDExpression4084 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_equalityExpression_in_bitwiseANDExpression4087 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_equalityExpressionNoIn_in_bitwiseANDExpressionNoIn4101 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_AND_in_bitwiseANDExpressionNoIn4105 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_equalityExpressionNoIn_in_bitwiseANDExpressionNoIn4108 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_bitwiseANDExpression_in_bitwiseXORExpression4124 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000100000000000L});
    public static final BitSet FOLLOW_XOR_in_bitwiseXORExpression4128 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_bitwiseANDExpression_in_bitwiseXORExpression4131 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000100000000000L});
    public static final BitSet FOLLOW_bitwiseANDExpressionNoIn_in_bitwiseXORExpressionNoIn4147 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000100000000000L});
    public static final BitSet FOLLOW_XOR_in_bitwiseXORExpressionNoIn4151 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_bitwiseANDExpressionNoIn_in_bitwiseXORExpressionNoIn4154 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000100000000000L});
    public static final BitSet FOLLOW_bitwiseXORExpression_in_bitwiseORExpression4169 = new BitSet(new long[]{0x0000000000000002L,0x0004000000000000L});
    public static final BitSet FOLLOW_OR_in_bitwiseORExpression4173 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_bitwiseXORExpression_in_bitwiseORExpression4176 = new BitSet(new long[]{0x0000000000000002L,0x0004000000000000L});
    public static final BitSet FOLLOW_bitwiseXORExpressionNoIn_in_bitwiseORExpressionNoIn4191 = new BitSet(new long[]{0x0000000000000002L,0x0004000000000000L});
    public static final BitSet FOLLOW_OR_in_bitwiseORExpressionNoIn4195 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_bitwiseXORExpressionNoIn_in_bitwiseORExpressionNoIn4198 = new BitSet(new long[]{0x0000000000000002L,0x0004000000000000L});
    public static final BitSet FOLLOW_bitwiseORExpression_in_logicalANDExpression4217 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_LAND_in_logicalANDExpression4221 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_bitwiseORExpression_in_logicalANDExpression4224 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_bitwiseORExpressionNoIn_in_logicalANDExpressionNoIn4238 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_LAND_in_logicalANDExpressionNoIn4242 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_bitwiseORExpressionNoIn_in_logicalANDExpressionNoIn4245 = new BitSet(new long[]{0x0000000000000002L,0x0000000001000000L});
    public static final BitSet FOLLOW_logicalANDExpression_in_logicalORExpression4260 = new BitSet(new long[]{0x0000000000000002L,0x0000000020000000L});
    public static final BitSet FOLLOW_LOR_in_logicalORExpression4264 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_logicalANDExpression_in_logicalORExpression4267 = new BitSet(new long[]{0x0000000000000002L,0x0000000020000000L});
    public static final BitSet FOLLOW_logicalANDExpressionNoIn_in_logicalORExpressionNoIn4282 = new BitSet(new long[]{0x0000000000000002L,0x0000000020000000L});
    public static final BitSet FOLLOW_LOR_in_logicalORExpressionNoIn4286 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_logicalANDExpressionNoIn_in_logicalORExpressionNoIn4289 = new BitSet(new long[]{0x0000000000000002L,0x0000000020000000L});
    public static final BitSet FOLLOW_logicalORExpression_in_conditionalExpression4308 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_QUE_in_conditionalExpression4312 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_assignmentExpression_in_conditionalExpression4315 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_COLON_in_conditionalExpression4317 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_assignmentExpression_in_conditionalExpression4320 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_logicalORExpressionNoIn_in_conditionalExpressionNoIn4334 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_QUE_in_conditionalExpressionNoIn4338 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_assignmentExpressionNoIn_in_conditionalExpressionNoIn4341 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_COLON_in_conditionalExpressionNoIn4343 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_assignmentExpressionNoIn_in_conditionalExpressionNoIn4346 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_conditionalExpression_in_assignmentExpression4374 = new BitSet(new long[]{0x0000002000000942L,0x0008005000000000L,0x0000200000214800L});
    public static final BitSet FOLLOW_assignmentOperator_in_assignmentExpression4381 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_assignmentExpression_in_assignmentExpression4384 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_conditionalExpressionNoIn_in_assignmentExpressionNoIn4461 = new BitSet(new long[]{0x0000002000000942L,0x0008005000000000L,0x0000200000214800L});
    public static final BitSet FOLLOW_assignmentOperator_in_assignmentExpressionNoIn4468 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_assignmentExpressionNoIn_in_assignmentExpressionNoIn4471 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_assignmentExpression_in_expression4493 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_COMMA_in_expression4497 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_assignmentExpression_in_expression4501 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_assignmentExpressionNoIn_in_expressionNoIn4538 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_COMMA_in_expressionNoIn4542 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_assignmentExpressionNoIn_in_expressionNoIn4546 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_SEMIC_in_semic4597 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EOF_in_semic4602 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RBRACE_in_semic4607 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EOL_in_semic4614 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MultiLineComment_in_semic4618 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_block_in_statement4647 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_statementTail_in_statement4652 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableStatement_in_statementTail4664 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_emptyStatement_in_statementTail4669 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expressionStatement_in_statementTail4674 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ifStatement_in_statementTail4679 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_iterationStatement_in_statementTail4684 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_continueStatement_in_statementTail4689 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_breakStatement_in_statementTail4694 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_returnStatement_in_statementTail4699 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_withStatement_in_statementTail4704 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_labelledStatement_in_statementTail4709 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_switchStatement_in_statementTail4714 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_throwStatement_in_statementTail4719 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tryStatement_in_statementTail4724 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_block4739 = new BitSet(new long[]{0x1040104A20004020L,0x00416000460A2306L,0x000006673490028AL});
    public static final BitSet FOLLOW_statement_in_block4741 = new BitSet(new long[]{0x1040104A20004020L,0x00416000460A2306L,0x000006673490028AL});
    public static final BitSet FOLLOW_RBRACE_in_block4744 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VAR_in_variableStatement4773 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_variableDeclaration_in_variableStatement4775 = new BitSet(new long[]{0x0000800008000000L,0x0000008000000000L,0x0000000000000202L});
    public static final BitSet FOLLOW_COMMA_in_variableStatement4779 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_variableDeclaration_in_variableStatement4781 = new BitSet(new long[]{0x0000800008000000L,0x0000008000000000L,0x0000000000000202L});
    public static final BitSet FOLLOW_semic_in_variableStatement4786 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_variableDeclaration4809 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_ASSIGN_in_variableDeclaration4813 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_assignmentExpression_in_variableDeclaration4816 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_variableDeclarationNoIn4831 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_ASSIGN_in_variableDeclarationNoIn4835 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_assignmentExpressionNoIn_in_variableDeclarationNoIn4838 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SEMIC_in_emptyStatement4857 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_expressionStatement4876 = new BitSet(new long[]{0x0000800000000000L,0x0000008000000000L,0x0000000000000202L});
    public static final BitSet FOLLOW_semic_in_expressionStatement4878 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IF_in_ifStatement4896 = new BitSet(new long[]{0x0000000000000000L,0x0000000040000000L});
    public static final BitSet FOLLOW_LPAREN_in_ifStatement4898 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_expression_in_ifStatement4900 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_ifStatement4902 = new BitSet(new long[]{0x1040104A20004020L,0x00416000460A2306L,0x0000066734900288L});
    public static final BitSet FOLLOW_statement_in_ifStatement4904 = new BitSet(new long[]{0x0000200000000002L});
    public static final BitSet FOLLOW_ELSE_in_ifStatement4910 = new BitSet(new long[]{0x1040104A20004020L,0x00416000460A2306L,0x0000066734900288L});
    public static final BitSet FOLLOW_statement_in_ifStatement4912 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_doStatement_in_iterationStatement4945 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_whileStatement_in_iterationStatement4950 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forStatement_in_iterationStatement4955 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DO_in_doStatement4967 = new BitSet(new long[]{0x1040104A20004020L,0x00416000460A2306L,0x0000066734900288L});
    public static final BitSet FOLLOW_statement_in_doStatement4969 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000020000000000L});
    public static final BitSet FOLLOW_WHILE_in_doStatement4971 = new BitSet(new long[]{0x0000000000000000L,0x0000000040000000L});
    public static final BitSet FOLLOW_LPAREN_in_doStatement4973 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_expression_in_doStatement4975 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_doStatement4977 = new BitSet(new long[]{0x0000800000000000L,0x0000008000000000L,0x0000000000000202L});
    public static final BitSet FOLLOW_semic_in_doStatement4979 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHILE_in_whileStatement5004 = new BitSet(new long[]{0x0000000000000000L,0x0000000040000000L});
    public static final BitSet FOLLOW_LPAREN_in_whileStatement5007 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_expression_in_whileStatement5010 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_whileStatement5012 = new BitSet(new long[]{0x1040104A20004020L,0x00416000460A2306L,0x0000066734900288L});
    public static final BitSet FOLLOW_statement_in_whileStatement5015 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_forStatement5028 = new BitSet(new long[]{0x0000000000000000L,0x0000000040000000L});
    public static final BitSet FOLLOW_LPAREN_in_forStatement5031 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000006514100280L});
    public static final BitSet FOLLOW_forControl_in_forStatement5034 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_forStatement5036 = new BitSet(new long[]{0x1040104A20004020L,0x00416000460A2306L,0x0000066734900288L});
    public static final BitSet FOLLOW_statement_in_forStatement5039 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forControlVar_in_forControl5050 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forControlExpression_in_forControl5055 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_forControlSemic_in_forControl5060 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VAR_in_forControlVar5071 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_variableDeclarationNoIn_in_forControlVar5073 = new BitSet(new long[]{0x0000000008000000L,0x0000000000001000L,0x0000000000000200L});
    public static final BitSet FOLLOW_IN_in_forControlVar5085 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_expression_in_forControlVar5087 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMMA_in_forControlVar5133 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_variableDeclarationNoIn_in_forControlVar5135 = new BitSet(new long[]{0x0000000008000000L,0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_SEMIC_in_forControlVar5140 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100280L});
    public static final BitSet FOLLOW_expression_in_forControlVar5144 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_SEMIC_in_forControlVar5147 = new BitSet(new long[]{0x0040100A00000022L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_expression_in_forControlVar5151 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expressionNoIn_in_forControlExpression5217 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L,0x0000000000000200L});
    public static final BitSet FOLLOW_IN_in_forControlExpression5232 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_expression_in_forControlExpression5236 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SEMIC_in_forControlExpression5282 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100280L});
    public static final BitSet FOLLOW_expression_in_forControlExpression5286 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_SEMIC_in_forControlExpression5289 = new BitSet(new long[]{0x0040100A00000022L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_expression_in_forControlExpression5293 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SEMIC_in_forControlSemic5352 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100280L});
    public static final BitSet FOLLOW_expression_in_forControlSemic5356 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_SEMIC_in_forControlSemic5359 = new BitSet(new long[]{0x0040100A00000022L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_expression_in_forControlSemic5363 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONTINUE_in_continueStatement5417 = new BitSet(new long[]{0x0000800000000000L,0x0000008000080000L,0x0000000000000202L});
    public static final BitSet FOLLOW_Identifier_in_continueStatement5422 = new BitSet(new long[]{0x0000800000000000L,0x0000008000000000L,0x0000000000000202L});
    public static final BitSet FOLLOW_semic_in_continueStatement5425 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BREAK_in_breakStatement5444 = new BitSet(new long[]{0x0000800000000000L,0x0000008000080000L,0x0000000000000202L});
    public static final BitSet FOLLOW_Identifier_in_breakStatement5449 = new BitSet(new long[]{0x0000800000000000L,0x0000008000000000L,0x0000000000000202L});
    public static final BitSet FOLLOW_semic_in_breakStatement5452 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURN_in_returnStatement5471 = new BitSet(new long[]{0x0040900A00000020L,0x00416080460A2106L,0x0000004514100282L});
    public static final BitSet FOLLOW_expression_in_returnStatement5476 = new BitSet(new long[]{0x0000800000000000L,0x0000008000000000L,0x0000000000000202L});
    public static final BitSet FOLLOW_semic_in_returnStatement5479 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WITH_in_withStatement5496 = new BitSet(new long[]{0x0000000000000000L,0x0000000040000000L});
    public static final BitSet FOLLOW_LPAREN_in_withStatement5499 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_expression_in_withStatement5502 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_withStatement5504 = new BitSet(new long[]{0x1040104A20004020L,0x00416000460A2306L,0x0000066734900288L});
    public static final BitSet FOLLOW_statement_in_withStatement5507 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SWITCH_in_switchStatement5528 = new BitSet(new long[]{0x0000000000000000L,0x0000000040000000L});
    public static final BitSet FOLLOW_LPAREN_in_switchStatement5530 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_expression_in_switchStatement5532 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_switchStatement5534 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_LBRACE_in_switchStatement5536 = new BitSet(new long[]{0x0000000400200000L,0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_defaultClause_in_switchStatement5543 = new BitSet(new long[]{0x0000000400200000L,0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_caseClause_in_switchStatement5549 = new BitSet(new long[]{0x0000000400200000L,0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_RBRACE_in_switchStatement5554 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CASE_in_caseClause5582 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_expression_in_caseClause5585 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_COLON_in_caseClause5587 = new BitSet(new long[]{0x1040104A20004022L,0x00416000460A2306L,0x0000066734900288L});
    public static final BitSet FOLLOW_statement_in_caseClause5590 = new BitSet(new long[]{0x1040104A20004022L,0x00416000460A2306L,0x0000066734900288L});
    public static final BitSet FOLLOW_DEFAULT_in_defaultClause5603 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_COLON_in_defaultClause5606 = new BitSet(new long[]{0x1040104A20004022L,0x00416000460A2306L,0x0000066734900288L});
    public static final BitSet FOLLOW_statement_in_defaultClause5609 = new BitSet(new long[]{0x1040104A20004022L,0x00416000460A2306L,0x0000066734900288L});
    public static final BitSet FOLLOW_Identifier_in_labelledStatement5626 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_COLON_in_labelledStatement5628 = new BitSet(new long[]{0x1040104A20004020L,0x00416000460A2306L,0x0000066734900288L});
    public static final BitSet FOLLOW_statement_in_labelledStatement5630 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_THROW_in_throwStatement5661 = new BitSet(new long[]{0x0040100A00000020L,0x00416000460A2106L,0x0000004514100080L});
    public static final BitSet FOLLOW_expression_in_throwStatement5666 = new BitSet(new long[]{0x0000800000000000L,0x0000008000000000L,0x0000000000000202L});
    public static final BitSet FOLLOW_semic_in_throwStatement5668 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRY_in_tryStatement5685 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_block_in_tryStatement5688 = new BitSet(new long[]{0x0400000000400000L});
    public static final BitSet FOLLOW_catchClause_in_tryStatement5692 = new BitSet(new long[]{0x0400000000000002L});
    public static final BitSet FOLLOW_finallyClause_in_tryStatement5694 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_finallyClause_in_tryStatement5699 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CATCH_in_catchClause5713 = new BitSet(new long[]{0x0000000000000000L,0x0000000040000000L});
    public static final BitSet FOLLOW_LPAREN_in_catchClause5716 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_Identifier_in_catchClause5719 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_catchClause5721 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_block_in_catchClause5724 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FINALLY_in_finallyClause5736 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_block_in_finallyClause5739 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FUNCTION_in_functionDeclaration5760 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_Identifier_in_functionDeclaration5764 = new BitSet(new long[]{0x0000000000000000L,0x0000000040000000L});
    public static final BitSet FOLLOW_formalParameterList_in_functionDeclaration5766 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_functionBody_in_functionDeclaration5768 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FUNCTION_in_functionExpression5795 = new BitSet(new long[]{0x0000000000000000L,0x0000000040080000L});
    public static final BitSet FOLLOW_Identifier_in_functionExpression5799 = new BitSet(new long[]{0x0000000000000000L,0x0000000040000000L});
    public static final BitSet FOLLOW_formalParameterList_in_functionExpression5802 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_functionBody_in_functionExpression5804 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_formalParameterList5832 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L,0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_formalParameterList5836 = new BitSet(new long[]{0x0000000008000000L,0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_COMMA_in_formalParameterList5840 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_Identifier_in_formalParameterList5842 = new BitSet(new long[]{0x0000000008000000L,0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_formalParameterList5850 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_functionBody5875 = new BitSet(new long[]{0x1040104A20004020L,0x00416000460A2306L,0x000006673490028AL});
    public static final BitSet FOLLOW_sourceElement_in_functionBody5877 = new BitSet(new long[]{0x1040104A20004020L,0x00416000460A2306L,0x000006673490028AL});
    public static final BitSet FOLLOW_RBRACE_in_functionBody5880 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sourceElement_in_program5909 = new BitSet(new long[]{0x1040104A20004022L,0x00416000460A2306L,0x0000066734900288L});
    public static final BitSet FOLLOW_functionDeclaration_in_sourceElement5938 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_statement_in_sourceElement5943 = new BitSet(new long[]{0x0000000000000002L});

}