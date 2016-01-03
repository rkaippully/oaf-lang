package org.oaflang.inference.typecheck;

import org.oaflang.inference.type.Type;

public class Infer extends Expected {

	private Type type;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
