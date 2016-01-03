package org.oaflang.inference.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oaflang.inference.TypeCheckException;
import org.oaflang.inference.TypeChecker;
import org.oaflang.inference.pprint.Precedence;
import org.oaflang.inference.util.Lists;
import org.oaflang.inference.util.Pair;

public class ForAll extends Type {

	private List<TypeVariable> vars;
	private Type rho;

	public ForAll(List<TypeVariable> vars, Type rho) {
		this.vars = vars;
		this.rho = rho;
	}

	@Override
	protected void addMetaTypeVariables(List<MetaTypeVariable> acc) {
		rho.addMetaTypeVariables(acc);
	}

	@Override
	protected void addFreeTypeVariables(List<TypeVariable> boundVars,
			List<TypeVariable> acc) {
		List<TypeVariable> b = new ArrayList<>(boundVars);
		b.addAll(vars);
		rho.addFreeTypeVariables(b, acc);
	}

	@Override
	protected void addTypeVariableBinders(List<TypeVariable> acc) {
		Lists.addAllIfAbsent(acc, vars);
		rho.addTypeVariableBinders(acc);
	}

	@Override
	protected Type doTypeVariableSubstitution(Map<TypeVariable, Type> env) {
		Map<TypeVariable, Type> newEnv = new HashMap<>(env);
		for (TypeVariable var : vars)
			newEnv.remove(var);
		return new ForAll(vars, rho.doTypeVariableSubstitution(newEnv));
	}

	@Override
	Precedence getPrecedence() {
		return vars.size() > 0 ? Precedence.TopPrecedence : Precedence.AtomicPrecedence;
	}

	@Override
	String doPrettyPrint() {
		StringBuilder sb = new StringBuilder();
		if (vars.size() > 0) {
			sb.append("forall ");
			for (int i = 0; i < vars.size(); i++) {
				if (i > 0)
					sb.append(' ');
				sb.append(vars.get(i).prettyPrint());
			}
			sb.append(". ");
		}
		return sb.append(rho.prettyPrint()).toString();
	};

	@Override
	protected void subsumptionCheckRho(Type that) throws TypeCheckException {
		Type rho = instantiate();
		rho.subsumptionCheckRho(that);
	}

	@Override
	protected Type instantiate() {
		TypeChecker checker = TypeChecker.getCurrent();
		List<Type> taus = new ArrayList<>(vars.size());
		for (int i = 0; i < vars.size(); i++)
			taus.add(checker.newTypeVariable());
		return rho.substituteTypeVariables(vars, taus);
	}

	@Override
	public Pair<List<TypeVariable>, Type> skolemise() {
		TypeChecker checker = TypeChecker.getCurrent();
		List<TypeVariable> sks1 = new ArrayList<>(vars.size());
		for (TypeVariable var : vars)
			sks1.add(checker.newSkolemTypeVariable(var));
		Pair<List<TypeVariable>, Type> t = rho.substituteTypeVariables(vars,
				sks1).skolemise();
		List<TypeVariable> sks2 = t.fst();
		List<TypeVariable> result = new ArrayList<>();
		result.addAll(sks1);
		result.addAll(sks2);
		return new Pair<List<TypeVariable>, Type>(result, t.snd());
	}

	@Override
	public Type zonk() {
		return new ForAll(vars, rho.zonk());
	}

	@Override
	protected boolean unifyInternal(Type that) throws TypeCheckException {
		return false;
	}
}
