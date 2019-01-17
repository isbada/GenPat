/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package mfix.core.node.ast.stmt;

import mfix.common.util.Constant;
import mfix.core.node.ast.Node;
import mfix.core.node.match.metric.FVector;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author: Jiajun
 * @date: 2018/9/21
 */
public class Blk extends Stmt {

    private static final long serialVersionUID = -8152168560236365788L;
    private List<Stmt> _statements = null;

    /**
     * Block:
     * { { Statement } }
     */
    public Blk(String fileName, int startLine, int endLine, ASTNode node) {
        this(fileName, startLine, endLine, node, null);
    }

    public Blk(String fileName, int startLine, int endLine, ASTNode node, Node parent) {
        super(fileName, startLine, endLine, node, parent);
        _nodeType = TYPE.BLOCK;
    }

    public void setStatement(List<Stmt> statements) {
        _statements = statements;
    }

    public List<Stmt> getStatement() {
        return _statements;
    }

    @Override
    public StringBuffer toSrcString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("{" + Constant.NEW_LINE);
        for (int i = 0; i < _statements.size(); i++) {
            stringBuffer.append(_statements.get(i).toSrcString());
            stringBuffer.append(Constant.NEW_LINE);
        }
        stringBuffer.append("}");
        return stringBuffer;
    }

    @Override
    protected void tokenize() {
        _tokens = new LinkedList<>();
        _tokens.add("{");
        for (int i = 0; i < _statements.size(); i++) {
            _tokens.addAll(_statements.get(i).tokens());
        }
        _tokens.add("}");
    }

    @Override
    public List<Stmt> getChildren() {
        return _statements;
    }

    @Override
    public List<Node> getAllChildren() {
        List<Node> children = new ArrayList<>(_statements.size());
        children.addAll(_statements);
        return children;
    }

    @Override
    public Stmt getParentStmt() {
        if (getParent() != null) {
            return getParent().getParentStmt();
        }
        return this;
    }

    @Override
    public boolean compare(Node other) {
        boolean match = false;
        if (other instanceof Blk) {
            Blk blk = (Blk) other;
            match = (_statements.size() == blk._statements.size());
            for (int i = 0; match && i < _statements.size(); i++) {
                match = match && _statements.get(i).compare(blk._statements.get(i));
            }
        }
        return match;
    }

    @Override
    public void computeFeatureVector() {
        _fVector = new FVector();
        for (Stmt stmt : _statements) {
            _fVector.combineFeature(stmt.getFeatureVector());
        }
    }

    @Override
    public boolean postAccurateMatch(Node node) {
        Blk blk = null;
        boolean match = false;
        if (getBindingNode() != null) {
            blk = (Blk) getBindingNode();
            match = (getBindingNode() == node);
        } else if (canBinding(node)) {
            blk = (Blk) node;
            setBindingNode(node);
            match = true;
        }
        if (blk == null) {
            continueTopDownMatchNull();
        } else {
            greedyMatchListNode(_statements, blk.getStatement());
        }
        return match;
    }

    @Override
    public boolean genModidications() {
        if(super.genModidications()) {
            Blk blk = (Blk) getBindingNode();
            genModificationList(_statements, blk.getStatement(), true);
            return true;
        }
        return false;
    }

    @Override
    public boolean ifMatch(Node node, Map<Node, Node> matchedNode, Map<String, String> matchedStrings) {
        if(node instanceof Blk) {
            return super.ifMatch(node, matchedNode, matchedStrings);
        }
        return false;
    }
}