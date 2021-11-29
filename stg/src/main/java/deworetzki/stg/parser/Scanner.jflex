package deworetzki.stg.parser;

import java_cup.runtime.*;
import deworetzki.parse.*;
import deworetzki.parse.symbol.*;
import deworetzki.messages.*;

import static deworetzik.stg.parse.Sym.*;

%%

%class Scanner
%abstract
%public
%implements PositionedLexer

%cup
%unicode

%line
%column

%eofclose
%eofval{
    // This needs to be specified when using a custom sym class name
    return new RichSymbol(currentPosition(), EOF);
%eofval}

%implements java.io.Closeable
%ctorarg Source source
%init{
    this.source = source;
%init}
%{
    public final Source source;

    // Implement java.io.Closeable for Scanner.
    @Override
    public void close() throws java.io.IOException {
        yyclose();
    }

    public Position currentPosition() {
        // Return the current position, counting lines and columns beginning from 1.
        return new Position(source, yyline + 1, yycolumn + 1);
    }

    protected abstract void handleIndentation(String indentation);
%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]
Indentation    = {WhiteSpace}

Comment     = {LineComment}
LineComment = "#" {InputCharacter}*

%%

{LineTerminator} {Indentation}* $    { /* Ignore empty lines */ }

{LineTerminator} {Indentation}*      { handleIndentation(yytext());
                                       return symbol(TERMINATOR);
                                     }

[^]     { // This rule matches any previously unmatched characters.
          char offendingChar = yytext().charAt(0);
          throw new ErrorMessage.IllegalInputCharacter(offendingChar, currentPosition());
        }
