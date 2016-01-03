package org.oaflang.inference.type;

import java.util.List;
import java.util.Map;

//Always bound by a ForAll
public abstract class TypeVariable extends Type {

	private String name;

	public TypeVariable(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	protected void addFreeTypeVariables(List<TypeVariable> boundVars,
			List<TypeVariable> acc) {
		if (!boundVars.contains(this) && !acc.contains(this))
			acc.add(this);
	}

	@Override
	protected Type doTypeVariableSubstitution(Map<TypeVariable, Type> env) {
		Type t = env.get(this);
		return t != null ? t : this;
	}

	@Override
	public TypeVariable zonk() {
		return this;
	}
}
