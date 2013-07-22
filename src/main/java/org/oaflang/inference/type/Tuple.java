package org.oaflang.inference.type;

import java.util.Map;
import java.util.Set;

public class Tuple extends TypeConstructor {

	public Tuple(Type... types) {
		super("*", types);
	}

	@Override
	public Type freshType(Set<TypeVariable> nonGenerics,
			Map<TypeVariable, TypeVariable> mappings) {
		Type[] newTypes = new Type[types.length];
		for (int i = 0; i < types.length; i++) {
			newTypes[i] = types[i].freshType(nonGenerics, mappings);
		}
		return new Tuple(newTypes);
	}

	@Override
	public String toString(boolean withName) {
		StringBuilder sb = new StringBuilder("(");
		for (Type t : types) {
			if (sb.length() > 1)
				sb.append(" * ");
			sb.append(t.toString(withName));
		}
		sb.append(')');
		return sb.toString();
	}

	@Override
	public String toString() {
		return toString(false);
	}
}
