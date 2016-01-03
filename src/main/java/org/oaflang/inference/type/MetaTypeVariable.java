package org.oaflang.inference.type;

import java.util.List;
import java.util.Map;

import org.oaflang.inference.TypeCheckException;
import org.oaflang.inference.util.Lists;

//Can unify with any Tau type
public class MetaTypeVariable extends Type {

	private int uniqueNum;
	private Type typeRef;

	public MetaTypeVariable(int uniqueNum, Type typeRef) {
		this.uniqueNum = uniqueNum;
		this.typeRef = typeRef;
	}

	public void setTypeRef(Type type) {
		this.typeRef = type;
	}

	@Override
	protected void addMetaTypeVariables(List<MetaTypeVariable> acc) {
		if (!acc.contains(this))
			acc.add(this);
	}

	@Override
	protected Type doTypeVariableSubstitution(Map<TypeVariable, Type> env) {
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MetaTypeVariable) {
			MetaTypeVariable that = (MetaTypeVariable) obj;
			return this.uniqueNum == that.uniqueNum;
		}
		return false;
	}

	@Override
	public String doPrettyPrint() {
		return typeRef != null ? typeRef.prettyPrint() : "$" + uniqueNum;
	}

	@Override
	public Type zonk() {
		if (typeRef == null)
			return this;
		else {
			// Short out multiple hops
			typeRef = typeRef.zonk();
			return typeRef;
		}
	}

	@Override
	protected boolean unifyInternal(Type that) throws TypeCheckException {
		if (this.equals(that))
			return true;
		else
			return unifyVar(that);
	}

	protected boolean unifyVar(Type that) throws TypeCheckException {
		if (typeRef != null)
			return typeRef.unify(that);
		else if (that instanceof MetaTypeVariable) {
			/*
			 * We know that this != that because the first condition in
			 * unifyInternal would have caught it.
			 */
			MetaTypeVariable tv2 = (MetaTypeVariable) that;
			if (tv2.typeRef == null) {
				this.typeRef = that;
				return true;
			} else
				return this.unify(tv2.typeRef);
		} else {
			List<MetaTypeVariable> tvs2 = Type.getMetaTypeVariables(Lists
					.list(that));
			if (tvs2.contains(this))
				throw new TypeCheckException("Occurs check failed for: "
						+ this.prettyPrint() + " in: " + that.prettyPrint());
			else {
				this.typeRef = that;
				return true;
			}
		}
	}
}
