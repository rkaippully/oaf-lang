package org.oaflang.inference.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.oaflang.inference.parser.ExpressionParser.AnnotatedExpressionContext;
import org.oaflang.inference.parser.ExpressionParser.AnnotatedLambdaContext;
import org.oaflang.inference.parser.ExpressionParser.ForAllTypeContext;
import org.oaflang.inference.parser.ExpressionParser.FunctionApplicationContext;
import org.oaflang.inference.parser.ExpressionParser.FunctionTypeContext;
import org.oaflang.inference.parser.ExpressionParser.LambdaExpressionContext;
import org.oaflang.inference.parser.ExpressionParser.LetExpressionContext;
import org.oaflang.inference.parser.ExpressionParser.LiteralExpressionContext;
import org.oaflang.inference.parser.ExpressionParser.ProgramContext;
import org.oaflang.inference.parser.ExpressionParser.TopLevelExpressionContext;
import org.oaflang.inference.parser.ExpressionParser.TypeConstantTypeContext;
import org.oaflang.inference.parser.ExpressionParser.TypeConstructorTypeContext;
import org.oaflang.inference.parser.ExpressionParser.TypeVariableTypeContext;
import org.oaflang.inference.parser.ExpressionParser.VariableExpressionContext;
import org.oaflang.inference.term.AnnLambda;
import org.oaflang.inference.term.Annotation;
import org.oaflang.inference.term.App;
import org.oaflang.inference.term.Lambda;
import org.oaflang.inference.term.Let;
import org.oaflang.inference.term.Literal;
import org.oaflang.inference.term.Term;
import org.oaflang.inference.term.Variable;
import org.oaflang.inference.type.BoundedTypeVariable;
import org.oaflang.inference.type.ForAll;
import org.oaflang.inference.type.Function;
import org.oaflang.inference.type.Type;
import org.oaflang.inference.type.TypeConstructor;
import org.oaflang.inference.type.TypeVariable;

public class TypeInferenceTreeListener extends ExpressionBaseListener {

	private Map<Term, Object> termMappings;
	private Stack<Object> workingElems;

	public Map<Term, Object> getTermMappings() {
		return termMappings;
	}

	@Override
	public void enterProgram(ProgramContext ctx) {
		termMappings = new HashMap<>();
		workingElems = new Stack<>();
	}

	@Override
	public void enterTopLevelExpression(TopLevelExpressionContext ctx) {
		workingElems.clear();
	}

	@Override
	public void exitTopLevelExpression(TopLevelExpressionContext ctx) {
		Object value;
		if (ctx.type() != null)
			value = (Type) workingElems.pop();
		else {
			String msg = ctx.errorMessage().getText();
			// Trim leading and trailing quotes
			value = msg.substring(1, msg.length() - 1);
		}
		Term term = (Term) workingElems.pop();
		termMappings.put(term, value);
	}

	@Override
	public void exitLiteralExpression(LiteralExpressionContext ctx) {
		TerminalNode node = ctx.literal().INTEGER();
		if (node != null) {
			workingElems.push(new Literal(Integer.parseInt(node.getText())));
		} else {
			node = ctx.literal().BOOLEAN();
			workingElems.push(new Literal(Boolean.parseBoolean(node.getText())));
		}
	}

	@Override
	public void exitVariableExpression(VariableExpressionContext ctx) {
		TerminalNode node = ctx.variable().IDENTIFIER();
		workingElems.push(new Variable(node.getText()));
	}

	@Override
	public void exitLetExpression(LetExpressionContext ctx) {
		Term body = (Term) workingElems.pop();
		Term rhs = (Term) workingElems.pop();
		workingElems.push(new Let(ctx.IDENTIFIER().getText(), rhs, body));
	}

	@Override
	public void exitLambdaExpression(LambdaExpressionContext ctx) {
		Term body = (Term) workingElems.pop();
		workingElems.push(new Lambda(ctx.IDENTIFIER().getText(), body));
	}

	@Override
	public void exitAnnotatedLambda(AnnotatedLambdaContext ctx) {
		Term body = (Term) workingElems.pop();
		Type type = (Type) workingElems.pop();
		workingElems.push(new AnnLambda(ctx.IDENTIFIER().getText(), type, body));
	}

	@Override
	public void exitFunctionApplication(FunctionApplicationContext ctx) {
		Term arg = (Term) workingElems.pop();
		Term fun = (Term) workingElems.pop();
		workingElems.push(new App(fun, arg));
	}

	@Override
	public void exitAnnotatedExpression(AnnotatedExpressionContext ctx) {
		Type type = (Type) workingElems.pop();
		Term body = (Term) workingElems.pop();
		workingElems.push(new Annotation(body, type));
	}

	@Override
	public void exitTypeVariableType(TypeVariableTypeContext ctx) {
		TerminalNode node = ctx.typeVariable().IDENTIFIER();
		workingElems.push(new BoundedTypeVariable(node.getText()));
	}

	@Override
	public void exitForAllType(ForAllTypeContext ctx) {
		Type type = (Type) workingElems.pop();
		int count = ctx.typeVariable().size(); 
		List<TypeVariable> vars = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			TerminalNode node = ctx.typeVariable().get(i).IDENTIFIER();
			vars.add(new BoundedTypeVariable(node.getText()));
		}
		workingElems.push(new ForAll(vars, type));
	}

	@Override
	public void exitFunctionType(FunctionTypeContext ctx) {
		Type result = (Type) workingElems.pop();
		Type arg = (Type) workingElems.pop();
		workingElems.push(new Function(arg, result));
	}

	@Override
	public void exitTypeConstantType(TypeConstantTypeContext ctx) {
		String typeName = ctx.typeConstant().getText();
		if (typeName.equals("Int"))
			workingElems.push(Type.IntType);
		else
			workingElems.push(Type.BoolType);
	}

	@Override
	public void exitTypeConstructorType(TypeConstructorTypeContext ctx) {
		String name = ctx.typeConstructor().IDENTIFIER().getText();
		int count = ctx.typeConstructor().type().size();
		List<Type> args = new ArrayList<>(count);
		for (int i = 0; i < count; i++)
			args.add(null);
		for (int i = count - 1; i >= 0; i--)
			args.set(i, (Type) workingElems.pop());
		workingElems.push(new TypeConstructor(name, args));
	}
}
