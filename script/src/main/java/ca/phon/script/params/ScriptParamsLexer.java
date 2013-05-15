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
// $ANTLR 3.4 C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g 2012-08-27 12:44:11

package ca.phon.script.params;


import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class ScriptParamsLexer extends Lexer {
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
    // delegators
    public Lexer[] getDelegates() {
        return new Lexer[] {};
    }

    public ScriptParamsLexer() {} 
    public ScriptParamsLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public ScriptParamsLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }
    public String getGrammarFileName() { return "C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g"; }

    // $ANTLR start "T__14"
    public final void mT__14() throws RecognitionException {
        try {
            int _type = T__14;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:6:7: ( ',' )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:6:9: ','
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
    // $ANTLR end "T__14"

    // $ANTLR start "T__15"
    public final void mT__15() throws RecognitionException {
        try {
            int _type = T__15;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:7:7: ( ';' )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:7:9: ';'
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
    // $ANTLR end "T__15"

    // $ANTLR start "T__16"
    public final void mT__16() throws RecognitionException {
        try {
            int _type = T__16;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:8:7: ( '=' )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:8:9: '='
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
    // $ANTLR end "T__16"

    // $ANTLR start "T__17"
    public final void mT__17() throws RecognitionException {
        try {
            int _type = T__17;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:9:7: ( 'bool' )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:9:9: 'bool'
            {
            match("bool"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__17"

    // $ANTLR start "T__18"
    public final void mT__18() throws RecognitionException {
        try {
            int _type = T__18;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:10:7: ( 'enum' )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:10:9: 'enum'
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
    // $ANTLR end "T__18"

    // $ANTLR start "T__19"
    public final void mT__19() throws RecognitionException {
        try {
            int _type = T__19;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:11:7: ( 'label' )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:11:9: 'label'
            {
            match("label"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__19"

    // $ANTLR start "T__20"
    public final void mT__20() throws RecognitionException {
        try {
            int _type = T__20;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:12:7: ( 'multibool' )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:12:9: 'multibool'
            {
            match("multibool"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__20"

    // $ANTLR start "T__21"
    public final void mT__21() throws RecognitionException {
        try {
            int _type = T__21;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:13:7: ( 'params' )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:13:9: 'params'
            {
            match("params"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__21"

    // $ANTLR start "T__22"
    public final void mT__22() throws RecognitionException {
        try {
            int _type = T__22;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:14:7: ( 'separator' )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:14:9: 'separator'
            {
            match("separator"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__22"

    // $ANTLR start "T__23"
    public final void mT__23() throws RecognitionException {
        try {
            int _type = T__23;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:15:7: ( 'string' )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:15:9: 'string'
            {
            match("string"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__23"

    // $ANTLR start "T__24"
    public final void mT__24() throws RecognitionException {
        try {
            int _type = T__24;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:16:7: ( '{' )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:16:9: '{'
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
    // $ANTLR end "T__24"

    // $ANTLR start "T__25"
    public final void mT__25() throws RecognitionException {
        try {
            int _type = T__25;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:17:7: ( '|' )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:17:9: '|'
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
    // $ANTLR end "T__25"

    // $ANTLR start "T__26"
    public final void mT__26() throws RecognitionException {
        try {
            int _type = T__26;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:18:7: ( '}' )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:18:9: '}'
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
    // $ANTLR end "T__26"

    // $ANTLR start "StringLiteral"
    public final void mStringLiteral() throws RecognitionException {
        try {
            int _type = StringLiteral;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:130:6: ( '\"' ( EscapeSequence |~ ( '\\\\' | '\"' ) )* '\"' )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:130:9: '\"' ( EscapeSequence |~ ( '\\\\' | '\"' ) )* '\"'
            {
            match('\"'); 

            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:130:13: ( EscapeSequence |~ ( '\\\\' | '\"' ) )*
            loop1:
            do {
                int alt1=3;
                int LA1_0 = input.LA(1);

                if ( (LA1_0=='\\') ) {
                    alt1=1;
                }
                else if ( ((LA1_0 >= '\u0000' && LA1_0 <= '!')||(LA1_0 >= '#' && LA1_0 <= '[')||(LA1_0 >= ']' && LA1_0 <= '\uFFFF')) ) {
                    alt1=2;
                }


                switch (alt1) {
            	case 1 :
            	    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:130:15: EscapeSequence
            	    {
            	    mEscapeSequence(); 


            	    }
            	    break;
            	case 2 :
            	    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:130:32: ~ ( '\\\\' | '\"' )
            	    {
            	    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '!')||(input.LA(1) >= '#' && input.LA(1) <= '[')||(input.LA(1) >= ']' && input.LA(1) <= '\uFFFF') ) {
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
            	    break loop1;
                }
            } while (true);


            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "StringLiteral"

    // $ANTLR start "BOOLEAN"
    public final void mBOOLEAN() throws RecognitionException {
        try {
            int _type = BOOLEAN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:134:2: ( 'true' | 'false' )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='t') ) {
                alt2=1;
            }
            else if ( (LA2_0=='f') ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;

            }
            switch (alt2) {
                case 1 :
                    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:134:4: 'true'
                    {
                    match("true"); 



                    }
                    break;
                case 2 :
                    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:134:13: 'false'
                    {
                    match("false"); 



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
    // $ANTLR end "BOOLEAN"

    // $ANTLR start "INT"
    public final void mINT() throws RecognitionException {
        try {
            int _type = INT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:138:2: ( ( '0' .. '9' )+ )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:138:4: ( '0' .. '9' )+
            {
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:138:4: ( '0' .. '9' )+
            int cnt3=0;
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0 >= '0' && LA3_0 <= '9')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:
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
            	    if ( cnt3 >= 1 ) break loop3;
                        EarlyExitException eee =
                            new EarlyExitException(3, input);
                        throw eee;
                }
                cnt3++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "INT"

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:142:2: ( LETTER ( LETTER | '0' .. '9' )* )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:142:4: LETTER ( LETTER | '0' .. '9' )*
            {
            mLETTER(); 


            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:142:11: ( LETTER | '0' .. '9' )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0 >= '0' && LA4_0 <= '9')||(LA4_0 >= 'A' && LA4_0 <= 'Z')||LA4_0=='_'||(LA4_0 >= 'a' && LA4_0 <= 'z')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
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
            	    break loop4;
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
    // $ANTLR end "ID"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:145:5: ( ( ' ' | '\\r' | '\\t' | '\\u000C' | '\\n' ) )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:145:8: ( ' ' | '\\r' | '\\t' | '\\u000C' | '\\n' )
            {
            if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||(input.LA(1) >= '\f' && input.LA(1) <= '\r')||input.LA(1)==' ' ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "EscapeSequence"
    public final void mEscapeSequence() throws RecognitionException {
        try {
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:151:6: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | UnicodeEscape | OctalEscape )
            int alt5=3;
            int LA5_0 = input.LA(1);

            if ( (LA5_0=='\\') ) {
                switch ( input.LA(2) ) {
                case '\"':
                case '\'':
                case '\\':
                case 'b':
                case 'f':
                case 'n':
                case 'r':
                case 't':
                    {
                    alt5=1;
                    }
                    break;
                case 'u':
                    {
                    alt5=2;
                    }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                    {
                    alt5=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 1, input);

                    throw nvae;

                }

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;

            }
            switch (alt5) {
                case 1 :
                    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:151:10: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' )
                    {
                    match('\\'); 

                    if ( input.LA(1)=='\"'||input.LA(1)=='\''||input.LA(1)=='\\'||input.LA(1)=='b'||input.LA(1)=='f'||input.LA(1)=='n'||input.LA(1)=='r'||input.LA(1)=='t' ) {
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
                    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:152:10: UnicodeEscape
                    {
                    mUnicodeEscape(); 


                    }
                    break;
                case 3 :
                    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:153:10: OctalEscape
                    {
                    mOctalEscape(); 


                    }
                    break;

            }

        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "EscapeSequence"

    // $ANTLR start "OctalEscape"
    public final void mOctalEscape() throws RecognitionException {
        try {
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:158:6: ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) )
            int alt6=3;
            int LA6_0 = input.LA(1);

            if ( (LA6_0=='\\') ) {
                int LA6_1 = input.LA(2);

                if ( ((LA6_1 >= '0' && LA6_1 <= '3')) ) {
                    int LA6_2 = input.LA(3);

                    if ( ((LA6_2 >= '0' && LA6_2 <= '7')) ) {
                        int LA6_4 = input.LA(4);

                        if ( ((LA6_4 >= '0' && LA6_4 <= '7')) ) {
                            alt6=1;
                        }
                        else {
                            alt6=2;
                        }
                    }
                    else {
                        alt6=3;
                    }
                }
                else if ( ((LA6_1 >= '4' && LA6_1 <= '7')) ) {
                    int LA6_3 = input.LA(3);

                    if ( ((LA6_3 >= '0' && LA6_3 <= '7')) ) {
                        alt6=2;
                    }
                    else {
                        alt6=3;
                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 6, 1, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;

            }
            switch (alt6) {
                case 1 :
                    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:158:10: '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 

                    if ( (input.LA(1) >= '0' && input.LA(1) <= '3') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


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
                case 2 :
                    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:159:10: '\\\\' ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 

                    if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


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
                case 3 :
                    // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:160:10: '\\\\' ( '0' .. '7' )
                    {
                    match('\\'); 

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

            }

        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "OctalEscape"

    // $ANTLR start "HexDigit"
    public final void mHexDigit() throws RecognitionException {
        try {
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:164:10: ( ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ) )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:
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

    // $ANTLR start "UnicodeEscape"
    public final void mUnicodeEscape() throws RecognitionException {
        try {
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:169:2: ( '\\\\' 'u' HexDigit HexDigit HexDigit HexDigit )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:169:6: '\\\\' 'u' HexDigit HexDigit HexDigit HexDigit
            {
            match('\\'); 

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
    // $ANTLR end "UnicodeEscape"

    // $ANTLR start "LETTER"
    public final void mLETTER() throws RecognitionException {
        try {
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:174:2: ( 'A' .. 'Z' | 'a' .. 'z' | '_' )
            // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:
            {
            if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
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
    // $ANTLR end "LETTER"

    public void mTokens() throws RecognitionException {
        // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:1:8: ( T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | StringLiteral | BOOLEAN | INT | ID | WS )
        int alt7=18;
        alt7 = dfa7.predict(input);
        switch (alt7) {
            case 1 :
                // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:1:10: T__14
                {
                mT__14(); 


                }
                break;
            case 2 :
                // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:1:16: T__15
                {
                mT__15(); 


                }
                break;
            case 3 :
                // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:1:22: T__16
                {
                mT__16(); 


                }
                break;
            case 4 :
                // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:1:28: T__17
                {
                mT__17(); 


                }
                break;
            case 5 :
                // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:1:34: T__18
                {
                mT__18(); 


                }
                break;
            case 6 :
                // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:1:40: T__19
                {
                mT__19(); 


                }
                break;
            case 7 :
                // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:1:46: T__20
                {
                mT__20(); 


                }
                break;
            case 8 :
                // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:1:52: T__21
                {
                mT__21(); 


                }
                break;
            case 9 :
                // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:1:58: T__22
                {
                mT__22(); 


                }
                break;
            case 10 :
                // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:1:64: T__23
                {
                mT__23(); 


                }
                break;
            case 11 :
                // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:1:70: T__24
                {
                mT__24(); 


                }
                break;
            case 12 :
                // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:1:76: T__25
                {
                mT__25(); 


                }
                break;
            case 13 :
                // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:1:82: T__26
                {
                mT__26(); 


                }
                break;
            case 14 :
                // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:1:88: StringLiteral
                {
                mStringLiteral(); 


                }
                break;
            case 15 :
                // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:1:102: BOOLEAN
                {
                mBOOLEAN(); 


                }
                break;
            case 16 :
                // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:1:110: INT
                {
                mINT(); 


                }
                break;
            case 17 :
                // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:1:114: ID
                {
                mID(); 


                }
                break;
            case 18 :
                // C:\\Users\\ghedlund\\Documents\\Phon\\gitprojects\\phon-old\\src\\ca\\phon\\engines\\search\\script\\params\\ScriptParams.g:1:117: WS
                {
                mWS(); 


                }
                break;

        }

    }


    protected DFA7 dfa7 = new DFA7(this);
    static final String DFA7_eotS =
        "\4\uffff\6\21\4\uffff\2\21\3\uffff\22\21\1\56\1\57\5\21\1\65\1\21"+
        "\2\uffff\1\67\4\21\1\uffff\1\65\1\uffff\1\21\1\75\1\21\1\77\1\21"+
        "\1\uffff\1\21\1\uffff\2\21\1\104\1\105\2\uffff";
    static final String DFA7_eofS =
        "\106\uffff";
    static final String DFA7_minS =
        "\1\11\3\uffff\1\157\1\156\1\141\1\165\1\141\1\145\4\uffff\1\162"+
        "\1\141\3\uffff\1\157\1\165\1\142\1\154\1\162\1\160\1\162\1\165\2"+
        "\154\1\155\1\145\1\164\2\141\1\151\1\145\1\163\2\60\1\154\1\151"+
        "\1\155\1\162\1\156\1\60\1\145\2\uffff\1\60\1\142\1\163\1\141\1\147"+
        "\1\uffff\1\60\1\uffff\1\157\1\60\1\164\1\60\1\157\1\uffff\1\157"+
        "\1\uffff\1\154\1\162\2\60\2\uffff";
    static final String DFA7_maxS =
        "\1\175\3\uffff\1\157\1\156\1\141\1\165\1\141\1\164\4\uffff\1\162"+
        "\1\141\3\uffff\1\157\1\165\1\142\1\154\1\162\1\160\1\162\1\165\2"+
        "\154\1\155\1\145\1\164\2\141\1\151\1\145\1\163\2\172\1\154\1\151"+
        "\1\155\1\162\1\156\1\172\1\145\2\uffff\1\172\1\142\1\163\1\141\1"+
        "\147\1\uffff\1\172\1\uffff\1\157\1\172\1\164\1\172\1\157\1\uffff"+
        "\1\157\1\uffff\1\154\1\162\2\172\2\uffff";
    static final String DFA7_acceptS =
        "\1\uffff\1\1\1\2\1\3\6\uffff\1\13\1\14\1\15\1\16\2\uffff\1\20\1"+
        "\21\1\22\33\uffff\1\4\1\5\5\uffff\1\17\1\uffff\1\6\5\uffff\1\10"+
        "\1\uffff\1\12\4\uffff\1\7\1\11";
    static final String DFA7_specialS =
        "\106\uffff}>";
    static final String[] DFA7_transitionS = {
            "\2\22\1\uffff\2\22\22\uffff\1\22\1\uffff\1\15\11\uffff\1\1\3"+
            "\uffff\12\20\1\uffff\1\2\1\uffff\1\3\3\uffff\32\21\4\uffff\1"+
            "\21\1\uffff\1\21\1\4\2\21\1\5\1\17\5\21\1\6\1\7\2\21\1\10\2"+
            "\21\1\11\1\16\6\21\1\12\1\13\1\14",
            "",
            "",
            "",
            "\1\23",
            "\1\24",
            "\1\25",
            "\1\26",
            "\1\27",
            "\1\30\16\uffff\1\31",
            "",
            "",
            "",
            "",
            "\1\32",
            "\1\33",
            "",
            "",
            "",
            "\1\34",
            "\1\35",
            "\1\36",
            "\1\37",
            "\1\40",
            "\1\41",
            "\1\42",
            "\1\43",
            "\1\44",
            "\1\45",
            "\1\46",
            "\1\47",
            "\1\50",
            "\1\51",
            "\1\52",
            "\1\53",
            "\1\54",
            "\1\55",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "\1\60",
            "\1\61",
            "\1\62",
            "\1\63",
            "\1\64",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "\1\66",
            "",
            "",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "\1\70",
            "\1\71",
            "\1\72",
            "\1\73",
            "",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "",
            "\1\74",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "\1\76",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "\1\100",
            "",
            "\1\101",
            "",
            "\1\102",
            "\1\103",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "\12\21\7\uffff\32\21\4\uffff\1\21\1\uffff\32\21",
            "",
            ""
    };

    static final short[] DFA7_eot = DFA.unpackEncodedString(DFA7_eotS);
    static final short[] DFA7_eof = DFA.unpackEncodedString(DFA7_eofS);
    static final char[] DFA7_min = DFA.unpackEncodedStringToUnsignedChars(DFA7_minS);
    static final char[] DFA7_max = DFA.unpackEncodedStringToUnsignedChars(DFA7_maxS);
    static final short[] DFA7_accept = DFA.unpackEncodedString(DFA7_acceptS);
    static final short[] DFA7_special = DFA.unpackEncodedString(DFA7_specialS);
    static final short[][] DFA7_transition;

    static {
        int numStates = DFA7_transitionS.length;
        DFA7_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA7_transition[i] = DFA.unpackEncodedString(DFA7_transitionS[i]);
        }
    }

    class DFA7 extends DFA {

        public DFA7(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 7;
            this.eot = DFA7_eot;
            this.eof = DFA7_eof;
            this.min = DFA7_min;
            this.max = DFA7_max;
            this.accept = DFA7_accept;
            this.special = DFA7_special;
            this.transition = DFA7_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | StringLiteral | BOOLEAN | INT | ID | WS );";
        }
    }
 

}