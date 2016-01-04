package org.oaflang.inference.test;

import java.util.Map.Entry;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Assert;
import org.junit.Test;
import org.oaflang.inference.TypeCheckException;
import org.oaflang.inference.TypeChecker;
import org.oaflang.inference.parser.ExpressionLexer;
import org.oaflang.inference.parser.ExpressionParser;
import org.oaflang.inference.parser.ExpressionParser.ProgramContext;
import org.oaflang.inference.parser.TypeInferenceTreeListener;
import org.oaflang.inference.term.Term;
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

	public void setupEnvironment() {
		Environment env = Environment.getCurrent();
		env.clear();

		Type intToInt = new Function(Type.IntType, new Function(Type.IntType,
				Type.IntType));
		env.add("+", intToInt);
		env.add("-", intToInt);
		env.add("*", intToInt);
		env.add("/", intToInt);

		env.add("Pair",
			new ForAll(Lists.list(aTv, bTv),
				new Function(aTv, new Function(bTv,
					new TypeConstructor("Pair", Lists.list(aTv, bTv))))));
	}

	@Test
	public void testAll() throws Exception {
		ExpressionLexer lexer = new ExpressionLexer(new ANTLRInputStream(
				getClass().getResourceAsStream("/TestProgram.txt")));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ExpressionParser parser = new ExpressionParser(tokens);

		ProgramContext tree = parser.program();
		TypeInferenceTreeListener listener = new TypeInferenceTreeListener();
		ParseTreeWalker.DEFAULT.walk(listener, tree);
		
		TypeChecker tc = TypeChecker.getCurrent();
		for (Entry<Term, Object> e : listener.getTermMappings().entrySet()) {
			setupEnvironment();

			Term term = e.getKey();
			Object expected = e.getValue();
			Object actual;
			try {
				actual = tc.typeCheck(term);
			} catch (TypeCheckException ex) {
				actual = ex.getMessage();
			}
			Assert.assertEquals(expected, actual);
		}
	}
}
