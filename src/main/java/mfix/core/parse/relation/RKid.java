/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package mfix.core.parse.relation;

import mfix.common.util.Pair;

import java.util.Set;

/**
 * @author: Jiajun
 * @date: 2018/12/5
 */
public class RKid extends Relation {

    /**
     * Record the structure: a relation can be the
     * kid of a structure, which captures the control
     * flow of the program
     */
    private RStruct _structure;
    /**
     * The index of child node in the {@code _structure}
     * NOTE: this index does not denotes the order of
     * the child against the siblings, but the coarse-grained
     * location information, i.e., while-condition and while-body.
     */
    private int _index;
    /**
     * The child node
     */
    private Relation _child;

    public RKid(RStruct structure) {
        super(RelationKind.CHILD);
        _structure = structure;
        this.usedBy(structure);
    }

    public void setIndex(int index) {
        _index = index;
    }

    public void setChild(Relation child) {
        _child = child;
        _child.usedBy(this);
    }

    public RStruct getStructure() {
        return _structure;
    }

    public int getIndex() {
        return _index;
    }

    public Relation getChildRelation() {
        return _child;
    }

    @Override
    protected Set<Relation> expandDownward0(Set<Relation> set) {
        set.add(_structure);
        set.add(_child);
        return set;
    }

    @Override
    public void doAbstraction0(double frequency) {
        _child.doAbstraction(frequency);
        _isAbstract = !_child.isConcerned() || _child.isAbstract();
    }

    @Override
    public boolean match(Relation relation, Set<Pair<Relation, Relation>> dependencies) {
        if (!super.match(relation, dependencies)) {
            return false;
        }

        RKid kid = (RKid) relation;
        if (_index != kid.getIndex()) {
            return false;
        }

        if(_child.match(kid.getChildRelation(), dependencies)) {
            dependencies.add(new Pair<>(_child, kid.getChildRelation()));
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "[KID]";
    }
}
