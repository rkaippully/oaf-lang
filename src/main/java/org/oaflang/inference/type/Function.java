package org.oaflang.inference.type;

import java.util.List;
import java.util.Map;

import org.oaflang.inference.TypeCheckException;
import org.oaflang.inference.pprint.Precedence;
import org.oaflang.inference.util.Pair;

public class Function extends Type {

	private Type arg;
	private Type result;

	public Function(Type arg, Type result) {
		this.arg = arg;
		this.result = result;
	}

	@Override
	protected void addMetaTypeVariables(List<MetaTypeVariable> acc) {
		result.addMetaTypeVariables(acc);
		arg.addMetaTypeVariables(acc);
	}

	@Override
	protected void addFreeTypeVariables(List<TypeVariable> boundVars,
			List<TypeVariable> acc) {
		result.addFreeTypeVariables(boundVars, acc);
		arg.addFreeTypeVariables(boundVars, acc);
	}

	@Override
	protected void addTypeVariableBinders(List<TypeVariable> acc) {
		arg.addTypeVariableBinders(acc);
		result.addTypeVariableBinders(acc);
	}

	@Override
	protected Type doTypeVariableSubstitution(Map<TypeVariable, Type> env) {
		return new Function(arg.doTypeVariableSubstitution(env),
				result.doTypeVariableSubstitution(env));
	}

	@Override
	Precedence getPrecedence() {
		return Precedence.ArrowPrecedence;
	}

	@Override
	String doPrettyPrint() {
		return arg.prettyPrint(Precedence.ArrowPrecedence) +
				" -> " +
				result.prettyPrint(Precedence.TopPrecedence);
	}

	@Override
	protected void subsumptionCheckRho(Type that) throws TypeCheckException {
		Pair<Type, Type> t = that.unifyFunction();
		Type thatArg =  t.fst();
		Type thatResult = t.snd();
		thatArg.subsumptionCheck(this.arg);
		this.result.subsumptionCheckRho(thatResult);
	}

	@Override
	public Pair<List<TypeVariable>, Type> skolemise() {
		Pair<List<TypeVariable>, Type> t = result.skolemise();
		return new Pair<List<TypeVariable>, Type>(t.fst(), new Function(arg, t.snd()));
	}

	@Override
	public Type zonk() {
		return new Function(arg.zonk(), result.zonk());
	}

	@Override
	protected boolean unifyInternal(Type that) throws TypeCheckException {
		if (that instanceof Function) {
			Function f = (Function) that;
			return this.arg.unify(f.arg) && this.result.unify(f.result);
		}
		return false;
	}

	@Override
	public Pair<Type, Type> unifyFunction() throws TypeCheckException {
		return new Pair<Type, Type>(arg, result);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Function) {
			Function that = (Function) obj;
			return this.arg.equals(that.arg) && this.result.equals(that.result);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return arg.hashCode() + result.hashCode();
	}
}
