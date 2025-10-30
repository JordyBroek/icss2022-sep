package nl.han.ica.icss.generator;


import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;

public class Generator {

	StringBuilder tree = new StringBuilder();

	public String generate(AST ast) {
        generateStyleRules(ast.root);
		return tree.toString();
	}

	private void generateStyleRules(ASTNode astNode) {
		for(ASTNode child : astNode.getChildren()) {
			if(child instanceof Stylerule){
				tree.append(((Stylerule) child).selectors.get(0)).append(" {\n");
				generateDeclarations(child);
				tree.append("}\n");
			}
		}
	}

	private void generateDeclarations(ASTNode astNode) {
		for(ASTNode child : astNode.getChildren()) {
			if(child instanceof Declaration){
				tree.append(" ").append(((Declaration) child).property.name).append(": ");
				generateLiteral(((Declaration) child).expression);
			}
		}
	}

	private void generateLiteral(Expression exp) {
		if(exp instanceof PercentageLiteral){
			tree.append(((PercentageLiteral) exp).value).append("%").append(";\n");
		} else if(exp instanceof PixelLiteral){
			tree.append(((PixelLiteral) exp).value).append("px").append(";\n");
		} else if(exp instanceof ScalarLiteral){
			tree.append(((ScalarLiteral) exp).value).append(";\n");
		} else if(exp instanceof BoolLiteral){
			tree.append(((BoolLiteral) exp).value).append(";\n");
		} else if(exp instanceof ColorLiteral){
			tree.append(((ColorLiteral) exp).value).append(";\n");
		}
	}
}
