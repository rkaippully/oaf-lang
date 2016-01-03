package org.oaflang.inference.term;

import org.oaflang.inference.TypeCheckException;
import org.oaflang.inference.type.Type;
import org.oaflang.inference.typecheck.Expected;
import org.oaflang.inference.util.LinkedList;
import org.oaflang.inference.util.Pair;

public class App extends Term {

	private Term fun;
	private Term arg;

	public App(Term fun, Term arg) {
		this.fun = fun;
		this.arg = arg;
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	@Override
	public String prettyPrint() {
		return prettyPrintApp(new LinkedList<Term>());
	}

	@Override
	String prettyPrintApp(LinkedList<Term> es) {
		return fun.prettyPrintApp(es.prepend(arg));
	}

	@Override
	public void typeCheckRho(Expected expected) throws TypeCheckException {
		Type funcType = fun.inferRho();
		Pair<Type,Type> t = funcType.unifyFunction();
		Type argType = t.fst();
		Type resultType = t.snd();
		arg.checkSigma(argType);
		resultType.instantiateSigma(expected);
	}
}
