grammar Expr;
init: expr ;
expr: 'NOT' expr
	| expr 'AND' expr
	| expr 'OR' expr
	| '(' expr ')'
	| WORD '*'
	| '"' phrase '"'
	| WORD
	| expr WS expr
	| /* epsilon */
	;
phrase: phrase WS phrase
	| WORD
	;

WORD: [A-Za-z0-9_@./#&+-]+ ;
WS : [ |\t]+ ;