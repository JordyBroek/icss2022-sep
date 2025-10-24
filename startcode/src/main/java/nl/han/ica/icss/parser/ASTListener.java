package nl.han.ica.icss.parser;

import java.util.Stack;


import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
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
		varAssign.name.name = ctx.LOWER_IDENT().getText();
		currentContainer.peek().addChild(varAssign);
		enterNode(varAssign);
	}
}