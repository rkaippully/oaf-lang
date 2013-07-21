package org.oaf.inference.type;

import java.util.Map;
import java.util.Set;

import org.oaf.inference.Inference;

/**
 * All type variables have their names assigned lazily when needed
 */
public class TypeVariable extends Type {

	private static int NextId = 0;

	private int id = NextId++;
	private Character name;
	private Type instance;

	@Override
	public Type freshType(Set<TypeVariable> nonGenerics,
			Map<TypeVariable, TypeVariable> mappings) {
		Type p = prune();
		if (p instanceof TypeVariable) {
			TypeVariable pruned = (TypeVariable) p;
			if (pruned.isGeneric(nonGenerics)) {
				// Create a copy of the generic variable
				if (!mappings.containsKey(pruned))
					mappings.put(pruned, new TypeVariable());
				return mappings.get(pruned);
			} else {
				return pruned;
			}
		} else {
			return p.freshType(nonGenerics, mappings);
		}
	}

	/**
	 * Checks is this type variable is a generic one - i.e. it does not occur in
	 * any of the nonGenerics.
	 */
	private boolean isGeneric(Set<TypeVariable> nonGenerics) {
		for (Type var : nonGenerics) {
			if (var.contains(this))
				return false;
		}
		return true;
	}

	/**
	 * Checks if the specified type variable occurs in this type.
	 */
	@Override
	protected boolean contains(TypeVariable var) {
		Type pruned = prune();
		if (pruned != this)
			return pruned.contains(var);
		else
			return this == var;
	}

	/**
	 * Collapses the list of type instances in a long chain of instantiated
	 * {@link TypeVariable type variables}.
	 */
	@Override
	public Type prune() {
		if (instance != null) {
			instance = instance.prune();
			return instance;
		}
		return this;
	}

	@Override
	public void unify(Type that) {
		if (this != that) {
			if (that.contains(this))
				throw new IllegalArgumentException("Recursive unification of "
						+ this.toString(true) + " and " + that.toString(true));
			this.instance = that;
		}
	}

	@Override
	public String toString(boolean withName) {
		if (instance != null)
			return instance.toString(withName);

		if (this.name == null) {
			if (withName)
				this.name = Inference.getNextVariableName();
			else
				return "v" + id;
		}
		return String.valueOf(this.name);
	}

	@Override
	public String toString() {
		return toString(false);
	}
}
