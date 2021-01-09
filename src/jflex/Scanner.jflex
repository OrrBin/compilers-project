/***************************/
/* Based on a template by Oren Ish-Shalom */
/***************************/

/*************/
/* USER CODE */
/*************/
import java_cup.runtime.*;



/******************************/
/* DOLAR DOLAR - DON'T TOUCH! */
/******************************/

%%

/************************************/
/* OPTIONS AND DECLARATIONS SECTION */
/************************************/

/*****************************************************/
/* Lexer is the name of the class JFlex will create. */
/* The code will be written to the file Lexer.java.  */
/*****************************************************/
%class Lexer

/********************************************************************/
/* The current line number can be accessed with the variable yyline */
/* and the current column number with the variable yycolumn.        */
/********************************************************************/
%line
%column

/******************************************************************/
/* CUP compatibility mode interfaces with a CUP generated parser. */
/******************************************************************/
%cup

/****************/
/* DECLARATIONS */
/****************/
/*****************************************************************************/
/* Code between %{ and %}, both of which must be at the beginning of a line, */
/* will be copied verbatim (letter to letter) into the Lexer class code.     */
/* Here you declare member variables and functions that are used inside the  */
/* scanner actions.                                                          */
/*****************************************************************************/
%{
	/*********************************************************************************/
	/* Create a new java_cup.runtime.Symbol with information about the current token */
	/*********************************************************************************/
	private Symbol symbol(int type)               {return new Symbol(type, yyline, yycolumn);}
	private Symbol symbol(int type, Object value) {return new Symbol(type, yyline, yycolumn, value);}

	/*******************************************/
	/* Enable line number extraction from main */
	/*******************************************/
	public int getLine()    { return yyline + 1; }
	public int getCharPos() { return yycolumn;   }
%}

/***********************/
/* MACRO DECALARATIONS */
/***********************/
INT	          = 0 | [1-9][0-9]*
ID            = ([a-zA-Z]+)([0-9]|[a-zA-Z]|[_])*
Char          = [^\r\n]
LineBreak     = \r|\n|\r\n
Space         = [\t ] | {LineBreak}

Comment       = {MultiComment} | {SingleComment}
MultiComment  = "/*" ~"*/"
SingleComment = "//" {Char}* {LineBreak}?

/******************************/
/* DOLAR DOLAR - DON'T TOUCH! */
/******************************/

%%

/************************************************************/
/* LEXER matches regular expressions to actions (Java code) */
/************************************************************/

/**************************************************************/
/* YYINITIAL is the state at which the lexer begins scanning. */
/* So these regular expressions will only be matched if the   */
/* scanner is in the start state YYINITIAL.                   */
/**************************************************************/

<YYINITIAL> {

// preserved
"public"                { return symbol(sym.PUBLIC); }
"main"                  { return symbol(sym.MAIN); }
"this"                  { return symbol(sym.THIS); }
"new"                   { return symbol(sym.NEW); }
"if"                    { return symbol(sym.IF); }
"else"                  { return symbol(sym.ELSE); }
"while"                 { return symbol(sym.WHILE); }
"class"                 { return symbol(sym.CLASS); }
"length"                { return symbol(sym.LENGTH); }
"extends"               { return symbol(sym.EXTENDS); }
"return"                { return symbol(sym.RETURN); }
"static"                { return symbol(sym.STATIC); }
"System.out.println"    { return symbol(sym.SYSOUT); }

// types
"boolean"               { return symbol(sym.BOOLEAN); }
"true"                  { return symbol(sym.TRUE); }
"false"                 { return symbol(sym.FALSE); }
"String"                { return symbol(sym.STRING); }
"int"                   { return symbol(sym.INT); }
"void"                  { return symbol(sym.VOID); }

// symbols
"{"                     { return symbol(sym.LBRACE); }
"}"                     { return symbol(sym.RBRACE); }
"("                     { return symbol(sym.LPAREN); }
")"                     { return symbol(sym.RPAREN); }
"["                     { return symbol(sym.LSQRBRACKET); }
"]"                     { return symbol(sym.RSQRBRACKET); }
","			            { return symbol(sym.COMMA); }
"+"                     { return symbol(sym.ADD); }
"-"                     { return symbol(sym.SUB); }
"*"                     { return symbol(sym.MULT); }
"<"                     { return symbol(sym.LT); }
"&&"                    { return symbol(sym.AND); }
"="                     { return symbol(sym.ASSIGNMENT); }
"."                     { return symbol(sym.DOT); }
";"                     { return symbol(sym.SEMICOLON); }
"!"                     { return symbol(sym.NOT); }

// regex
{Comment}               { }
{ID}		            { return symbol(sym.ID, new String(yytext())); }
{INT}                   { return symbol(sym.NUMBER, Integer.parseInt(yytext())); }
{Space}                 { /* do nothing */ }
<<EOF>>		            { return symbol(sym.EOF); }
}
