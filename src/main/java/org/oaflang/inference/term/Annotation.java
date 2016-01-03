package org.oaflang.inference.term;

import org.oaflang.inference.TypeCheckException;
import org.oaflang.inference.type.Type;
import org.oaflang.inference.typecheck.Expected;

public class Annotation extends Term {

	private Term body;
	private Type ann;

	public Annotation(Term body, Type sigma) {
		this.body = body;
		this.ann = sigma;
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	@Override
	public String prettyPrint() {
		return body.prettyPrintWithParens() + " :: "
				+ ann.prettyPrintWithParens();
	}

	@Override
	public void typeCheckRho(Expected expected) throws TypeCheckException {
		body.checkSigma(ann);
		ann.instantiateSigma(expected);
	}
}
