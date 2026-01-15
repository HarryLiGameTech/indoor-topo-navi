grammar MapFile;

// -----------------------------------------------------------------------------
// PARSER RULES
// -----------------------------------------------------------------------------

// Entry point: A program is a list of top-level definitions or expressions
program
    : topLevelDef* EOF
    ;

topLevelDef
    : 'type' ID '=' typeExpr                                    # TypeDef
    | 'def' ID '(' paramList? ')' (':' typeExpr)? '=' expr      # FuncDef
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
    | ID
    | '{' (fieldDecl (',' fieldDecl)*)? '}'   // Record Type
    | '(' typeExpr ')'
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
    | expr ('*' | '/') expr                               # MulDivExpr
    | expr ('+' | '-') expr                               # AddSubExpr
    | expr ('==' | '<' | '>' | '<=' | '>=') expr          # CompExpr

    // Control Flow (Lowest Precedence / Right Associative)
    | 'if' expr 'then' expr 'else' expr                   # IfExpr
    | 'let' ID (':' typeExpr)? '=' expr 'in' expr         # LetExpr
    | 'let' 'rec' ID (':' typeExpr)? '=' expr 'in' expr   # LetRecExpr
    | 'fix' ID ':' typeExpr '.' expr                      # FixExpr
    | ('\\' | 'fn') ID ':' typeExpr ('=>' | '.') expr     # LamExpr
    ;

// Basic atomic units
atom
    : INT
    | FLOAT
    | 'true'
    | 'false'
    | ID
    | '{' (fieldAssign (',' fieldAssign)*)? '}'   // Record Literal
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
    : '{' (stmt ';')* expr? '}'
    ;

stmt
    : 'let' ID (':' typeExpr)? '=' expr     # LetStmt  // 'let' without 'in'
    | expr                                  # ExprStmt // Side-effect
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

// Comments & Whitespace
WS
    : [ \t\r\n]+ -> skip
    ;

COMMENT
    : '//' ~[\r\n]* -> skip
    ;

BLOCK_COMMENT
    : '/*' .*? '*/' -> skip
    ;