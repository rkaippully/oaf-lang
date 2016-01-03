package org.oaflang.inference;

import org.oaflang.inference.term.Term;
import org.oaflang.inference.type.MetaTypeVariable;
import org.oaflang.inference.type.SkolemTypeVariable;
import org.oaflang.inference.type.Type;
import org.oaflang.inference.type.TypeVariable;
import org.oaflang.inference.typecheck.Environment;

public class TypeChecker {

	private static final ThreadLocal<TypeChecker> typeCheckers = new ThreadLocal<TypeChecker>() {
		@Override
		protected TypeChecker initialValue() {
			return new TypeChecker();
		};
	};

	private TypeChecker() {
	}

	public static TypeChecker getCurrent() {
		return typeCheckers.get();
	}

	public static void clear() {
		typeCheckers.remove();
	}

	public Type typeCheck(Term expr) throws TypeCheckException {
		Type type = expr.inferSigma();
		return type.zonk();
	}

	public MetaTypeVariable newTypeVariable() {
		Environment env = Environment.getCurrent();
		return new MetaTypeVariable(env.getNextTypeVariableNumber(), null);
	}

	public SkolemTypeVariable newSkolemTypeVariable(TypeVariable var) {
		Environment env = Environment.getCurrent();
		return new SkolemTypeVariable(var.getName(), env.getNextTypeVariableNumber());
	}
}
