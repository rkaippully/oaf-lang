package org.oaflang.inference.term;

import org.oaflang.inference.TypeCheckException;
import org.oaflang.inference.TypeChecker;
import org.oaflang.inference.type.Function;
import org.oaflang.inference.type.Type;
import org.oaflang.inference.typecheck.Check;
import org.oaflang.inference.typecheck.Environment;
import org.oaflang.inference.typecheck.Expected;
import org.oaflang.inference.typecheck.Infer;
import org.oaflang.inference.util.Pair;

public class Lambda extends Term {

	private String varName;
	private Term body;

	public Lambda(String varName, Term body) {
		this.varName = varName;
		this.body = body;
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	@Override
	public String prettyPrint() {
		return "\\" + varName + " -> " + body.prettyPrint();
	}

	@Override
	public void typeCheckRho(Expected expected) throws TypeCheckException {
		if (expected instanceof Check) {
			Check check = (Check) expected;
			Pair<Type, Type> t = check.getType().unifyFunction();
			Type varType = t.fst();
			Type bodyType = t.snd();
			Environment env = Environment.getCurrent();
			env.extend(varName, varType);
			body.checkRho(bodyType);
			env.restore();
		} else {
			Infer infer = (Infer) expected;
			TypeChecker checker = TypeChecker.getCurrent();
			Type varType = checker.newTypeVariable();
			Environment env = Environment.getCurrent();
			env.extend(varName, varType);
			Type bodyType = body.inferRho();
			env.restore();
			infer.setType(new Function(varType, bodyType));
		}
	}
}
