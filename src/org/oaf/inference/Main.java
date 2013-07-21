package org.oaf.inference;

import org.oaf.inference.ast.Apply;
import org.oaf.inference.ast.Identifier;
import org.oaf.inference.ast.If;
import org.oaf.inference.ast.Lambda;
import org.oaf.inference.ast.Let;
import org.oaf.inference.ast.LetRec;
import org.oaf.inference.ast.Literal;
import org.oaf.inference.ast.Node;

public class Main {

	public static void main(String[] args) throws Exception {
		Node[] tests = new Node[] {
		//
		//	let rec
		//		factorial =
		//			fn[n] =>
		//				if (== n 0) then 1 else (* n (factorial (- n 1))) fi
		//	in
		//		(factorial 5)
		//
		new LetRec(
				"factorial",
				new Lambda("n",
						new If(
								new Apply(
										new Apply(new Identifier("=="), new Identifier("n")),
										new Literal(0)),
								new Literal(1),
								new Apply(
										new Apply(new Identifier("*"), new Identifier("n")),
										new Apply(new Identifier("factorial"),
												new Apply(
														new Apply(new Identifier("-"), new Identifier("n")),
														new Literal(1)
												)
										)
								)
						)
				),
				new Apply(new Identifier("factorial"), new Literal(5))
		),

		// fn x => (pair(x(3) (x(true)))
        new Lambda("x",
                new Apply(
                    new Apply(
                    		new Identifier("pair"),
                    		new Apply(new Identifier("x"), new Literal(3))
                    ),
                    new Apply(new Identifier("x"), new Literal(true))
                )
        ),

        // pair(f(3), f(true))
        new Apply(
            new Apply(
            		new Identifier("pair"),
            		new Apply(
            				new Identifier("f"),
            				new Literal(4)
            		)
            ),
            new Apply(
            		new Identifier("f"),
            		new Literal(true)
            )
        ),

        // let f = (fn x => x) in ((pair (f 4)) (f true))
        new Let("f", new Lambda("x", new Identifier("x")), new Apply(new Apply(new Identifier("pair"), new Apply(new Identifier("f"), new Literal(4))), new Apply(new Identifier("f"), new Literal(true)))),

        // fn f => f f
        new Lambda("f", new Apply(new Identifier("f"), new Identifier("f"))),

        // let g = fn f => 5 in g g
        new Let("g",
            new Lambda("f", new Literal(5)),
            new Apply(new Identifier("g"), new Identifier("g"))),

        // example that demonstrates generic and non-generic variables:
        // fn g => let f = fn x => g in pair (f 3, f true)
        new Lambda("g",
               new Let("f",
                   new Lambda("x", new Identifier("g")),
                   new Apply(
                        new Apply(new Identifier("pair"),
                              new Apply(new Identifier("f"), new Literal(3))
                        ),
                        new Apply(new Identifier("f"), new Literal(true))))),

        // Function composition
        // fn f (fn g (fn arg (f g arg)))
        new Lambda("f", new Lambda("g", new Lambda("arg", new Apply(new Identifier("g"), new Apply(new Identifier("f"), new Identifier("arg")))))),

		// map function
        //
		// let rec map =
		// 		fn [f] =>
		// 			fn [lst] =>
		// 				if (empty? lst)
		// 					then []
		// 					else (cons (f (first lst)) (map f (rest lst)))
		// in map
		new LetRec("map",
				new Lambda("f",
						new Lambda("lst",
								new If(new Apply(new Identifier("empty?"), new Identifier("lst")),
										new Identifier("[]"),
										new Apply(
												new Apply(new Identifier("cons"), new Apply(new Identifier("f"), new Apply(new Identifier("first"), new Identifier("lst")))),
												new Apply(
														new Apply(new Identifier("map"), new Identifier("f")),
														new Apply(new Identifier("rest"), new Identifier("lst"))))
										))),
				new Identifier("map")),

		// reduce function
		//
		// let rec reduce =
		// 		fn [f] =>
		//			fn [initVal] =>
		//				fn [lst] =>
		//					if (empty? lst) then initval
		//						else (reduce f (f initVal (first lst)) (rest lst))
		// in reduce
		new LetRec("reduce",
				new Lambda("f",
						new Lambda("initVal",
								new Lambda("lst", 
										new If(new Apply(new Identifier("empty?"), new Identifier("lst")),
												new Identifier("initVal"),
												new Apply(
													new Apply(
															new Apply(new Identifier("reduce"), new Identifier("f")),
															new Apply(
																	new Apply(new Identifier("f"), new Identifier("initVal")),
																	new Apply(new Identifier("first"), new Identifier("lst"))
															)
													),
													new Apply(new Identifier("rest"), new Identifier("lst"))
												)
										)
								)
						)
				),
				new Identifier("reduce")),

		// iterate function from clojure - (iterate f x) returns an infinite sequence of x, (f x), (f (f x)) etc
		// (let rec iterate =
		// 		fn [f] =>
		// 			fn [x] =>
		// 				(cons x (iterate f (f x)))
		// 	in iterate)
		new LetRec("iterate",
				new Lambda("f",
						new Lambda("x",
								new Apply(
										new Apply(new Identifier("cons"), new Identifier("x")
									),
									new Apply(
											new Apply(new Identifier("iterate"), new Identifier("f")),
											new Apply(new Identifier("f"), new Identifier("x"))
									)
								)
						)
				),
				new Identifier("iterate")),

		// zipmap - returns a list of pairs (assumes lists are of same length)
		// (let rec zipmap =
		// 		fn [a] =>
		// 			fn [b] =>
		//				(if (empty? a)
		// 					then []
		// 					else (cons (pair (first a) (first b)) (zipmap (rest a) (rest b))))
		// 	in zipmap)
		new LetRec("zipmap",
				new Lambda("a",
						new Lambda("b",
								new If(
										new Apply(new Identifier("empty?"), new Identifier("a")),
										new Identifier("[]"),
										new Apply(
												new Apply(
														new Identifier("cons"),
														new Apply(
																new Apply(new Identifier("pair"), new Apply(new Identifier("first"), new Identifier("a"))),
																new Apply(new Identifier("first"), new Identifier("b"))
														)
												),
												new Apply(
														new Apply(
																new Identifier("zipmap"),
																new Apply(new Identifier("rest"), new Identifier("a"))
														),
														new Apply(new Identifier("rest"), new Identifier("b"))
												)
										)
								)
						)
				),
				new Identifier("zipmap")),

		};

		for (Node node : tests)
			Inference.analyze(node);
	}
}
