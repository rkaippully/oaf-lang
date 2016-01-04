package org.oaflang.inference.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.oaflang.inference.TypeCheckException;
import org.oaflang.inference.pprint.Precedence;
import org.oaflang.inference.util.Lists;
import org.oaflang.inference.util.Pair;

public class TypeConstructor extends Type {

	private String name;
	private List<? extends Type> args;

	public TypeConstructor(String name, List<? extends Type> args) {
		this.name = name;
		this.args = args;
	}

	@Override
	protected void addMetaTypeVariables(List<MetaTypeVariable> acc) {
		for (Type t : args)
			t.addMetaTypeVariables(acc);
	}

	@Override
	protected void addFreeTypeVariables(List<TypeVariable> boundVars,
			List<TypeVariable> acc) {
		for (Type t : args)
			t.addFreeTypeVariables(boundVars, acc);
	}

	@Override
	protected void addTypeVariableBinders(List<TypeVariable> acc) {
		for (Type t : args)
			t.addTypeVariableBinders(acc);
	}

	@Override
	protected Type doTypeVariableSubstitution(Map<TypeVariable, Type> env) {
		List<Type> subs = new ArrayList<>(args.size());
		for (Type t : args)
			subs.add(t.doTypeVariableSubstitution(env));
		return new TypeConstructor(name, subs);
	}

	@Override
	Precedence getPrecedence() {
		return Precedence.TypeConsPrecedence;
	}

	@Override
	String doPrettyPrint() {
		StringBuilder sb = new StringBuilder(name);
		for (Type t : args)
			sb.append(' ').append(t.prettyPrint());
		return sb.toString();
	}

	@Override
	public Pair<List<TypeVariable>, Type> skolemise() {
		List<TypeVariable> tvs = new ArrayList<>();
		List<Type> skolemised = new ArrayList<>(args.size());
		for (Type t : args) {
			Pair<List<TypeVariable>, Type> p = t.skolemise();
			Lists.addAllIfAbsent(tvs, p.fst());
			skolemised.add(p.snd());
		}
		return new Pair<List<TypeVariable>, Type>(tvs, new TypeConstructor(
				name, skolemised));
	}

	@Override
	public Type zonk() {
		List<Type> vars = new ArrayList<>(args.size());
		for (Type t : args)
			vars.add(t.zonk());
		return new TypeConstructor(name, vars);
	}

	@Override
	protected boolean unifyInternal(Type that) throws TypeCheckException {
		if (that instanceof TypeConstructor) {
			TypeConstructor tc = (TypeConstructor) that;
			if (this.args.size() != tc.args.size())
				return false;
			for (int i = 0; i < this.args.size(); i++)
				if (!this.args.get(i).unify(tc.args.get(i)))
					return false;
			return true;
		}
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TypeConstructor) {
			TypeConstructor that = (TypeConstructor) obj;
			return this.name.equals(that.name) && this.args.equals(that.args);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode() + args.hashCode();
	}
}
