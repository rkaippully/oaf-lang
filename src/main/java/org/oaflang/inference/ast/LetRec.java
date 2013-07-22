package org.oaflang.inference.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.oaflang.inference.Inference;
import org.oaflang.inference.type.Type;
import org.oaflang.inference.type.TypeVariable;

/**
 * A lambda abstraction
 */
public class LetRec extends Node {

	private String var;
	private Node defn;
	private Node body;

	public LetRec(String var, Node defn, Node body) {
		this.var = var;
		this.defn = defn;
		this.body = body;
	}

	@Override
	public String toString() {
		return "(let rec " + var + " = " + defn + " in " + body + ")";
	}

	@Override
	public Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics) {
		TypeVariable newType = new TypeVariable();
        HashMap<String, Type> newEnv = new HashMap<>(env);
        newEnv.put(var, newType);
        HashSet<TypeVariable> newNonGenerics = new HashSet<>(nonGenerics);
        newNonGenerics.add(newType);
        Type defnType = defn.getType(newEnv, newNonGenerics);
		//System.out.println(this + ": var: " + newType + ", defn: " + defnType);
        Inference.unify(newType, defnType);
		//System.out.println("Unified: var: " + newType + ", defn: " + defnType);
        Type resultType = body.getType(newEnv, nonGenerics);
		//System.out.println(this + ": result: " + resultType);
        return resultType;
	}
}
