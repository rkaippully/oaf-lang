package org.oaflang.inference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.oaflang.inference.ast.Node;
import org.oaflang.inference.type.Arrow;
import org.oaflang.inference.type.Tuple;
import org.oaflang.inference.type.Type;
import org.oaflang.inference.type.TypeConstructor;
import org.oaflang.inference.type.TypeVariable;

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

	public static Type analyze(Node node)  {
		Set<TypeVariable> nonGenerics = new HashSet<>();
		nextVariableName = StartingVariableName;
		return node.getType(StandardEnv, nonGenerics);
	}

	/**
	 * Robinson's unification algorithm
	 */
	public static void unify(Type a, Type b) {
		Type t1 = a.prune();
		Type t2 = b.prune();
		t1.unify(t2);
	}

	private static final char StartingVariableName = 'a';
	private static char nextVariableName = StartingVariableName;

	public static char getNextVariableName() {
		return nextVariableName++;
	}
}
