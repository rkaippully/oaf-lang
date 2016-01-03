package org.oaflang.inference.term;

import org.oaflang.inference.TypeCheckException;
import org.oaflang.inference.type.Type;
import org.oaflang.inference.typecheck.Expected;

public class Literal extends Term {

	private Integer intVal;
	private Boolean boolVal;

	public Literal(int val) {
		this.intVal = val;
	}
	
	public Literal(boolean val) {
		this.boolVal = val;
	}

	@Override
	public boolean isAtomic() {
		return true;
	}

	@Override
	public String prettyPrint() {
		return intVal != null ? intVal.toString() : boolVal.toString();
	}

	@Override
	public void typeCheckRho(Expected expected) throws TypeCheckException {
		Type t = this.intVal != null ? Type.IntType : Type.BoolType;
		t.instantiateSigma(expected);
	}
}
