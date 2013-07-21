package org.oaf.inference.type;

import java.util.Map;
import java.util.Set;

public abstract class Type {

	/**
	 * Creates a copy of the type, the generic variables are copied and the
	 * non-generic variables are shared.
	 */
	public Type freshType(Set<TypeVariable> nonGenerics,
			Map<TypeVariable, TypeVariable> mappings) {
		throw new IllegalArgumentException("Cannot get fresh instance of "
				+ this);
	}

	/**
	 * Collapses the list of type instances in a long chain of instantiated
	 * {@link TypeVariable type variables}.
	 */
	public Type prune() {
		return this;
	}

	/**
	 * Checks if the specified type variable occurs in this type.
	 */
	protected boolean contains(TypeVariable var) {
		return this == var;
	}

	/**
	 * Unify this with that.
	 */
	public void unify(Type that) {
		throw new IllegalArgumentException("Cannot unify " + this + " with "
				+ that);
	}

	/**
	 * A representation for printing
	 */
	public abstract String toString(boolean withName);
}
