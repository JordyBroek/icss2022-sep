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

// Conditionals
// EQUAL_OPERATOR: '==';
// GREATER_OPERATOR: '>';
// LESSER_OPERATOR: '<';

//--- PARSER: ---
stylesheet: (variableAssignment | stylerule)* EOF;
stylerule: selector+ OPEN_BRACE statement CLOSE_BRACE;

selector: (classSelector | idSelector | tagSelector) | selector attribute;
classSelector: CLASS_IDENT;
idSelector: ID_IDENT;
tagSelector: LOWER_IDENT | CAPITAL_IDENT;

// Body
statement: (declaration | variableAssignment | conditional | elseClause)*;
attribute: BOX_BRACKET_OPEN (variableReference | boolLiteral) BOX_BRACKET_CLOSE;
declaration: LOWER_IDENT COLON expression+ SEMICOLON;
variableAssignment: (LOWER_IDENT | CAPITAL_IDENT) ASSIGNMENT_OPERATOR expression SEMICOLON;

conditional: IF attribute OPEN_BRACE statement CLOSE_BRACE elseClause?;
elseClause: ELSE OPEN_BRACE statement CLOSE_BRACE;
//conditionExpression: boolLiteral | comparisonExpression | '(' conditionExpression ')';
//comparisonExpression: expression (EQUAL_OPERATOR | GREATER_OPERATOR | LESSER_OPERATOR) expression;

expression: term ((PLUS | MIN) term)*;
term: factor ((MUL) factor)*;
factor: pixelLiteral | scalarLiteral | percentageLiteral | colorLiteral | variableReference | boolLiteral;

boolLiteral: TRUE | FALSE;
pixelLiteral: PIXELSIZE;
colorLiteral: COLOR;
percentageLiteral: PERCENTAGE;
scalarLiteral: SCALAR;
variableReference: LOWER_IDENT | CAPITAL_IDENT;



