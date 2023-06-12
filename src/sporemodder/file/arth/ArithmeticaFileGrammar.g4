grammar ArithmeticaFileGrammar;
program:   (NEWLINE* (function | statement))* ;

function
    :   DEF IDENTIFIER '(' ')' NEWLINE+ statement* ENDDEF NEWLINE+   
    ;

statement 
    :   ifFragment statement* (elseFragment statement*)? endifFragment  # ConditionalStatement
    |   IDENTIFIER '=' expr (NEWLINE+ | EOF)  # AssignStatement
    ;
    
ifFragment : IF '(' conditionalExpr ')' NEWLINE* ;
elseFragment : ELSE NEWLINE* ;
endifFragment : ENDIF NEWLINE* ;
    
conditionalExpr
    :   conditionalExpr (AND | OR) conditionalExpr
    |   '(' conditionalExpr ')'
    |   expr
    ;
    
expr   
    :   RAND  # Random
    |   (ADD | SUB)? IDENTIFIER   # Variable
    |   FLOATING_LITERAL  # Number
    |   IDENTIFIER '(' expr? ')'  # Call
    |	expr POW expr  # PowerOp
    |   expr (MULT | DIV) expr  # MultiplicativeOp
    |   expr (ADD | SUB) expr  # AdditiveOp
    |   expr (EQ | NEQ | LESS | GREATER | LEQ | GEQ) expr  # ComparativeOp
    |   (ADD | SUB)? '(' expr ')'  # Parenthesis
    ;
    
IF: 'if' ;
ELSE: 'else' ;
ENDIF: 'endif' ;
AND: 'and' ;
OR: 'or' ;
DEF: 'def' ;
ENDDEF: 'enddef' ;

RAND: 'RAND';

POW: '^' ;
ADD: '+' ;
SUB: '-' ;
MULT: '*' ;
DIV: '/' ;
EQ: ('~=' | '==') ;
NEQ: '!=' ;
LESS: '<' ;
GREATER: '>' ;
LEQ: '<=' ;
GEQ: '>=' ;
    
IDENTIFIER: [a-zA-Z_][a-zA-Z0-9_]*;
    
FLOATING_LITERAL:
	(ADD | SUB)? Fractionalconstant Exponentpart? 
	| (ADD | SUB)? Digitsequence Exponentpart?;
	
fragment Fractionalconstant:
	Digitsequence '.' Digitsequence
	| Digitsequence '.';

fragment Exponentpart:
	'e' (ADD | SUB)? Digitsequence
	| 'E' (ADD | SUB)? Digitsequence;

fragment DIGIT: [0-9];

fragment Digitsequence: DIGIT ('\''? DIGIT)*;
    
//NEWLINE : [\r\n]+ ;

NEWLINE : ('\r'? '\n' | '\r')+ ;

LINE_COMMENT: '#' ~ [\r\n]* -> skip;

WS  : [ \t]+ -> skip ;