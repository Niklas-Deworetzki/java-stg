package deworetzki.stg.parser;

import java_cup.runtime.*;
import deworetzki.parse.*;
import deworetzki.parse.symbol.*;
import deworetzki.messages.*;

import static deworetzik.stg.parse.Sym.*;

%%

%class Scanner
%public
%implements PositionedLexer

%cup
%unicode

%line
%column

%eofclose false
%eofval{
    // This needs to be specified when using a custom sym class name
    return new RichSymbol(currentPosition(), EOF);
%eofval}

%ctorarg Source source

%init{
    this.source = source;
%init}
%{
    public final Source source;

    public Position currentPosition() {
        // Return the current position, counting lines and columns beginning from 1.
        return new Position(source, yyline + 1, yycolumn + 1);
    }

    private int integerValue(String str) throws ErrorMessage {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ignored) {
            throw new ErrorMessage.InvalidNumber(currentPosition(), str);
        }
    }
%}

WhiteSpace  = \s+

Comment     = {LineComment}
LineComment = "//".*

ArbitraryCharacter = [^#{()}\\\s]
VariableName  = [^A-Z0-9#{()}\\\s] {ArbitraryCharacter}*
TypeName      = [A-Z] {ArbitraryCharacter}*
PrimitiveName = {VariableName} "#"

BoxedInteger     = 0|[1-9][0-9]*
PrimitiveInteger = {BoxedInteger} "#"

%%

{WhiteSpace}                         { /* Ignore whitespace */  }
{Comment}                            { /* Ignore comments */ }

"{"      { return symbol(LEFT); }
"("      { return symbol(LEFT); }
"}"      { return symbol(RIGHT); }
")"      { return symbol(RIGHT); }

"let"     { return symbol(LET); }
"letrec"  { return symbol(LETREC); }
"in"      { return symbol(IN); }
"case"    { return symbol(CASE); }
"of"      { return symbol(OF); }
"default" { return symbol(DEFAULT); }

"="      { return symbol(EQ); }
"\\"     { return symbol(LAMBDA); }
"->"     { return symbol(ARROW); }
"-u>"    { return symbol(U_ARROW); }
"-n>"    { return symbol(N_ARROW); }

{VariableName}   { return symbol(VARIABLE, yytext()); }
{TypeName}       { return symbol(TYPE, yytext()); }
{PrimitiveName}  { return symbol(PRIMITIVE, yytext()); }

{BoxedInteger}     { return symbol(INTBOX, integerValue(yytext())); }
{PrimitiveInteger} { return symbol(INTLIT, integerValue(yytext().substring(0, yytext().length() - 1))); }

[^]     { // This rule matches any previously unmatched characters.
          char offendingChar = yytext().charAt(0);
          throw new ErrorMessage.IllegalInputCharacter(currentPosition(), offendingChar);
        }
