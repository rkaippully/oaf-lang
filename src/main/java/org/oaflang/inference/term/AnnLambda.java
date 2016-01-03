package org.oaflang.inference.term;

import org.oaflang.inference.TypeCheckException;
import org.oaflang.inference.type.Function;
import org.oaflang.inference.type.Type;
import org.oaflang.inference.typecheck.Check;
import org.oaflang.inference.typecheck.Environment;
import org.oaflang.inference.typecheck.Expected;
import org.oaflang.inference.typecheck.Infer;
import org.oaflang.inference.util.Pair;

/**
 * Annotated Lambda
 */
public class AnnLambda extends Term {

	private String name;
	private Type ann;
	private Term body;

	public AnnLambda(String name, Type sigma, Term body) {
		this.name = name;
		this.ann = sigma;
		this.body = body;
	}

	@Override
	public boolean isAtomic() {
		return false;
	}

	@Override
	public String prettyPrint() {
		return "\\(" + name + " :: " + ann.prettyPrint() + "). "
				+ body.prettyPrint();
	}

	@Override
	public void typeCheckRho(Expected expected) throws TypeCheckException {
		if (expected instanceof Check) {
			Check check = (Check) expected;
			Pair<Type, Type> t = check.getType().unifyFunction();
			Type argType = t.fst();
			Type bodyType = t.snd();
			argType.subsumptionCheck(ann);
			Environment env = Environment.getCurrent();
			env.extend(name, ann);
			body.checkRho(bodyType);
			env.restore();
		} else {
			Infer infer = (Infer) expected;
			Environment env = Environment.getCurrent();
			env.extend(name, ann);
			Type bodyType = body.inferRho();
			env.restore();
			infer.setType(new Function(ann, bodyType));
		}
	}
}
