package org.oaflang.inference.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.oaflang.inference.TypeCheckException;
import org.oaflang.inference.TypeChecker;
import org.oaflang.inference.term.AnnLambda;
import org.oaflang.inference.term.App;
import org.oaflang.inference.term.Lambda;
import org.oaflang.inference.term.Let;
import org.oaflang.inference.term.Literal;
import org.oaflang.inference.term.Term;
import org.oaflang.inference.term.Variable;
import org.oaflang.inference.type.BoundedTypeVariable;
import org.oaflang.inference.type.ForAll;
import org.oaflang.inference.type.Function;
import org.oaflang.inference.type.Type;
import org.oaflang.inference.type.TypeConstructor;
import org.oaflang.inference.type.TypeVariable;
import org.oaflang.inference.typecheck.Environment;
import org.oaflang.inference.util.Lists;

/**
 * Tests for type inference. Each test creates an AST and passes it to the type checker.
 * We get a type back and verify it is the expected type.
 */
public class TestInference {

	private final TypeVariable aTv = new BoundedTypeVariable("a");
	private final TypeVariable bTv = new BoundedTypeVariable("b");

	@Before
	public void setup() {
		Environment env = Environment.getCurrent();
		env.clear();

		env.add("if",
			new ForAll(Lists.list(aTv), 
				new Function(Type.BoolType, new Function(aTv,
						new Function(aTv, aTv)))));
		env.add("isZero", new Function(Type.IntType, Type.BoolType));
		env.add("*", new Function(Type.IntType, new Function(Type.IntType,
				Type.IntType)));
		env.add("pred", new Function(Type.IntType, Type.IntType));

		env.add("Pair",
			new ForAll(Lists.list(aTv, bTv),
				new Function(aTv, new Function(bTv,
					new TypeConstructor("Pair", Lists.list(aTv, bTv))))));
	}

	@Test
	public void testFactorial() throws TypeCheckException {
		// (let factorial = (fn n => (((if (isZero n)) 1) ((* n) (factorial (pred n))))) in (factorial 5)) : Int
		Term factorial = new Let("factorial",
			new Lambda("n",
				new App(
					new App(
						new App(new Variable("if"), new App(new Variable("isZero"), new Variable("n"))),
						new Literal(1)),
					new App(
						new App(
							new Variable("*"),
							new Variable("n")),
						new App(new Variable("pred"), new Variable("n"))))),
			new App(new Variable("factorial"), new Literal(5)));

		TypeChecker tc = TypeChecker.getCurrent();
		Type type = tc.typeCheck(factorial);
		Assert.assertEquals("Int", type.prettyPrint());
	}

	@Test
	public void testPairTest1() {
		// 	(fn x => ((pair (x 3)) (x true))) : Cannot unify types: Int != Bool
		Term pairTest1 = new Lambda("x",
			new App(
				new App(
					new Variable("Pair"),
					new App(new Variable("x"), new Literal(3))),
				new App(
					new Variable("x"), new Literal(true))));

        Exception e = null;
        try {
    		TypeChecker tc = TypeChecker.getCurrent();
    		tc.typeCheck(pairTest1);
        } catch (TypeCheckException ex) {
        	e = ex;
		}
        Assert.assertEquals("Cannot unify types: Int != Bool", e.getMessage());
	}

	@Test
	public void testPairTest2() throws TypeCheckException {
		// 	(fn (x :: forall a. a -> a) => ((pair (x 3)) (x true))) : (forall a. a -> a) -> Pair Int Bool
		Term pairTest2 = new AnnLambda("x",
			new ForAll(Lists.list(aTv), new Function(aTv, aTv)),
			new App(
				new App(
					new Variable("Pair"),
					new App(new Variable("x"), new Literal(3))),
				new App(
					new Variable("x"), new Literal(true))));

		TypeChecker tc = TypeChecker.getCurrent();
		Type type = tc.typeCheck(pairTest2);
		Assert.assertEquals("(forall a. a -> a) -> Pair Int Bool", type.prettyPrint());
	}

	@Test
	public void testPairTest3() throws TypeCheckException {
		// (let f = (fn x => x) in ((pair (f 4)) (f true))) : Pair Int Bool
		Term pairTest3 = new Let("f",
			new Lambda("x", new Variable("x")),
			new App(
				new App(new Variable("Pair"),
					new App(new Variable("f"), new Literal(4))),
				new App(new Variable("f"), new Literal(true))));

		TypeChecker tc = TypeChecker.getCurrent();
		Type type = tc.typeCheck(pairTest3);
		Assert.assertEquals("Pair Int Bool", type.prettyPrint());
	}

