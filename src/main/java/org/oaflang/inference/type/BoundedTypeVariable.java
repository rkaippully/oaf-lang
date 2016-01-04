package org.oaflang.inference.type;

import org.oaflang.inference.TypeCheckException;

/**
 * A type variable bound by a forall
 */
public class BoundedTypeVariable extends TypeVariable {

	public BoundedTypeVariable(String name) {
		super(name);
	}

	@Override
	public int hashCode() {
		return this.getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		BoundedTypeVariable that = (BoundedTypeVariable) obj;
		return this.getName().equals(that.getName());
	}

	@Override
	public String doPrettyPrint() {
		return getName();
	}

	@Override
	protected boolean unifyInternal(Type that) throws TypeCheckException {
		throw new TypeCheckException(
				"Panic! Unexpected types in unification: "
						+ this.prettyPrint() + " " + that.prettyPrint());
	}
}
