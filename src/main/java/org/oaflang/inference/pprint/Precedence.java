package org.oaflang.inference.pprint;

public enum Precedence {
	TopPrecedence,
	ArrowPrecedence,     // a -> b
	TypeConsPrecedence,  // T a b
	AtomicPrecedence     // t
}
