grammar MapFile;

// Top-level structure
program : topoMap* EOF;

topoMap : 'TopoMap' name=Identifier '{' topoMapContent* '}';

topoMapContent
    :   topoNodeDeclaration             # nodeContent
    |   relationshipDeclaration         # relationshipContent
    |   pathDeclaration                 # pathContent
    |   directionDeclaration            # directionContent
    |   modifierDeclaration             # modifierContent
    |   LineComment                     # commentContent
    ;

// Declaration rules
topoNodeDeclaration : 'TopoNode' '(' name=Identifier ')' (LineComment)?;

relationshipDeclaration : relationType=RCC '(' subject=Identifier ',' object=Identifier ')' (LineComment)?;

pathDeclaration : 'AtomicPath' '(' from=Identifier ',' to=Identifier ','
    cost=expr ',' modifier=Identifier ')';

directionDeclaration : 'Direction' '(' from=Identifier ',' via=Identifier ','
    to=Identifier ',' direction=TPCC ')';

modifierDeclaration : 'modifier' name=Identifier '{' modifierText* '}';

// TODO
modifierText : 'TODO';

expr
    :   primitive                                                       # exprPrimitive
    |   ident=Identifier                                                # exprIdentifier
    |   '(' expr ')'                                                    # exprParan
    |   fn=Identifier '(' (args+=expr (',' args+=expr)*)? ')'           # exprFnCall
    |   lhs=expr ('**') rhs=expr                                        # exprPow
    |   lhs=expr op=('*'|'/'|'%') rhs=expr                              # exprMul
    |   lhs=expr op=('+'|'-') rhs=expr                                  # exprAdd
    |   'if' cond=expr 'then' thenExpr=expr 'else' elseExpr=expr        # exprIf
    ;

primitive
    :   Int                         # primitiveInt
    |   ('true' | 'false')          # primitiveBool
    |   Float                       # primitiveFloat
    |   RegularStringLiteral        # primitiveString
    ;


RCC: 'DC' | 'EC' | 'EQ' | 'TPP' | 'NTPP';

TPCC: 'FRONT' | 'FRONT_RIGHT' | 'RIGHT' | 'REAR_RIGHT' | 'REAR' | 'REAR_LEFT' | 'LEFT' | 'FRONT_LEFT';

Int: '-'? (Dec | Hex | Oct | Bin);
Dec: [0-9]+;
Hex: '0x' HexDigit+;
Oct: '0o' [0-7]+;
Bin: '0b' [01]+;
Float: '-'?[0-9]+[.][0-9]+;
RegularStringLiteral: '"'  (~["\\\r\n] | CommonCharacter)* '"';


Identifier: [a-zA-Z][a-zA-Z_]*;

LineComment
    : '//' (~[\r\n])* -> skip
    ;

fragment HexDigit: [0-9] | [A-F] | [a-f];
fragment ExponentPart: [eE] ('+' | '-')? [0-9] ('_'* [0-9])*;

fragment CommonCharacter
	: '\\\''
	| '\\"'
	| '\\\\'
	| '\\0'
	| '\\a'
	| '\\b'
	| '\\f'
	| '\\n'
	| '\\r'
	| '\\t'
	| '\\v'
	;

fragment NewLineAsciiChar
	: '\r\n' | '\r' | '\n'
	;

// Whitespaces
Whitespace: [ \t\r]+ -> channel(HIDDEN);