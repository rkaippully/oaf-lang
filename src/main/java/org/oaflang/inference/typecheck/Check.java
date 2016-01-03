package org.oaflang.inference.typecheck;

import org.oaflang.inference.type.Type;

public class Check extends Expected {

	private Type type;

	public Check(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}
}
