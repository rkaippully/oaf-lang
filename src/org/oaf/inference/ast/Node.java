package org.oaf.inference.ast;

import java.util.Map;
import java.util.Set;

import org.oaf.inference.type.Type;
import org.oaf.inference.type.TypeVariable;

public abstract class Node {

	public abstract Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics);
}
