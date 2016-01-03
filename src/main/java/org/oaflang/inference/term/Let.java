package org.oaflang.inference.term;

import org.oaflang.inference.TypeCheckException;
import org.oaflang.inference.type.Type;
import org.oaflang.inference.typecheck.Environment;
import org.oaflang.inference.typecheck.Expected;

public class Let extends Term {

	private String name;
	private Term rhs;
	private Term body;

	/**
	 * let name = rhs in body
	 */
	public Let(String name, Term rhs, Term body) {
		this.name = name;
		this.rhs = rhs;
		this.body = body;
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	@Override
	public String prettyPrint() {
		return "let " + name + " = " + rhs.prettyPrint() + " in "
				+ body.prettyPrint();
	}

	@Override
	public void typeCheckRho(Expected expected) throws TypeCheckException {
		Type varType = rhs.inferSigma();
		Environment env = Environment.getCurrent();
		env.extend(name, varType);
		body.typeCheckRho(expected);
		env.restore();
	}
}
