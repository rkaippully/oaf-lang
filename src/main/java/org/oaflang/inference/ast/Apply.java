package org.oaflang.inference.ast;

import java.util.Map;
import java.util.Set;

import org.oaflang.inference.Inference;
import org.oaflang.inference.type.Arrow;
import org.oaflang.inference.type.Type;
import org.oaflang.inference.type.TypeVariable;

public class Apply extends Node {

	private Node fn;
	private Node arg;

	public Apply(Node fn, Node arg) {
		this.fn = fn;
		this.arg = arg;
	}

	@Override
	public String toString() {
		return "(" + fn + " " + arg + ")";
	}

	@Override
	public Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics) {
		Type fnType = fn.getType(env, nonGenerics);
		Type argType = arg.getType(env, nonGenerics);
		Type resultType = new TypeVariable();
		//System.out.println(this + " : fn: " + fnType + ", arg: " + argType
		//		+ ", result: " + resultType);
		Inference.unify(new Arrow(argType, resultType), fnType);
		//System.out.println("Unified: fn: " + fnType + ", arg: " + argType
		//		+ ", result: " + resultType);
		return resultType;
	}
}