package org.oaf.inference.ast;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.oaf.inference.type.Type;
import org.oaf.inference.type.TypeVariable;

public class Identifier extends Node {

	private String name;

	public Identifier(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics) {
		if (env.containsKey(name)) {
			Map<TypeVariable, TypeVariable> mappings = new HashMap<>();
			return env.get(name).freshType(nonGenerics, mappings);
		}
		throw new IllegalArgumentException("Undefined identifier " + name);
	}
}
