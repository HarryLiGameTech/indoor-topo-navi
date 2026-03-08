grammar MapFile;

// -----------------------------------------------------------------------------
// PARSER RULES
// -----------------------------------------------------------------------------

// Entry point
surfaceDef
    : 'root' ID '(' paramList? ')' surfaceBody                        # SurfaceDefRootExpr
    | 'topo-sketchMap' ID '(' paramList? ')' surfaceBody                    # SurfaceDefTopoMapExpr
    | 'transport' ID 'is' expr surfaceBody                            # SurfaceDefTransportExpr
    | 'building-includes' globalConfigBody                            # SurfaceDefGlobalConfigExpr
    ;

globalConfigBody
    : '{' NL* (globalConfigElement NL+)* globalConfigElement? NL* '}'
    ;

surfaceBody
    : '{' NL* (surfaceBodyElement NL+)* surfaceBodyElement? NL* '}'
    ;

globalConfigElement
    : 'vehicle' ID                                         # GlobalConfigElementVehicleRef
    | 'submap' ID ('using' ID)?                            # GlobalConfigElementSubmapRef
    ;

surfaceBodyElement
    : coreDef                                             # SurfaceElementCoreDef
    | 'topo-node' ID recordAssign?                         # SurfaceElementTopoNode
    | 'atomic-path' pathSpec recordAssign requirements?   # SurfaceElementAtomicPath
    | 'station' ID 'at' identifier ('at' expr)* recordAssign (requirements ('on' expr)?)?  # SurfaceElementStation
    | 'arrow' arrowSpec arrowHeading '>>' expr            # SurfaceElementArrow
    ;

// Core language
coreDef
    : 'type' ID '=' typeExpr                                    # TypeDef
    | 'def' ID ('(' paramList? ')')? (':' typeExpr)? '=' expr    # FuncDef
    | 'let' ID (':' typeExpr)? '=' expr                         # LetDef
    | expr                                                      # ScriptExpr
    ;

paramList
    : param (',' param)*
    ;

param
    : ID ':' typeExpr
    ;

// --- Types ---
// Right-associative arrow: Int -> Int -> Int means Int -> (Int -> Int)
typeExpr
    : typeAtom ('->' typeExpr)?
    ;

typeAtom
    : 'Int'
    | 'Float'
    | 'Bool'
    | 'String'
    | identifier
    | recordType
    | '(' typeExpr ')'
    ;

recordType
    : '{' (fieldDecl (',' fieldDecl)*)? '}'
    ;

fieldDecl
    : ID ':' typeExpr
    ;

// --- Expressions ---
// Precedence is determined by the order of alternatives (Highest to Lowest)
expr
    : atom                                                # AtomExpr
    | expr '.' ID                                         # ProjExpr

    // Application (Left-Associative)
    | expr atom                                           # AppMlExpr  // f x
    | expr '(' (expr (',' expr)*)? ')'                    # AppCExpr   // f(x, y)

    // Arithmetic & Logic
    | '-' expr                                            # NegExpr
    | expr op=('*' | '/') expr                            # MulDivExpr
    | expr op=('+' | '-') expr                            # AddSubExpr
    | expr op=('==' | '<' | '>' | '<=' | '>=') expr       # CompExpr

    // Control Flow (Lowest Precedence / Right Associative)
    | 'if' cond=expr 'then' ifExpr=expr 'else' elseExpr=expr                  # IfExpr
    | 'let' ID (':' letType=typeExpr)? '=' assignValue=expr 'in' expr         # LetExpr
    | 'let' 'rec' ID (':' typeExpr)? '=' expr 'in' expr   # LetRecExpr
    | 'fix' ID ':' typeExpr '.' expr                      # FixExpr
    | ('\\' | 'fn') ID ':' typeExpr ('=>' | '.') expr     # LamExpr
    ;

// Basic atomic units
atom
    : INT
    | FLOAT
    | STRING
    | 'true'
    | 'false'
    | identifier
    | '{' NL* (fieldAssign (',' NL* fieldAssign)*)? NL* '}'   // Record Literal
    | block                                       // Code Block
    | '(' expr ')'
    ;

fieldAssign
    : ID '=' expr
    ;

// --- Code Blocks ---
// Example: { let pi = 3.14; pi * r }
// Allows statements terminated by semicolon, ending with an optional expression.
block
    : '{' NL* (stmt ';' NL*)* (expr NL*)? '}'
    ;

stmt
    : 'let' ID (':' typeExpr)? '=' expr     # LetStmt  // 'let' without 'in'
    | expr                                  # ExprStmt // Side-effect
    ;

pathSpec
    : '[' ID (direction=('<->' | '->') ID) ']'
    ;

arrowSpec
    : '[' ID '->' ID ']'
    ;

arrowHeading
    : '^^'
    | '\\/' // '\/'
    ;

requirements
    : 'requires' ID
    | 'requires' '<' (ID ('&&' ID)*)? '>'
    ;

recordAssign
    : '{' NL* (fieldAssign (',' NL* fieldAssign)*)? NL* '}'
    ;

identifier
    : path+=ID ('::' path+=ID)*
    ;

// -----------------------------------------------------------------------------
// LEXER RULES
// -----------------------------------------------------------------------------

// Identifiers
ID
    : [a-zA-Z_] [a-zA-Z0-9_]*
    ;

// Literals
// Float must come before Int to ensure greedy matching of dots
FLOAT
    : [0-9]+ '.' [0-9]+
    ;

INT
    : [0-9]+
    ;

// Strings (Simple double-quoted)
STRING
    : '"' (~["\r\n] | '\\"')* '"'
    ;

NL
    : ('\r'? '\n')+
    ;

// Comments & Whitespace
WS
    : [ \t]+ -> skip
    ;

COMMENT
    : '//' ~[\r\n]* -> skip
    ;

BLOCK_COMMENT
    : '/*' .*? '*/' -> skip
    ;

