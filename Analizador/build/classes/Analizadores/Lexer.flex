package Analizadores;
import static Analizadores.Tokens.*;
%%
%class Lexer
%type Tokens
Letra=[a-zA-Z]+
Digito=[0-9]+
Puntuacion=[;,]
Agrupacion=[\(\)]
OperadorArit=[-/\+\*]
OperadorAsig=[=]
Espacio=[ \t\r]+
SaltoDeLinea=[\n]
%{
	public String Lexeme;
%}
%%

int | float | char {Lexeme=yytext(); return tipo;}

{Puntuacion} {Lexeme=yytext(); return puntuacion;}
{Agrupacion} {Lexeme=yytext(); return agrupacion;}
{OperadorArit} {Lexeme=yytext(); return operadorAritmetico;}
{OperadorAsig} {Lexeme=yytext(); return operadorAsignacion;}

{SaltoDeLinea} {Lexeme=yytext(); return salto;}
{Espacio} {/*Ignore*/}
"//" {/*Ignore*/}

{Letra} ({Letra}|{Digito})* {Lexeme=yytext(); return id;}
("(-"{Digito}+")") | {Digito}+ {Lexeme=yytext(); return num;}
 . {Lexeme=yytext(); return error;}