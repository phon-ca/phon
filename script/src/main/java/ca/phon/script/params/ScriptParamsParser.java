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
// $ANTLR 3.4 C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g 2012-08-27 12:44:10

package ca.phon.script.params;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.RewriteEarlyExitException;
import org.antlr.runtime.tree.RewriteRuleSubtreeStream;
import org.antlr.runtime.tree.RewriteRuleTokenStream;
import org.antlr.runtime.tree.TreeAdaptor;

import ca.phon.util.StringUtils;


@SuppressWarnings({"all", "warnings", "unchecked"})
public class ScriptParamsParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "BOOLEAN", "EscapeSequence", "HexDigit", "ID", "INT", "LETTER", "OctalEscape", "StringLiteral", "UnicodeEscape", "WS", "','", "';'", "'='", "'bool'", "'enum'", "'label'", "'multibool'", "'params'", "'separator'", "'string'", "'{'", "'|'", "'}'"
    };

    public static final int EOF=-1;
    public static final int T__14=14;
    public static final int T__15=15;
    public static final int T__16=16;
    public static final int T__17=17;
    public static final int T__18=18;
    public static final int T__19=19;
    public static final int T__20=20;
    public static final int T__21=21;
    public static final int T__22=22;
    public static final int T__23=23;
    public static final int T__24=24;
    public static final int T__25=25;
    public static final int T__26=26;
    public static final int BOOLEAN=4;
    public static final int EscapeSequence=5;
    public static final int HexDigit=6;
    public static final int ID=7;
    public static final int INT=8;
    public static final int LETTER=9;
    public static final int OctalEscape=10;
    public static final int StringLiteral=11;
    public static final int UnicodeEscape=12;
    public static final int WS=13;

    // delegates
    public Parser[] getDelegates() {
        return new Parser[] {};
    }

    // delegators


    public ScriptParamsParser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }
    public ScriptParamsParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

protected TreeAdaptor adaptor = new CommonTreeAdaptor();

