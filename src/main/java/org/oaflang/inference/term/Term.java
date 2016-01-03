package org.oaflang.inference.term;

import java.util.Collection;
import java.util.List;

import org.oaflang.inference.TypeCheckException;
import org.oaflang.inference.pprint.PrettyPrintable;
import org.oaflang.inference.type.MetaTypeVariable;
import org.oaflang.inference.type.Type;
import org.oaflang.inference.type.TypeVariable;
import org.oaflang.inference.typecheck.Check;
import org.oaflang.inference.typecheck.Environment;
import org.oaflang.inference.typecheck.Expected;
import org.oaflang.inference.typecheck.Infer;
import org.oaflang.inference.util.LinkedList;
import org.oaflang.inference.util.Lists;
import org.oaflang.inference.util.Pair;

public abstract class Term implements PrettyPrintable {

	public abstract boolean isAtomic();

	String prettyPrintWithParens() {
		return isAtomic() ? prettyPrint() : "(" + prettyPrint() + ")";
	}

	String prettyPrintApp(LinkedList<Term> es) {
		StringBuilder sb = new StringBuilder();
		for (Term e : es)
			sb.append(' ').append(e.prettyPrintWithParens());
		return prettyPrintWithParens() + sb.toString();
	}

	/* Type checking */

	public Type inferSigma() throws TypeCheckException {
		Type expectedType = inferRho();
		Environment env = Environment.getCurrent();
		Collection<Type> envTypes = env.getTypes();
		List<MetaTypeVariable> envTypeVars = Type
				.getMetaTypeVariables(envTypes);
		List<MetaTypeVariable> resultTypeVars = Type.getMetaTypeVariables(Lists
				.list(expectedType));
		resultTypeVars.removeAll(envTypeVars);
		return expectedType.quantify(resultTypeVars);
	}

	public void checkSigma(Type sigma) throws TypeCheckException {
		Pair<List<TypeVariable>, Type> t = sigma.skolemise();
		List<TypeVariable> skolTypeVars = t.fst();
		Type rho = t.snd();
		checkRho(rho);
		Environment env = Environment.getCurrent();
		Collection<Type> envTypes = env.getTypes();
		List<TypeVariable> escTypes = Type.getFreeTypeVariables(new LinkedList<>(
				envTypes).prepend(sigma));
		List<TypeVariable> badTypeVars = Lists
				.intersect(skolTypeVars, escTypes);
		if (!badTypeVars.isEmpty())
			throw new TypeCheckException("Type is not polymorphic enough");
	}

	public abstract void typeCheckRho(Expected expected)
			throws TypeCheckException;

	public Type inferRho() throws TypeCheckException {
		Infer inf = new Infer();
		typeCheckRho(inf);
		return inf.getType();
	}

	protected void checkRho(Type rho) throws TypeCheckException {
		typeCheckRho(new Check(rho));
	}

	@Override
	public String toString() {
		return prettyPrint();
	}
}
