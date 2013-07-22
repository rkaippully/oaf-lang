package org.oaflang.inference.ast;

import java.util.Map;
import java.util.Set;

import org.oaflang.inference.type.Type;
import org.oaflang.inference.type.TypeVariable;

public abstract class Node {

	public abstract Type getType(Map<String, Type> env, Set<TypeVariable> nonGenerics);
}
