package org.oaf.inference.ast;

import java.util.Map;
import java.util.Set;

import org.oaf.inference.Inference;
import org.oaf.inference.type.Type;
import org.oaf.inference.type.TypeVariable;

/**
 * An integer literal
 */
public class Literal extends Node {

	private Integer intVal;
	private Boolean boolVal;

	public Literal (int i) {
		this.intVal = i;
	}

	public Literal (boolean b) {
		this.boolVal = b;
	}

	@Override
	public Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics) {
		return intVal != null ? Inference.IntegerType : Inference.BooleanType;
	}

	@Override
	public String toString() {
		return intVal != null ? intVal.toString() : boolVal.toString();
	}
}
