package org.oaf.inference.ast;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.oaf.inference.type.Type;
import org.oaf.inference.type.TypeVariable;

/**
 * A lambda abstraction
 */
public class Let extends Node {

	private String var;
	private Node defn;
	private Node body;

	public Let(String var, Node defn, Node body) {
		this.var = var;
		this.defn = defn;
		this.body = body;
	}

	@Override
	public String toString() {
		return "(let " + var + " = " + defn + " in " + body + ")";
	}

	@Override
	public Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics) {
        Type defnType = defn.getType(env, nonGenerics);
        HashMap<String, Type> newEnv = new HashMap<>(env);
        newEnv.put(var, defnType);
		//System.out.println(this + ": defn: " + defnType);
        Type t = body.getType(newEnv, nonGenerics);
		//System.out.println(this + ": defn: " + defnType + ", result: " + t);
        return t;
	}
}
