package org.oaflang.inference.type;

import java.util.Map;

import org.oaflang.inference.TypeCheckException;

public class TypeConstant extends Type {

	private String name;

	public TypeConstant(String name) {
		this.name = name;
	}

	@Override
	protected Type doTypeVariableSubstitution(Map<TypeVariable, Type> env) {
		return this;
	}

	@Override
	String doPrettyPrint() {
		return name;
	}

	@Override
	public Type zonk() {
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TypeConstant) {
			TypeConstant that = (TypeConstant) obj;
			return this.name.equals(that.name);
		}
		return false;
	}

	@Override
	protected boolean unifyInternal(Type that) throws TypeCheckException {
		return this.equals(that);
	}
}
