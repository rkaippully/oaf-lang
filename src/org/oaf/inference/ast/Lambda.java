package org.oaf.inference.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.oaf.inference.type.Arrow;
import org.oaf.inference.type.Type;
import org.oaf.inference.type.TypeVariable;

/**
 * A lambda abstraction
 */
public class Lambda extends Node {

	private String var;
	private Node body;

	public Lambda(String var, Node body) {
		this.var = var;
		this.body = body;
	}

	@Override
	public String toString() {
		return "fn[" + var + "] => " + body;
	}

	@Override
	public Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics) {
        TypeVariable argType = new TypeVariable();
        Map<String, Type> newEnv = new HashMap<>(env);
        newEnv.put(var, argType);
        HashSet<TypeVariable> newNonGenerics = new HashSet<>(nonGenerics);
        newNonGenerics.add(argType);
		//System.out.println(this + ": arg: " + argType);
        Type resultType = body.getType(newEnv, newNonGenerics);
		//System.out.println(this + ": arg: " + argType + ", result: " + resultType);
        return new Arrow(argType, resultType);
	}
}
