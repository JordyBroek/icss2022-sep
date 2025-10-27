package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

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

    public AST getAST() {
        return ast;
    }

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
    public void enterStylerule(ICSSParser.StyleruleContext ctx){
        Stylerule rule = new Stylerule();
        ICSSParser.SelectorContext selectorCtx = ctx.selector();
        Selector selector = null;

        if (selectorCtx.classSelector() != null) {
            selector = new ClassSelector(selectorCtx.classSelector().getText());
        } else if (selectorCtx.idSelector() != null) {
            selector = new IdSelector(selectorCtx.idSelector().getText());
        } else if (selectorCtx.tagSelector() != null) {
            selector = new TagSelector(selectorCtx.tagSelector().getText());
        }

        if (selector != null) {
            rule.addChild(selector);
        }
        currentContainer.peek().addChild(rule);
        enterNode(rule);
    }


    @Override
    public void exitStylerule(ICSSParser.StyleruleContext ctx){
        exitNode();
    }

    @Override
    public void enterDeclaration(ICSSParser.DeclarationContext ctx){
        Declaration decl = new Declaration();
        decl.property = new PropertyName(ctx.LOWER_IDENT().getText());
        currentContainer.peek().addChild(decl);
        enterNode(decl);
    }

    @Override
    public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
        exitNode();
    }

    @Override
    public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
        VariableAssignment varAssign = new VariableAssignment();
        String varName = ctx.VAR_IDENT().getText();
        varAssign.name = new VariableReference(varName);
        currentContainer.peek().addChild(varAssign);
        enterNode(varAssign);
    }

    @Override
    public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
        exitNode();
    }

    @Override
    public void exitExpression(ICSSParser.ExpressionContext ctx) {
        Expression expr = buildExpression(ctx);
        if (expr != null) {
            currentContainer.peek().addChild(expr);
        }
    }

    private Expression buildTerm(ICSSParser.TermContext ctx) {
        List<ICSSParser.FactorContext> factors = ctx.factor();
        if (factors.isEmpty()) {
            return null;
        }

        Expression left = buildFactor(factors.get(0));
        for (int i = 1; i < factors.size(); i++) {
            Expression right = buildFactor(factors.get(i));
            if (left == null || right == null) continue;

            MultiplyOperation op = new MultiplyOperation();
            op.addChild(left);
            op.addChild(right);
            left = op;
        }
        return left;
    }

    private Expression buildFactor(ICSSParser.FactorContext ctx) {
        if (ctx.pixelLiteral() != null) {
            return new PixelLiteral(ctx.pixelLiteral().getText());
        } else if (ctx.colorLiteral() != null) {
            return new ColorLiteral(ctx.colorLiteral().getText());
        } else if (ctx.percentageLiteral() != null) {
            return new PercentageLiteral(ctx.percentageLiteral().getText());
        } else if (ctx.scalarLiteral() != null) {
            return new ScalarLiteral(ctx.scalarLiteral().getText());
        } else if (ctx.boolLiteral() != null) {
            return buildBoolLiteral(ctx.boolLiteral());
        } else if (ctx.variableReference() != null) {
            return new VariableReference(ctx.variableReference().VAR_IDENT().getText());
        }
        return null;
    }

    private Literal buildLiteral(ParserRuleContext ctx) {
        if (ctx instanceof ICSSParser.PixelLiteralContext)
            return new PixelLiteral(ctx.getText());
        if (ctx instanceof ICSSParser.PercentageLiteralContext)
            return new PercentageLiteral(ctx.getText());
        if (ctx instanceof ICSSParser.ScalarLiteralContext)
            return new ScalarLiteral(ctx.getText());
        if (ctx instanceof ICSSParser.ColorLiteralContext)
            return new ColorLiteral(ctx.getText());
        return null;
    }

    private BoolLiteral buildBoolLiteral(ICSSParser.BoolLiteralContext ctx) {
        return new BoolLiteral(ctx.TRUE() != null);
    }

    @Override
    public void enterConditional(ICSSParser.ConditionalContext ctx) {
        IfClause ifClause = new IfClause();
        ICSSParser.AttributeContext attctx = ctx.attribute();
        Expression condition = null;

        if (attctx.variableReference() != null) {
            condition = new VariableReference(attctx.variableReference().VAR_IDENT().getText());
        } else if (attctx.boolLiteral() != null) {
            condition = buildBoolLiteral(attctx.boolLiteral());
        }
        if (condition != null) {
            ifClause.addChild(condition);
        }
        currentContainer.peek().addChild(ifClause);
        enterNode(ifClause);
    }

    @Override
    public void exitConditional(ICSSParser.ConditionalContext ctx) {
        exitNode();
    }

    @Override
    public void enterElseClause(ICSSParser.ElseClauseContext ctx) {
        ElseClause elseClause = new ElseClause();
        currentContainer.peek().addChild(elseClause);
        enterNode(elseClause);
    }

    @Override
    public void exitElseClause(ICSSParser.ElseClauseContext ctx) {
        exitNode();
    }

    private Expression buildExpression(ICSSParser.ExpressionContext ctx) {
        List<ICSSParser.TermContext> terms = ctx.term();
        if (terms.isEmpty()) {
            return null;
        }

        Expression left = buildTerm(terms.get(0));
        for (int i = 1; i < terms.size(); i++) {
            Expression right = buildTerm(terms.get(i));
            if (left == null || right == null) continue;

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