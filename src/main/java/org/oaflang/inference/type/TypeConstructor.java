package org.oaflang.inference.type;

import java.util.Map;
import java.util.Set;

import org.oaflang.inference.Inference;

public class TypeConstructor extends Type {

	protected Type[] types;
	private String name;

	public TypeConstructor(String name, Type... types) {
		this.name = name;
		this.types = types;
	}

	@Override
	public Type freshType(Set<TypeVariable> nonGenerics,
			Map<TypeVariable, TypeVariable> mappings) {
		Type[] newTypes = new Type[types.length];
		for (int i = 0; i < types.length; i++) {
			newTypes[i] = types[i].freshType(nonGenerics, mappings);
		}
		return new TypeConstructor(name, newTypes);
	}

	/**
	 * Checks if the specified type variable occurs in this type constructor.
	 */
	@Override
	protected boolean contains(TypeVariable var) {
		for (Type t : types)
			if (t.contains(var))
				return true;
		return false;
	}

	@Override
	public void unify(Type t) {
		if (t instanceof TypeVariable) {
			t.unify(this);
		} else if (t instanceof TypeConstructor) {
			TypeConstructor that = (TypeConstructor) t;
			if (!this.name.equals(that.name)
					|| this.types.length != that.types.length)
				throw new IllegalArgumentException("Type mismatch: "
						+ this.toString(true) + " != " + that.toString(true));
			for (int i = 0; i < this.types.length; i++)
				Inference.unify(this.types[i], that.types[i]);
		} else
			super.unify(t);
	}

	@Override
	public String toString(boolean withName) {
		StringBuilder sb = new StringBuilder();
		sb.append(this.name);
		if (this.types.length > 0) {
			sb.append('[');
			for (int i = 0; i < this.types.length; i++) {
				if (i > 0)
					sb.append(", ");
				sb.append(this.types[i].toString(withName));
			}
			sb.append(']');
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return toString(false);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TypeVariable)
			obj = ((TypeVariable)obj).prune();

		if (!(obj instanceof TypeConstructor))
			return false;

		TypeConstructor that = (TypeConstructor) obj;
		if (!this.name.equals(that.name) || this.types.length != that.types.length)
			return false;
		for (int i = 0; i < this.types.length; i++)
			if (!this.types[i].equals(that.types[i]))
				return false;
		
		return true;
	}
}
