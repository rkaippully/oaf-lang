package org.oaflang.inference.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oaflang.inference.TypeCheckException;
import org.oaflang.inference.TypeChecker;
import org.oaflang.inference.pprint.Precedence;
import org.oaflang.inference.pprint.PrettyPrintable;
import org.oaflang.inference.typecheck.Check;
import org.oaflang.inference.typecheck.Expected;
import org.oaflang.inference.typecheck.Infer;
import org.oaflang.inference.util.LinkedList;
import org.oaflang.inference.util.Lists;
import org.oaflang.inference.util.Pair;

public abstract class Type implements PrettyPrintable {

	public static final Type IntType = new TypeConstant("Int");
	public static final Type BoolType = new TypeConstant("Bool");

	/**
	 * This function takes account of zonking and returns a set (no duplicates)
	 * of unbound meta type variables
	 */
	public static List<MetaTypeVariable> getMetaTypeVariables(
			Collection<Type> types) {
		List<MetaTypeVariable> result = new ArrayList<>();
		for (Type type : types)
			type.zonk().addMetaTypeVariables(result);
		return result;
	}

	protected void addMetaTypeVariables(List<MetaTypeVariable> result) {
	}

	/**
	 * This function takes account of zonking and returns a set (no duplicates)
	 * of free type variables
	 */
	public static List<TypeVariable> getFreeTypeVariables(Iterable<Type> types) {
		List<TypeVariable> bounded = new ArrayList<>();
		List<TypeVariable> result = new ArrayList<>();
		for (Type type : types)
			type.zonk().addFreeTypeVariables(bounded, result);
		return result;
	}

	protected void addFreeTypeVariables(List<TypeVariable> boundVars,
			List<TypeVariable> acc) {
	}

	/**
	 * Get all the binders used in {@link ForAll}s in the type, so that when
	 * quantifying an outer {@link ForAll} we can avoid these inner ones.
	 */
	protected List<TypeVariable> getTypeVariableBinders() {
		List<TypeVariable> result = new ArrayList<>();
		addTypeVariableBinders(result);
		return result;
	}

	protected void addTypeVariableBinders(List<TypeVariable> result) {
	}

	/**
	 * Replace the specified quantified {@link TypeVariable}s in this type by
	 * given {@link MetaTypeVariable}s. No worries about capture, because the
	 * two kinds of type variables are distinct.
	 */
	protected Type substituteTypeVariables(List<TypeVariable> typeVars,
			List<? extends Type> taus) {
		Map<TypeVariable, Type> env = new HashMap<>();
		for (int i = 0; i < typeVars.size(); i++)
			env.put(typeVars.get(i), taus.get(i));
		return doTypeVariableSubstitution(env);
	}

	protected abstract Type doTypeVariableSubstitution(Map<TypeVariable, Type> env);

	@Override
	public String toString() {
		return prettyPrint(Precedence.TopPrecedence);
	}

	@Override
	public final String prettyPrint() {
		return prettyPrint(Precedence.TopPrecedence);
	}

	final String prettyPrint(Precedence precedence) {
		String s = doPrettyPrint();
		if (precedence.compareTo(getPrecedence()) >= 0)
			return "(" + s + ")";
		else
			return s;
	}

	abstract String doPrettyPrint();

	Precedence getPrecedence() {
		return Precedence.AtomicPrecedence;
	}

	public String prettyPrintWithParens() {
		return prettyPrint(Precedence.TypeConsPrecedence);
	}

	/**
	 * Checks that 'this' type is at least as polymorphic as 'that'.
	 */
	public void subsumptionCheck(Type that) throws TypeCheckException {
		Pair<List<TypeVariable>, Type> t = that.skolemise();
		List<TypeVariable> skolTypeVars = t.fst();
		Type rho = t.snd();
		subsumptionCheckRho(rho);
		List<TypeVariable> escTypes = getFreeTypeVariables(new LinkedList<Type>()
				.prepend(that).prepend(this));
		List<TypeVariable> badTypeVars = Lists
				.intersect(skolTypeVars, escTypes);
		if (!badTypeVars.isEmpty())
			throw new TypeCheckException("Type mismatch: "
					+ this.prettyPrint() + " is not as polymorphic as "
					+ that.prettyPrint());
	}

	protected void subsumptionCheckRho(Type rho) throws TypeCheckException {
		if (rho instanceof Function)
			rho.subsumptionCheckRho(this);
		else
			// Revert to ordinary unification
			unify(rho);
	}

	public void instantiateSigma(Expected expected) throws TypeCheckException {
		if (expected instanceof Check) {
			Check check = (Check) expected;
			subsumptionCheck(check.getType());
		} else {
			Infer infer = (Infer) expected;
			infer.setType(this.instantiate());
		}
	}

	protected Type instantiate() {
		return this;
	}

	/**
	 * Performs deep skolemisation, returning the skolem constants and the
	 * skolemised type.
	 */
	public Pair<List<TypeVariable>, Type> skolemise() {
		return new Pair<List<TypeVariable>, Type>(
				new ArrayList<TypeVariable>(), this);
	}

	/**
	 * Quantify over the specified type variables (all flexible)
	 */
	public Type quantify(List<MetaTypeVariable> vars) {
		List<TypeVariable> usedBinders = getTypeVariableBinders();
		List<TypeVariable> newBinders = formNewBinders(usedBinders, vars.size());
		for (int i = 0; i < vars.size(); i++)
			vars.get(i).setTypeRef(newBinders.get(i));
		return new ForAll(newBinders, this.zonk());
	}

	private List<TypeVariable> formNewBinders(List<TypeVariable> usedBinders,
			int count) {
		List<TypeVariable> result = new ArrayList<>(count);
		int n = 0;
		char ch = 'a';
		for (int i = 0; i < count;) {
			StringBuilder name = new StringBuilder();
			name.append(ch);
			if (n > 0)
				name.append(n);

			TypeVariable tv = new BoundedTypeVariable(name.toString());
			if (!usedBinders.contains(tv)) {
				result.add(tv);
				i++;
			}

			if (ch == 'z') {
				ch = 'a';
				n++;
			} else {
				ch++;
			}
		}
		return result;
	}

	public abstract Type zonk();

	/**
	 * Attempt to unify this type with that and throw a
	 * {@link TypeCheckException} in case of failure.
	 */
	protected final boolean unify(Type that) throws TypeCheckException {
		if (that instanceof BoundedTypeVariable)
			throw new TypeCheckException(
					"Panic! Unexpected types in unification: "
							+ this.prettyPrint() + " " + that.prettyPrint());

		if (that instanceof MetaTypeVariable) {
			MetaTypeVariable tv = (MetaTypeVariable) that;
			return tv.unifyVar(this);
		} else if (!unifyInternal(that))
			throw new TypeCheckException("Cannot unify types: "
					+ this.prettyPrint() + " != " + that.prettyPrint());

		return true;
	}

	protected abstract boolean unifyInternal(Type that)
			throws TypeCheckException;

	public Pair<Type, Type> unifyFunction() throws TypeCheckException {
		TypeChecker checker = TypeChecker.getCurrent();
		Type argType = checker.newTypeVariable();
		Type resType = checker.newTypeVariable();
		this.unify(new Function(argType, resType));
		return new Pair<Type, Type>(argType, resType);
	}

	@Override
	public abstract boolean equals(Object obj);
}