public void setTreeAdaptor(TreeAdaptor adaptor) {
    this.adaptor = adaptor;
}
public TreeAdaptor getTreeAdaptor() {
    return adaptor;
}
    public String[] getTokenNames() { return ScriptParamsParser.tokenNames; }
    public String getGrammarFileName() { return "C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g"; }


    /** The list of script params */
    private List<ScriptParam> scriptParams = new ArrayList<ScriptParam>();

    public ScriptParam[] getScriptParams() {
    	return scriptParams.toArray(new ScriptParam[0]);
    }


    public static class params_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "params"
    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:28:1: params : 'params' '=' paramDef ( ',' paramDef )* ';' -> ^( 'params' ( paramDef )+ ) ;
    public final ScriptParamsParser.params_return params() throws RecognitionException {
        ScriptParamsParser.params_return retval = new ScriptParamsParser.params_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token string_literal1=null;
        Token char_literal2=null;
        Token char_literal4=null;
        Token char_literal6=null;
        ScriptParamsParser.paramDef_return paramDef3 =null;

        ScriptParamsParser.paramDef_return paramDef5 =null;


        CommonTree string_literal1_tree=null;
        CommonTree char_literal2_tree=null;
        CommonTree char_literal4_tree=null;
        CommonTree char_literal6_tree=null;
        RewriteRuleTokenStream stream_21=new RewriteRuleTokenStream(adaptor,"token 21");
        RewriteRuleTokenStream stream_15=new RewriteRuleTokenStream(adaptor,"token 15");
        RewriteRuleTokenStream stream_16=new RewriteRuleTokenStream(adaptor,"token 16");
        RewriteRuleTokenStream stream_14=new RewriteRuleTokenStream(adaptor,"token 14");
        RewriteRuleSubtreeStream stream_paramDef=new RewriteRuleSubtreeStream(adaptor,"rule paramDef");
        try {
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:29:2: ( 'params' '=' paramDef ( ',' paramDef )* ';' -> ^( 'params' ( paramDef )+ ) )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:29:4: 'params' '=' paramDef ( ',' paramDef )* ';'
            {
            string_literal1=(Token)match(input,21,FOLLOW_21_in_params48);  
            stream_21.add(string_literal1);


            char_literal2=(Token)match(input,16,FOLLOW_16_in_params50);  
            stream_16.add(char_literal2);


            pushFollow(FOLLOW_paramDef_in_params52);
            paramDef3=paramDef();

            state._fsp--;

            stream_paramDef.add(paramDef3.getTree());

            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:29:26: ( ',' paramDef )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==14) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:29:27: ',' paramDef
            	    {
            	    char_literal4=(Token)match(input,14,FOLLOW_14_in_params55);  
            	    stream_14.add(char_literal4);


            	    pushFollow(FOLLOW_paramDef_in_params57);
            	    paramDef5=paramDef();

            	    state._fsp--;

            	    stream_paramDef.add(paramDef5.getTree());

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            char_literal6=(Token)match(input,15,FOLLOW_15_in_params61);  
            stream_15.add(char_literal6);


            // AST REWRITE
            // elements: 21, paramDef
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 29:46: -> ^( 'params' ( paramDef )+ )
            {
                // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:29:49: ^( 'params' ( paramDef )+ )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(
                stream_21.nextNode()
                , root_1);

                if ( !(stream_paramDef.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_paramDef.hasNext() ) {
                    adaptor.addChild(root_1, stream_paramDef.nextTree());

                }
                stream_paramDef.reset();

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "params"


    public static class paramDef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "paramDef"
    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:32:1: paramDef : '{' paramSubDef '}' ;
    public final ScriptParamsParser.paramDef_return paramDef() throws RecognitionException {
        ScriptParamsParser.paramDef_return retval = new ScriptParamsParser.paramDef_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token char_literal7=null;
        Token char_literal9=null;
        ScriptParamsParser.paramSubDef_return paramSubDef8 =null;


        CommonTree char_literal7_tree=null;
        CommonTree char_literal9_tree=null;

        try {
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:33:2: ( '{' paramSubDef '}' )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:33:4: '{' paramSubDef '}'
            {
            root_0 = (CommonTree)adaptor.nil();


            char_literal7=(Token)match(input,24,FOLLOW_24_in_paramDef82); 
            char_literal7_tree = 
            (CommonTree)adaptor.create(char_literal7)
            ;
            adaptor.addChild(root_0, char_literal7_tree);


            pushFollow(FOLLOW_paramSubDef_in_paramDef84);
            paramSubDef8=paramSubDef();

            state._fsp--;

            adaptor.addChild(root_0, paramSubDef8.getTree());

            char_literal9=(Token)match(input,26,FOLLOW_26_in_paramDef86); 
            char_literal9_tree = 
            (CommonTree)adaptor.create(char_literal9)
            ;
            adaptor.addChild(root_0, char_literal9_tree);


            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "paramDef"


    public static class paramSubDef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "paramSubDef"
    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:36:1: paramSubDef : ( stringDef | booleanDef | enumDef | separatorDef | multiBoolDef | labelDef );
    public final ScriptParamsParser.paramSubDef_return paramSubDef() throws RecognitionException {
        ScriptParamsParser.paramSubDef_return retval = new ScriptParamsParser.paramSubDef_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        ScriptParamsParser.stringDef_return stringDef10 =null;

        ScriptParamsParser.booleanDef_return booleanDef11 =null;

        ScriptParamsParser.enumDef_return enumDef12 =null;

        ScriptParamsParser.separatorDef_return separatorDef13 =null;

        ScriptParamsParser.multiBoolDef_return multiBoolDef14 =null;

        ScriptParamsParser.labelDef_return labelDef15 =null;



        try {
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:37:2: ( stringDef | booleanDef | enumDef | separatorDef | multiBoolDef | labelDef )
            int alt2=6;
            switch ( input.LA(1) ) {
            case 23:
                {
                alt2=1;
                }
                break;
            case 17:
                {
                alt2=2;
                }
                break;
            case 18:
                {
                alt2=3;
                }
                break;
            case 22:
                {
                alt2=4;
                }
                break;
            case 20:
                {
                alt2=5;
                }
                break;
            case 19:
                {
                alt2=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;

            }

            switch (alt2) {
                case 1 :
                    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:37:4: stringDef
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_stringDef_in_paramSubDef98);
                    stringDef10=stringDef();

                    state._fsp--;

                    adaptor.addChild(root_0, stringDef10.getTree());

                    }
                    break;
                case 2 :
                    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:38:4: booleanDef
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_booleanDef_in_paramSubDef103);
                    booleanDef11=booleanDef();

                    state._fsp--;

                    adaptor.addChild(root_0, booleanDef11.getTree());

                    }
                    break;
                case 3 :
                    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:39:4: enumDef
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_enumDef_in_paramSubDef108);
                    enumDef12=enumDef();

                    state._fsp--;

                    adaptor.addChild(root_0, enumDef12.getTree());

                    }
                    break;
                case 4 :
                    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:40:4: separatorDef
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_separatorDef_in_paramSubDef113);
                    separatorDef13=separatorDef();

                    state._fsp--;

                    adaptor.addChild(root_0, separatorDef13.getTree());

                    }
                    break;
                case 5 :
                    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:41:4: multiBoolDef
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_multiBoolDef_in_paramSubDef118);
                    multiBoolDef14=multiBoolDef();

                    state._fsp--;

                    adaptor.addChild(root_0, multiBoolDef14.getTree());

                    }
                    break;
                case 6 :
                    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:42:4: labelDef
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    pushFollow(FOLLOW_labelDef_in_paramSubDef123);
                    labelDef15=labelDef();

                    state._fsp--;

                    adaptor.addChild(root_0, labelDef15.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "paramSubDef"


    public static class separatorDef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "separatorDef"
    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:45:1: separatorDef : 'separator' ',' desc= StringLiteral ( ',' collapsed= BOOLEAN )? ;
    public final ScriptParamsParser.separatorDef_return separatorDef() throws RecognitionException {
        ScriptParamsParser.separatorDef_return retval = new ScriptParamsParser.separatorDef_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token desc=null;
        Token collapsed=null;
        Token string_literal16=null;
        Token char_literal17=null;
        Token char_literal18=null;

        CommonTree desc_tree=null;
        CommonTree collapsed_tree=null;
        CommonTree string_literal16_tree=null;
        CommonTree char_literal17_tree=null;
        CommonTree char_literal18_tree=null;

        try {
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:46:2: ( 'separator' ',' desc= StringLiteral ( ',' collapsed= BOOLEAN )? )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:46:4: 'separator' ',' desc= StringLiteral ( ',' collapsed= BOOLEAN )?
            {
            root_0 = (CommonTree)adaptor.nil();


            string_literal16=(Token)match(input,22,FOLLOW_22_in_separatorDef135); 
            string_literal16_tree = 
            (CommonTree)adaptor.create(string_literal16)
            ;
            adaptor.addChild(root_0, string_literal16_tree);


            char_literal17=(Token)match(input,14,FOLLOW_14_in_separatorDef137); 
            char_literal17_tree = 
            (CommonTree)adaptor.create(char_literal17)
            ;
            adaptor.addChild(root_0, char_literal17_tree);


            desc=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_separatorDef143); 
            desc_tree = 
            (CommonTree)adaptor.create(desc)
            ;
            adaptor.addChild(root_0, desc_tree);


            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:46:41: ( ',' collapsed= BOOLEAN )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==14) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:46:42: ',' collapsed= BOOLEAN
                    {
                    char_literal18=(Token)match(input,14,FOLLOW_14_in_separatorDef146); 
                    char_literal18_tree = 
                    (CommonTree)adaptor.create(char_literal18)
                    ;
                    adaptor.addChild(root_0, char_literal18_tree);


                    collapsed=(Token)match(input,BOOLEAN,FOLLOW_BOOLEAN_in_separatorDef152); 
                    collapsed_tree = 
                    (CommonTree)adaptor.create(collapsed)
                    ;
                    adaptor.addChild(root_0, collapsed_tree);


                    }
                    break;

            }



            		SeparatorScriptParam param = new SeparatorScriptParam(StringUtils.strip((desc!=null?desc.getText():null), "\""));
            		if(collapsed != null)
            		{
            			param.setCollapsed(Boolean.parseBoolean((collapsed!=null?collapsed.getText():null)));
            		}
            		scriptParams.add(param);
            		

            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "separatorDef"


    public static class stringDef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "stringDef"
    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:57:1: stringDef : 'string' ',' ID ',' def= StringLiteral ',' desc= StringLiteral ;
    public final ScriptParamsParser.stringDef_return stringDef() throws RecognitionException {
        ScriptParamsParser.stringDef_return retval = new ScriptParamsParser.stringDef_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token def=null;
        Token desc=null;
        Token string_literal19=null;
        Token char_literal20=null;
        Token ID21=null;
        Token char_literal22=null;
        Token char_literal23=null;

        CommonTree def_tree=null;
        CommonTree desc_tree=null;
        CommonTree string_literal19_tree=null;
        CommonTree char_literal20_tree=null;
        CommonTree ID21_tree=null;
        CommonTree char_literal22_tree=null;
        CommonTree char_literal23_tree=null;

        try {
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:58:2: ( 'string' ',' ID ',' def= StringLiteral ',' desc= StringLiteral )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:58:4: 'string' ',' ID ',' def= StringLiteral ',' desc= StringLiteral
            {
            root_0 = (CommonTree)adaptor.nil();


            string_literal19=(Token)match(input,23,FOLLOW_23_in_stringDef170); 
            string_literal19_tree = 
            (CommonTree)adaptor.create(string_literal19)
            ;
            adaptor.addChild(root_0, string_literal19_tree);


            char_literal20=(Token)match(input,14,FOLLOW_14_in_stringDef172); 
            char_literal20_tree = 
            (CommonTree)adaptor.create(char_literal20)
            ;
            adaptor.addChild(root_0, char_literal20_tree);


            ID21=(Token)match(input,ID,FOLLOW_ID_in_stringDef174); 
            ID21_tree = 
            (CommonTree)adaptor.create(ID21)
            ;
            adaptor.addChild(root_0, ID21_tree);


            char_literal22=(Token)match(input,14,FOLLOW_14_in_stringDef176); 
            char_literal22_tree = 
            (CommonTree)adaptor.create(char_literal22)
            ;
            adaptor.addChild(root_0, char_literal22_tree);


            def=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_stringDef182); 
            def_tree = 
            (CommonTree)adaptor.create(def)
            ;
            adaptor.addChild(root_0, def_tree);


            char_literal23=(Token)match(input,14,FOLLOW_14_in_stringDef184); 
            char_literal23_tree = 
            (CommonTree)adaptor.create(char_literal23)
            ;
            adaptor.addChild(root_0, char_literal23_tree);


            desc=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_stringDef190); 
            desc_tree = 
            (CommonTree)adaptor.create(desc)
            ;
            adaptor.addChild(root_0, desc_tree);



            		StringScriptParam param = new StringScriptParam((ID21!=null?ID21.getText():null), 
            			StringUtils.strip((desc!=null?desc.getText():null), "\""), StringUtils.strip((def!=null?def.getText():null), "\""));
            		scriptParams.add(param);
            		

            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "stringDef"


    public static class booleanDef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "booleanDef"
    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:66:1: booleanDef : 'bool' ',' ID ',' def= BOOLEAN ',' lbl= StringLiteral ',' desc= StringLiteral ;
    public final ScriptParamsParser.booleanDef_return booleanDef() throws RecognitionException {
        ScriptParamsParser.booleanDef_return retval = new ScriptParamsParser.booleanDef_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token def=null;
        Token lbl=null;
        Token desc=null;
        Token string_literal24=null;
        Token char_literal25=null;
        Token ID26=null;
        Token char_literal27=null;
        Token char_literal28=null;
        Token char_literal29=null;

        CommonTree def_tree=null;
        CommonTree lbl_tree=null;
        CommonTree desc_tree=null;
        CommonTree string_literal24_tree=null;
        CommonTree char_literal25_tree=null;
        CommonTree ID26_tree=null;
        CommonTree char_literal27_tree=null;
        CommonTree char_literal28_tree=null;
        CommonTree char_literal29_tree=null;

        try {
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:67:2: ( 'bool' ',' ID ',' def= BOOLEAN ',' lbl= StringLiteral ',' desc= StringLiteral )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:67:4: 'bool' ',' ID ',' def= BOOLEAN ',' lbl= StringLiteral ',' desc= StringLiteral
            {
            root_0 = (CommonTree)adaptor.nil();


            string_literal24=(Token)match(input,17,FOLLOW_17_in_booleanDef207); 
            string_literal24_tree = 
            (CommonTree)adaptor.create(string_literal24)
            ;
            adaptor.addChild(root_0, string_literal24_tree);


            char_literal25=(Token)match(input,14,FOLLOW_14_in_booleanDef209); 
            char_literal25_tree = 
            (CommonTree)adaptor.create(char_literal25)
            ;
            adaptor.addChild(root_0, char_literal25_tree);


            ID26=(Token)match(input,ID,FOLLOW_ID_in_booleanDef211); 
            ID26_tree = 
            (CommonTree)adaptor.create(ID26)
            ;
            adaptor.addChild(root_0, ID26_tree);


            char_literal27=(Token)match(input,14,FOLLOW_14_in_booleanDef213); 
            char_literal27_tree = 
            (CommonTree)adaptor.create(char_literal27)
            ;
            adaptor.addChild(root_0, char_literal27_tree);


            def=(Token)match(input,BOOLEAN,FOLLOW_BOOLEAN_in_booleanDef219); 
            def_tree = 
            (CommonTree)adaptor.create(def)
            ;
            adaptor.addChild(root_0, def_tree);


            char_literal28=(Token)match(input,14,FOLLOW_14_in_booleanDef221); 
            char_literal28_tree = 
            (CommonTree)adaptor.create(char_literal28)
            ;
            adaptor.addChild(root_0, char_literal28_tree);


            lbl=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_booleanDef227); 
            lbl_tree = 
            (CommonTree)adaptor.create(lbl)
            ;
            adaptor.addChild(root_0, lbl_tree);


            char_literal29=(Token)match(input,14,FOLLOW_14_in_booleanDef229); 
            char_literal29_tree = 
            (CommonTree)adaptor.create(char_literal29)
            ;
            adaptor.addChild(root_0, char_literal29_tree);


            desc=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_booleanDef235); 
            desc_tree = 
            (CommonTree)adaptor.create(desc)
            ;
            adaptor.addChild(root_0, desc_tree);



            		BooleanScriptParam param = new BooleanScriptParam((ID26!=null?ID26.getText():null), StringUtils.strip((lbl!=null?lbl.getText():null), "\""),
            			StringUtils.strip((desc!=null?desc.getText():null), "\""), new Boolean((def!=null?def.getText():null)));
            		scriptParams.add(param);
            		

            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "booleanDef"


    public static class multiBoolDef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "multiBoolDef"
    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:75:1: multiBoolDef : 'multibool' ',' ids= idList ',' defs= boolList ',' descs= stringList ',' desc= StringLiteral ( ',' cols= INT )? ;
    public final ScriptParamsParser.multiBoolDef_return multiBoolDef() throws RecognitionException {
        ScriptParamsParser.multiBoolDef_return retval = new ScriptParamsParser.multiBoolDef_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token desc=null;
        Token cols=null;
        Token string_literal30=null;
        Token char_literal31=null;
        Token char_literal32=null;
        Token char_literal33=null;
        Token char_literal34=null;
        Token char_literal35=null;
        ScriptParamsParser.idList_return ids =null;

        ScriptParamsParser.boolList_return defs =null;

        ScriptParamsParser.stringList_return descs =null;


        CommonTree desc_tree=null;
        CommonTree cols_tree=null;
        CommonTree string_literal30_tree=null;
        CommonTree char_literal31_tree=null;
        CommonTree char_literal32_tree=null;
        CommonTree char_literal33_tree=null;
        CommonTree char_literal34_tree=null;
        CommonTree char_literal35_tree=null;

        try {
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:76:2: ( 'multibool' ',' ids= idList ',' defs= boolList ',' descs= stringList ',' desc= StringLiteral ( ',' cols= INT )? )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:76:4: 'multibool' ',' ids= idList ',' defs= boolList ',' descs= stringList ',' desc= StringLiteral ( ',' cols= INT )?
            {
            root_0 = (CommonTree)adaptor.nil();


            string_literal30=(Token)match(input,20,FOLLOW_20_in_multiBoolDef251); 
            string_literal30_tree = 
            (CommonTree)adaptor.create(string_literal30)
            ;
            adaptor.addChild(root_0, string_literal30_tree);


            char_literal31=(Token)match(input,14,FOLLOW_14_in_multiBoolDef253); 
            char_literal31_tree = 
            (CommonTree)adaptor.create(char_literal31)
            ;
            adaptor.addChild(root_0, char_literal31_tree);


            pushFollow(FOLLOW_idList_in_multiBoolDef259);
            ids=idList();

            state._fsp--;

            adaptor.addChild(root_0, ids.getTree());

            char_literal32=(Token)match(input,14,FOLLOW_14_in_multiBoolDef261); 
            char_literal32_tree = 
            (CommonTree)adaptor.create(char_literal32)
            ;
            adaptor.addChild(root_0, char_literal32_tree);


            pushFollow(FOLLOW_boolList_in_multiBoolDef267);
            defs=boolList();

            state._fsp--;

            adaptor.addChild(root_0, defs.getTree());

            char_literal33=(Token)match(input,14,FOLLOW_14_in_multiBoolDef269); 
            char_literal33_tree = 
            (CommonTree)adaptor.create(char_literal33)
            ;
            adaptor.addChild(root_0, char_literal33_tree);


            pushFollow(FOLLOW_stringList_in_multiBoolDef275);
            descs=stringList();

            state._fsp--;

            adaptor.addChild(root_0, descs.getTree());

            char_literal34=(Token)match(input,14,FOLLOW_14_in_multiBoolDef277); 
            char_literal34_tree = 
            (CommonTree)adaptor.create(char_literal34)
            ;
            adaptor.addChild(root_0, char_literal34_tree);


            desc=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_multiBoolDef283); 
            desc_tree = 
            (CommonTree)adaptor.create(desc)
            ;
            adaptor.addChild(root_0, desc_tree);


            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:76:101: ( ',' cols= INT )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==14) ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:76:102: ',' cols= INT
                    {
                    char_literal35=(Token)match(input,14,FOLLOW_14_in_multiBoolDef286); 
                    char_literal35_tree = 
                    (CommonTree)adaptor.create(char_literal35)
                    ;
                    adaptor.addChild(root_0, char_literal35_tree);


                    cols=(Token)match(input,INT,FOLLOW_INT_in_multiBoolDef292); 
                    cols_tree = 
                    (CommonTree)adaptor.create(cols)
                    ;
                    adaptor.addChild(root_0, cols_tree);


                    }
                    break;

            }



            		String[] _ids = (ids!=null?input.toString(ids.start,ids.stop):null).split("\\|");
            		String[] defaults = (defs!=null?input.toString(defs.start,defs.stop):null).split("\\|");
            		Boolean[] _defs = new Boolean[defaults.length];
            		for(int i = 0; i < defaults.length; i++)
            			_defs[i] = new Boolean(defaults[i]);
            		String[] _descs = (descs!=null?input.toString(descs.start,descs.stop):null).split("\\|");
            		// strip quotes
            		for(int i = 0; i < _descs.length; i++) 
            			_descs[i] = StringUtils.strip(_descs[i], "\"");
            		MultiboolScriptParam param = 
            			new MultiboolScriptParam(_ids, _defs, _descs, StringUtils.strip((desc!=null?desc.getText():null), "\"")
            				, (cols == null ? 2 : new Integer((cols!=null?cols.getText():null))));
            		scriptParams.add(param);
            		

            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "multiBoolDef"


    public static class enumDef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "enumDef"
    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:94:1: enumDef : 'enum' ',' ID ',' list= stringList ',' def= INT ',' desc= StringLiteral ;
    public final ScriptParamsParser.enumDef_return enumDef() throws RecognitionException {
        ScriptParamsParser.enumDef_return retval = new ScriptParamsParser.enumDef_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token def=null;
        Token desc=null;
        Token string_literal36=null;
        Token char_literal37=null;
        Token ID38=null;
        Token char_literal39=null;
        Token char_literal40=null;
        Token char_literal41=null;
        ScriptParamsParser.stringList_return list =null;


        CommonTree def_tree=null;
        CommonTree desc_tree=null;
        CommonTree string_literal36_tree=null;
        CommonTree char_literal37_tree=null;
        CommonTree ID38_tree=null;
        CommonTree char_literal39_tree=null;
        CommonTree char_literal40_tree=null;
        CommonTree char_literal41_tree=null;

        try {
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:95:2: ( 'enum' ',' ID ',' list= stringList ',' def= INT ',' desc= StringLiteral )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:95:4: 'enum' ',' ID ',' list= stringList ',' def= INT ',' desc= StringLiteral
            {
            root_0 = (CommonTree)adaptor.nil();


            string_literal36=(Token)match(input,18,FOLLOW_18_in_enumDef310); 
            string_literal36_tree = 
            (CommonTree)adaptor.create(string_literal36)
            ;
            adaptor.addChild(root_0, string_literal36_tree);


            char_literal37=(Token)match(input,14,FOLLOW_14_in_enumDef312); 
            char_literal37_tree = 
            (CommonTree)adaptor.create(char_literal37)
            ;
            adaptor.addChild(root_0, char_literal37_tree);


            ID38=(Token)match(input,ID,FOLLOW_ID_in_enumDef314); 
            ID38_tree = 
            (CommonTree)adaptor.create(ID38)
            ;
            adaptor.addChild(root_0, ID38_tree);


            char_literal39=(Token)match(input,14,FOLLOW_14_in_enumDef316); 
            char_literal39_tree = 
            (CommonTree)adaptor.create(char_literal39)
            ;
            adaptor.addChild(root_0, char_literal39_tree);


            pushFollow(FOLLOW_stringList_in_enumDef322);
            list=stringList();

            state._fsp--;

            adaptor.addChild(root_0, list.getTree());

            char_literal40=(Token)match(input,14,FOLLOW_14_in_enumDef324); 
            char_literal40_tree = 
            (CommonTree)adaptor.create(char_literal40)
            ;
            adaptor.addChild(root_0, char_literal40_tree);


            def=(Token)match(input,INT,FOLLOW_INT_in_enumDef330); 
            def_tree = 
            (CommonTree)adaptor.create(def)
            ;
            adaptor.addChild(root_0, def_tree);


            char_literal41=(Token)match(input,14,FOLLOW_14_in_enumDef332); 
            char_literal41_tree = 
            (CommonTree)adaptor.create(char_literal41)
            ;
            adaptor.addChild(root_0, char_literal41_tree);


            desc=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_enumDef338); 
            desc_tree = 
            (CommonTree)adaptor.create(desc)
            ;
            adaptor.addChild(root_0, desc_tree);



            		String[] opts = (list!=null?input.toString(list.start,list.stop):null).split("\\|");
            		
            		// strip quotes
            		for(int i = 0; i < opts.length; i++) opts[i] = StringUtils.strip(opts[i], "\"");
            		EnumScriptParam param = new EnumScriptParam((ID38!=null?ID38.getText():null), 
            			StringUtils.strip((desc!=null?desc.getText():null), "\""), new Integer((def!=null?def.getText():null)), opts);
            		scriptParams.add(param);
            		

            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "enumDef"


    public static class labelDef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "labelDef"
    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:107:1: labelDef : 'label' ',' val= StringLiteral ',' desc= StringLiteral ;
    public final ScriptParamsParser.labelDef_return labelDef() throws RecognitionException {
        ScriptParamsParser.labelDef_return retval = new ScriptParamsParser.labelDef_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token val=null;
        Token desc=null;
        Token string_literal42=null;
        Token char_literal43=null;
        Token char_literal44=null;

        CommonTree val_tree=null;
        CommonTree desc_tree=null;
        CommonTree string_literal42_tree=null;
        CommonTree char_literal43_tree=null;
        CommonTree char_literal44_tree=null;

        try {
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:108:2: ( 'label' ',' val= StringLiteral ',' desc= StringLiteral )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:108:4: 'label' ',' val= StringLiteral ',' desc= StringLiteral
            {
            root_0 = (CommonTree)adaptor.nil();


            string_literal42=(Token)match(input,19,FOLLOW_19_in_labelDef354); 
            string_literal42_tree = 
            (CommonTree)adaptor.create(string_literal42)
            ;
            adaptor.addChild(root_0, string_literal42_tree);


            char_literal43=(Token)match(input,14,FOLLOW_14_in_labelDef356); 
            char_literal43_tree = 
            (CommonTree)adaptor.create(char_literal43)
            ;
            adaptor.addChild(root_0, char_literal43_tree);


            val=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_labelDef362); 
            val_tree = 
            (CommonTree)adaptor.create(val)
            ;
            adaptor.addChild(root_0, val_tree);


            char_literal44=(Token)match(input,14,FOLLOW_14_in_labelDef364); 
            char_literal44_tree = 
            (CommonTree)adaptor.create(char_literal44)
            ;
            adaptor.addChild(root_0, char_literal44_tree);


            desc=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_labelDef370); 
            desc_tree = 
            (CommonTree)adaptor.create(desc)
            ;
            adaptor.addChild(root_0, desc_tree);



            		LabelScriptParam param = new LabelScriptParam(StringUtils.strip((val!=null?val.getText():null), "\""), 
            			StringUtils.strip((desc!=null?desc.getText():null), "\""));
            		scriptParams.add(param);
            		

            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "labelDef"


    public static class idList_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "idList"
    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:116:1: idList : ID ( '|' ID )* ;
    public final ScriptParamsParser.idList_return idList() throws RecognitionException {
        ScriptParamsParser.idList_return retval = new ScriptParamsParser.idList_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token ID45=null;
        Token char_literal46=null;
        Token ID47=null;

        CommonTree ID45_tree=null;
        CommonTree char_literal46_tree=null;
        CommonTree ID47_tree=null;

        try {
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:117:2: ( ID ( '|' ID )* )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:117:4: ID ( '|' ID )*
            {
            root_0 = (CommonTree)adaptor.nil();


            ID45=(Token)match(input,ID,FOLLOW_ID_in_idList386); 
            ID45_tree = 
            (CommonTree)adaptor.create(ID45)
            ;
            adaptor.addChild(root_0, ID45_tree);


            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:117:7: ( '|' ID )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==25) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:117:8: '|' ID
            	    {
            	    char_literal46=(Token)match(input,25,FOLLOW_25_in_idList389); 
            	    char_literal46_tree = 
            	    (CommonTree)adaptor.create(char_literal46)
            	    ;
            	    adaptor.addChild(root_0, char_literal46_tree);


            	    ID47=(Token)match(input,ID,FOLLOW_ID_in_idList391); 
            	    ID47_tree = 
            	    (CommonTree)adaptor.create(ID47)
            	    ;
            	    adaptor.addChild(root_0, ID47_tree);


            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "idList"


    public static class boolList_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "boolList"
    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:120:1: boolList : BOOLEAN ( '|' BOOLEAN )* ;
    public final ScriptParamsParser.boolList_return boolList() throws RecognitionException {
        ScriptParamsParser.boolList_return retval = new ScriptParamsParser.boolList_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token BOOLEAN48=null;
        Token char_literal49=null;
        Token BOOLEAN50=null;

        CommonTree BOOLEAN48_tree=null;
        CommonTree char_literal49_tree=null;
        CommonTree BOOLEAN50_tree=null;

        try {
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:121:2: ( BOOLEAN ( '|' BOOLEAN )* )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:121:4: BOOLEAN ( '|' BOOLEAN )*
            {
            root_0 = (CommonTree)adaptor.nil();


            BOOLEAN48=(Token)match(input,BOOLEAN,FOLLOW_BOOLEAN_in_boolList405); 
            BOOLEAN48_tree = 
            (CommonTree)adaptor.create(BOOLEAN48)
            ;
            adaptor.addChild(root_0, BOOLEAN48_tree);


            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:121:12: ( '|' BOOLEAN )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==25) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:121:13: '|' BOOLEAN
            	    {
            	    char_literal49=(Token)match(input,25,FOLLOW_25_in_boolList408); 
            	    char_literal49_tree = 
            	    (CommonTree)adaptor.create(char_literal49)
            	    ;
            	    adaptor.addChild(root_0, char_literal49_tree);


            	    BOOLEAN50=(Token)match(input,BOOLEAN,FOLLOW_BOOLEAN_in_boolList410); 
            	    BOOLEAN50_tree = 
            	    (CommonTree)adaptor.create(BOOLEAN50)
            	    ;
            	    adaptor.addChild(root_0, BOOLEAN50_tree);


            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "boolList"


    public static class stringList_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "stringList"
    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:124:1: stringList : StringLiteral ( '|' StringLiteral )* ;
    public final ScriptParamsParser.stringList_return stringList() throws RecognitionException {
        ScriptParamsParser.stringList_return retval = new ScriptParamsParser.stringList_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token StringLiteral51=null;
        Token char_literal52=null;
        Token StringLiteral53=null;

        CommonTree StringLiteral51_tree=null;
        CommonTree char_literal52_tree=null;
        CommonTree StringLiteral53_tree=null;

        try {
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:125:2: ( StringLiteral ( '|' StringLiteral )* )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:125:4: StringLiteral ( '|' StringLiteral )*
            {
            root_0 = (CommonTree)adaptor.nil();


            StringLiteral51=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_stringList424); 
            StringLiteral51_tree = 
            (CommonTree)adaptor.create(StringLiteral51)
            ;
            adaptor.addChild(root_0, StringLiteral51_tree);


            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:125:18: ( '|' StringLiteral )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==25) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:125:19: '|' StringLiteral
            	    {
            	    char_literal52=(Token)match(input,25,FOLLOW_25_in_stringList427); 
            	    char_literal52_tree = 
            	    (CommonTree)adaptor.create(char_literal52)
            	    ;
            	    adaptor.addChild(root_0, char_literal52_tree);


            	    StringLiteral53=(Token)match(input,StringLiteral,FOLLOW_StringLiteral_in_stringList429); 
            	    StringLiteral53_tree = 
            	    (CommonTree)adaptor.create(StringLiteral53)
            	    ;
            	    adaptor.addChild(root_0, StringLiteral53_tree);


            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "stringList"

    // Delegated rules


 

    public static final BitSet FOLLOW_21_in_params48 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_16_in_params50 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_paramDef_in_params52 = new BitSet(new long[]{0x000000000000C000L});
    public static final BitSet FOLLOW_14_in_params55 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_paramDef_in_params57 = new BitSet(new long[]{0x000000000000C000L});
    public static final BitSet FOLLOW_15_in_params61 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_24_in_paramDef82 = new BitSet(new long[]{0x0000000000DE0000L});
    public static final BitSet FOLLOW_paramSubDef_in_paramDef84 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_paramDef86 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stringDef_in_paramSubDef98 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanDef_in_paramSubDef103 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_enumDef_in_paramSubDef108 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_separatorDef_in_paramSubDef113 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_multiBoolDef_in_paramSubDef118 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_labelDef_in_paramSubDef123 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_22_in_separatorDef135 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_separatorDef137 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_StringLiteral_in_separatorDef143 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_14_in_separatorDef146 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_BOOLEAN_in_separatorDef152 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_23_in_stringDef170 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_stringDef172 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_ID_in_stringDef174 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_stringDef176 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_StringLiteral_in_stringDef182 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_stringDef184 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_StringLiteral_in_stringDef190 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_17_in_booleanDef207 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_booleanDef209 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_ID_in_booleanDef211 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_booleanDef213 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_BOOLEAN_in_booleanDef219 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_booleanDef221 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_StringLiteral_in_booleanDef227 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_booleanDef229 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_StringLiteral_in_booleanDef235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_20_in_multiBoolDef251 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_multiBoolDef253 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_idList_in_multiBoolDef259 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_multiBoolDef261 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_boolList_in_multiBoolDef267 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_multiBoolDef269 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_stringList_in_multiBoolDef275 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_multiBoolDef277 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_StringLiteral_in_multiBoolDef283 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_14_in_multiBoolDef286 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_INT_in_multiBoolDef292 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_18_in_enumDef310 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_enumDef312 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_ID_in_enumDef314 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_enumDef316 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_stringList_in_enumDef322 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_enumDef324 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_INT_in_enumDef330 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_enumDef332 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_StringLiteral_in_enumDef338 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_19_in_labelDef354 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_labelDef356 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_StringLiteral_in_labelDef362 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_labelDef364 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_StringLiteral_in_labelDef370 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_idList386 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_25_in_idList389 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_ID_in_idList391 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_BOOLEAN_in_boolList405 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_25_in_boolList408 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_BOOLEAN_in_boolList410 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_StringLiteral_in_stringList424 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_25_in_stringList427 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_StringLiteral_in_stringList429 = new BitSet(new long[]{0x0000000002000002L});

}