	@Test
	public void testPairTest4() {
		// ((pair (f 4)) (f true)) : Undefined symbol: f
		Term undefinedSymbol = new App(
			new App(new Variable("Pair"),
				new App(new Variable("f"), new Literal(4))),
			new App(new Variable("f"), new Literal(true)));

        Exception e = null;
        try {
    		TypeChecker tc = TypeChecker.getCurrent();
    		tc.typeCheck(undefinedSymbol);
        } catch (TypeCheckException ex) {
        	e = ex;
		}
        Assert.assertEquals("Undefined symbol: f", e.getMessage());
	}

	@Test
	public void testRecur() {
		// (fn f => (f f)) : Occurs check failed for: $1 in: $1 -> $2
		Term recur = new Lambda("f", new App(new Variable("f"), new Variable("f")));

        Exception e = null;
        try {
    		TypeChecker tc = TypeChecker.getCurrent();
    		tc.typeCheck(recur);
        } catch (TypeCheckException ex) {
        	e = ex;
		}
        Assert.assertEquals("Occurs check failed for: $1 in: $1 -> $2", e.getMessage());
	}

	@Test
	public void testConstFunction() throws TypeCheckException {
		// (let g = (fn f => 5) in (g g)) : Int
		Term constFunction = new Let("g",
			new Lambda("f", new Literal(5)),
			new App(new Variable("g"), new Variable("g")));

		TypeChecker tc = TypeChecker.getCurrent();
		Type type = tc.typeCheck(constFunction);
		Assert.assertEquals("Int", type.prettyPrint());
	}

	@Test
	public void testScoped() throws TypeCheckException {
		// (fn g => (let f = (fn x => g) in ((pair (f 3)) (f true)))) : (forall a. a -> Pair a a)
		Term scoped = new Lambda("g",
			new Let("f",
				new Lambda("x", new Variable("g")),
				new App(
					new App(new Variable("Pair"),
						new App(new Variable("f"), new Literal(3))),
					new App(new Variable("f"), new Literal(true)))));

		TypeChecker tc = TypeChecker.getCurrent();
		Type type = tc.typeCheck(scoped);
		Assert.assertEquals("(forall a. a -> Pair a a)", type.prettyPrint());
	}

	@Test
	public void testComposition() throws TypeCheckException {
		// (fn f => (fn g => (fn arg => (g (f arg))))) : (forall a b c. (b -> c) -> (c -> a) -> b -> a)
		Term composition = new Lambda("f",
			new Lambda("g",
				new Lambda("arg",
					new App(
						new Variable("g"),
						new App(
							new Variable("f"),
							new Variable("arg"))))));

		TypeChecker tc = TypeChecker.getCurrent();
		Type type = tc.typeCheck(composition);
		Assert.assertEquals("(forall a b c. (b -> c) -> (c -> a) -> b -> a)", type.prettyPrint());
	}

	@Test
	public void testPredicative() throws TypeCheckException {
		// (let poly = (fn f => ((pair (f 3)) (f True))) (poly (\x => x))) : Pair Int Bool
		Term predicative = new Let("poly",
			new AnnLambda("f",
				new ForAll(Lists.list(aTv), new Function(aTv, aTv)),
				new App(
					new App(new Variable("Pair"),
						new App(new Variable("f"), new Literal(3))),
					new App(new Variable("f"), new Literal(true)))),
			new App(new Variable("poly"), new Lambda("x", new Variable("x"))));

		TypeChecker tc = TypeChecker.getCurrent();
		Type type = tc.typeCheck(predicative);
		Assert.assertEquals("Pair Int Bool", type.prettyPrint());
	}

	@Test
	public void testImpredicative() {
		// (let revapp = (fn x => (fn f => f x)) (let poly = (fn f => ((pair (f 3)) (f True)))) ((revapp (\x => x) poly)) : Type mismatch: a12 -> a12 is not as polymorphic as (forall a. a -> a)
		Term impredicative = new Let("revapp",
			new Lambda("x",
				new Lambda("f",
					new App(new Variable("f"), new Variable("x")))),
			new Let("poly",
				new AnnLambda("f",
					new ForAll(Lists.list(aTv), new Function(aTv, aTv)),
					new App(
						new App(new Variable("Pair"),
							new App(new Variable("f"), new Literal(3))),
						new App(new Variable("f"), new Literal(true)))),
				new App(
					new App(new Variable("revapp"),
						new Lambda("x", new Variable("x"))),
					new Variable("poly"))));

        Exception e = null;
        try {
    		TypeChecker tc = TypeChecker.getCurrent();
    		tc.typeCheck(impredicative);
        } catch (TypeCheckException ex) {
        	e = ex;
		}
        Assert.assertEquals("Type mismatch: a12 -> a12 is not as polymorphic as (forall a. a -> a)", e.getMessage());
	}
}
