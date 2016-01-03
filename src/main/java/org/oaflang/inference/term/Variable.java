package org.oaflang.inference.term;

import org.oaflang.inference.TypeCheckException;
import org.oaflang.inference.type.Type;
import org.oaflang.inference.typecheck.Environment;
import org.oaflang.inference.typecheck.Expected;

public class Variable extends Term {

	private String name;

	public Variable(String name) {
		this.name = name;
	}

	@Override
	public boolean isAtomic() {
		return true;
	}

	@Override
	public String prettyPrint() {
		return name;
	}

	@Override
	public void typeCheckRho(Expected expected) throws TypeCheckException {
		Environment env = Environment.getCurrent();
		Type t = env.lookup(name);
		if (t != null)
			t.instantiateSigma(expected);
		else
			throw new TypeCheckException("Undefined symbol: " + prettyPrint());
	}
}
