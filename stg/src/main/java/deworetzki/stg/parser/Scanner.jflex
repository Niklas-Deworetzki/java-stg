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
WhiteSpace     = {LineTerminator} | [ \t\f]
Indentation    = {WhiteSpace}

Comment     = {LineComment}
LineComment = "//".*

ArbitraryCharacter = [^#\s]
VariableName  = [^A-Z0-9#\s] {ArbitraryCharacter}*
TypeName      = [A-Z] {ArbitraryCharacter}*
PrimitiveName = {VariableName} "#"

BoxedInteger     = 0|[1-9][0-9]*
PrimitiveInteger = {PrimitiveInteger} "#"

%%

{LineTerminator} {Indentation}* $    { /* Ignore empty lines */ }
{WhiteSpace}+                        { /* Ignore whitespace */  }

{LineTerminator} {Indentation}*      { handleIndentation(yytext());
                                       return symbol(TERMINATOR);
                                     }

;   { return symbol(TERMINATOR); }

"{"      { return symbol(LBRA); }
"}"      { return symbol(RBRA); }

"let"     { return symbol(LET); }
"letrec"  { return symbol(LETREC); }
"in"      { retrun symbol(IN); }
"case"    { return symbol(CASE); }
"of"      { return symbol(OF); }
"default" { return symbol(DEFAULT); }
"_"       { return symbol(UNDERSCORE); }

"="      { return symbol(EQ); }
"\\"     { return symbol(LAMBDA); }
"->"     { return symbol(SINGLEARROW); }
"=>"     { return symbol(DOUBLEARROW); }

{VariableName}  { return symbol(VARIABLE, yytext()); }
{TypeName}      { return symbol(TYPE, yytext()); }
{PrimitiveName} { return symbol(PRIMITIVE, yytext()); }

{PrimitiveInteger} { return symbol(INTLIT, Integer.parseInt(yytext().substring(0, yytext().length() - 1)); }

[^]     { // This rule matches any previously unmatched characters.
          char offendingChar = yytext().charAt(0);
          throw new ErrorMessage.IllegalInputCharacter(offendingChar, currentPosition());
        }
