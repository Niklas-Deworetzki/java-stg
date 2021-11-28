package deworetzki.stg.parser;

import java_cup.runtime.*;
import deworetzki.parse.*;
import deworetzki.parse.symbol.*;

%%

%class Scanner
%public

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

    private Position currentPosition() {
        // Return the current position, counting lines and columns beginning from 1.
        return new Position(source, yyline + 1, yycolumn + 1);
    }

    private Symbol symbol(int type) {
        return new RichSymbol(currentPosition(), type);
    }

    private Symbol symbol(int type, Object value) {
        return new RichSymbol(currentPosition(), type, value);
    }
%}

%%

\/\/.*                  { /* Ignore line comments */ }
\/\*([^*]|\*[^/])*\*\/  { /* Ignore multi-line comments */ }

\s+     { /* Ignore whitespace */ }

\(      { return symbol(LPAR); }
\)      { return symbol(RPAR); }
\[      { return symbol(LBRA); }
\]      { return symbol(RBRA); }
,       { return symbol(COMMA); }
\<=>    { return symbol(SWAP); }

\+      { return symbol(ADD); }
\-      { return symbol(SUB); }
\*      { return symbol(MUL); }
\/      { return symbol(DIV); }
%       { return symbol(MOD); }

&       { return symbol(AND); }
\|      { return symbol(OR); }
\^      { return symbol(XOR); }

&&      { return symbol(CONJ); }
\|\|    { return symbol(DISJ); }

\<      { return symbol(LST); }
\<=     { return symbol(LSE); }
>       { return symbol(GRT); }
>=      { return symbol(GRE); }
=       { return symbol(EQU); }
\!=      { return symbol(NEQ); }


int         { return symbol(INT); }
stack       { return symbol(STACK); }
procedure   { return symbol(PROCEDURE); }
call        { return symbol(CALL); }
uncall      { return symbol(UNCALL); }
if          { return symbol(IF); }
then        { return symbol(THEN); }
else        { return symbol(ELSE); }
fi          { return symbol(FI); }
from        { return symbol(FROM); }
do          { return symbol(DO); }
loop        { return symbol(LOOP); }
until       { return symbol(UNTIL); }
push        { return symbol(PUSH); }
pop         { return symbol(POP); }
local       { return symbol(LOCAL); }
delocal     { return symbol(DELOCAL); }
skip        { return symbol(SKIP); }
empty       { return symbol(EMPTY); }
top         { return symbol(TOP); }
nil         { return symbol(NIL); }

read        { return symbol(READ); }
write       { return symbol(WRITE); }
readc       { return symbol(READC); }
writec      { return symbol(WRITEC); }

main        { return symbol(MAIN); }

[a-zA-Z][a-zA-Z_0-9]* { return symbol(IDENT, new Identifier(yytext(), currentPosition())); }

0|-?[1-9][0-9]*  { try {
                       return symbol(INTLIT, Integer.parseInt(yytext()));
                   } catch (NumberFormatException invalidNumber) {
                       throw new CompileError.InvalidLiteral("number", yytext(), currentPosition());
                   }
                 }

'.'              { return symbol(INTLIT, (int) yytext().charAt(1)); }
'\\n'            { return symbol(INTLIT, (int) '\n'); }
'\\r'            { return symbol(INTLIT, (int) '\r'); }
'\\t'            { return symbol(INTLIT, (int) '\t'); }

[^]     { // This rule matches any previously unmatched characters.
          char offendingChar = yytext().charAt(0);
          throw new RuntimeException("Illegal symbol " + offendingChar);
          //throw new CompileError.IllegalCharacter(offendingChar, currentPosition());
        }
