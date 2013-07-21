package org.oaf.inference.ast;

import java.util.Map;
import java.util.Set;

import org.oaf.inference.Inference;
import org.oaf.inference.type.Type;
import org.oaf.inference.type.TypeVariable;

public class If extends Node {

	private Node cond;
	private Node thenPart;
	private Node elsePart;

	public If(Node cond, Node thenPart, Node elsePart) {
		this.cond = cond;
		this.thenPart = thenPart;
		this.elsePart = elsePart;
	}

	@Override
	public String toString() {
		return "if (" + cond + ") then " + thenPart + " else " + elsePart + " fi";
	}

	@Override
	public Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics) {
		Type condType = cond.getType(env, nonGenerics);
		Inference.unify(condType, Inference.BooleanType);
		Type t1 = thenPart.getType(env, nonGenerics);
		Type t2 = elsePart.getType(env, nonGenerics);
		//System.out.println(this + ": thenPart: " + t1 + ", elsePart: " + t2);
		Inference.unify(t1, t2);
		//System.out.println("Unified: thenPart: " + t1 + ", elsePart: " + t2);
		return t1;
	}
}
