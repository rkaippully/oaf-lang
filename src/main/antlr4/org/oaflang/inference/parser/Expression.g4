grammar Expression;       

program :
    topLevelExpression+
    ;

topLevelExpression :
    expression '<>' (type | errorMessage)
    ;

expression :
    literal                                              # LiteralExpression
    | variable                                           # VariableExpression
    | 'let' IDENTIFIER '=' expression 'in' expression    # LetExpression
    | '\\' IDENTIFIER '->' expression                    # LambdaExpression
    | expression expression                              # FunctionApplication
    | expression '::' type                               # AnnotatedExpression
    | '\\' IDENTIFIER '::' type '->' expression          # AnnotatedLambda
    | '(' expression ')'                                 # ParenthesizedExpression
    ;

literal :
    INTEGER
    | BOOLEAN
    ;

variable :
    IDENTIFIER
    ;

type :
    typeVariable                         # TypeVariableType
    | 'forall' typeVariable+ '.' type    # ForAllType
    | type '->' type                     # FunctionType
    | typeConstant                       # TypeConstantType
    | typeConstructor                    # TypeConstructorType
    | '(' type ')'                       # ParenthesizedType
    ;

typeVariable :
    IDENTIFIER
    ;

typeConstant :
    'Int'
    | 'Bool'
    ;

typeConstructor :
    IDENTIFIER type*
    ;

errorMessage :
    STRING
    ;

STRING :
    '"' .*? '"'
    ;

INTEGER : [0-9]+ ;

BOOLEAN : 'True' | 'False' ;

IDENTIFIER : [a-zA-Z+\-*/] [a-zA-Z0-9+\-*/]* ;

WS : [ \t\r\n]+ -> skip ; // toss out whitespace