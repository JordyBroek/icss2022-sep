package nl.han.ica.icss.parser;

import java.util.Stack;


import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;
import org.antlr.v4.runtime.Token;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {

	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private IHANStack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}
    public AST getAST() {return ast;}

	private void enterNode(ASTNode node) {
		currentContainer.push(node);
	}

	private void exitNode() {
		currentContainer.pop();
	}

	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		Stylesheet stylesheet = new Stylesheet();
		ast.setRoot(stylesheet);
		enterNode(stylesheet);
	}

	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		exitNode();
	}

	@Override
	public void enterStylerule(ICSSParser.StyleruleContext ctx) {
		Stylerule rule = new Stylerule();

		ICSSParser.SelectorContext selectorCtx = ctx.selector();
		Selector selector = null;

		if (selectorCtx.ID_IDENT() != null) {
			selector = new IdSelector(selectorCtx.ID_IDENT().getText());
		} else if (selectorCtx.CLASS_IDENT() != null) {
			selector = new ClassSelector(selectorCtx.CLASS_IDENT().getText());
		} else if (selectorCtx.LOWER_IDENT() != null) {
			selector = new TagSelector(selectorCtx.LOWER_IDENT().getText());
		} else if (selectorCtx.CAPITAL_IDENT() != null) {
			selector = new TagSelector(selectorCtx.CAPITAL_IDENT().getText());
		}

		if (selector != null) {
			rule.addChild(selector);
		}

		currentContainer.peek().addChild(rule);
		enterNode(rule);
	}

	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
		exitNode();
	}

	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		Declaration decl = new Declaration();
		decl.property = new PropertyName(ctx.CAPITAL_IDENT().getText());
		currentContainer.peek().addChild(decl);
		enterNode(decl);
	}

	@Override
	public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		VariableAssignment varAssign = new VariableAssignment();
		varAssign.name = new VariableReference(ctx.LOWER_IDENT().getText());
		currentContainer.peek().addChild(varAssign);
		enterNode(varAssign);
	}


	@Override
	public void exitExpression(ICSSParser.ExpressionContext ctx) {
		Expression left = buildTerm(ctx.term(0));

		for (int i = 1; i < ctx.term().size(); i++) {
			Expression right = buildTerm(ctx.term(i));
			String operator = ctx.getChild(2 * i - 1).getText();

			Operation opNode = null;
			if (operator.equals("+")) {
				opNode = new AddOperation();
			} else if (operator.equals("-")) {
				opNode = new SubtractOperation();
			}

			if (opNode != null) {
				opNode.addChild(left);
				opNode.addChild(right);
				left = opNode;
			}
		}

		currentContainer.peek().addChild(left);
	}

	private Expression buildTerm(ICSSParser.TermContext ctx) {
		Expression left = buildFactor(ctx.factor(0));

		for (int i = 1; i < ctx.factor().size(); i++) {
			Expression right = buildFactor(ctx.factor(i));
			MultiplyOperation op = new MultiplyOperation();
			op.addChild(left);
			op.addChild(right);
			left = op;
		}

		return left;
	}

	private Expression buildFactor(ICSSParser.FactorContext ctx) {
		if (ctx.literal() != null) {
			return buildLiteral(ctx.literal());
		} else if (ctx.variableReference() != null) {
			return new VariableReference(ctx.variableReference().LOWER_IDENT().getText());
		} else if (ctx.boolLiteral() != null) {
			return buildBoolLiteral(ctx.boolLiteral());
		} else if (ctx.expression() != null) {
			return buildExpression(ctx.expression());
		}
		return null;
	}

	private Literal buildLiteral(ICSSParser.LiteralContext ctx) {
		if (ctx.PIXELSIZE() != null)
			return new PixelLiteral(ctx.PIXELSIZE().getText());
		if (ctx.PERCENTAGE() != null)
			return new PercentageLiteral(ctx.PERCENTAGE().getText());
		if (ctx.SCALAR() != null)
			return new ScalarLiteral(ctx.SCALAR().getText());
		if (ctx.COLOR() != null)
			return new ColorLiteral(ctx.COLOR().getText());
		return null;
	}

	private BoolLiteral buildBoolLiteral(ICSSParser.BoolLiteralContext ctx) {
		return new BoolLiteral(ctx.TRUE() != null);
	}

	@Override
	public void enterConditional(ICSSParser.ConditionalContext ctx) {
		IfClause ifClause = new IfClause();
		currentContainer.peek().addChild(ifClause);
		enterNode(ifClause);
	}

	@Override
	public void exitConditional(ICSSParser.ConditionalContext ctx) {
		exitNode();
	}

	@Override
	public void exitComparisonExpression(ICSSParser.ComparisonExpressionContext ctx) {
		Expression left = buildExpression(ctx.expression(0));
		Expression right = buildExpression(ctx.expression(1));
		String operator = ctx.getChild(1).getText();

		Operation opNode = null;
		switch (operator) {
			case "==":
				opNode = new EqualOperation();
				break;
			case ">":
				opNode = new GreaterThanOperation();
				break;
			case "<":
				opNode = new LessThanOperation();
				break;
		}

		if (opNode != null) {
			opNode.addChild(left);
			opNode.addChild(right);
			currentContainer.peek().addChild(opNode);
		}
	}

	private Expression buildExpression(ICSSParser.ExpressionContext ctx) {
		Expression left = buildTerm(ctx.term(0));

		for (int i = 1; i < ctx.term().size(); i++) {
			Expression right = buildTerm(ctx.term(i));
			String operator = ctx.getChild(2 * i - 1).getText();

			Operation opNode = null;
			if (operator.equals("+")) {
				opNode = new AddOperation();
			} else if (operator.equals("-")) {
				opNode = new SubtractOperation();
			}

			if (opNode != null) {
				opNode.addChild(left);
				opNode.addChild(right);
				left = opNode;
			}
		}
		return left;
	}



}