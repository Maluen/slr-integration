grammar Query;
init: expr ;
expr: 'NOT' expr			# Not
	| expr 'AND' expr		# And
	| expr 'OR' expr		# Or
	| '(' expr ')'			# Parenthesis
	| WORD '*'				# Wildcard
	| '"' phrase '"'		# Strict
	| WS expr				# SpacesLeft
	| expr WS				# SpacesRight
	| phrase				# PhraseCase
	| WORD					# Word
	| 						# Epsilon
	;
phrase: WORD WS phrase
	| WORD
	;

WORD: [A-Za-z0-9_@./#&+-]+ ;
WS : [ |\t]+ ;