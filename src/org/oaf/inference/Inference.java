package org.oaf.inference;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.oaf.inference.ast.Node;
import org.oaf.inference.type.Arrow;
import org.oaf.inference.type.Tuple;
import org.oaf.inference.type.Type;
import org.oaf.inference.type.TypeConstructor;
import org.oaf.inference.type.TypeVariable;

public class Inference {

	public static final Type BooleanType = new TypeConstructor("Boolean");
	public static final Type IntegerType = new TypeConstructor("Integer");

	private static final Map<String, Type> StandardEnv = new HashMap<>();
	static {
		Type var1 = new TypeVariable();
		Type var2 = new TypeVariable();

		StandardEnv.put("==", new Arrow(var1, new Arrow(var1, BooleanType)));

		StandardEnv.put("*", new Arrow(IntegerType, new Arrow(IntegerType, IntegerType)));
		StandardEnv.put("-", new Arrow(IntegerType, new Arrow(IntegerType, IntegerType)));

		StandardEnv.put("pair", new Arrow(var1, new Arrow(var2, new Tuple(var1, var2))));

		// List
		Type listType = new TypeConstructor("List", var1);
		StandardEnv.put("[]", listType);
		StandardEnv.put("first", new Arrow(listType, var1));
		StandardEnv.put("rest", new Arrow(listType, listType));
		StandardEnv.put("empty?", new Arrow(listType, BooleanType));
		StandardEnv.put("cons", new Arrow(var1, new Arrow(listType, listType)));
	}

	public static void analyze(Node node) throws Exception {
		Set<TypeVariable> nonGenerics = new HashSet<>();
		PrintStream out = new PrintStream(System.out, true, "UTF-8");
		try {
			nextVariableName = StartingVariableName;
			Type t = node.getType(StandardEnv, nonGenerics);
			out.println(node + " : " + t.toString(true));
		} catch (IllegalArgumentException e) {
			out.println(node + " : " + e.getMessage());
		}
	}

	/**
	 * Robinson's unification algorithm
	 */
	public static void unify(Type a, Type b) {
		Type t1 = a.prune();
		Type t2 = b.prune();
		t1.unify(t2);
	}

	// Greek letter alpha
	private static final char StartingVariableName = '\u03b1';
	private static char nextVariableName = StartingVariableName;

	public static char getNextVariableName() {
		return nextVariableName++;
	}
}
