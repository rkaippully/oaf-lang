package org.oaflang.inference.type;

import org.oaflang.inference.TypeCheckException;

/**
 * A skolem constant. The string is just to improve error messages.
 */
public class SkolemTypeVariable extends TypeVariable {

	private int uniqueNum;

	public SkolemTypeVariable(String name, int uniqueNum) {
		super(name);
		this.uniqueNum = uniqueNum;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SkolemTypeVariable) {
			SkolemTypeVariable that = (SkolemTypeVariable) obj;
			return this.uniqueNum == that.uniqueNum;
		}
		return false;
	}

	@Override
	public String doPrettyPrint() {
		return getName() + uniqueNum;
	}

	@Override
	protected boolean unifyInternal(Type that) throws TypeCheckException {
		return this.equals(that);
	}
}
