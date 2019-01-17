/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package mfix.core.node.ast.stmt;

import mfix.core.node.ast.Node;
import mfix.core.node.ast.expr.ClassInstCreation;
import mfix.core.node.ast.expr.Expr;
import mfix.core.node.match.metric.FVector;
import mfix.core.node.modify.Update;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author: Jiajun
 * @date: 2018/9/21
 */
public class ThrowStmt extends Stmt {

	private Expr _expression = null;
	
	/**
	 * ThrowStatement:
     *	throw Expression ;
	 */
	public ThrowStmt(String fileName, int startLine, int endLine, ASTNode node) {
		this(fileName, startLine, endLine, node, null);
	}

	public ThrowStmt(String fileName, int startLine, int endLine, ASTNode node, Node parent) {
		super(fileName, startLine, endLine, node, parent);
		_nodeType = TYPE.THROW;
	}
	
	public void setExpression(Expr expression){
		_expression = expression;
	}

	public Expr getExpression() { return _expression; }

	public String getExceptionType(){
		if(_expression instanceof ClassInstCreation){
			return ((ClassInstCreation)_expression).getClassType().toString();
		} else {
			return _expression.getType().toString();
		}
	}

	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("throw ");
		stringBuffer.append(_expression.toSrcString());
		stringBuffer.append(";");
		return stringBuffer;
	}
	
	@Override
	protected void tokenize() {
		_tokens = new LinkedList<>();
		_tokens.add("throw");
		_tokens.addAll(_expression.tokens());
		_tokens.add(";");
	}
	
	@Override
	public List<Node> getAllChildren() {
		List<Node> children = new ArrayList<>(1);
		children.add(_expression);
		return children;
	}
	
	@Override
	public List<Stmt> getChildren() {
		return new ArrayList<>(0);
	}
	
	@Override
	public boolean compare(Node other) {
		boolean match = false;
		if(other instanceof ThrowStmt) {
			ThrowStmt throwStmt = (ThrowStmt) other;
			match = _expression.compare(throwStmt._expression);
		}
		return match;
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new FVector();
		_fVector.inc(FVector.KEY_THROW);
		_fVector.combineFeature(_expression.getFeatureVector());
	}

	@Override
	public boolean postAccurateMatch(Node node) {
		boolean match = false;
		ThrowStmt throwStmt = null;
		if(getBindingNode() != null) {
			throwStmt = (ThrowStmt) getBindingNode();
			match = (throwStmt == node);
		} else if(canBinding(node)) {
			throwStmt = (ThrowStmt) node;
			match = true;
		}

		if(throwStmt == null) {
			continueTopDownMatchNull();
		} else {
			_expression.postAccurateMatch(throwStmt.getExpression());
		}
		return false;
	}

	@Override
	public boolean genModidications() {
		if (super.genModidications()) {
			ThrowStmt throwStmt = (ThrowStmt) getBindingNode();
			if(_expression.getBindingNode() != throwStmt.getExpression()) {
				Update update = new Update(this, _expression, throwStmt.getExpression());
				_modifications.add(update);
			} else {
				_expression.genModidications();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean ifMatch(Node node, Map<Node, Node> matchedNode, Map<String, String> matchedStrings) {
		if (node instanceof ThrowStmt) {
			ThrowStmt throwStmt = (ThrowStmt) node;
			return _expression.ifMatch(throwStmt.getExpression(), matchedNode, matchedStrings)
					&& super.ifMatch(node, matchedNode, matchedStrings);
		}
		return false;
	}
}