grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';




//--- PARSER: ---
stylesheet: stylerule* | statement* EOF;

stylerule: selector OPEN_BRACE blockstatement CLOSE_BRACE;

selector
  : ID_IDENT | CLASS_IDENT | LOWER_IDENT | CAPITAL_IDENT;

blockstatement: statement*;

statement: declaration | variableAssignment | conditional | expression;

declaration: CAPITAL_IDENT ASSIGNMENT_OPERATOR expression SEMICOLON;

variableAssignment: LOWER_IDENT ASSIGNMENT_OPERATOR expression SEMICOLON;

conditional: IF BOX_BRACKET_OPEN expression BOX_BRACKET_CLOSE OPEN_BRACE blockstatement CLOSE_BRACE (ELSE OPEN_BRACE blockstatement CLOSE_BRACE)?;

expression: calcliteral | variableReference | expression operator expression;

calcliteral: PIXELSIZE | PERCENTAGE | SCALAR;

literal: | COLOR | TRUE | FALSE;

variableReference: LOWER_IDENT;

operator: PLUS | MIN | MUL;



