package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new HANLinkedList<>();

    }

    @Override
    public void apply(AST ast) {
        variableValues = new HANLinkedList<>();
        Stylesheet stylesheet = ast.root;
        addScope(1);

        for(ASTNode childNode: stylesheet.body){
            if(childNode instanceof Stylerule){
                transformStyleRule((Stylerule) childNode);
            } else if (childNode instanceof VariableAssignment){
                addVarAssignment(1, (VariableAssignment) childNode);
            }
        }
        removeScope(1);
    }

    private void transformStyleRule(Stylerule stylerule) {
        ArrayList<ASTNode> transformedRule = new ArrayList<>();
        int depth = 2;
        addScope(depth);

        for(ASTNode childofStyleRule: stylerule.body){
            transformStyleRuleChildren(depth, childofStyleRule, transformedRule);
        }
        removeScope(depth);
        stylerule.body = transformedRule;
    }

    private void transformStyleRuleChildren(int depth, ASTNode childNode, ArrayList<ASTNode> transformedRule) {
        if(childNode instanceof Declaration){
            transformDeclaration((Declaration) childNode, transformedRule);
        }

        if(childNode instanceof VariableAssignment){
            addVarAssignment(depth, (VariableAssignment) childNode);
        }

        if(childNode instanceof IfClause){
            IfClause ifClause = (IfClause) childNode;
            ifClause.conditionalExpression = transformIfExpression(ifClause);

            if(((BoolLiteral) ifClause.conditionalExpression).value) {
                if (ifClause.elseClause != null) {
                    ifClause.elseClause.body = new ArrayList<>();
                }
            } else {
                if(ifClause.elseClause == null) {
                    ifClause.body = new ArrayList<>();
                } else {
                    ifClause.body = ifClause.elseClause.body;
                    ifClause.elseClause.body = new ArrayList<>();
                }
            }
            transformIfClause(depth, ifClause, transformedRule);
        }
    }

    private void transformDeclaration(Declaration decl, ArrayList<ASTNode> transformedRule) {
        if(decl.expression instanceof Operation) {
            decl.expression = calculate((Operation) decl.expression);
            transformExpression(decl, transformedRule);
        } else if(decl.expression instanceof VariableReference){
            decl.expression = getVarLiteral((VariableReference) decl.expression);
            transformExpression(decl, transformedRule);
        } else {
            transformExpression(decl, transformedRule);
        }
    }

    private void transformExpression(Declaration decl, ArrayList<ASTNode> transformedRule) {
        transformedRule.add(decl);
    }

    private void transformIfClause(int depth, IfClause ifClause, ArrayList<ASTNode> transformedRule) {
        for(ASTNode childNode: ifClause.getChildren()){
            transformStyleRuleChildren(depth, childNode, transformedRule);
        }
    }

    private Literal transformIfExpression(IfClause ifClause) {
        if(!(ifClause.conditionalExpression instanceof BoolLiteral)){
            return getVarLiteral((VariableReference) ifClause.conditionalExpression);
        }
        return (BoolLiteral) ifClause.conditionalExpression;
    }

    private Literal calculate(Operation op) {
        Expression leftValue = op.lhs;
        Expression rightValue = op.rhs;

        if(leftValue instanceof Operation){
            leftValue = calculate((Operation) leftValue);
        }
        if(rightValue instanceof Operation){
            rightValue = calculate((Operation) rightValue);
        }

        if(leftValue instanceof VariableReference){
            leftValue = getVarLiteral((VariableReference) leftValue);
        }
        if(rightValue instanceof VariableReference){
            rightValue = getVarLiteral((VariableReference) rightValue);
        }

        if(leftValue instanceof PercentageLiteral || rightValue instanceof PercentageLiteral){
            return new PercentageLiteral(calcValue(op, getValue(leftValue), getValue(rightValue)));
        } else if (leftValue instanceof PixelLiteral || rightValue instanceof PixelLiteral){
            return new PixelLiteral(calcValue(op, getValue(leftValue), getValue(rightValue)));
        } else if (leftValue instanceof ScalarLiteral && rightValue instanceof ScalarLiteral){
            return new ScalarLiteral(calcValue(op, getValue(leftValue), getValue(rightValue)));
        }
        return null;
    }

    private int calcValue(Operation op, int leftValue, int rightValue) {
        if(op instanceof MultiplyOperation){
            return leftValue * rightValue;
        } else if(op instanceof SubtractOperation){
            return leftValue - rightValue;
        } else if(op instanceof AddOperation){
            return leftValue + rightValue;
        }
        return 0;
    }

    private int getValue(Expression value){
        if(value instanceof ScalarLiteral){
            return ((ScalarLiteral) value).value;
        } else if(value instanceof PixelLiteral){
            return ((PixelLiteral) value).value;
        } else if(value instanceof PercentageLiteral){
            return ((PercentageLiteral) value).value;
        } else if(value instanceof VariableReference){
            return getValue(getVarLiteral((VariableReference) value));
        }
        return 0;
    }

    private Literal getVarLiteral(VariableReference varRef) {
        for(int i = variableValues.getSize(); i >= 1; i--){
            HashMap<String, Literal> hash = variableValues.get(i);
            if(hash.containsKey(varRef.name)){
                return hash.get(varRef.name);
            }
        }
        return null;
    }

    private void addVarAssignment(int depth, VariableAssignment varAssignment) {
        Expression exp = varAssignment.expression;
        if(exp instanceof Operation){
            Literal calculatedExp = calculate((Operation) exp);
            variableValues.get(depth).put(varAssignment.name.name, calculatedExp);
            transformVarAssignment(varAssignment, calculatedExp);
        } else if (exp instanceof Literal){
            variableValues.get(depth).put(varAssignment.name.name, (Literal) varAssignment.expression);
        }
    }

    private void transformVarAssignment(VariableAssignment varAssignment, Literal calculatedExp) {
        varAssignment.expression = calculatedExp;
    }

    private void addScope(int depth){
        variableValues.insert(depth, new HashMap<>());
    }

    private void removeScope(int depth){
        variableValues.delete(depth);
    }
}
