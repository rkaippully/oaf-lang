Higher Rank Type Inference Implementation in Java
=================================================

Modern functional programming languages such as ML and Haskell boast of extremely
powerful type systems. Part of the attractiveness of these type systems arise from the
fact that the programmers do not have to provide type annotations. The compiler is smart
enough to infer the types of (almost) all expressions in the program. Furthermore, the
compiler will infer the most general type of the expression. This is a huge win for
programmers because they can write code without any type annotations and still have the
compiler check the types and detect potential problems.

In practise, most Haskell programmers provide type annotations with the code. This serves
two purposes - firstly as program documentation and secondly to have the compiler verify
the programmer provided type against the one it inferred.

Damas-Hindley-Milner Type Inference
-----------------------------------
The standard [Damas-Hindley-Milner type system](https://en.wikipedia.org/wiki/Hindley%E2%80%93Milner_type_system)
specifies **Algorithm W** that can infer types of expressions for lambda calculus with
parameteric polymorphism. This algorithm is straightforward to implement and runs in linear
time making it attractive for practical implementations. A detailed implementation is specified
in [Basic Polymorphic Typechecking by Luca Cardelli](http://lucacardelli.name/indexPapers.html).

The *basic-hm* branch in this repo provides an implementation of the above algorithm.

Higher Rank Polymorphism
------------------------
It is sometimes useful to write functions with higher rank types - that is functions that
take polymorphic functions as their arguments. Unaided type inference is undecidable for
such type systems, but it is possible to infer types with little assistance from the programmer
via some type annotations. An extension of *Algorithm W* to support higher ranked polymorphism
is presented in the paper [Practical type inference for arbitrary-rank types](http://research.microsoft.com/en-us/um/people/simonpj/papers/higher-rank/putting.pdf).

The *master* branch in this repo provides and implementation of the higher rank type inference algorithm.  