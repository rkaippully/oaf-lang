package org.oaflang.inference.test;

import org.junit.Assert;
import org.junit.Test;
import org.oaflang.inference.Inference;
import org.oaflang.inference.ast.Apply;
import org.oaflang.inference.ast.Identifier;
import org.oaflang.inference.ast.If;
import org.oaflang.inference.ast.Lambda;
import org.oaflang.inference.ast.Let;
import org.oaflang.inference.ast.LetRec;
import org.oaflang.inference.ast.Literal;
import org.oaflang.inference.ast.Node;
import org.oaflang.inference.type.Type;

/**
 * Tests for HM type inference. Each test creates an AST and passes it to the "inferencer"
 * We get a type back and verify it is the expected type.
 */
public class TestInference {

	@Test
	public void testFactorial() {
		/*
			let rec
				factorial =
					fn[n] =>
						if (== n 0) then 1 else (* n (factorial (- n 1))) fi
			in
				(factorial 5)
		*/
		Node root = new LetRec(
				"factorial",
				new Lambda("n",
						new If(
								new Apply(
										new Apply(new Identifier("=="), new Identifier("n")),
										new Literal(0)
								),
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
		);
		Type type = Inference.analyze(root);
		Assert.assertEquals(Inference.IntegerType, type);
	}

	@Test
	public void testTypeMismatch() {
		// fn x => (pair(x(3) (x(true)))
        Node root = new Lambda(
        		"x",
                new Apply(
                		new Apply(
                				new Identifier("pair"),
                				new Apply(new Identifier("x"), new Literal(3))
                		),
                		new Apply(new Identifier("x"), new Literal(true))
                )
        );

        Exception e = null;
        try {
        	Inference.analyze(root);
        } catch (IllegalArgumentException ex) {
        	e = ex;
        }
        Assert.assertTrue(e instanceof IllegalArgumentException);
        Assert.assertEquals("Type mismatch: Boolean != Integer", e.getMessage());
	}

	@Test
	public void testUndefinedSymbol() {
        // pair(f(3), f(true))
        Node root = new Apply(
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
        );

        Exception e = null;
        try {
        	Inference.analyze(root);
        } catch (IllegalArgumentException ex) {
        	e = ex;
        }
        Assert.assertTrue(e instanceof IllegalArgumentException);
        Assert.assertEquals("Undefined identifier f", e.getMessage());
	}

	@Test
	public void testPairs() {
        // let f = (fn x => x) in ((pair (f 4)) (f true))
        Node root = new Let(
        		"f",
        		new Lambda("x", new Identifier("x")),
        		new Apply(
        				new Apply(
        						new Identifier("pair"),
        						new Apply(new Identifier("f"), new Literal(4))
        				),
        				new Apply(new Identifier("f"), new Literal(true))
        		)
        );

        Type type = Inference.analyze(root);
        Assert.assertEquals("(Integer * Boolean)", type.toString(true));
	}

	@Test
	public void testRecursiveUnification() {
        // fn f => f f
        Node root = new Lambda("f", new Apply(new Identifier("f"), new Identifier("f")));

        Exception e = null;
        try {
        	Inference.analyze(root);
        } catch (IllegalArgumentException ex) {
        	e = ex;
        }
        Assert.assertTrue(e instanceof IllegalArgumentException);
        Assert.assertEquals("Recursive unification of a and (a -> b)", e.getMessage());
	}

	@Test
	public void testApplication() {
		// let g = fn f => 5 in g g
        Node root = new Let("g",
        		new Lambda("f", new Literal(5)),
                new Apply(new Identifier("g"), new Identifier("g")));

        Type type = Inference.analyze(root);
        Assert.assertEquals("Integer", type.toString(true));
	}

	@Test
	public void testGenerics() {
        // example that demonstrates generic and non-generic variables:
        // fn g => let f = fn x => g in pair (f 3, f true)
        Node root = new Lambda("g",
                new Let("f",
                		new Lambda("x", new Identifier("g")),
                		new Apply(
                				new Apply(
                						new Identifier("pair"),
                						new Apply(new Identifier("f"), new Literal(3))
                				),
                				new Apply(new Identifier("f"), new Literal(true))
                		)
                )
        );

        Type type = Inference.analyze(root);
        Assert.assertEquals("(a -> (a * a))", type.toString(true));
	}

	@Test
	public void testFunctionComposition() {
		// fn f (fn g (fn arg (f g arg)))
        Node root = new Lambda("f",
        		new Lambda("g",
        				new Lambda("arg",
        						new Apply(
        								new Identifier("g"),
        								new Apply(new Identifier("f"), new Identifier("arg"))
        						)
        				)
        		)
        );

        Type type = Inference.analyze(root);
        Assert.assertEquals("((a -> b) -> ((b -> c) -> (a -> c)))", type.toString(true));
	}

	@Test
	public void testMap() {
		// map function
        //
		// let rec map =
		// 		fn [f] =>
		// 			fn [lst] =>
		// 				if (empty? lst)
		// 					then []
		// 					else (cons (f (first lst)) (map f (rest lst)))
		// in map
        Node root =
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
        				new Identifier("map"));

        Type type = Inference.analyze(root);
        Assert.assertEquals("((a -> b) -> (List[a] -> List[b]))", type.toString(true));
	}

	@Test
	public void testReduce() {
		// reduce function
		//
		// let rec reduce =
		// 		fn [f] =>
		//			fn [initVal] =>
		//				fn [lst] =>
		//					if (empty? lst) then initval
		//						else (reduce f (f initVal (first lst)) (rest lst))
		// in reduce
		Node root = new LetRec("reduce",
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
				new Identifier("reduce"));

        Type type = Inference.analyze(root);
        Assert.assertEquals("((a -> (b -> a)) -> (a -> (List[b] -> a)))", type.toString(true));
	}

	@Test
	public void testIterate() {
		// iterate function from clojure - (iterate f x) returns an infinite sequence of x, (f x), (f (f x)) etc
		// (let rec iterate =
		// 		fn [f] =>
		// 			fn [x] =>
		// 				(cons x (iterate f (f x)))
		// 	in iterate)
		Node root = new LetRec("iterate",
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
				new Identifier("iterate"));

        Type type = Inference.analyze(root);
        Assert.assertEquals("((a -> a) -> (a -> List[a]))", type.toString(true));
	}

	@Test
	public void testZipMap() {
		// zipmap - returns a list of pairs (assumes lists are of same length)
		// (let rec zipmap =
		// 		fn [a] =>
		// 			fn [b] =>
		//				(if (empty? a)
		// 					then []
		// 					else (cons (pair (first a) (first b)) (zipmap (rest a) (rest b))))
		// 	in zipmap)
		Node root = new LetRec("zipmap",
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
				new Identifier("zipmap"));

        Type type = Inference.analyze(root);
        Assert.assertEquals("(List[a] -> (List[b] -> List[(a * b)]))", type.toString(true));
	}
}
